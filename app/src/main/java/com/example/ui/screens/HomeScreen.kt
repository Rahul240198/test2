package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.bridge.UserSession
import com.example.model.CertificateRequest
import com.example.model.CertificateType
import com.example.model.ParishionerProfile
import com.example.model.RequestStatus
import com.example.model.WebsiteSyncStatus
import com.example.ui.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RequestFilter(val label: String) {
  ALL("All"),
  ACTIVE("Active"),
  READY("Ready"),
  CANCELLED("Cancelled"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  session: UserSession,
  profileState: UiState<ParishionerProfile?>,
  requestsState: UiState<List<CertificateRequest>>,
  onEditProfile: () -> Unit,
  onRequestCertificate: (CertificateType) -> Unit,
  onSelectRequest: (CertificateRequest) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedFilter by remember { mutableStateOf(RequestFilter.ALL) }
  val profile = (profileState as? UiState.Success)?.data

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 0. Dummy Test Session Confirmation Banner (lets user verify login & test again easily)
      if (session.isDummyMode) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().testTag("dummy_home_session_banner"),
            shape = MaterialTheme.shapes.medium,
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
              ),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Login verified",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Logged In • Dummy Home Screen",
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                  text = "Signed in with ${session.mobileNumber} (Test Mode)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                )
              }
              OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.testTag("home_logout_test_button"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.small,
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Logout,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Out", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
      // 1. Sanctuary Hero Welcome Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.large,
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
          Column {
            Box(modifier = Modifier.fillMaxWidth().height(145.dp)) {
              Image(
                painter = painterResource(id = R.drawable.img_sanctuary_hero),
                contentDescription = "St. Anthony's Church, Ajni courtyard",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
              Box(
                modifier =
                  Modifier.fillMaxSize()
                    .background(
                      Brush.verticalGradient(
                        colors =
                          listOf(
                            Color.Black.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                          )
                      )
                    )
              )
              if (session.isDummyMode) {
                Surface(
                  modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                  shape = RoundedCornerShape(50),
                  color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Science,
                      contentDescription = "Dummy test mode",
                      tint = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Dummy Test Mode",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                }
              }
              Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(
                  text = "St. Anthony's Church, Ajni",
                  style = MaterialTheme.typography.headlineMedium,
                  color = Color.White,
                )
                Text(
                  text =
                    profile?.fullName?.takeIf { it.isNotBlank() }?.let { "Peace be with you, $it" }
                      ?: "Welcome to the Parishioner Portal",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.secondaryContainer,
                )
              }
            }

            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                AssistChip(
                  onClick = onEditProfile,
                  label = {
                    Text(
                      text = profile?.phone?.ifBlank { session.mobileNumber } ?: session.mobileNumber,
                      style = MaterialTheme.typography.labelMedium,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Filled.PhoneAndroid,
                      contentDescription = "Mobile number",
                      modifier = Modifier.size(16.dp),
                    )
                  },
                )

                AssistChip(
                  onClick = onEditProfile,
                  label = {
                    Text(
                      text =
                        profile?.parishId?.takeIf { it.isNotBlank() }?.let { "Parish ID: $it" }
                          ?: "Link Parish ID",
                      style = MaterialTheme.typography.labelMedium,
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Filled.Badge,
                      contentDescription = "Parish ID",
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  colors =
                    AssistChipDefaults.assistChipColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer,
                      labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                      leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                OutlinedButton(
                  onClick = onEditProfile,
                  modifier = Modifier.weight(1f).testTag("home_edit_profile_button"),
                  shape = MaterialTheme.shapes.medium,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Edit Details")
                }
                Button(
                  onClick = { onRequestCertificate(CertificateType.BIRTH) },
                  modifier = Modifier.weight(1f).testTag("home_new_request_button"),
                  shape = MaterialTheme.shapes.medium,
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary
                    ),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("New Request")
                }
              }
            }
          }
        }
      }

      // 2. Quick Certificate Request Carousel (All 5 Sacramental Certificates)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Sacramental Certificates",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "5 services",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.secondary,
            )
          }

          LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(CertificateType.entries) { type ->
              CertificateQuickCard(type = type, onClick = { onRequestCertificate(type) })
            }
          }
        }
      }

      // 3. Certificate Requests Tracker Header & Filter Chips
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "My Certificate Requests",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
          )

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(RequestFilter.entries) { filter ->
              FilterChip(
                selected = selectedFilter == filter,
                onClick = { selectedFilter = filter },
                label = { Text(filter.label) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  ),
              )
            }
          }
        }
      }

      // 4. Requests List / Loading / Empty State
      when (requestsState) {
        UiState.Loading -> {
          item {
            Box(
              modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
          }
        }
        is UiState.Error -> {
          item {
            Card(
              colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
              Text(
                text = requestsState.message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
            }
          }
        }
        is UiState.Success -> {
          val filtered =
            requestsState.data.filter { req ->
              when (selectedFilter) {
                RequestFilter.ALL -> true
                RequestFilter.ACTIVE ->
                  req.status == RequestStatus.SUBMITTED.name ||
                    req.status == RequestStatus.UNDER_REVIEW.name
                RequestFilter.READY -> req.status == RequestStatus.READY.name
                RequestFilter.CANCELLED -> req.status == RequestStatus.CANCELLED.name
              }
            }

          if (filtered.isEmpty()) {
            item {
              EmptyRequestsCard(
                hasAnyRequests = requestsState.data.isNotEmpty(),
                onStartRequest = { onRequestCertificate(CertificateType.BIRTH) },
              )
            }
          } else {
            items(filtered, key = { it.id }) { request ->
              CertificateRequestCard(
                request = request,
                onClick = { onSelectRequest(request) },
              )
            }
          }
        }
      }

      // 5. Parish Office Summary Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.large,
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Church,
                contentDescription = "Parish Office",
                tint = MaterialTheme.colorScheme.primary,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Parish Office • Ajni",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Text(
              text = "Railway Colony, Ajni, Nagpur, Maharashtra 440003",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Certificate Collection: Tue–Sat, 9:30 AM – 12:30 PM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CertificateQuickCard(type: CertificateType, onClick: () -> Unit) {
  Card(
    modifier =
      Modifier.width(156.dp)
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .testTag("quick_cert_${type.name.lowercase()}"),
    shape = MaterialTheme.shapes.medium,
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier =
          Modifier.size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = certificateIcon(type),
          contentDescription = type.label,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp),
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = type.shortLabel,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "Apply now",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.secondary,
        )
      }
    }
  }
}

@Composable
private fun EmptyRequestsCard(hasAnyRequests: Boolean, onStartRequest: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_sacramental_seal),
        contentDescription = "Parish registry ledger illustration",
        modifier = Modifier.height(130.dp).clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop,
      )
      Text(
        text =
          if (hasAnyRequests) "No matching certificate requests"
          else "No certificate requests yet",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text =
          if (hasAnyRequests) "Try switching the status filter above."
          else
            "Tap any sacrament above to request a Birth, Communion, Confirmation, Marriage, or Death certificate.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      AnimatedVisibility(visible = !hasAnyRequests) {
        Button(onClick = onStartRequest, shape = MaterialTheme.shapes.medium) {
          Text("Request a Certificate")
        }
      }
    }
  }
}

@Composable
private fun CertificateRequestCard(request: CertificateRequest, onClick: () -> Unit) {
  val type = CertificateType.fromName(request.certificateType)
  val status = RequestStatus.fromName(request.status)
  val syncStatus = WebsiteSyncStatus.fromName(request.websiteSyncStatus)
  val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
  val submittedDate = remember(request.createdAtMillis) {
    dateFormat.format(Date(request.createdAtMillis))
  }

  Card(
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .testTag("request_card_${request.id}"),
    shape = MaterialTheme.shapes.medium,
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = certificateIcon(type),
          contentDescription = type.label,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = type.label,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "${request.subjectName} • Event: ${request.eventDate}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          StatusBadge(status = status)
          Text(
            text = "• $submittedDate • ${syncStatus.label}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open request details",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
fun StatusBadge(status: RequestStatus) {
  val (bg, fg) =
    when (status) {
      RequestStatus.SUBMITTED ->
        MaterialTheme.colorScheme.secondaryContainer to
          MaterialTheme.colorScheme.onSecondaryContainer
      RequestStatus.UNDER_REVIEW ->
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
      RequestStatus.READY ->
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
      RequestStatus.CANCELLED ->
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

  Surface(shape = RoundedCornerShape(50), color = bg) {
    Text(
      text = status.label,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      color = fg,
    )
  }
}

fun certificateIcon(type: CertificateType): ImageVector =
  when (type) {
    CertificateType.BIRTH -> Icons.Filled.ChildCare
    CertificateType.COMMUNION -> Icons.Filled.AutoAwesome
    CertificateType.CONFIRMATION -> Icons.Filled.Verified
    CertificateType.MARRIAGE -> Icons.Filled.Favorite
    CertificateType.DEATH -> Icons.Filled.LocalFlorist
  }
