package com.repforge.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary

/**
 * Sweaty-hands friendly numeric stepper counter:
 * - Huge 48dp+ tap targets for plus and minus buttons
 * - Haptic feedback on each tick
 * - Monospace figures to eliminate visual jitter
 */
@Composable
fun StepperCounter(
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double = 2.5,
    minValue: Double = 0.0,
    maxValue: Double = 500.0,
    unitLabel: String = "kg",
    isInteger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val formattedValue = if (isInteger) {
        "%.0f".format(value)
    } else {
        if (value % 1.0 == 0.0) "%.0f".format(value) else "%.1f".format(value)
    }

    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSlateSurface)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Minus button (min 48dp tap target)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clickable(
                    role = Role.Button,
                    enabled = value > minValue,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val newValue = (value - step).coerceAtLeast(minValue)
                        onValueChange(newValue)
                    }
                )
                .semantics {
                    contentDescription = "Decrease by $step $unitLabel"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = null,
                tint = if (value > minValue) ForgeAmber else CarbonSlateCard,
                modifier = Modifier.size(24.dp)
            )
        }

        // Numeric display with unit
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = 64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedValue,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            if (unitLabel.isNotBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unitLabel,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Plus button (min 48dp tap target)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clickable(
                    role = Role.Button,
                    enabled = value < maxValue,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val newValue = (value + step).coerceAtMost(maxValue)
                        onValueChange(newValue)
                    }
                )
                .semantics {
                    contentDescription = "Increase by $step $unitLabel"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = if (value < maxValue) ForgeAmber else CarbonSlateCard,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
