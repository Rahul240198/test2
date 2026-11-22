package com.example.data.bridge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bridgeDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "parish_website_bridge")

class WebsiteBridgePreferences(context: Context) {

  private val dataStore = context.applicationContext.bridgeDataStore

  val configFlow: Flow<WebsiteBridgeConfig> =
    dataStore.data.map { prefs ->
      WebsiteBridgeConfig(
        baseUrl = prefs[KEY_BASE_URL] ?: "",
        apiToken = prefs[KEY_API_TOKEN] ?: "",
        autoSyncEnabled = prefs[KEY_AUTO_SYNC] ?: true,
        lastSyncMessage =
          prefs[KEY_LAST_SYNC_MSG]
            ?: "Configure your parish website API URL or sign in with Dummy Test Mode",
        lastHealthOk = prefs[KEY_LAST_HEALTH_OK],
      )
    }

  val sessionFlow: Flow<UserSession> =
    dataStore.data.map { prefs ->
      UserSession(
        isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false,
        userId = prefs[KEY_USER_ID] ?: "",
        mobileNumber = prefs[KEY_MOBILE_NUMBER] ?: "",
        parishId = prefs[KEY_PARISH_ID] ?: "",
        fullName = prefs[KEY_FULL_NAME] ?: "",
        authToken = prefs[KEY_SESSION_TOKEN] ?: "",
        isDummyMode = prefs[KEY_IS_DUMMY_MODE] ?: false,
      )
    }

  suspend fun saveSession(session: UserSession) {
    dataStore.edit { prefs ->
      prefs[KEY_IS_LOGGED_IN] = session.isLoggedIn
      prefs[KEY_USER_ID] = session.userId
      prefs[KEY_MOBILE_NUMBER] = session.mobileNumber
      prefs[KEY_PARISH_ID] = session.parishId
      prefs[KEY_FULL_NAME] = session.fullName
      prefs[KEY_SESSION_TOKEN] = session.authToken
      prefs[KEY_IS_DUMMY_MODE] = session.isDummyMode
    }
  }

  suspend fun clearSession() {
    dataStore.edit { prefs ->
      prefs[KEY_IS_LOGGED_IN] = false
      prefs.remove(KEY_USER_ID)
      prefs.remove(KEY_MOBILE_NUMBER)
      prefs.remove(KEY_PARISH_ID)
      prefs.remove(KEY_FULL_NAME)
      prefs.remove(KEY_SESSION_TOKEN)
      prefs[KEY_IS_DUMMY_MODE] = false
    }
  }

  suspend fun saveConfig(
    baseUrl: String,
    apiToken: String,
    autoSyncEnabled: Boolean,
  ) {
    val normalized = normalizeUrl(baseUrl)
    dataStore.edit { prefs ->
      prefs[KEY_BASE_URL] = normalized
      prefs[KEY_API_TOKEN] = apiToken.trim()
      prefs[KEY_AUTO_SYNC] = autoSyncEnabled
      if (normalized.isBlank()) {
        prefs[KEY_LAST_SYNC_MSG] = "Website API URL cleared; running in local/test mode."
        prefs.remove(KEY_LAST_HEALTH_OK)
      }
    }
  }

  suspend fun recordStatus(message: String, healthOk: Boolean?) {
    dataStore.edit { prefs ->
      prefs[KEY_LAST_SYNC_MSG] = message
      if (healthOk != null) {
        prefs[KEY_LAST_HEALTH_OK] = healthOk
      } else {
        prefs.remove(KEY_LAST_HEALTH_OK)
      }
    }
  }

  private fun normalizeUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return ""
    val withScheme =
      if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
      } else {
        "https://$trimmed"
      }
    return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
  }

  private companion object {
    val KEY_BASE_URL = stringPreferencesKey("bridge_base_url")
    val KEY_API_TOKEN = stringPreferencesKey("bridge_api_token")
    val KEY_AUTO_SYNC = booleanPreferencesKey("bridge_auto_sync")
    val KEY_LAST_SYNC_MSG = stringPreferencesKey("bridge_last_sync_msg")
    val KEY_LAST_HEALTH_OK = booleanPreferencesKey("bridge_last_health_ok")

    val KEY_IS_LOGGED_IN = booleanPreferencesKey("session_is_logged_in")
    val KEY_USER_ID = stringPreferencesKey("session_user_id")
    val KEY_MOBILE_NUMBER = stringPreferencesKey("session_mobile_number")
    val KEY_PARISH_ID = stringPreferencesKey("session_parish_id")
    val KEY_FULL_NAME = stringPreferencesKey("session_full_name")
    val KEY_SESSION_TOKEN = stringPreferencesKey("session_auth_token")
    val KEY_IS_DUMMY_MODE = booleanPreferencesKey("session_is_dummy_mode")
  }
}
