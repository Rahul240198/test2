package com.example.data

import com.example.data.bridge.CertificateRequestDto
import com.example.data.bridge.ParishionerDto
import com.example.data.bridge.UserSession
import com.example.data.bridge.WebsiteApiBridgeClient
import com.example.data.bridge.WebsiteBridgePreferences
import com.example.data.local.ParishDao
import com.example.model.CertificateRequest
import com.example.model.CertificateType
import com.example.model.ParishionerProfile
import com.example.model.RequestStatus
import com.example.model.WebsiteSyncStatus
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ParishRepository(
  private val dao: ParishDao,
  private val preferences: WebsiteBridgePreferences,
  private val apiClient: WebsiteApiBridgeClient,
) {

  val sessionFlow: Flow<UserSession> = preferences.sessionFlow

  fun observeProfile(userId: String): Flow<ParishionerProfile?> = dao.observeProfile(userId)

  fun observeCertificateRequests(userId: String): Flow<List<CertificateRequest>> =
    dao.observeCertificateRequests(userId)

  suspend fun getProfile(userId: String): ParishionerProfile? = dao.getProfile(userId)

  suspend fun loginWithMobileApi(
    rawMobile: String,
    parishId: String?,
  ): Result<UserSession> {
    val cleanMobile = normalizeMobileNumber(rawMobile)
    if (cleanMobile.length < 10) {
      return Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number."))
    }

    val config = preferences.configFlow.first()
    if (config.baseUrl.isBlank()) {
      return Result.failure(
        IllegalStateException(
          "Website API Base URL is not set. Enter your parish server URL below or use Dummy Test Login."
        )
      )
    }

    val loginResult = apiClient.loginWithMobile(config, cleanMobile, parishId)
    if (loginResult.isFailure) {
      return Result.failure(loginResult.exceptionOrNull() ?: IllegalStateException("Login failed"))
    }

    val response = loginResult.getOrThrow()
    val dto = response.parishioner
    val resolvedUserId =
      dto?.userId?.takeIf { it.isNotBlank() } ?: "parish_${cleanMobile.takeLast(10)}"
    val resolvedParishId =
      dto?.parishId?.takeIf { it.isNotBlank() } ?: parishId?.trim().orEmpty()
    val resolvedName = dto?.fullName?.takeIf { it.isNotBlank() } ?: "Parishioner"
    val now = System.currentTimeMillis()

    val profile =
      dto.toEntity(
        fallbackUserId = resolvedUserId,
        fallbackMobile = cleanMobile,
        fallbackParishId = resolvedParishId,
        syncStatus = WebsiteSyncStatus.SYNCED_TO_WEBSITE,
        now = now,
      )
    dao.upsertProfile(profile)

    val remoteRequests =
      response.requests
        ?.mapIndexed { idx, reqDto ->
          reqDto.toEntity(
            userId = resolvedUserId,
            fallbackId = "api_${resolvedUserId}_$idx",
            syncStatus = WebsiteSyncStatus.SYNCED_TO_WEBSITE,
            now = now,
          )
        }
        .orEmpty()
    if (remoteRequests.isNotEmpty()) {
      dao.upsertCertificateRequests(remoteRequests)
    }

    val session =
      UserSession(
        isLoggedIn = true,
        userId = resolvedUserId,
        mobileNumber = cleanMobile,
        parishId = resolvedParishId,
        fullName = resolvedName,
        authToken = response.token.orEmpty(),
        isDummyMode = false,
      )
    preferences.saveSession(session)
    preferences.recordStatus("Logged in via St. Anthony's Parish API", true)
    return Result.success(session)
  }

  suspend fun loginWithDummyAccount(
    rawMobile: String = DUMMY_MOBILE,
    parishId: String? = null,
  ): Result<UserSession> {
    val digits = normalizeMobileNumber(rawMobile).ifBlank { DUMMY_MOBILE }
    if (digits.length < 10) {
      return Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number."))
    }

    val userId = "dummy_${digits.takeLast(10)}"
    val resolvedParishId =
      parishId?.trim()?.takeIf { it.isNotBlank() } ?: DUMMY_PARISH_ID
    val now = System.currentTimeMillis()

    val existingProfile = dao.getProfile(userId)
    val profile =
      existingProfile
        ?: ParishionerProfile(
          userId = userId,
          parishId = resolvedParishId,
          fullName = if (digits.endsWith(DUMMY_MOBILE.takeLast(10))) "Maria D'Souza" else "Parishioner ($digits)",
          email = "maria.dsouza@example.com",
          phone = "+91 ${digits.takeLast(10)}",
          address = "Qtr. 14-B, Railway Colony, Ajni, Nagpur 440003",
          sccUnit = "St. Jude Unit (Ward IV)",
          headOfFamily = "Joseph D'Souza",
          dateOfBirth = "1992-05-14",
          baptismDate = "1992-06-21",
          spouseName = "Joseph D'Souza",
          websiteSyncStatus = WebsiteSyncStatus.DUMMY_TEST_MODE.name,
          createdAtMillis = now,
          updatedAtMillis = now,
        )
    dao.upsertProfile(profile)

    val existingRequests = dao.getCertificateRequests(userId)
    if (existingRequests.isEmpty()) {
      dao.upsertCertificateRequests(
        listOf(
          CertificateRequest(
            id = "AJN-REQ-${UUID.randomUUID().toString().take(6).uppercase()}",
            userId = userId,
            certificateType = CertificateType.BIRTH.name,
            applicantName = profile.fullName,
            subjectName = "Leo D'Souza",
            eventDate = "2022-03-14",
            placeOrParish = "St. Anthony's Church, Ajni",
            parentsOrSpouseNames = "Joseph & Maria D'Souza",
            sponsorOrNextOfKin = "Peter Fernandes",
            registryReference = profile.parishId,
            purpose = "School admission verification",
            notes = "Sample test request for preview",
            status = RequestStatus.UNDER_REVIEW.name,
            websiteSyncStatus = WebsiteSyncStatus.DUMMY_TEST_MODE.name,
            createdAtMillis = now - 86_400_000L * 2,
            updatedAtMillis = now - 86_400_000L,
          ),
          CertificateRequest(
            id = "AJN-REQ-${UUID.randomUUID().toString().take(6).uppercase()}",
            userId = userId,
            certificateType = CertificateType.COMMUNION.name,
            applicantName = profile.fullName,
            subjectName = "Aaron D'Souza",
            eventDate = "2024-04-21",
            placeOrParish = "St. Anthony's Church, Ajni",
            parentsOrSpouseNames = "Joseph & Maria D'Souza",
            sponsorOrNextOfKin = "Fr. Thomas",
            registryReference = profile.parishId,
            purpose = "Parish sacramental record",
            notes = "Ready for office pickup",
            status = RequestStatus.READY.name,
            websiteSyncStatus = WebsiteSyncStatus.DUMMY_TEST_MODE.name,
            createdAtMillis = now - 86_400_000L * 7,
            updatedAtMillis = now - 86_400_000L * 3,
          ),
        )
      )
    }

    val session =
      UserSession(
        isLoggedIn = true,
        userId = userId,
        mobileNumber = "+91 ${digits.takeLast(10)}",
        parishId = profile.parishId,
        fullName = profile.fullName,
        authToken = "dummy-test-token",
        isDummyMode = true,
      )
    preferences.saveSession(session)
    return Result.success(session)
  }

  suspend fun signOut() {
    preferences.clearSession()
  }

  suspend fun saveProfile(profile: ParishionerProfile) {
    val now = System.currentTimeMillis()
    val updated = profile.copy(updatedAtMillis = now)
    dao.upsertProfile(updated)
    val currentSession = preferences.sessionFlow.first()
    if (currentSession.isLoggedIn && currentSession.userId == profile.userId) {
      preferences.saveSession(
        currentSession.copy(
          fullName = updated.fullName,
          parishId = updated.parishId,
          mobileNumber = updated.phone.ifBlank { currentSession.mobileNumber },
        )
      )
    }
  }

  suspend fun submitCertificateRequest(request: CertificateRequest): CertificateRequest {
    val now = System.currentTimeMillis()
    val id =
      request.id.ifBlank { "AJN-REQ-${UUID.randomUUID().toString().take(6).uppercase()}" }
    val entity = request.copy(id = id, createdAtMillis = now, updatedAtMillis = now)
    dao.upsertCertificateRequest(entity)
    return entity
  }

  suspend fun cancelCertificateRequest(userId: String, requestId: String) {
    dao.updateRequestStatus(
      userId = userId,
      requestId = requestId,
      status = RequestStatus.CANCELLED.name,
      updatedAt = System.currentTimeMillis(),
    )
  }

  suspend fun updateRequestSyncStatus(
    userId: String,
    requestId: String,
    syncStatus: WebsiteSyncStatus,
  ) {
    dao.updateRequestSyncStatus(
      userId = userId,
      requestId = requestId,
      syncStatus = syncStatus.name,
      updatedAt = System.currentTimeMillis(),
    )
  }

  suspend fun refreshFromWebsiteApi(session: UserSession): Result<Int> {
    if (session.isDummyMode) {
      return Result.success(0)
    }
    val config = preferences.configFlow.first()
    if (config.baseUrl.isBlank()) {
      return Result.failure(IllegalStateException("Website API URL is not configured."))
    }
    val now = System.currentTimeMillis()
    var syncedItems = 0

    apiClient.fetchProfile(config, session.authToken).onSuccess { dto ->
      dao.upsertProfile(
        dto.toEntity(
          fallbackUserId = session.userId,
          fallbackMobile = session.mobileNumber,
          fallbackParishId = session.parishId,
          syncStatus = WebsiteSyncStatus.SYNCED_TO_WEBSITE,
          now = now,
        )
      )
      syncedItems++
    }

    apiClient.fetchCertificateRequests(config, session.authToken).onSuccess { list ->
      val mapped =
        list.mapIndexed { idx, reqDto ->
          reqDto.toEntity(
            userId = session.userId,
            fallbackId = "api_${session.userId}_$idx",
            syncStatus = WebsiteSyncStatus.SYNCED_TO_WEBSITE,
            now = now,
          )
        }
      dao.upsertCertificateRequests(mapped)
      syncedItems += mapped.size
    }

    return Result.success(syncedItems)
  }

  private fun ParishionerDto?.toEntity(
    fallbackUserId: String,
    fallbackMobile: String,
    fallbackParishId: String,
    syncStatus: WebsiteSyncStatus,
    now: Long,
  ): ParishionerProfile =
    ParishionerProfile(
      userId = this?.userId?.takeIf { it.isNotBlank() } ?: fallbackUserId,
      parishId = this?.parishId?.takeIf { it.isNotBlank() } ?: fallbackParishId,
      fullName = this?.fullName?.takeIf { it.isNotBlank() } ?: "Parishioner",
      email = this?.email.orEmpty(),
      phone = this?.phone?.takeIf { it.isNotBlank() } ?: fallbackMobile,
      address = this?.address.orEmpty(),
      sccUnit = this?.sccUnit.orEmpty(),
      headOfFamily = this?.headOfFamily.orEmpty(),
      dateOfBirth = this?.dateOfBirth.orEmpty(),
      baptismDate = this?.baptismDate.orEmpty(),
      spouseName = this?.spouseName.orEmpty(),
      websiteSyncStatus = syncStatus.name,
      createdAtMillis = now,
      updatedAtMillis = now,
    )

  private fun CertificateRequestDto.toEntity(
    userId: String,
    fallbackId: String,
    syncStatus: WebsiteSyncStatus,
    now: Long,
  ): CertificateRequest =
    CertificateRequest(
      id = requestId?.takeIf { it.isNotBlank() } ?: fallbackId,
      userId = userId,
      certificateType = CertificateType.fromName(certificateType.orEmpty()).name,
      applicantName = applicantName.orEmpty(),
      subjectName = subjectName.orEmpty(),
      eventDate = eventDate.orEmpty(),
      placeOrParish = placeOrParish.orEmpty(),
      parentsOrSpouseNames = parentsOrSpouseNames.orEmpty(),
      sponsorOrNextOfKin = sponsorOrNextOfKin.orEmpty(),
      registryReference = registryReference.orEmpty(),
      purpose = purpose.orEmpty(),
      notes = notes.orEmpty(),
      status = RequestStatus.fromName(status.orEmpty()).name,
      websiteSyncStatus = syncStatus.name,
      createdAtMillis = createdAtMillis ?: now,
      updatedAtMillis = now,
    )

  companion object {
    const val DUMMY_MOBILE = "9823012345"
    const val DUMMY_PARISH_ID = "AJN-1935-042"

    fun normalizeMobileNumber(raw: String): String {
      val digitsOnly = raw.filter { it.isDigit() }
      return when {
        digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.drop(2)
        digitsOnly.length == 11 && digitsOnly.startsWith("0") -> digitsOnly.drop(1)
        else -> digitsOnly
      }
    }
  }
}
