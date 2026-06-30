package com.enspm.alumni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enspm.alumni.auth.ui.*
import com.enspm.alumni.core.session.SessionManager
import com.enspm.alumni.core.session.SessionState
import com.enspm.alumni.ui.theme.AlumniTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlumniTheme {
                AlumniApp(sessionManager)
            }
        }
    }
}

@Composable
private fun AlumniApp(sessionManager: SessionManager) {
    val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.Authenticated -> navController.navigate("shell") { popUpTo(0) }
            SessionState.Unauthenticated -> navController.navigate("login") { popUpTo(0) }
            SessionState.Loading -> Unit
        }
    }

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen() }
        composable("login") {
            val vm: LoginViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LoginScreen(state, vm::onEmailChange, vm::onPasswordChange, vm::login) { navController.navigate("forgot") }
        }
        composable("forgot") {
            val vm: PasswordResetViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            PasswordResetScreen(
                state = state,
                actions = PasswordResetActions(vm::setEmail, vm::setOtp, vm::setPassword, vm::setConfirmation, vm::forgotPassword, vm::verifyOtp, vm::resendOtp, vm::resetPassword),
                onBackToLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } },
            )
        }
        composable("shell") { ShellScreen() }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chargement de la session…") }
}

@Composable
private fun ShellScreen() {
    val viewModel: ShellViewModel = hiltViewModel()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Alumni ENSPM", style = MaterialTheme.typography.headlineMedium)
            Text("Session active — shell principal Lite")
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = viewModel::logout) { Text("Se déconnecter") }
        }
    }
}
