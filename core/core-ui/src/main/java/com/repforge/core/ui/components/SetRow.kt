package com.repforge.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.core.domain.model.SetType
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextGhost
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary

/**
 * Sweaty-hands optimized SetRow composable:
 * - Direct visual feedback with ghost text from previous session
 * - Minimum 48dp checkmark button target
 * - Haptic pulse on set checkoff
 * - SetType cycling badge
 */
@Composable
fun SetRow(
    setNumber: Int,
    setType: SetType,
    weightDisplay: String,
    repsDisplay: String,
    isCompleted: Boolean,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onSetTypeClick: () -> Unit,
    onToggleComplete: () -> Unit,
    ghostText: String? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val checkBgColor by animateColorAsState(
        targetValue = if (isCompleted) KineticLime else CarbonSlateSurface,
        label = "checkBgColor"
    )
    val checkBorderColor by animateColorAsState(
        targetValue = if (isCompleted) KineticLime else CarbonSlateCard,
        label = "checkBorderColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Set type & number badge
        SetTypeBadge(
            setType = setType,
            setNumber = setNumber,
            onClick = onSetTypeClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 2. Ghost text column (previous performance)
        Column(
            modifier = Modifier.weight(1.3f),
            horizontalAlignment = Alignment.Start
        ) {
            if (ghostText != null && ghostText.isNotBlank()) {
                Text(
                    text = ghostText,
                    color = TextGhost,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "—",
                    color = TextGhost.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 3. Weight display / input
        Box(
            modifier = Modifier
                .weight(1.1f)
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CarbonSlateSurface)
                .border(1.dp, CarbonSlateCard, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = weightDisplay.ifBlank { "0" },
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 4. Reps display / input
        Box(
            modifier = Modifier
                .weight(0.9f)
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CarbonSlateSurface)
                .border(1.dp, CarbonSlateCard, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = repsDisplay.ifBlank { "0" },
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 5. Completion Checkmark (Huge 48dp tap target)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(checkBgColor)
                .border(1.5.dp, checkBorderColor, RoundedCornerShape(12.dp))
                .clickable(
                    role = Role.Checkbox,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleComplete()
                    }
                )
                .semantics {
                    contentDescription = if (isCompleted) {
                        "Set $setNumber completed"
                    } else {
                        "Mark set $setNumber as complete"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isCompleted) Color.Black else TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
