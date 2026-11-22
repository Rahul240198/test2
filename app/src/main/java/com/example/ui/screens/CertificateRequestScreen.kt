package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.model.CertificateType
import com.example.model.ParishionerProfile

@Composable
fun CertificateRequestScreen(
  selectedType: CertificateType,
  onSelectType: (CertificateType) -> Unit,
  profile: ParishionerProfile?,
  isSubmitting: Boolean,
  onSubmit: (
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
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  var applicantName by rememberSaveable { mutableStateOf("") }
  var subjectName by rememberSaveable { mutableStateOf("") }
  var eventDate by rememberSaveable { mutableStateOf("") }
  var placeOrParish by rememberSaveable { mutableStateOf("St. Anthony's Church, Ajni") }
  var parentsOrSpouseNames by rememberSaveable { mutableStateOf("") }
  var sponsorOrNextOfKin by rememberSaveable { mutableStateOf("") }
  var registryReference by rememberSaveable { mutableStateOf("") }
  var purpose by rememberSaveable { mutableStateOf("") }
  var notes by rememberSaveable { mutableStateOf("") }
  var validationError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(profile?.userId, profile?.fullName, profile?.parishId) {
    if (applicantName.isBlank() && !profile?.fullName.isNullOrBlank()) {
      applicantName = profile?.fullName.orEmpty()
    }
    if (registryReference.isBlank() && !profile?.parishId.isNullOrBlank()) {
      registryReference = profile?.parishId.orEmpty()
    }
  }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier =
        Modifier.widthIn(max = 760.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Request a Certificate",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text =
            "Select the sacrament or civil parish record you need from St. Anthony's Church, Ajni.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // 5 Certificate Type Selector Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
      ) {
        items(CertificateType.entries) { type ->
          FilterChip(
            selected = selectedType == type,
            onClick = {
              onSelectType(type)
              validationError = null
            },
            modifier = Modifier.testTag("cert_chip_${type.name.lowercase()}"),
            label = { Text(type.shortLabel) },
            leadingIcon = {
              Icon(
                imageVector = certificateIcon(type),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
            },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.secondaryContainer,
              ),
          )
        }
      }

      // Selected Certificate Header Banner
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = certificateIcon(selectedType),
              contentDescription = selectedType.label,
              tint = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.size(24.dp),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = selectedType.label,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
              text = selectedType.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
            )
          }
        }
      }

      // Form Fields Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedTextField(
            value = applicantName,
            onValueChange = { applicantName = it.take(100) },
            modifier = Modifier.fillMaxWidth().testTag("cert_applicant_input"),
            label = { Text("Applicant Full Name *") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )

          OutlinedTextField(
            value = subjectName,
            onValueChange = { subjectName = it.take(100) },
            modifier = Modifier.fillMaxWidth().testTag("cert_subject_input"),
            label = { Text("${selectedType.subjectLabel} *") },
            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )

          OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it.take(30) },
            modifier = Modifier.fillMaxWidth().testTag("cert_event_date_input"),
            label = { Text("${selectedType.eventDateLabel} *") },
            placeholder = { Text("e.g., 2018-04-22 or 22 Apr 2018") },
            leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          )

          OutlinedTextField(
            value = parentsOrSpouseNames,
            onValueChange = { parentsOrSpouseNames = it.take(160) },
            modifier = Modifier.fillMaxWidth().testTag("cert_parents_input"),
            label = { Text(selectedType.parentsLabel) },
            leadingIcon = { Icon(Icons.Filled.Groups, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )

          OutlinedTextField(
            value = sponsorOrNextOfKin,
            onValueChange = { sponsorOrNextOfKin = it.take(120) },
            modifier = Modifier.fillMaxWidth().testTag("cert_sponsor_input"),
            label = { Text(selectedType.sponsorLabel) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
              ),
          )

          OutlinedTextField(
            value = placeOrParish,
            onValueChange = { placeOrParish = it.take(120) },
            modifier = Modifier.fillMaxWidth().testTag("cert_place_input"),
            label = { Text(selectedType.placeLabel) },
            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          )

          OutlinedTextField(
            value = registryReference,
            onValueChange = { registryReference = it.take(60) },
            modifier = Modifier.fillMaxWidth().testTag("cert_registry_ref_input"),
            label = { Text("Parish ID / Register Reference (Optional)") },
            placeholder = { Text("e.g., AJN-1935-042 or Reg. Vol. IV") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          )

          OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it.take(200) },
            modifier = Modifier.fillMaxWidth().testTag("cert_purpose_input"),
            label = { Text("Purpose of Certificate *") },
            placeholder = { Text("e.g., School admission, Passport, Marriage preparation") },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
              ),
          )

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it.take(400) },
            modifier = Modifier.fillMaxWidth().testTag("cert_notes_input"),
            label = { Text("Additional Notes for Parish Office (Optional)") },
            minLines = 2,
            maxLines = 4,
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
              ),
          )

          if (validationError != null) {
            Text(
              text = validationError.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
            )
          }

          Button(
            onClick = {
              if (
                applicantName.isBlank() ||
                  subjectName.isBlank() ||
                  eventDate.isBlank() ||
                  purpose.isBlank()
              ) {
                validationError =
                  "Please fill in all required (*) fields: Applicant Name, Certificate Name, Event Date, and Purpose."
                return@Button
              }
              validationError = null
              onSubmit(
                selectedType,
                applicantName,
                subjectName,
                eventDate,
                placeOrParish,
                parentsOrSpouseNames,
                sponsorOrNextOfKin,
                registryReference,
                purpose,
                notes,
              ) {
                subjectName = ""
                eventDate = ""
                parentsOrSpouseNames = ""
                sponsorOrNextOfKin = ""
                purpose = ""
                notes = ""
              }
            },
            enabled = !isSubmitting,
            modifier =
              Modifier.fillMaxWidth().height(52.dp).testTag("submit_certificate_button"),
            shape = MaterialTheme.shapes.medium,
            colors =
              ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Submitting...")
            } else {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Submit ${selectedType.shortLabel} Request",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              )
            }
          }
        }
      }
    }
  }
}
