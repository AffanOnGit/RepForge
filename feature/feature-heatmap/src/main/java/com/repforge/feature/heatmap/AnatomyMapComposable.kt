package com.repforge.feature.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime

/**
 * Interactive 2D Anatomical Map rendering all 19 sub-muscles.
 * Color scales smoothly from CarbonSlateCard (untrained) to ForgeAmber to KineticLime (peak volume).
 */
@Composable
fun AnatomyMapComposable(
    isFrontView: Boolean,
    heatData: Map<SubMuscle, SubMuscleHeatData>,
    onSubMuscleClick: (SubMuscle) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .pointerInput(isFrontView) {
                detectTapGestures { offset ->
                    val hitMuscle = detectMuscleHit(offset, size.width.toFloat(), size.height.toFloat(), isFrontView)
                    if (hitMuscle != null) {
                        onSubMuscleClick(hitMuscle)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // Draw Head & Neck Silhouette
            drawCircle(
                color = CarbonSlateSurface,
                radius = 28.dp.toPx(),
                center = Offset(cx, 42.dp.toPx())
            )
            drawRoundRect(
                color = CarbonSlateSurface,
                topLeft = Offset(cx - 14.dp.toPx(), 65.dp.toPx()),
                size = Size(28.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            if (isFrontView) {
                drawFrontAnatomy(cx, h, heatData)
            } else {
                drawRearAnatomy(cx, h, heatData)
            }
        }
    }
}

private fun DrawScope.drawFrontAnatomy(
    cx: Float,
    h: Float,
    heatData: Map<SubMuscle, SubMuscleHeatData>
) {
    val topY = 90.dp.toPx()

    // 1. Shoulders: Front Delts & Side Delts
    drawMuscleBox(cx - 75.dp.toPx(), topY + 4.dp.toPx(), 24.dp.toPx(), 28.dp.toPx(), SubMuscle.SIDE_DELTS, heatData)
    drawMuscleBox(cx + 51.dp.toPx(), topY + 4.dp.toPx(), 24.dp.toPx(), 28.dp.toPx(), SubMuscle.SIDE_DELTS, heatData)

    drawMuscleBox(cx - 48.dp.toPx(), topY + 8.dp.toPx(), 20.dp.toPx(), 26.dp.toPx(), SubMuscle.FRONT_DELTS, heatData)
    drawMuscleBox(cx + 28.dp.toPx(), topY + 8.dp.toPx(), 20.dp.toPx(), 26.dp.toPx(), SubMuscle.FRONT_DELTS, heatData)

    // 2. Chest: Upper, Mid, Lower
    drawMuscleBox(cx - 26.dp.toPx(), topY + 8.dp.toPx(), 24.dp.toPx(), 18.dp.toPx(), SubMuscle.UPPER_CHEST, heatData)
    drawMuscleBox(cx + 2.dp.toPx(), topY + 8.dp.toPx(), 24.dp.toPx(), 18.dp.toPx(), SubMuscle.UPPER_CHEST, heatData)

    drawMuscleBox(cx - 26.dp.toPx(), topY + 28.dp.toPx(), 24.dp.toPx(), 20.dp.toPx(), SubMuscle.MID_CHEST, heatData)
    drawMuscleBox(cx + 2.dp.toPx(), topY + 28.dp.toPx(), 24.dp.toPx(), 20.dp.toPx(), SubMuscle.MID_CHEST, heatData)

    drawMuscleBox(cx - 26.dp.toPx(), topY + 50.dp.toPx(), 24.dp.toPx(), 16.dp.toPx(), SubMuscle.LOWER_CHEST, heatData)
    drawMuscleBox(cx + 2.dp.toPx(), topY + 50.dp.toPx(), 24.dp.toPx(), 16.dp.toPx(), SubMuscle.LOWER_CHEST, heatData)

    // 3. Arms: Biceps & Forearms
    drawMuscleBox(cx - 72.dp.toPx(), topY + 36.dp.toPx(), 20.dp.toPx(), 36.dp.toPx(), SubMuscle.BICEPS, heatData)
    drawMuscleBox(cx + 52.dp.toPx(), topY + 36.dp.toPx(), 20.dp.toPx(), 36.dp.toPx(), SubMuscle.BICEPS, heatData)

    drawMuscleBox(cx - 76.dp.toPx(), topY + 76.dp.toPx(), 18.dp.toPx(), 44.dp.toPx(), SubMuscle.FOREARMS, heatData)
    drawMuscleBox(cx + 58.dp.toPx(), topY + 76.dp.toPx(), 18.dp.toPx(), 44.dp.toPx(), SubMuscle.FOREARMS, heatData)

    // 4. Core: Upper Abs, Lower Abs, Obliques
    drawMuscleBox(cx - 18.dp.toPx(), topY + 72.dp.toPx(), 36.dp.toPx(), 24.dp.toPx(), SubMuscle.UPPER_ABS, heatData)
    drawMuscleBox(cx - 18.dp.toPx(), topY + 100.dp.toPx(), 36.dp.toPx(), 28.dp.toPx(), SubMuscle.LOWER_ABS, heatData)

    drawMuscleBox(cx - 38.dp.toPx(), topY + 72.dp.toPx(), 16.dp.toPx(), 48.dp.toPx(), SubMuscle.OBLIQUES, heatData)
    drawMuscleBox(cx + 22.dp.toPx(), topY + 72.dp.toPx(), 16.dp.toPx(), 48.dp.toPx(), SubMuscle.OBLIQUES, heatData)

    // 5. Legs: Quads & Calves
    drawMuscleBox(cx - 32.dp.toPx(), topY + 138.dp.toPx(), 28.dp.toPx(), 80.dp.toPx(), SubMuscle.QUADS, heatData)
    drawMuscleBox(cx + 4.dp.toPx(), topY + 138.dp.toPx(), 28.dp.toPx(), 80.dp.toPx(), SubMuscle.QUADS, heatData)

    drawMuscleBox(cx - 28.dp.toPx(), topY + 230.dp.toPx(), 22.dp.toPx(), 70.dp.toPx(), SubMuscle.CALVES, heatData)
    drawMuscleBox(cx + 6.dp.toPx(), topY + 230.dp.toPx(), 22.dp.toPx(), 70.dp.toPx(), SubMuscle.CALVES, heatData)
}

private fun DrawScope.drawRearAnatomy(
    cx: Float,
    h: Float,
    heatData: Map<SubMuscle, SubMuscleHeatData>
) {
    val topY = 90.dp.toPx()

    // 1. Upper Back / Traps
    drawMuscleBox(cx - 28.dp.toPx(), topY + 4.dp.toPx(), 56.dp.toPx(), 32.dp.toPx(), SubMuscle.UPPER_BACK_TRAPS, heatData)

    // 2. Rear Delts & Triceps
    drawMuscleBox(cx - 68.dp.toPx(), topY + 10.dp.toPx(), 22.dp.toPx(), 24.dp.toPx(), SubMuscle.REAR_DELTS, heatData)
    drawMuscleBox(cx + 46.dp.toPx(), topY + 10.dp.toPx(), 22.dp.toPx(), 24.dp.toPx(), SubMuscle.REAR_DELTS, heatData)

    drawMuscleBox(cx - 72.dp.toPx(), topY + 38.dp.toPx(), 22.dp.toPx(), 36.dp.toPx(), SubMuscle.TRICEPS, heatData)
    drawMuscleBox(cx + 50.dp.toPx(), topY + 38.dp.toPx(), 22.dp.toPx(), 36.dp.toPx(), SubMuscle.TRICEPS, heatData)

    // 3. Lats & Lower Back
    drawMuscleBox(cx - 44.dp.toPx(), topY + 40.dp.toPx(), 24.dp.toPx(), 44.dp.toPx(), SubMuscle.LATS, heatData)
    drawMuscleBox(cx + 20.dp.toPx(), topY + 40.dp.toPx(), 24.dp.toPx(), 44.dp.toPx(), SubMuscle.LATS, heatData)

    drawMuscleBox(cx - 18.dp.toPx(), topY + 76.dp.toPx(), 36.dp.toPx(), 36.dp.toPx(), SubMuscle.LOWER_BACK, heatData)

    // 4. Glutes
    drawMuscleBox(cx - 36.dp.toPx(), topY + 116.dp.toPx(), 34.dp.toPx(), 44.dp.toPx(), SubMuscle.GLUTES, heatData)
    drawMuscleBox(cx + 2.dp.toPx(), topY + 116.dp.toPx(), 34.dp.toPx(), 44.dp.toPx(), SubMuscle.GLUTES, heatData)

    // 5. Hamstrings & Calves
    drawMuscleBox(cx - 32.dp.toPx(), topY + 164.dp.toPx(), 28.dp.toPx(), 62.dp.toPx(), SubMuscle.HAMSTRINGS, heatData)
    drawMuscleBox(cx + 4.dp.toPx(), topY + 164.dp.toPx(), 28.dp.toPx(), 62.dp.toPx(), SubMuscle.HAMSTRINGS, heatData)

    drawMuscleBox(cx - 28.dp.toPx(), topY + 234.dp.toPx(), 22.dp.toPx(), 70.dp.toPx(), SubMuscle.CALVES, heatData)
    drawMuscleBox(cx + 6.dp.toPx(), topY + 234.dp.toPx(), 22.dp.toPx(), 70.dp.toPx(), SubMuscle.CALVES, heatData)
}

private fun DrawScope.drawMuscleBox(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    muscle: SubMuscle,
    heatData: Map<SubMuscle, SubMuscleHeatData>
) {
    val intensity = heatData[muscle]?.heatIntensity ?: 0f
    val color = calculateHeatColor(intensity)

    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Fill
    )

    drawRoundRect(
        color = if (intensity > 0.1f) color.copy(alpha = 0.8f) else CarbonSlateCard,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun calculateHeatColor(intensity: Float): Color {
    return when {
        intensity <= 0.05f -> CarbonSlateCard
        intensity < 0.5f -> {
            val factor = intensity / 0.5f
            lerpColor(CarbonSlateCard, ForgeAmber, factor)
        }
        else -> {
            val factor = (intensity - 0.5f) / 0.5f
            lerpColor(ForgeAmber, KineticLime, factor)
        }
    }
}

private fun lerpColor(c1: Color, c2: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = 1f
    )
}

private fun detectMuscleHit(offset: Offset, w: Float, h: Float, isFrontView: Boolean): SubMuscle? {
    val cx = w / 2f
    val y = offset.y
    val x = offset.x

    return if (isFrontView) {
        when {
            y in 90f..140f && (x in (cx - 75f)..(cx - 45f) || x in (cx + 45f)..(cx + 75f)) -> SubMuscle.SIDE_DELTS
            y in 90f..140f && (x in (cx - 45f)..(cx - 25f) || x in (cx + 25f)..(cx + 45f)) -> SubMuscle.FRONT_DELTS
            y in 90f..120f && x in (cx - 25f)..(cx + 25f) -> SubMuscle.UPPER_CHEST
            y in 120f..145f && x in (cx - 25f)..(cx + 25f) -> SubMuscle.MID_CHEST
            y in 145f..165f && x in (cx - 25f)..(cx + 25f) -> SubMuscle.LOWER_CHEST
            y in 125f..170f && (x in (cx - 75f)..(cx - 50f) || x in (cx + 50f)..(cx + 75f)) -> SubMuscle.BICEPS
            y in 170f..230f && (x in (cx - 78f)..(cx - 50f) || x in (cx + 50f)..(cx + 78f)) -> SubMuscle.FOREARMS
            y in 165f..200f && x in (cx - 20f)..(cx + 20f) -> SubMuscle.UPPER_ABS
            y in 200f..235f && x in (cx - 20f)..(cx + 20f) -> SubMuscle.LOWER_ABS
            y in 165f..235f && (x in (cx - 40f)..(cx - 20f) || x in (cx + 20f)..(cx + 40f)) -> SubMuscle.OBLIQUES
            y in 235f..330f -> SubMuscle.QUADS
            y > 330f -> SubMuscle.CALVES
            else -> null
        }
    } else {
        when {
            y in 90f..135f && x in (cx - 30f)..(cx + 30f) -> SubMuscle.UPPER_BACK_TRAPS
            y in 95f..135f && (x in (cx - 70f)..(cx - 40f) || x in (cx + 40f)..(cx + 70f)) -> SubMuscle.REAR_DELTS
            y in 135f..180f && (x in (cx - 75f)..(cx - 45f) || x in (cx + 45f)..(cx + 75f)) -> SubMuscle.TRICEPS
            y in 135f..185f && (x in (cx - 45f)..(cx - 18f) || x in (cx + 18f)..(cx + 45f)) -> SubMuscle.LATS
            y in 170f..215f && x in (cx - 20f)..(cx + 20f) -> SubMuscle.LOWER_BACK
            y in 215f..265f -> SubMuscle.GLUTES
            y in 265f..335f -> SubMuscle.HAMSTRINGS
            y > 335f -> SubMuscle.CALVES
            else -> null
        }
    }
}
