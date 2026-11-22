package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.bridge.WebsiteBridgeConfig

@Composable
fun ApiBridgeScreen(
  config: WebsiteBridgeConfig,
  isTesting: Boolean,
  isSyncing: Boolean,
  onSaveConfig: (baseUrl: String, apiToken: String, autoSync: Boolean) -> Unit,
  onTestConnection: (baseUrl: String, apiToken: String, autoSync: Boolean) -> Unit,
  onSyncAllPending: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var baseUrl by remember { mutableStateOf(config.baseUrl) }
  var apiToken by remember { mutableStateOf(config.apiToken) }
  var autoSync by remember { mutableStateOf(config.autoSyncEnabled) }
  var showApiSpec by remember { mutableStateOf(true) }

  LaunchedEffect(config.baseUrl, config.apiToken, config.autoSyncEnabled) {
    baseUrl = config.baseUrl
    apiToken = config.apiToken
    autoSync = config.autoSyncEnabled
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
          text = "Website API Bridge",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text =
            "Point the mobile app at your St. Anthony's Church website REST API to authenticate existing mobile accounts and sync parishioner updates.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // Live Status Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
          CardDefaults.cardColors(
            containerColor =
              when (config.lastHealthOk) {
                true -> MaterialTheme.colorScheme.tertiaryContainer
                false -> MaterialTheme.colorScheme.errorContainer
                null -> MaterialTheme.colorScheme.secondaryContainer
              }
          ),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector =
              if (config.lastHealthOk == true) Icons.Filled.CloudDone
              else Icons.Filled.CloudSync,
            contentDescription = "API bridge status",
            tint =
              when (config.lastHealthOk) {
                true -> MaterialTheme.colorScheme.onTertiaryContainer
                false -> MaterialTheme.colorScheme.onErrorContainer
                null -> MaterialTheme.colorScheme.onSecondaryContainer
              },
            modifier = Modifier.size(28.dp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text =
                when (config.lastHealthOk) {
                  true -> "Website API Connected"
                  false -> "Website Endpoint Unreachable"
                  null -> "Ready for Parish Website API URL"
                },
              style = MaterialTheme.typography.titleMedium,
            )
            Text(text = config.lastSyncMessage, style = MaterialTheme.typography.bodySmall)
          }
        }
      }

      // Endpoint Configuration Card
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
          Text(
            text = "Endpoint Configuration",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
          )

          OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            modifier = Modifier.fillMaxWidth().testTag("bridge_url_input"),
            label = { Text("Website REST API Base URL") },
            placeholder = { Text("https://api.stanthonysajni.org/v1/") },
            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
          )

          OutlinedTextField(
            value = apiToken,
            onValueChange = { apiToken = it },
            modifier = Modifier.fillMaxWidth().testTag("bridge_token_input"),
            label = { Text("Static API Key (Optional if Login returns JWT)") },
            leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Auto-sync on Save & Submit",
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = "Automatically call PUT /parishioners/me and POST /certificates/requests.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
              checked = autoSync,
              onCheckedChange = { autoSync = it },
              modifier = Modifier.testTag("bridge_autosync_switch"),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { onTestConnection(baseUrl, apiToken, autoSync) },
              enabled = !isTesting,
              modifier = Modifier.weight(1f).height(48.dp).testTag("test_bridge_button"),
              shape = MaterialTheme.shapes.medium,
            ) {
              if (isTesting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Filled.Api, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Health")
              }
            }

            Button(
              onClick = { onSaveConfig(baseUrl, apiToken, autoSync) },
              modifier = Modifier.weight(1f).height(48.dp).testTag("save_bridge_button"),
              shape = MaterialTheme.shapes.medium,
              colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
              Text("Save Config")
            }
          }

          Button(
            onClick = onSyncAllPending,
            enabled = !isSyncing,
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sync_all_bridge_button"),
            shape = MaterialTheme.shapes.medium,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
              ),
          ) {
            if (isSyncing) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onSecondary,
                strokeWidth = 2.dp,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Syncing Records...")
            } else {
              Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Sync Pending Records to Website",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              )
            }
          }
        }
      }

      // Developer REST API Contract Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Website Backend API Contract",
              style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { showApiSpec = !showApiSpec }) {
              Text(if (showApiSpec) "Hide" else "View Spec")
              Icon(
                imageVector = if (showApiSpec) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
              )
            }
          }
          Text(
            text =
              "Expose these REST endpoints on your church website backend to connect your existing SQL database:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          AnimatedVisibility(visible = showApiSpec) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              HorizontalDivider()
              EndpointSpecBlock(
                endpoint = "POST /auth/mobile-login",
                description =
                  "Body: { \"mobileNumber\": \"9823012345\", \"parishId\": \"optional\" }\nReturns: { \"success\": true, \"token\": \"jwt\", \"parishioner\": { ... }, \"requests\": [...] }",
              )
              EndpointSpecBlock(
                endpoint = "GET / PUT /parishioners/me",
                description =
                  "Header: Authorization: Bearer <token>\nReads or updates parishioner details (parishId, fullName, email, phone, address, sccUnit, headOfFamily, dateOfBirth, baptismDate, spouseName).",
              )
              EndpointSpecBlock(
                endpoint = "GET / POST /certificates/requests",
                description =
                  "Lists or raises certificate requests for certificateType: BIRTH | COMMUNION | CONFIRMATION | MARRIAGE | DEATH.",
              )
              EndpointSpecBlock(
                endpoint = "GET /health",
                description = "Returns { \"status\": \"ok\", \"parish\": \"St. Anthony's Ajni\" }",
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EndpointSpecBlock(endpoint: String, description: String) {
  Surface(
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.surfaceContainerLow,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = endpoint,
        style =
          MaterialTheme.typography.labelLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
          ),
        color = MaterialTheme.colorScheme.primary,
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
