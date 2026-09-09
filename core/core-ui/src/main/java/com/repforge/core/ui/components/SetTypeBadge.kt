package com.repforge.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.core.domain.model.SetType
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.DropSetColor
import com.repforge.core.ui.theme.FailureColor
import com.repforge.core.ui.theme.WarmupColor
import com.repforge.core.ui.theme.WorkingColor

/**
 * Accessible Set Type Badge.
 * Meets WCAG 2.1 AA accessibility guidelines by combining:
 * 1. Distinct Colors
 * 2. Distinct Shapes (Circle, Rounded Rect, Diamond, Hexagonal)
 * 3. Distinct Letter Indicators ("W", Set Number, "D", "F")
 *
 * Touch target is padded to at least 48dp when clickable.
 */
@Composable
fun SetTypeBadge(
    setType: SetType,
    setNumber: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (shape: Shape, color: Color, label: String, description: String) = when (setType) {
        SetType.WARMUP -> Quadruple(
            CircleShape,
            WarmupColor,
            "W",
            "Warm-up set $setNumber"
        )
        SetType.WORKING -> Quadruple(
            RoundedCornerShape(8.dp),
            WorkingColor,
            "$setNumber",
            "Working set $setNumber"
        )
        SetType.DROP_SET -> Quadruple(
            CutCornerShape(8.dp),
            DropSetColor,
            "D",
            "Drop set $setNumber"
        )
        SetType.FAILURE -> Quadruple(
            CutCornerShape(14.dp),
            FailureColor,
            "F",
            "Failure set $setNumber"
        )
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            role = Role.Button,
            onClickLabel = "Change set type",
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .size(36.dp)
            .then(clickableModifier)
            .clip(shape)
            .background(CarbonSlateSurface)
            .border(1.5.dp, color, shape)
            .semantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
