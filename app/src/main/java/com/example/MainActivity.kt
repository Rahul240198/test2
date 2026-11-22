package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ParishRepository
import com.example.data.bridge.WebsiteApiBridgeClient
import com.example.data.bridge.WebsiteBridgePreferences
import com.example.data.local.ParishDatabase
import com.example.ui.AuthViewModel
import com.example.ui.ParishAppShell
import com.example.ui.auth.LoginScreen
import com.example.ui.theme.StAnthonysTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      StAnthonysTheme {
        ParishAppRoot()
      }
    }
  }
}

@Composable
fun ParishAppRoot() {
  val authViewModel: AuthViewModel =
    viewModel(
      factory = viewModelFactory {
        initializer {
          val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
          val prefs = WebsiteBridgePreferences(app)
          val client = WebsiteApiBridgeClient()
          val dao = ParishDatabase.getInstance(app).parishDao()
          AuthViewModel(
            repository = ParishRepository(dao, prefs, client),
            bridgePreferences = prefs,
          )
        }
      }
    )

  val session by authViewModel.session.collectAsStateWithLifecycle()
  val bridgeConfig by authViewModel.bridgeConfig.collectAsStateWithLifecycle()
  val isLoggingIn by authViewModel.isLoggingIn.collectAsStateWithLifecycle()
  val loginError by authViewModel.loginError.collectAsStateWithLifecycle()

  val currentSession = session
  when {
    currentSession == null -> {
      Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      }
    }
    !currentSession.isLoggedIn -> {
      LoginScreen(
        bridgeConfig = bridgeConfig,
        isLoading = isLoggingIn,
        errorMessage = loginError,
        onClearError = { authViewModel.clearError() },
        onLoginWithMobileApi = { mobile, parishId, apiUrl ->
          authViewModel.loginWithMobileApi(mobile, parishId, apiUrl)
        },
        onDummyLogin = { mobile, parishId ->
          authViewModel.loginWithDummyAccount(mobile, parishId)
        },
      )
    }
    else -> {
      key("${currentSession.userId}_${currentSession.isDummyMode}") {
        ParishAppShell(
          session = currentSession,
          onSignOut = { authViewModel.signOut() },
        )
      }
    }
  }
}
