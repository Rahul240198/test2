package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ParishRepository
import com.example.data.bridge.WebsiteBridgeConfig

@Composable
fun LoginScreen(
  bridgeConfig: WebsiteBridgeConfig,
  isLoading: Boolean,
  errorMessage: String?,
  onClearError: () -> Unit,
  onLoginWithMobileApi: (mobileNumber: String, parishId: String, apiUrlOverride: String?) -> Unit,
  onDummyLogin: (mobileNumber: String, parishId: String) -> Unit,
) {
  var mobileNumber by rememberSaveable { mutableStateOf("") }
  var parishId by rememberSaveable { mutableStateOf("") }
  var apiBaseUrl by rememberSaveable { mutableStateOf(bridgeConfig.baseUrl) }
  var showApiUrlConfig by remember { mutableStateOf(false) }

  LaunchedEffect(bridgeConfig.baseUrl) {
    if (apiBaseUrl.isBlank() && bridgeConfig.baseUrl.isNotBlank()) {
      apiBaseUrl = bridgeConfig.baseUrl
    }
  }

  val hasConfiguredApi = apiBaseUrl.trim().isNotBlank()

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Sanctuary Watercolor Hero
      Box(modifier = Modifier.fillMaxWidth().height(255.dp)) {
        Image(
          painter = painterResource(id = R.drawable.img_sanctuary_hero),
          contentDescription = "St. Anthony's Church, Ajni sanctuary illustration",
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
                      Color.Black.copy(alpha = 0.25f),
                      Color.Transparent,
                      MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                      MaterialTheme.colorScheme.background,
                    )
                )
              )
        )
        Surface(
          modifier =
            Modifier.align(Alignment.TopStart)
              .statusBarsPadding()
              .padding(horizontal = 20.dp, vertical = 12.dp),
          shape = RoundedCornerShape(50),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Church,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Est. 1935 • Ajni, Nagpur",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimary,
            )
          }
        }
      }

      Column(
        modifier =
          Modifier.widthIn(max = 520.dp)
            .fillMaxWidth()
            .offset(y = (-36).dp)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Parish Crest Emblem
        Box(
          modifier =
            Modifier.size(72.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .border(3.dp, MaterialTheme.colorScheme.secondaryContainer, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_launcher_crest),
            contentDescription = "St. Anthony's Church crest",
            modifier = Modifier.size(56.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "St. Anthony's Church",
          style = MaterialTheme.typography.displayMedium,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center,
        )
        Text(
          text = "Parishioner Portal • Railway Colony, Ajni",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Mobile Number Login Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.large,
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = "Parishioner Login",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                if (hasConfiguredApi)
                  "Enter your registered mobile number to sign in via the St. Anthony's parish website API."
                else
                  "Enter your 10-digit mobile number and tap Login to preview the parishioner Home screen (Dummy Test Mode active until API URL is set).",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
              value = mobileNumber,
              onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '+' || it == ' ' }.take(15)
                mobileNumber = filtered
                if (errorMessage != null) onClearError()
              },
              modifier = Modifier.fillMaxWidth().testTag("login_mobile_input"),
              label = { Text("Registered Mobile Number *") },
              placeholder = { Text("9823012345") },
              supportingText = {
                Text(
                  if (hasConfiguredApi) "Verified against POST /auth/mobile-login"
                  else "Test login: enter 9823012345 or any 10-digit mobile number"
                )
              },
              prefix = {
                Text(
                  text = "+91 ",
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.primary,
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Filled.PhoneAndroid,
                  contentDescription = "Mobile number",
                  tint = MaterialTheme.colorScheme.primary,
                )
              },
              singleLine = true,
              keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            OutlinedTextField(
              value = parishId,
              onValueChange = {
                parishId = it.take(40)
                if (errorMessage != null) onClearError()
              },
              modifier = Modifier.fillMaxWidth().testTag("login_parish_id_input"),
              label = { Text("Parish / Family Card ID (Optional)") },
              placeholder = { Text("e.g., AJN-1935-042") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Filled.Badge,
                  contentDescription = "Parish ID",
                  tint = MaterialTheme.colorScheme.secondary,
                )
              },
              singleLine = true,
              keyboardOptions =
                KeyboardOptions(
                  capitalization = KeyboardCapitalization.Characters,
                  imeAction = ImeAction.Done,
                ),
            )

            // Optional collapsible Website API Base URL setting
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text =
                  if (hasConfiguredApi) "API: ${apiBaseUrl.trim()}"
                  else "Mode: Dummy Test Login (No API URL set)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
              )
              TextButton(
                onClick = { showApiUrlConfig = !showApiUrlConfig },
                modifier = Modifier.testTag("toggle_login_api_url_button"),
              ) {
                Text(if (showApiUrlConfig) "Hide URL" else if (hasConfiguredApi) "Edit URL" else "Set API URL")
                Icon(
                  imageVector =
                    if (showApiUrlConfig) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
              }
            }

            AnimatedVisibility(visible = showApiUrlConfig) {
              OutlinedTextField(
                value = apiBaseUrl,
                onValueChange = {
                  apiBaseUrl = it
                  if (errorMessage != null) onClearError()
                },
                modifier = Modifier.fillMaxWidth().testTag("login_api_url_input"),
                label = { Text("Website REST API Base URL (Optional)") },
                placeholder = { Text("https://api.stanthonysajni.org/v1/") },
                supportingText = { Text("Leave blank for Dummy Test Login, or set your server URL for live API auth.") },
                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                singleLine = true,
                keyboardOptions =
                  KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
              )
            }

            AnimatedVisibility(visible = errorMessage != null) {
              Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer,
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = "Login error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                  )
                }
              }
            }

            Button(
              onClick = {
                onLoginWithMobileApi(
                  mobileNumber,
                  parishId,
                  apiBaseUrl.trim().takeIf { it.isNotBlank() },
                )
              },
              enabled = !isLoading,
              modifier = Modifier.fillMaxWidth().height(54.dp).testTag("mobile_login_button"),
              shape = MaterialTheme.shapes.medium,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
              if (isLoading) {
                CircularProgressIndicator(
                  modifier = Modifier.size(22.dp),
                  color = MaterialTheme.colorScheme.onPrimary,
                  strokeWidth = 2.5.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Signing In...")
              } else {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Login,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = if (hasConfiguredApi) "Login via Parish API" else "Login to Home Screen",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              HorizontalDivider(modifier = Modifier.weight(1f))
              Text(
                text = "  QUICK TEST SHORTCUT  ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
              )
              HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // One-tap Dummy Login Card for instant testing without typing
            Surface(
              shape = MaterialTheme.shapes.medium,
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Filled.Science,
                    contentDescription = "Dummy test login",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "One-Tap Dummy Test Account",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                }
                Text(
                  text =
                    "Demo Parishioner: Maria D'Souza • Mobile: +91 ${ParishRepository.DUMMY_MOBILE} • Parish ID: ${ParishRepository.DUMMY_PARISH_ID}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                )
                OutlinedButton(
                  onClick = { onDummyLogin(ParishRepository.DUMMY_MOBILE, parishId) },
                  enabled = !isLoading,
                  modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dummy_login_button"),
                  shape = MaterialTheme.shapes.medium,
                ) {
                  Text(
                    text = "Quick Dummy Login (+91 ${ParishRepository.DUMMY_MOBILE})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Archdiocese of Nagpur • St. Anthony's Parish Office",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}
