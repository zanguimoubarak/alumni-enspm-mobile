package com.enspm.alumni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.enspm.alumni.auth.ui.*
import com.enspm.alumni.core.session.SessionManager
import com.enspm.alumni.core.ui.ShellActionCard
import com.enspm.alumni.core.session.SessionState
import com.enspm.alumni.feed.ui.FeedRoute
import com.enspm.alumni.feed.ui.PostDetailRoute
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
        composable("shell") { ShellScreen(onOpenFeed = { navController.navigate("feed") }) }
        composable("feed") { FeedRoute(onPostClick = { navController.navigate("posts/$it") }) }
        composable("posts/{postId}", arguments = listOf(navArgument("postId") { type = NavType.LongType })) {
            PostDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chargement de la session…") }
}

@Composable
private fun ShellScreen(onOpenFeed: () -> Unit) {
    val viewModel: ShellViewModel = hiltViewModel()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Alumni ENSPM", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = viewModel::logout) { Text("Déconnexion") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Bonjour 👋", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Votre session est active. Les modules Alumni seront activés progressivement.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ShellActionCard("Fil d’actualité", "Lire les publications Alumni", modifier = Modifier.clickable(onClick = onOpenFeed))
            ShellActionCard("Réseau alumni", "Retrouver les membres ENSPM — bientôt disponible")
            ShellActionCard("Opportunités", "Stages, emplois et formations — bientôt disponible")
            ShellActionCard("Notifications", "Suivre les alertes importantes — bientôt disponible")
        }
    }
}
