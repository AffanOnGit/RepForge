package com.repforge.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateLight
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary

enum class ForgeButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    GHOST,
    KINETIC
}

/**
 * Standard button for RepForge following the sweaty-hands UX philosophy:
 * - Minimum 48dp touch target (default 52dp height)
 * - High contrast visual styling
 * - Distinct loading state
 */
@Composable
fun ForgeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ForgeButtonVariant = ForgeButtonVariant.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isFullWidth: Boolean = false
) {
    val buttonModifier = modifier
        .defaultMinSize(minHeight = 52.dp, minWidth = 48.dp)
        .then(if (isFullWidth) Modifier.fillMaxWidth() else Modifier)

    val shape = RoundedCornerShape(12.dp)

    val content: @Composable () -> Unit = {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = if (variant == ForgeButtonVariant.PRIMARY) Color.Black else ForgeAmber,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }

    when (variant) {
        ForgeButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForgeAmber,
                    contentColor = Color.Black,
                    disabledContainerColor = CarbonSlateLight,
                    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                )
            ) { content() }
        }

        ForgeButtonVariant.KINETIC -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KineticLime,
                    contentColor = Color.Black,
                    disabledContainerColor = CarbonSlateLight,
                    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                )
            ) { content() }
        }

        ForgeButtonVariant.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CarbonSlateCard,
                    contentColor = TextPrimary,
                    disabledContainerColor = CarbonSlateLight,
                    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                )
            ) { content() }
        }

        ForgeButtonVariant.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                border = BorderStroke(
                    1.5.dp,
                    if (enabled) ForgeAmber else CarbonSlateCard
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ForgeAmber,
                    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                )
            ) { content() }
        }

        ForgeButtonVariant.GHOST -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ForgeAmber,
                    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                )
            ) { content() }
        }
    }
}
