package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.bridge.UserSession
import com.example.model.ParishionerProfile
import com.example.model.WebsiteSyncStatus
import com.example.ui.UiState

@Composable
fun ProfileScreen(
  session: UserSession,
  profileState: UiState<ParishionerProfile?>,
  isSaving: Boolean,
  onSaveProfile: (ParishionerProfile) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val loadedProfile = (profileState as? UiState.Success)?.data

  var parishId by remember { mutableStateOf(session.parishId) }
  var fullName by remember { mutableStateOf(session.fullName) }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf(session.mobileNumber) }
  var address by remember { mutableStateOf("") }
  var sccUnit by remember { mutableStateOf("") }
  var headOfFamily by remember { mutableStateOf("") }
  var dateOfBirth by remember { mutableStateOf("") }
  var baptismDate by remember { mutableStateOf("") }
  var spouseName by remember { mutableStateOf("") }
  var initializedFromLocal by remember { mutableStateOf(false) }

  LaunchedEffect(loadedProfile) {
    if (loadedProfile != null && !initializedFromLocal) {
      parishId = loadedProfile.parishId
      fullName = loadedProfile.fullName
      email = loadedProfile.email
      phone = loadedProfile.phone.ifBlank { session.mobileNumber }
      address = loadedProfile.address
      sccUnit = loadedProfile.sccUnit
      headOfFamily = loadedProfile.headOfFamily
      dateOfBirth = loadedProfile.dateOfBirth
      baptismDate = loadedProfile.baptismDate
      spouseName = loadedProfile.spouseName
      initializedFromLocal = true
    }
  }

  val syncStatus =
    loadedProfile?.websiteSyncStatus?.let { WebsiteSyncStatus.fromName(it) }
      ?: if (session.isDummyMode) WebsiteSyncStatus.DUMMY_TEST_MODE
      else WebsiteSyncStatus.PENDING_API_SYNC

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier =
        Modifier.widthIn(max = 760.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Profile Summary Header Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val initials =
            fullName
              .trim()
              .split(" ")
              .filter { it.isNotBlank() }
              .take(2)
              .joinToString("") { it.first().uppercaseChar().toString() }
              .ifBlank { "PA" }

          Box(
            modifier =
              Modifier.size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = initials,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = fullName.ifBlank { "Parishioner Profile" },
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
              text = phone.ifBlank { session.mobileNumber },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
            Surface(
              shape = RoundedCornerShape(50),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f),
            ) {
              Text(
                text =
                  if (parishId.isNotBlank()) "Parish ID: $parishId • ${syncStatus.label}"
                  else syncStatus.label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondaryContainer,
              )
            }
          }
        }
      }

      if (profileState is UiState.Loading) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
      } else {
        ProfileSectionCard(title = "Parish Registry & Household") {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it.take(100) },
            modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
            label = { Text("Full Name *") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )
          OutlinedTextField(
            value = parishId,
            onValueChange = { parishId = it.take(40) },
            modifier = Modifier.fillMaxWidth().testTag("profile_parish_id_input"),
            label = { Text("Parish / Family Card ID") },
            placeholder = { Text("e.g., AJN-1935-042") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next,
              ),
          )
          OutlinedTextField(
            value = sccUnit,
            onValueChange = { sccUnit = it.take(80) },
            modifier = Modifier.fillMaxWidth().testTag("profile_scc_input"),
            label = { Text("SCC / Ward Unit") },
            placeholder = { Text("e.g., St. Jude Ward, Ajni") },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )
          OutlinedTextField(
            value = headOfFamily,
            onValueChange = { headOfFamily = it.take(100) },
            modifier = Modifier.fillMaxWidth().testTag("profile_family_head_input"),
            label = { Text("Head of Family") },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )
        }

        ProfileSectionCard(title = "Contact & Address") {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.take(30) },
            modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"),
            label = { Text("Mobile / Contact Number") },
            placeholder = { Text("+91 98XXXXXXXX") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
          )
          OutlinedTextField(
            value = email,
            onValueChange = { email = it.take(120) },
            modifier = Modifier.fillMaxWidth().testTag("profile_email_input"),
            label = { Text("Email Address (Optional)") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
          )
          OutlinedTextField(
            value = address,
            onValueChange = { address = it.take(250) },
            modifier = Modifier.fillMaxWidth().testTag("profile_address_input"),
            label = { Text("Residential Address") },
            placeholder = { Text("Quarter/House No., Railway Colony, Ajni, Nagpur") },
            leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
            minLines = 2,
            maxLines = 3,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
              ),
          )
        }

        ProfileSectionCard(title = "Sacramental Details") {
          OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it.take(30) },
            modifier = Modifier.fillMaxWidth().testTag("profile_dob_input"),
            label = { Text("Date of Birth (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          )
          OutlinedTextField(
            value = baptismDate,
            onValueChange = { baptismDate = it.take(30) },
            modifier = Modifier.fillMaxWidth().testTag("profile_baptism_input"),
            label = { Text("Date of Baptism (Optional)") },
            leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          )
          OutlinedTextField(
            value = spouseName,
            onValueChange = { spouseName = it.take(100) },
            modifier = Modifier.fillMaxWidth().testTag("profile_spouse_input"),
            label = { Text("Spouse Name (If Applicable)") },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
              ),
          )
        }

        Button(
          onClick = {
            onSaveProfile(
              (loadedProfile ?: ParishionerProfile(userId = session.userId)).copy(
                userId = session.userId,
                parishId = parishId,
                fullName = fullName,
                email = email,
                phone = phone,
                address = address,
                sccUnit = sccUnit,
                headOfFamily = headOfFamily,
                dateOfBirth = dateOfBirth,
                baptismDate = baptismDate,
                spouseName = spouseName,
              )
            )
          },
          enabled = !isSaving,
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("save_profile_button"),
          shape = MaterialTheme.shapes.medium,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
          if (isSaving) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saving Changes...")
          } else {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Save & Update Details",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
          }
        }

        OutlinedButton(
          onClick = onSignOut,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button"),
          shape = MaterialTheme.shapes.medium,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Log Out")
        }
      }
    }
  }
}

@Composable
private fun ProfileSectionCard(title: String, content: @Composable () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
      )
      content()
    }
  }
}
