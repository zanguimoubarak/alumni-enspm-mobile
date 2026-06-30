package com.enspm.alumni.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.enspm.alumni.core.ui.GradientPrimaryButton
import com.enspm.alumni.core.ui.LiteCard
import com.enspm.alumni.core.ui.LiteTextField

@Composable
fun LoginScreen(state: LoginUiState, onEmail: (String) -> Unit, onPassword: (String) -> Unit, onLogin: () -> Unit, onForgot: () -> Unit) {
    AuthScaffold(title = "Alumni ENSPM", subtitle = "Connexion à votre espace mobile Lite") {
        LiteTextField(state.email, onEmail, "Email", error = state.fieldErrors["email"])
        LiteTextField(state.password, onPassword, "Mot de passe", error = state.fieldErrors["password"], visualTransformation = PasswordVisualTransformation())
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        GradientPrimaryButton("Se connecter", state.loading, onLogin)
        TextButton(onClick = onForgot) { Text("Mot de passe oublié ?") }
    }
}

@Composable
fun PasswordResetScreen(state: PasswordResetUiState, actions: PasswordResetActions, onBackToLogin: () -> Unit) {
    AuthScaffold(title = "Réinitialisation", subtitle = "OTP envoyé par l'API Alumni ENSPM") {
        when (state.step) {
            PasswordResetStep.Email -> {
                LiteTextField(state.email, actions.onEmail, "Email", error = state.fieldErrors["email"])
                GradientPrimaryButton("Recevoir le code OTP", state.loading, actions.onForgot)
            }
            PasswordResetStep.Otp -> {
                LiteTextField(state.email, actions.onEmail, "Email", error = state.fieldErrors["email"])
                LiteTextField(state.otp, actions.onOtp, "Code OTP", error = state.fieldErrors["otp"])
                GradientPrimaryButton("Vérifier le code", state.loading, actions.onVerify)
                TextButton(onClick = actions.onResend, enabled = !state.loading) { Text("Renvoyer le code") }
            }
            PasswordResetStep.Reset -> {
                LiteTextField(state.password, actions.onPassword, "Nouveau mot de passe", error = state.fieldErrors["password"], visualTransformation = PasswordVisualTransformation())
                LiteTextField(state.confirmation, actions.onConfirmation, "Confirmation", error = state.fieldErrors["password_confirmation"], visualTransformation = PasswordVisualTransformation())
                GradientPrimaryButton("Réinitialiser", state.loading, actions.onReset)
            }
            PasswordResetStep.Done -> {
                Text(state.message ?: "Mot de passe réinitialisé.")
                GradientPrimaryButton("Retour à la connexion", false, onBackToLogin)
            }
        }
        if (state.step != PasswordResetStep.Done) state.message?.let { Text(it) }
        TextButton(onClick = onBackToLogin) { Text("Retour connexion") }
    }
}

data class PasswordResetActions(
    val onEmail: (String) -> Unit,
    val onOtp: (String) -> Unit,
    val onPassword: (String) -> Unit,
    val onConfirmation: (String) -> Unit,
    val onForgot: () -> Unit,
    val onVerify: () -> Unit,
    val onResend: () -> Unit,
    val onReset: () -> Unit,
)

@Composable
private fun AuthScaffold(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            LiteCard { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}
