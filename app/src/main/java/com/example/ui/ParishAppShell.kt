package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ParishRepository
import com.example.data.bridge.UserSession
import com.example.data.bridge.WebsiteApiBridgeClient
import com.example.data.bridge.WebsiteBridgePreferences
import com.example.data.local.ParishDatabase
import com.example.model.CertificateRequest
import com.example.model.CertificateType
import com.example.model.RequestStatus
import com.example.model.WebsiteSyncStatus
import com.example.ui.screens.CertificateRequestScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StatusBadge

enum class ParishTab(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
  REQUEST("Certificates", Icons.Filled.Description, Icons.Outlined.Description),
  PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParishAppShell(
  session: UserSession,
  onSignOut: () -> Unit,
) {
  val viewModel: ParishViewModel =
    viewModel(
      key = "${session.userId}_${session.isDummyMode}",
      factory = viewModelFactory {
        initializer {
          val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
          val prefs = WebsiteBridgePreferences(app)
          val client = WebsiteApiBridgeClient()
          val dao = ParishDatabase.getInstance(app).parishDao()
          ParishViewModel(
            repository = ParishRepository(dao, prefs, client),
            bridgePreferences = prefs,
            bridgeClient = client,
            session = session,
          )
        }
      },
    )

  val profileState by viewModel.profileState.collectAsStateWithLifecycle()
  val requestsState by viewModel.requestsState.collectAsStateWithLifecycle()
  val isSavingProfile by viewModel.isSavingProfile.collectAsStateWithLifecycle()
  val isSubmittingRequest by viewModel.isSubmittingRequest.collectAsStateWithLifecycle()

  var currentTab by rememberSaveable { mutableStateOf(ParishTab.HOME) }
  var selectedCertType by rememberSaveable { mutableStateOf(CertificateType.BIRTH) }
  var inspectedRequest by remember { mutableStateOf<CertificateRequest?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { message -> snackbarHostState.showSnackbar(message) }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!useRail) {
          NavigationBar(
            windowInsets = WindowInsets.navigationBars,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          ) {
            ParishTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = tab },
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label, maxLines = 1) },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                  ),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useRail) {
          NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          ) {
            Spacer(modifier = Modifier.height(12.dp))
            ParishTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = tab },
                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}"),
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          when (currentTab) {
            ParishTab.HOME -> {
              HomeScreen(
                session = session,
                profileState = profileState,
                requestsState = requestsState,
                onEditProfile = { currentTab = ParishTab.PROFILE },
                onRequestCertificate = { certType ->
                  selectedCertType = certType
                  currentTab = ParishTab.REQUEST
                },
                onSelectRequest = { inspectedRequest = it },
                onSignOut = {
                  viewModel.signOut()
                  onSignOut()
                },
              )
            }
            ParishTab.REQUEST -> {
              CertificateRequestScreen(
                selectedType = selectedCertType,
                onSelectType = { selectedCertType = it },
                profile = (profileState as? UiState.Success)?.data,
                isSubmitting = isSubmittingRequest,
                onSubmit = {
                  type,
                  applicant,
                  subject,
                  date,
                  place,
                  parents,
                  sponsor,
                  ref,
                  purpose,
                  notes,
                  onDone ->
                  viewModel.submitCertificateRequest(
                    type = type,
                    applicantName = applicant,
                    subjectName = subject,
                    eventDate = date,
                    placeOrParish = place,
                    parentsOrSpouseNames = parents,
                    sponsorOrNextOfKin = sponsor,
                    registryReference = ref,
                    purpose = purpose,
                    notes = notes,
                    onSuccess = {
                      onDone()
                      currentTab = ParishTab.HOME
                    },
                  )
                },
              )
            }
            ParishTab.PROFILE -> {
              ProfileScreen(
                session = session,
                profileState = profileState,
                isSaving = isSavingProfile,
                onSaveProfile = { viewModel.saveProfile(it) },
                onSignOut = {
                  viewModel.signOut()
                  onSignOut()
                },
              )
            }
          }
        }
      }
    }
  }

  val activeRequest = inspectedRequest
  if (activeRequest != null) {
    CertificateDetailSheet(
      request = activeRequest,
      onDismiss = { inspectedRequest = null },
      onCancelRequest = {
        viewModel.cancelCertificateRequest(activeRequest.id)
        inspectedRequest = null
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CertificateDetailSheet(
  request: CertificateRequest,
  onDismiss: () -> Unit,
  onCancelRequest: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val type = CertificateType.fromName(request.certificateType)
  val status = RequestStatus.fromName(request.status)
  val syncStatus = WebsiteSyncStatus.fromName(request.websiteSyncStatus)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = type.label,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        StatusBadge(status = status)
      }

      Text(
        text = "Reference ID: ${request.id} • ${syncStatus.label}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      HorizontalDivider()

      DetailRow("Applicant", request.applicantName)
      DetailRow("Name on Certificate", request.subjectName)
      DetailRow("Event Date", request.eventDate)
      if (request.placeOrParish.isNotBlank()) DetailRow("Place / Parish", request.placeOrParish)
      if (request.parentsOrSpouseNames.isNotBlank()) {
        DetailRow("Parents / Couple", request.parentsOrSpouseNames)
      }
      if (request.sponsorOrNextOfKin.isNotBlank()) {
        DetailRow("Sponsor / Next of Kin", request.sponsorOrNextOfKin)
      }
      if (request.registryReference.isNotBlank()) {
        DetailRow("Registry Ref", request.registryReference)
      }
      DetailRow("Purpose", request.purpose)
      if (request.notes.isNotBlank()) DetailRow("Notes", request.notes)

      if (status == RequestStatus.SUBMITTED || status == RequestStatus.UNDER_REVIEW) {
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
          onClick = onCancelRequest,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("cancel_request_button"),
          colors =
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Cancel Certificate Request")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "$label:",
      modifier = Modifier.width(136.dp),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}
