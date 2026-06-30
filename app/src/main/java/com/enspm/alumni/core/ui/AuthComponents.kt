package com.enspm.alumni.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

val EnspmBlue = Color(0xFF2563EB)
val EnspmIndigo = Color(0xFF4F46E5)
val EnspmBorder = Color(0xFFE2E8F0)
val EnspmMuted = Color(0xFF64748B)

@Composable
fun LiteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EnspmBlue,
            unfocusedBorderColor = EnspmBorder,
        ),
    )
}

@Composable
fun GradientPrimaryButton(text: String, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Brush.horizontalGradient(listOf(EnspmBlue, EnspmIndigo)), RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.7f)),
        border = BorderStroke(0.dp, Color.Transparent),
    ) {
        Text(if (loading) "Chargement…" else text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LiteCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, EnspmBorder, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) { content() }
}
