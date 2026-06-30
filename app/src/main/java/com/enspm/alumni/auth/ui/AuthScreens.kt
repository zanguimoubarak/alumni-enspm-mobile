package com.enspm.alumni.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.enspm.alumni.core.ui.AuthFooter
import com.enspm.alumni.core.ui.AuthHeader
import com.enspm.alumni.core.ui.AuthStepLabel
import com.enspm.alumni.core.ui.GradientPrimaryButton
import com.enspm.alumni.core.ui.LiteCard
import com.enspm.alumni.core.ui.LiteMessageBanner
import com.enspm.alumni.core.ui.LiteTextField
import com.enspm.alumni.core.ui.MessageTone

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onLogin: () -> Unit,
    onForgot: () -> Unit,
) {
    AuthScaffold(
        title = "Bienvenue",
        subtitle = "Connectez-vous à votre espace Alumni ENSPM.",
    ) {
        LiteTextField(
            value = state.email,
            onValueChange = onEmail,
            label = "Email",
            error = state.fieldErrors["email"],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        LiteTextField(
            value = state.password,
            onValueChange = onPassword,
            label = "Mot de passe",
            error = state.fieldErrors["password"],
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        )
        TextButton(
            onClick = onForgot,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Mot de passe oublié ?")
        }
        state.message?.let { message ->
            LiteMessageBanner(message = message, tone = MessageTone.Error)
        }
        GradientPrimaryButton("Se connecter", state.loading, onLogin)
        Text(
            text = "Connexion sécurisée via votre compte Alumni.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PasswordResetScreen(
    state: PasswordResetUiState,
    actions: PasswordResetActions,
    onBackToLogin: () -> Unit,
) {
    val copy = state.step.copy()
    AuthScaffold(title = copy.title, subtitle = copy.subtitle) {
        if (copy.stepIndex != null) {
            AuthStepLabel(current = copy.stepIndex, total = 3, label = copy.stepLabel)
        }
        when (state.step) {
            PasswordResetStep.Email -> EmailStep(state, actions)
            PasswordResetStep.Otp -> OtpStep(state, actions)
            PasswordResetStep.Reset -> ResetStep(state, actions)
            PasswordResetStep.Done -> DoneStep(state, onBackToLogin)
        }
        if (state.step != PasswordResetStep.Done) {
            state.message?.let { message ->
                LiteMessageBanner(message = message, tone = MessageTone.Info)
            }
            TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Retour à la connexion")
            }
        }
    }
}

@Composable
private fun EmailStep(state: PasswordResetUiState, actions: PasswordResetActions) {
    LiteTextField(
        value = state.email,
        onValueChange = actions.onEmail,
        label = "Email",
        error = state.fieldErrors["email"],
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
    )
    Text(
        text = "Nous vous enverrons un code de vérification si l’adresse existe.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GradientPrimaryButton("Recevoir le code", state.loading, actions.onForgot)
}

@Composable
private fun OtpStep(state: PasswordResetUiState, actions: PasswordResetActions) {
    LiteTextField(
        value = state.email,
        onValueChange = actions.onEmail,
        label = "Email",
        error = state.fieldErrors["email"],
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
    )
    LiteTextField(
        value = state.otp,
        onValueChange = actions.onOtp,
        label = "Code de vérification",
        error = state.fieldErrors["otp"],
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
    )
    Text(
        text = "Vérifiez aussi vos spams si vous ne recevez pas le code.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GradientPrimaryButton("Vérifier le code", state.loading, actions.onVerify)
    TextButton(onClick = actions.onResend, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
        Text("Renvoyer le code")
    }
}

@Composable
private fun ResetStep(state: PasswordResetUiState, actions: PasswordResetActions) {
    LiteTextField(
        value = state.password,
        onValueChange = actions.onPassword,
        label = "Nouveau mot de passe",
        error = state.fieldErrors["password"],
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
    )
    LiteTextField(
        value = state.confirmation,
        onValueChange = actions.onConfirmation,
        label = "Confirmation",
        error = state.fieldErrors["password_confirmation"],
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
    )
    Text(
        text = "Utilisez un mot de passe sécurisé et facile à retenir.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GradientPrimaryButton("Réinitialiser", state.loading, actions.onReset)
}

@Composable
private fun DoneStep(state: PasswordResetUiState, onBackToLogin: () -> Unit) {
    LiteMessageBanner(
        message = state.message ?: "Mot de passe réinitialisé. Vous pouvez vous reconnecter.",
        tone = MessageTone.Success,
    )
    GradientPrimaryButton("Retour à la connexion", false, onBackToLogin)
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

private data class PasswordResetCopy(
    val title: String,
    val subtitle: String,
    val stepIndex: Int?,
    val stepLabel: String,
)

private fun PasswordResetStep.copy(): PasswordResetCopy = when (this) {
    PasswordResetStep.Email -> PasswordResetCopy(
        title = "Réinitialiser le mot de passe",
        subtitle = "Entrez votre email pour recevoir un code de vérification.",
        stepIndex = 1,
        stepLabel = "Email",
    )
    PasswordResetStep.Otp -> PasswordResetCopy(
        title = "Vérification",
        subtitle = "Saisissez le code envoyé à votre adresse email.",
        stepIndex = 2,
        stepLabel = "Code",
    )
    PasswordResetStep.Reset -> PasswordResetCopy(
        title = "Nouveau mot de passe",
        subtitle = "Choisissez un mot de passe sécurisé.",
        stepIndex = 3,
        stepLabel = "Sécurité",
    )
    PasswordResetStep.Done -> PasswordResetCopy(
        title = "Mot de passe modifié",
        subtitle = "Votre accès est prêt. Revenez à la connexion.",
        stepIndex = null,
        stepLabel = "Succès",
    )
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Spacer(Modifier.height(28.dp))
            AuthHeader(title = title, subtitle = subtitle)
            Spacer(Modifier.height(24.dp))
            LiteCard { content() }
            Spacer(Modifier.height(20.dp))
            AuthFooter("Alumni ENSPM Mobile Lite")
        }
    }
}
