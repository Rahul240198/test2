package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ParishRepository
import com.example.data.bridge.BridgeSyncOutcome
import com.example.data.bridge.UserSession
import com.example.data.bridge.WebsiteApiBridgeClient
import com.example.data.bridge.WebsiteBridgeConfig
import com.example.data.bridge.WebsiteBridgePreferences
import com.example.model.CertificateRequest
import com.example.model.CertificateType
import com.example.model.ParishionerProfile
import com.example.model.RequestStatus
import com.example.model.WebsiteSyncStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

class AuthViewModel(
  private val repository: ParishRepository,
  private val bridgePreferences: WebsiteBridgePreferences,
) : ViewModel() {

  val session: StateFlow<UserSession?> =
    repository.sessionFlow
      .map<UserSession, UserSession?> { it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null,
      )

  val bridgeConfig: StateFlow<WebsiteBridgeConfig> =
    bridgePreferences.configFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = WebsiteBridgeConfig(),
    )

  private val _isLoggingIn = MutableStateFlow(false)
  val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

  private val _loginError = MutableStateFlow<String?>(null)
  val loginError: StateFlow<String?> = _loginError.asStateFlow()

  fun clearError() {
    _loginError.value = null
  }

  fun saveApiBaseUrl(baseUrl: String, apiToken: String) {
    viewModelScope.launch {
      val current = bridgePreferences.configFlow.first()
      bridgePreferences.saveConfig(baseUrl, apiToken, current.autoSyncEnabled)
    }
  }

  fun loginWithMobileApi(mobileNumber: String, parishId: String, apiUrlOverride: String? = null) {
    viewModelScope.launch {
      _loginError.value = null
      _isLoggingIn.value = true
      val normalized = ParishRepository.normalizeMobileNumber(mobileNumber)
      if (normalized.length < 10) {
        _loginError.value = "Please enter a valid 10-digit mobile number to login."
        _isLoggingIn.value = false
        return@launch
      }

      if (!apiUrlOverride.isNullOrBlank()) {
        val current = bridgePreferences.configFlow.first()
        bridgePreferences.saveConfig(apiUrlOverride, current.apiToken, true)
      }

      val effectiveConfig = bridgePreferences.configFlow.first()
      val result =
        if (effectiveConfig.baseUrl.isBlank()) {
          // No live parish API URL set yet: authenticate in Dummy Test Mode so login lands on Home
          repository.loginWithDummyAccount(normalized, parishId)
        } else {
          repository.loginWithMobileApi(normalized, parishId)
        }

      if (result.isFailure) {
        _loginError.value =
          result.exceptionOrNull()?.localizedMessage ?: "Mobile login failed. Please check API URL."
      }
      _isLoggingIn.value = false
    }
  }

  fun loginWithDummyAccount(mobileInput: String, parishId: String) {
    viewModelScope.launch {
      _loginError.value = null
      _isLoggingIn.value = true
      val normalized = ParishRepository.normalizeMobileNumber(mobileInput)
      val chosenMobile =
        if (normalized.length >= 10) normalized else ParishRepository.DUMMY_MOBILE
      val result = repository.loginWithDummyAccount(chosenMobile, parishId)
      if (result.isFailure) {
        _loginError.value =
          result.exceptionOrNull()?.localizedMessage ?: "Dummy login failed."
      }
      _isLoggingIn.value = false
    }
  }

  fun signOut() {
    viewModelScope.launch { repository.signOut() }
  }
}

class ParishViewModel(
  private val repository: ParishRepository,
  private val bridgePreferences: WebsiteBridgePreferences,
  private val bridgeClient: WebsiteApiBridgeClient,
  val session: UserSession,
) : ViewModel() {

  val profileState: StateFlow<UiState<ParishionerProfile?>> =
    repository
      .observeProfile(session.userId)
      .map<ParishionerProfile?, UiState<ParishionerProfile?>> { UiState.Success(it) }
      .catch { emit(UiState.Error(it.message ?: "Unable to load parishioner profile")) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val requestsState: StateFlow<UiState<List<CertificateRequest>>> =
    repository
      .observeCertificateRequests(session.userId)
      .map<List<CertificateRequest>, UiState<List<CertificateRequest>>> { UiState.Success(it) }
      .catch { emit(UiState.Error(it.message ?: "Unable to load certificate requests")) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val bridgeConfig: StateFlow<WebsiteBridgeConfig> =
    bridgePreferences.configFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = WebsiteBridgeConfig(),
    )

  private val _isSavingProfile = MutableStateFlow(false)
  val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

  private val _isSubmittingRequest = MutableStateFlow(false)
  val isSubmittingRequest: StateFlow<Boolean> = _isSubmittingRequest.asStateFlow()

  private val _isTestingBridge = MutableStateFlow(false)
  val isTestingBridge: StateFlow<Boolean> = _isTestingBridge.asStateFlow()

  private val _isSyncingBridge = MutableStateFlow(false)
  val isSyncingBridge: StateFlow<Boolean> = _isSyncingBridge.asStateFlow()

  private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val events: SharedFlow<String> = _events.asSharedFlow()

  fun saveProfile(updated: ParishionerProfile) {
    if (updated.fullName.trim().isEmpty()) {
      _events.tryEmit("Please enter your full name before saving.")
      return
    }
    viewModelScope.launch {
      _isSavingProfile.value = true
      val config = bridgePreferences.configFlow.first()
      val initialSync =
        if (session.isDummyMode && config.baseUrl.isBlank()) {
          WebsiteSyncStatus.DUMMY_TEST_MODE.name
        } else {
          WebsiteSyncStatus.PENDING_API_SYNC.name
        }
      val profileToSave =
        updated.copy(
          userId = session.userId,
          websiteSyncStatus = initialSync,
        )
      repository.saveProfile(profileToSave)

      if (config.autoSyncEnabled && config.baseUrl.isNotBlank()) {
        when (val syncOutcome = bridgeClient.syncProfile(config, session.authToken, profileToSave)) {
          is BridgeSyncOutcome.Synced -> {
            repository.saveProfile(
              profileToSave.copy(websiteSyncStatus = WebsiteSyncStatus.SYNCED_TO_WEBSITE.name)
            )
            bridgePreferences.recordStatus(syncOutcome.message, true)
            _events.tryEmit("Profile updated & synced with parish website API.")
          }
          is BridgeSyncOutcome.Failed -> {
            bridgePreferences.recordStatus(syncOutcome.errorMessage, false)
            _events.tryEmit("Saved locally; API sync failed: ${syncOutcome.errorMessage}")
          }
          BridgeSyncOutcome.NotConfigured -> {
            _events.tryEmit("Parishioner profile updated.")
          }
        }
      } else {
        val msg =
          if (session.isDummyMode) "Profile updated locally (Dummy Test Mode)."
          else "Profile updated locally. Configure API URL to sync with website."
        _events.tryEmit(msg)
      }
      _isSavingProfile.value = false
    }
  }

  fun submitCertificateRequest(
    type: CertificateType,
    applicantName: String,
    subjectName: String,
    eventDate: String,
    placeOrParish: String,
    parentsOrSpouseNames: String,
    sponsorOrNextOfKin: String,
    registryReference: String,
    purpose: String,
    notes: String,
    onSuccess: () -> Unit,
  ) {
    if (
      applicantName.isBlank() ||
        subjectName.isBlank() ||
        eventDate.isBlank() ||
        purpose.isBlank()
    ) {
      _events.tryEmit("Please complete applicant name, certificate name, date, and purpose.")
      return
    }
    viewModelScope.launch {
      _isSubmittingRequest.value = true
      val config = bridgePreferences.configFlow.first()
      val initialSync =
        if (session.isDummyMode && config.baseUrl.isBlank()) {
          WebsiteSyncStatus.DUMMY_TEST_MODE.name
        } else {
          WebsiteSyncStatus.PENDING_API_SYNC.name
        }
      val draft =
        CertificateRequest(
          id = "",
          userId = session.userId,
          certificateType = type.name,
          applicantName = applicantName.trim(),
          subjectName = subjectName.trim(),
          eventDate = eventDate.trim(),
          placeOrParish = placeOrParish.trim(),
          parentsOrSpouseNames = parentsOrSpouseNames.trim(),
          sponsorOrNextOfKin = sponsorOrNextOfKin.trim(),
          registryReference = registryReference.trim(),
          purpose = purpose.trim(),
          notes = notes.trim(),
          status = RequestStatus.SUBMITTED.name,
          websiteSyncStatus = initialSync,
        )

      val created = repository.submitCertificateRequest(draft)

      if (config.autoSyncEnabled && config.baseUrl.isNotBlank()) {
        when (
          val syncOutcome =
            bridgeClient.syncCertificateRequest(config, session.authToken, created)
        ) {
          is BridgeSyncOutcome.Synced -> {
            repository.updateRequestSyncStatus(
              session.userId,
              created.id,
              WebsiteSyncStatus.SYNCED_TO_WEBSITE,
            )
            bridgePreferences.recordStatus(syncOutcome.message, true)
            _events.tryEmit("${type.label} request submitted & synced with parish website.")
          }
          is BridgeSyncOutcome.Failed -> {
            bridgePreferences.recordStatus(syncOutcome.errorMessage, false)
            _events.tryEmit("${type.label} saved locally; API sync pending (${syncOutcome.errorMessage}).")
          }
          BridgeSyncOutcome.NotConfigured -> {
            _events.tryEmit("${type.label} request submitted.")
          }
        }
      } else {
        val msg =
          if (session.isDummyMode) "${type.label} request submitted (Dummy Test Mode)."
          else "${type.label} request saved; ready to sync with parish API."
        _events.tryEmit(msg)
      }
      _isSubmittingRequest.value = false
      onSuccess()
    }
  }

  fun cancelCertificateRequest(requestId: String) {
    viewModelScope.launch {
      repository.cancelCertificateRequest(session.userId, requestId)
      val config = bridgePreferences.configFlow.first()
      if (config.autoSyncEnabled && config.baseUrl.isNotBlank()) {
        bridgeClient.cancelCertificateRequest(config, session.authToken, requestId)
      }
      _events.tryEmit("Certificate request cancelled.")
    }
  }

  fun saveBridgeSettings(baseUrl: String, apiToken: String, autoSyncEnabled: Boolean) {
    viewModelScope.launch {
      bridgePreferences.saveConfig(baseUrl, apiToken, autoSyncEnabled)
      _events.tryEmit("Website API settings saved.")
    }
  }

  fun testBridgeConnection(baseUrl: String, apiToken: String, autoSyncEnabled: Boolean) {
    viewModelScope.launch {
      _isTestingBridge.value = true
      bridgePreferences.saveConfig(baseUrl, apiToken, autoSyncEnabled)
      val latestConfig = bridgePreferences.configFlow.first()
      when (val outcome = bridgeClient.testConnection(latestConfig)) {
        is BridgeSyncOutcome.Synced -> {
          bridgePreferences.recordStatus(outcome.message, true)
          _events.tryEmit(outcome.message)
        }
        is BridgeSyncOutcome.Failed -> {
          bridgePreferences.recordStatus(outcome.errorMessage, false)
          _events.tryEmit(outcome.errorMessage)
        }
        BridgeSyncOutcome.NotConfigured -> {
          bridgePreferences.recordStatus("Enter your church website API URL first.", null)
          _events.tryEmit("Please enter your website API base URL first.")
        }
      }
      _isTestingBridge.value = false
    }
  }

  fun syncPendingRecordsWithWebsite() {
    viewModelScope.launch {
      _isSyncingBridge.value = true
      val config = bridgePreferences.configFlow.first()
      if (config.baseUrl.isBlank()) {
        _isSyncingBridge.value = false
        _events.tryEmit("Configure your parish website API URL first.")
        return@launch
      }
      val effectiveConfig = config.copy(autoSyncEnabled = true)
      var syncedCount = 0
      var failedCount = 0

      val profile = (profileState.value as? UiState.Success)?.data
      if (profile != null) {
        when (bridgeClient.syncProfile(effectiveConfig, session.authToken, profile)) {
          is BridgeSyncOutcome.Synced -> {
            repository.saveProfile(
              profile.copy(websiteSyncStatus = WebsiteSyncStatus.SYNCED_TO_WEBSITE.name)
            )
            syncedCount++
          }
          is BridgeSyncOutcome.Failed -> failedCount++
          BridgeSyncOutcome.NotConfigured -> {}
        }
      }

      val requests = (requestsState.value as? UiState.Success)?.data.orEmpty()
      requests
        .filter { it.websiteSyncStatus != WebsiteSyncStatus.SYNCED_TO_WEBSITE.name }
        .forEach { req ->
          when (bridgeClient.syncCertificateRequest(effectiveConfig, session.authToken, req)) {
            is BridgeSyncOutcome.Synced -> {
              repository.updateRequestSyncStatus(
                session.userId,
                req.id,
                WebsiteSyncStatus.SYNCED_TO_WEBSITE,
              )
              syncedCount++
            }
            is BridgeSyncOutcome.Failed -> failedCount++
            BridgeSyncOutcome.NotConfigured -> {}
          }
        }

      val summary =
        if (failedCount == 0) {
          "Synced $syncedCount record(s) with parish website API."
        } else {
          "Synced $syncedCount record(s); $failedCount failed — verify server availability."
        }
      bridgePreferences.recordStatus(summary, failedCount == 0)
      _events.tryEmit(summary)
      _isSyncingBridge.value = false
    }
  }

  fun signOut() {
    viewModelScope.launch { repository.signOut() }
  }

  private companion object {
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
