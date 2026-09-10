package com.repforge.feature.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import kotlin.math.sqrt

// ─────────────────────────────────────────────────────────────────────────────
//  Coordinate system: normalised viewport 100×200 units → scaled at runtime
// ─────────────────────────────────────────────────────────────────────────────

/** Scale a normalised x from [0,100] to canvas px */
private fun Float.sx(w: Float) = this * w / 100f
/** Scale a normalised y from [0,200] to canvas px */
private fun Float.sy(h: Float) = this * h / 200f

// ─────────────────────────────────────────────────────────────────────────────
//  Heat colour interpolation
// ─────────────────────────────────────────────────────────────────────────────

/** Lerp a single float channel between two colours. */
private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

/**
 * Maps intensity [0,1] → colour:
 *   0.0  → CarbonSlateCard (untrained)
 *   0.5  → ForgeAmber
 *   1.0  → KineticLime (peak)
 */
fun calculateHeatColor(intensity: Float): Color {
    val cold = CarbonSlateCard
    val mid  = ForgeAmber
    val hot  = KineticLime

    return when {
        intensity <= 0f   -> cold
        intensity >= 1f   -> hot
        intensity < 0.5f  -> {
            val t = intensity * 2f
            Color(
                red   = lerp(cold.red,   mid.red,   t),
                green = lerp(cold.green, mid.green, t),
                blue  = lerp(cold.blue,  mid.blue,  t),
                alpha = lerp(0.55f,       0.95f,    t)
            )
        }
        else -> {
            val t = (intensity - 0.5f) * 2f
            Color(
                red   = lerp(mid.red,   hot.red,   t),
                green = lerp(mid.green, hot.green, t),
                blue  = lerp(mid.blue,  hot.blue,  t),
                alpha = 1f
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Muscle hit detection via bounding-rect lookup
// ─────────────────────────────────────────────────────────────────────────────

/** Normalised bounding rectangles (x0,y0,x1,y1) for hit detection.  */
private val frontHitRects: Map<SubMuscle, Rect> = mapOf(
    // Chest
    SubMuscle.UPPER_CHEST     to Rect(Offset(36f.sx(1f), 19f.sy(1f)), Offset(64f.sx(1f), 28f.sy(1f))),
    SubMuscle.MID_CHEST       to Rect(Offset(36f.sx(1f), 28f.sy(1f)), Offset(64f.sx(1f), 35f.sy(1f))),
    SubMuscle.LOWER_CHEST     to Rect(Offset(36f.sx(1f), 35f.sy(1f)), Offset(64f.sx(1f), 41f.sy(1f))),
    // Shoulders
    SubMuscle.FRONT_DELTS     to Rect(Offset(28f.sx(1f), 19f.sy(1f)), Offset(38f.sx(1f), 32f.sy(1f))),
    SubMuscle.SIDE_DELTS      to Rect(Offset(22f.sx(1f), 19f.sy(1f)), Offset(30f.sx(1f), 32f.sy(1f))),
    // Arms (front)
    SubMuscle.BICEPS          to Rect(Offset(22f.sx(1f), 33f.sy(1f)), Offset(35f.sx(1f), 53f.sy(1f))),
    SubMuscle.FOREARMS        to Rect(Offset(20f.sx(1f), 53f.sy(1f)), Offset(33f.sx(1f), 70f.sy(1f))),
    // Core
    SubMuscle.UPPER_ABS       to Rect(Offset(39f.sx(1f), 41f.sy(1f)), Offset(61f.sx(1f), 53f.sy(1f))),
    SubMuscle.LOWER_ABS       to Rect(Offset(39f.sx(1f), 53f.sy(1f)), Offset(61f.sx(1f), 63f.sy(1f))),
    SubMuscle.OBLIQUES        to Rect(Offset(30f.sx(1f), 42f.sy(1f)), Offset(40f.sx(1f), 62f.sy(1f))),
    // Legs (front)
    SubMuscle.QUADS           to Rect(Offset(33f.sx(1f), 64f.sy(1f)), Offset(67f.sx(1f), 100f.sy(1f))),
    SubMuscle.CALVES          to Rect(Offset(35f.sx(1f), 100f.sy(1f)), Offset(65f.sx(1f), 130f.sy(1f)))
)

private val rearHitRects: Map<SubMuscle, Rect> = mapOf(
    SubMuscle.UPPER_BACK_TRAPS to Rect(Offset(35f.sx(1f), 8f.sy(1f)), Offset(65f.sx(1f), 26f.sy(1f))),
    SubMuscle.REAR_DELTS      to Rect(Offset(24f.sx(1f), 19f.sy(1f)), Offset(37f.sx(1f), 31f.sy(1f))),
    SubMuscle.TRICEPS         to Rect(Offset(21f.sx(1f), 32f.sy(1f)), Offset(34f.sx(1f), 52f.sy(1f))),
    SubMuscle.LATS            to Rect(Offset(30f.sx(1f), 26f.sy(1f)), Offset(70f.sx(1f), 56f.sy(1f))),
    SubMuscle.LOWER_BACK      to Rect(Offset(36f.sx(1f), 55f.sy(1f)), Offset(64f.sx(1f), 67f.sy(1f))),
    SubMuscle.GLUTES          to Rect(Offset(33f.sx(1f), 67f.sy(1f)), Offset(67f.sx(1f), 85f.sy(1f))),
    SubMuscle.HAMSTRINGS      to Rect(Offset(33f.sx(1f), 85f.sy(1f)), Offset(67f.sx(1f), 118f.sy(1f))),
    SubMuscle.CALVES          to Rect(Offset(34f.sx(1f), 118f.sy(1f)), Offset(66f.sx(1f), 145f.sy(1f)))
)

/** Scale the hit rects to actual canvas size. */
private fun scaledHitRects(
    rects: Map<SubMuscle, Rect>,
    w: Float,
    h: Float
): Map<SubMuscle, Rect> = rects.mapValues { (_, r) ->
    Rect(
        left   = r.left   * w,
        top    = r.top    * h,
        right  = r.right  * w,
        bottom = r.bottom * h
    )
}

fun detectMuscleHit(offset: Offset, w: Float, h: Float, isFront: Boolean): SubMuscle? {
    val rects = scaledHitRects(if (isFront) frontHitRects else rearHitRects, w, h)
    return rects.entries.firstOrNull { (_, rect) -> rect.contains(offset) }?.key
}

// ─────────────────────────────────────────────────────────────────────────────
//  Composable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Interactive 2D Anatomical Map rendered with Compose Canvas Bezier paths.
 *
 * Each muscle region is a properly shaped [Path] filled with a heat colour
 * derived from [heatData]. Tap detection uses scaled bounding rectangles.
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
                    val hit = detectMuscleHit(offset, size.width.toFloat(), size.height.toFloat(), isFrontView)
                    if (hit != null) onSubMuscleClick(hit)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (isFrontView) drawFrontBody(w, h, heatData)
            else             drawRearBody(w, h, heatData)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Front body drawing
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawFrontBody(w: Float, h: Float, data: Map<SubMuscle, SubMuscleHeatData>) {
    val strokeColor = CarbonSlateSurface
    val stroke = Stroke(width = 1.5f)

    fun heat(m: SubMuscle) = calculateHeatColor(data[m]?.heatIntensity ?: 0f)

    // ── Outer silhouette (body outline) ─────────────────────────────────────
    val silhouette = Path().apply {
        // Head
        moveTo(50f.sx(w), 0f.sy(h))
        cubicTo(44f.sx(w), 0f.sy(h), 40f.sx(w), 4f.sy(h), 40f.sx(w), 9f.sy(h))
        cubicTo(40f.sx(w), 14f.sy(h), 44f.sx(w), 17f.sy(h), 50f.sx(w), 17f.sy(h))
        cubicTo(56f.sx(w), 17f.sy(h), 60f.sx(w), 14f.sy(h), 60f.sx(w), 9f.sy(h))
        cubicTo(60f.sx(w), 4f.sy(h), 56f.sx(w), 0f.sy(h), 50f.sx(w), 0f.sy(h))
        close()
        // Neck
        moveTo(45f.sx(w), 16f.sy(h))
        lineTo(45f.sx(w), 20f.sy(h))
        lineTo(55f.sx(w), 20f.sy(h))
        lineTo(55f.sx(w), 16f.sy(h))
        close()
        // Torso outline (shoulders → waist → hips)
        moveTo(45f.sx(w), 19f.sy(h))
        cubicTo(38f.sx(w), 19f.sy(h), 20f.sx(w), 18f.sy(h), 18f.sx(w), 26f.sy(h)) // L shoulder
        cubicTo(16f.sx(w), 32f.sy(h), 18f.sx(w), 42f.sy(h), 18f.sx(w), 48f.sy(h)) // L upper arm
        cubicTo(18f.sx(w), 56f.sy(h), 18f.sx(w), 66f.sy(h), 20f.sx(w), 70f.sy(h)) // L forearm
        lineTo(24f.sx(w), 72f.sy(h))                                                 // L hand top
        lineTo(26f.sx(w), 70f.sy(h))
        lineTo(28f.sx(w), 64f.sy(h))
        cubicTo(30f.sx(w), 58f.sy(h), 30f.sx(w), 50f.sy(h), 32f.sx(w), 48f.sy(h)) // L elbow inner
        lineTo(34f.sx(w), 63f.sy(h))                                                 // waist-hip
        cubicTo(33f.sx(w), 67f.sy(h), 32f.sx(w), 70f.sy(h), 33f.sx(w), 75f.sy(h))
        lineTo(33f.sx(w), 131f.sy(h))                                                // L leg outer
        cubicTo(33f.sx(w), 137f.sy(h), 34f.sx(w), 140f.sy(h), 35f.sx(w), 141f.sy(h))
        lineTo(40f.sx(w), 141f.sy(h))                                                // L foot
        lineTo(40f.sx(w), 131f.sy(h))
        cubicTo(42f.sx(w), 119f.sy(h), 44f.sx(w), 110f.sy(h), 45f.sx(w), 106f.sy(h)) // L calf
        cubicTo(46f.sx(w), 102f.sy(h), 46f.sx(w), 96f.sy(h), 46f.sx(w), 90f.sy(h)) // L inner leg
        lineTo(50f.sx(w), 88f.sy(h))                                                  // crotch
        lineTo(54f.sx(w), 90f.sy(h))
        cubicTo(54f.sx(w), 96f.sy(h), 54f.sx(w), 102f.sy(h), 55f.sx(w), 106f.sy(h))
        cubicTo(56f.sx(w), 110f.sy(h), 58f.sx(w), 119f.sy(h), 60f.sx(w), 131f.sy(h))
        lineTo(60f.sx(w), 141f.sy(h))
        lineTo(65f.sx(w), 141f.sy(h))
        lineTo(65f.sx(w), 131f.sy(h))
        cubicTo(67f.sx(w), 140f.sy(h), 67f.sx(w), 137f.sy(h), 67f.sx(w), 131f.sy(h))
        lineTo(67f.sx(w), 75f.sy(h))
        cubicTo(68f.sx(w), 70f.sy(h), 67f.sx(w), 67f.sy(h), 66f.sx(w), 63f.sy(h))
        lineTo(68f.sx(w), 48f.sy(h))
        cubicTo(70f.sx(w), 50f.sy(h), 70f.sx(w), 58f.sy(h), 72f.sx(w), 64f.sy(h))
        lineTo(74f.sx(w), 70f.sy(h))
        lineTo(76f.sx(w), 72f.sy(h))
        lineTo(80f.sx(w), 70f.sy(h))
        cubicTo(82f.sx(w), 66f.sy(h), 82f.sx(w), 56f.sy(h), 82f.sx(w), 48f.sy(h))
        cubicTo(82f.sx(w), 42f.sy(h), 84f.sx(w), 32f.sy(h), 82f.sx(w), 26f.sy(h))
        cubicTo(80f.sx(w), 18f.sy(h), 62f.sx(w), 19f.sy(h), 55f.sx(w), 19f.sy(h))
        close()
    }
    drawPath(silhouette, color = CarbonSlateCard.copy(alpha = 0.25f), style = Fill)
    drawPath(silhouette, color = strokeColor, style = stroke)

    // ── Chest regions ────────────────────────────────────────────────────────
    drawMuscleShape(path = frontPecLeft(w, h), color = heat(SubMuscle.MID_CHEST))
    drawMuscleShape(path = frontPecRight(w, h), color = heat(SubMuscle.MID_CHEST))
    drawMuscleShape(path = frontUpperChestLeft(w, h), color = heat(SubMuscle.UPPER_CHEST))
    drawMuscleShape(path = frontUpperChestRight(w, h), color = heat(SubMuscle.UPPER_CHEST))
    drawMuscleShape(path = frontLowerChestLeft(w, h), color = heat(SubMuscle.LOWER_CHEST))
    drawMuscleShape(path = frontLowerChestRight(w, h), color = heat(SubMuscle.LOWER_CHEST))

    // ── Shoulders ────────────────────────────────────────────────────────────
    drawMuscleShape(path = frontDeltLeft(w, h), color = heat(SubMuscle.FRONT_DELTS))
    drawMuscleShape(path = frontDeltRight(w, h), color = heat(SubMuscle.FRONT_DELTS))
    drawMuscleShape(path = frontSideDeltLeft(w, h), color = heat(SubMuscle.SIDE_DELTS))
    drawMuscleShape(path = frontSideDeltRight(w, h), color = heat(SubMuscle.SIDE_DELTS))

    // ── Arms ─────────────────────────────────────────────────────────────────
    drawMuscleShape(path = frontBicepLeft(w, h), color = heat(SubMuscle.BICEPS))
    drawMuscleShape(path = frontBicepRight(w, h), color = heat(SubMuscle.BICEPS))
    drawMuscleShape(path = frontForearmLeft(w, h), color = heat(SubMuscle.FOREARMS))
    drawMuscleShape(path = frontForearmRight(w, h), color = heat(SubMuscle.FOREARMS))

    // ── Abs & Core ───────────────────────────────────────────────────────────
    drawMuscleShape(path = frontUpperAbs(w, h), color = heat(SubMuscle.UPPER_ABS))
    drawMuscleShape(path = frontLowerAbs(w, h), color = heat(SubMuscle.LOWER_ABS))
    drawMuscleShape(path = frontObliquesLeft(w, h), color = heat(SubMuscle.OBLIQUES))
    drawMuscleShape(path = frontObliquesRight(w, h), color = heat(SubMuscle.OBLIQUES))

    // ── Legs ─────────────────────────────────────────────────────────────────
    drawMuscleShape(path = frontQuadLeft(w, h), color = heat(SubMuscle.QUADS))
    drawMuscleShape(path = frontQuadRight(w, h), color = heat(SubMuscle.QUADS))
    drawMuscleShape(path = frontCalfLeft(w, h), color = heat(SubMuscle.CALVES))
    drawMuscleShape(path = frontCalfRight(w, h), color = heat(SubMuscle.CALVES))
}

// ─────────────────────────────────────────────────────────────────────────────
//  Rear body drawing
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawRearBody(w: Float, h: Float, data: Map<SubMuscle, SubMuscleHeatData>) {
    val strokeColor = CarbonSlateSurface
    val stroke = Stroke(width = 1.5f)

    fun heat(m: SubMuscle) = calculateHeatColor(data[m]?.heatIntensity ?: 0f)

    // Silhouette (same outline reused)
    val silhouette = rearSilhouette(w, h)
    drawPath(silhouette, color = CarbonSlateCard.copy(alpha = 0.25f), style = Fill)
    drawPath(silhouette, color = strokeColor, style = stroke)

    // Neck & Traps
    drawMuscleShape(path = rearNeck(w, h), color = heat(SubMuscle.UPPER_BACK_TRAPS))
    drawMuscleShape(path = rearTrapsLeft(w, h), color = heat(SubMuscle.UPPER_BACK_TRAPS))
    drawMuscleShape(path = rearTrapsRight(w, h), color = heat(SubMuscle.UPPER_BACK_TRAPS))
    // Rear delts
    drawMuscleShape(path = rearDeltLeft(w, h), color = heat(SubMuscle.REAR_DELTS))
    drawMuscleShape(path = rearDeltRight(w, h), color = heat(SubMuscle.REAR_DELTS))
    // Triceps
    drawMuscleShape(path = rearTricepLeft(w, h), color = heat(SubMuscle.TRICEPS))
    drawMuscleShape(path = rearTricepRight(w, h), color = heat(SubMuscle.TRICEPS))
    // Lats
    drawMuscleShape(path = rearLatsLeft(w, h), color = heat(SubMuscle.LATS))
    drawMuscleShape(path = rearLatsRight(w, h), color = heat(SubMuscle.LATS))
    // Lower back
    drawMuscleShape(path = rearLowerBack(w, h), color = heat(SubMuscle.LOWER_BACK))
    // Glutes
    drawMuscleShape(path = rearGluteLeft(w, h), color = heat(SubMuscle.GLUTES))
    drawMuscleShape(path = rearGluteRight(w, h), color = heat(SubMuscle.GLUTES))
    // Hamstrings
    drawMuscleShape(path = rearHamstringLeft(w, h), color = heat(SubMuscle.HAMSTRINGS))
    drawMuscleShape(path = rearHamstringRight(w, h), color = heat(SubMuscle.HAMSTRINGS))
    // Calves
    drawMuscleShape(path = rearCalfLeft(w, h), color = heat(SubMuscle.CALVES))
    drawMuscleShape(path = rearCalfRight(w, h), color = heat(SubMuscle.CALVES))
}

/** Draw a muscle path with fill and an outline stroke for definition. */
private fun DrawScope.drawMuscleShape(path: Path, color: Color) {
    drawPath(path, color = color.copy(alpha = (color.alpha * 0.85f).coerceAtLeast(0.3f)), style = Fill)
    drawPath(path, color = color.copy(alpha = 0.6f), style = Stroke(width = 1.2f))
}

// ─────────────────────────────────────────────────────────────────────────────
//  Front muscle paths (left = viewer's left = body's right)
// ─────────────────────────────────────────────────────────────────────────────

private fun frontUpperChestLeft(w: Float, h: Float) = Path().apply {
    moveTo(38f.sx(w), 20f.sy(h))
    cubicTo(38f.sx(w), 20f.sy(h), 36f.sx(w), 22f.sy(h), 36f.sx(w), 27f.sy(h))
    cubicTo(36f.sx(w), 27f.sy(h), 41f.sx(w), 25f.sy(h), 49f.sx(w), 24f.sy(h))
    cubicTo(49f.sx(w), 22f.sy(h), 48f.sx(w), 20f.sy(h), 46f.sx(w), 20f.sy(h))
    close()
}

private fun frontUpperChestRight(w: Float, h: Float) = Path().apply {
    moveTo(62f.sx(w), 20f.sy(h))
    cubicTo(62f.sx(w), 20f.sy(h), 64f.sx(w), 22f.sy(h), 64f.sx(w), 27f.sy(h))
    cubicTo(64f.sx(w), 27f.sy(h), 59f.sx(w), 25f.sy(h), 51f.sx(w), 24f.sy(h))
    cubicTo(51f.sx(w), 22f.sy(h), 52f.sx(w), 20f.sy(h), 54f.sx(w), 20f.sy(h))
    close()
}

private fun frontPecLeft(w: Float, h: Float) = Path().apply {
    moveTo(36f.sx(w), 27f.sy(h))
    cubicTo(36f.sx(w), 27f.sy(h), 35f.sx(w), 33f.sy(h), 36f.sx(w), 37f.sy(h))
    cubicTo(38f.sx(w), 39f.sy(h), 43f.sx(w), 39f.sy(h), 49f.sx(w), 37f.sy(h))
    cubicTo(49f.sx(w), 33f.sy(h), 49f.sx(w), 27f.sy(h), 49f.sx(w), 24f.sy(h))
    cubicTo(43f.sx(w), 25f.sy(h), 38f.sx(w), 26f.sy(h), 36f.sx(w), 27f.sy(h))
    close()
}

private fun frontPecRight(w: Float, h: Float) = Path().apply {
    moveTo(64f.sx(w), 27f.sy(h))
    cubicTo(64f.sx(w), 27f.sy(h), 65f.sx(w), 33f.sy(h), 64f.sx(w), 37f.sy(h))
    cubicTo(62f.sx(w), 39f.sy(h), 57f.sx(w), 39f.sy(h), 51f.sx(w), 37f.sy(h))
    cubicTo(51f.sx(w), 33f.sy(h), 51f.sx(w), 27f.sy(h), 51f.sx(w), 24f.sy(h))
    cubicTo(57f.sx(w), 25f.sy(h), 62f.sx(w), 26f.sy(h), 64f.sx(w), 27f.sy(h))
    close()
}

private fun frontLowerChestLeft(w: Float, h: Float) = Path().apply {
    moveTo(36f.sx(w), 37f.sy(h))
    cubicTo(36f.sx(w), 37f.sy(h), 36f.sx(w), 40f.sy(h), 38f.sx(w), 42f.sy(h))
    cubicTo(40f.sx(w), 43f.sy(h), 45f.sx(w), 42f.sy(h), 49f.sx(w), 40f.sy(h))
    lineTo(49f.sx(w), 37f.sy(h))
    cubicTo(43f.sx(w), 39f.sy(h), 38f.sx(w), 39f.sy(h), 36f.sx(w), 37f.sy(h))
    close()
}

private fun frontLowerChestRight(w: Float, h: Float) = Path().apply {
    moveTo(64f.sx(w), 37f.sy(h))
    cubicTo(64f.sx(w), 37f.sy(h), 64f.sx(w), 40f.sy(h), 62f.sx(w), 42f.sy(h))
    cubicTo(60f.sx(w), 43f.sy(h), 55f.sx(w), 42f.sy(h), 51f.sx(w), 40f.sy(h))
    lineTo(51f.sx(w), 37f.sy(h))
    cubicTo(57f.sx(w), 39f.sy(h), 62f.sx(w), 39f.sy(h), 64f.sx(w), 37f.sy(h))
    close()
}

private fun frontDeltLeft(w: Float, h: Float) = Path().apply {
    moveTo(36f.sx(w), 20f.sy(h))
    cubicTo(33f.sx(w), 19f.sy(h), 28f.sx(w), 20f.sy(h), 26f.sx(w), 24f.sy(h))
    cubicTo(24f.sx(w), 28f.sy(h), 25f.sx(w), 31f.sy(h), 28f.sx(w), 33f.sy(h))
    cubicTo(32f.sx(w), 33f.sy(h), 36f.sx(w), 33f.sy(h), 36f.sx(w), 27f.sy(h))
    close()
}

private fun frontDeltRight(w: Float, h: Float) = Path().apply {
    moveTo(64f.sx(w), 20f.sy(h))
    cubicTo(67f.sx(w), 19f.sy(h), 72f.sx(w), 20f.sy(h), 74f.sx(w), 24f.sy(h))
    cubicTo(76f.sx(w), 28f.sy(h), 75f.sx(w), 31f.sy(h), 72f.sx(w), 33f.sy(h))
    cubicTo(68f.sx(w), 33f.sy(h), 64f.sx(w), 33f.sy(h), 64f.sx(w), 27f.sy(h))
    close()
}

private fun frontSideDeltLeft(w: Float, h: Float) = Path().apply {
    moveTo(26f.sx(w), 24f.sy(h))
    cubicTo(22f.sx(w), 24f.sy(h), 20f.sx(w), 28f.sy(h), 20f.sx(w), 33f.sy(h))
    cubicTo(22f.sx(w), 35f.sy(h), 25f.sx(w), 35f.sy(h), 28f.sx(w), 33f.sy(h))
    cubicTo(25f.sx(w), 31f.sy(h), 24f.sx(w), 28f.sy(h), 26f.sx(w), 24f.sy(h))
    close()
}

private fun frontSideDeltRight(w: Float, h: Float) = Path().apply {
    moveTo(74f.sx(w), 24f.sy(h))
    cubicTo(78f.sx(w), 24f.sy(h), 80f.sx(w), 28f.sy(h), 80f.sx(w), 33f.sy(h))
    cubicTo(78f.sx(w), 35f.sy(h), 75f.sx(w), 35f.sy(h), 72f.sx(w), 33f.sy(h))
    cubicTo(75f.sx(w), 31f.sy(h), 76f.sx(w), 28f.sy(h), 74f.sx(w), 24f.sy(h))
    close()
}

private fun frontBicepLeft(w: Float, h: Float) = Path().apply {
    moveTo(20f.sx(w), 33f.sy(h))
    cubicTo(18f.sx(w), 37f.sy(h), 18f.sx(w), 44f.sy(h), 20f.sx(w), 50f.sy(h))
    cubicTo(22f.sx(w), 52f.sy(h), 26f.sx(w), 53f.sy(h), 29f.sx(w), 50f.sy(h))
    cubicTo(30f.sx(w), 46f.sy(h), 30f.sx(w), 40f.sy(h), 28f.sx(w), 34f.sy(h))
    cubicTo(25f.sx(w), 33f.sy(h), 22f.sx(w), 33f.sy(h), 20f.sx(w), 33f.sy(h))
    close()
}

private fun frontBicepRight(w: Float, h: Float) = Path().apply {
    moveTo(80f.sx(w), 33f.sy(h))
    cubicTo(82f.sx(w), 37f.sy(h), 82f.sx(w), 44f.sy(h), 80f.sx(w), 50f.sy(h))
    cubicTo(78f.sx(w), 52f.sy(h), 74f.sx(w), 53f.sy(h), 71f.sx(w), 50f.sy(h))
    cubicTo(70f.sx(w), 46f.sy(h), 70f.sx(w), 40f.sy(h), 72f.sx(w), 34f.sy(h))
    cubicTo(75f.sx(w), 33f.sy(h), 78f.sx(w), 33f.sy(h), 80f.sx(w), 33f.sy(h))
    close()
}

private fun frontForearmLeft(w: Float, h: Float) = Path().apply {
    moveTo(20f.sx(w), 50f.sy(h))
    cubicTo(18f.sx(w), 55f.sy(h), 18f.sx(w), 62f.sy(h), 20f.sx(w), 68f.sy(h))
    lineTo(26f.sx(w), 68f.sy(h))
    cubicTo(28f.sx(w), 62f.sy(h), 29f.sx(w), 56f.sy(h), 29f.sx(w), 50f.sy(h))
    cubicTo(26f.sx(w), 53f.sy(h), 22f.sx(w), 52f.sy(h), 20f.sx(w), 50f.sy(h))
    close()
}

private fun frontForearmRight(w: Float, h: Float) = Path().apply {
    moveTo(80f.sx(w), 50f.sy(h))
    cubicTo(82f.sx(w), 55f.sy(h), 82f.sx(w), 62f.sy(h), 80f.sx(w), 68f.sy(h))
    lineTo(74f.sx(w), 68f.sy(h))
    cubicTo(72f.sx(w), 62f.sy(h), 71f.sx(w), 56f.sy(h), 71f.sx(w), 50f.sy(h))
    cubicTo(74f.sx(w), 53f.sy(h), 78f.sx(w), 52f.sy(h), 80f.sx(w), 50f.sy(h))
    close()
}

private fun frontUpperAbs(w: Float, h: Float) = Path().apply {
    moveTo(38f.sx(w), 42f.sy(h))
    cubicTo(37f.sx(w), 43f.sy(h), 36f.sx(w), 46f.sy(h), 38f.sx(w), 52f.sy(h))
    lineTo(62f.sx(w), 52f.sy(h))
    cubicTo(64f.sx(w), 46f.sy(h), 63f.sx(w), 43f.sy(h), 62f.sx(w), 42f.sy(h))
    // Segmentation lines for rectus abdominis effect
    lineTo(38f.sx(w), 42f.sy(h))
    close()
}

private fun frontLowerAbs(w: Float, h: Float) = Path().apply {
    moveTo(38f.sx(w), 52f.sy(h))
    cubicTo(37f.sx(w), 55f.sy(h), 37f.sx(w), 60f.sy(h), 40f.sx(w), 63f.sy(h))
    lineTo(60f.sx(w), 63f.sy(h))
    cubicTo(63f.sx(w), 60f.sy(h), 63f.sx(w), 55f.sy(h), 62f.sx(w), 52f.sy(h))
    close()
}

private fun frontObliquesLeft(w: Float, h: Float) = Path().apply {
    moveTo(36f.sx(w), 37f.sy(h))
    cubicTo(34f.sx(w), 42f.sy(h), 33f.sx(w), 50f.sy(h), 34f.sx(w), 60f.sy(h))
    cubicTo(35f.sx(w), 62f.sy(h), 37f.sx(w), 63f.sy(h), 38f.sx(w), 62f.sy(h))
    cubicTo(38f.sx(w), 55f.sy(h), 38f.sx(w), 48f.sy(h), 38f.sx(w), 42f.sy(h))
    close()
}

private fun frontObliquesRight(w: Float, h: Float) = Path().apply {
    moveTo(64f.sx(w), 37f.sy(h))
    cubicTo(66f.sx(w), 42f.sy(h), 67f.sx(w), 50f.sy(h), 66f.sx(w), 60f.sy(h))
    cubicTo(65f.sx(w), 62f.sy(h), 63f.sx(w), 63f.sy(h), 62f.sx(w), 62f.sy(h))
    cubicTo(62f.sx(w), 55f.sy(h), 62f.sx(w), 48f.sy(h), 62f.sx(w), 42f.sy(h))
    close()
}

private fun frontQuadLeft(w: Float, h: Float) = Path().apply {
    moveTo(34f.sx(w), 63f.sy(h))
    cubicTo(32f.sx(w), 70f.sy(h), 31f.sx(w), 80f.sy(h), 33f.sx(w), 100f.sy(h))
    lineTo(46f.sx(w), 100f.sy(h))
    cubicTo(46f.sx(w), 90f.sy(h), 46f.sx(w), 78f.sy(h), 44f.sx(w), 67f.sy(h))
    cubicTo(41f.sx(w), 63f.sy(h), 37f.sx(w), 63f.sy(h), 34f.sx(w), 63f.sy(h))
    close()
}

private fun frontQuadRight(w: Float, h: Float) = Path().apply {
    moveTo(66f.sx(w), 63f.sy(h))
    cubicTo(68f.sx(w), 70f.sy(h), 69f.sx(w), 80f.sy(h), 67f.sx(w), 100f.sy(h))
    lineTo(54f.sx(w), 100f.sy(h))
    cubicTo(54f.sx(w), 90f.sy(h), 54f.sx(w), 78f.sy(h), 56f.sx(w), 67f.sy(h))
    cubicTo(59f.sx(w), 63f.sy(h), 63f.sx(w), 63f.sy(h), 66f.sx(w), 63f.sy(h))
    close()
}

private fun frontCalfLeft(w: Float, h: Float) = Path().apply {
    moveTo(33f.sx(w), 100f.sy(h))
    cubicTo(32f.sx(w), 110f.sy(h), 33f.sx(w), 120f.sy(h), 35f.sx(w), 130f.sy(h))
    lineTo(43f.sx(w), 130f.sy(h))
    cubicTo(45f.sx(w), 120f.sy(h), 46f.sx(w), 110f.sy(h), 46f.sx(w), 100f.sy(h))
    close()
}

private fun frontCalfRight(w: Float, h: Float) = Path().apply {
    moveTo(67f.sx(w), 100f.sy(h))
    cubicTo(68f.sx(w), 110f.sy(h), 67f.sx(w), 120f.sy(h), 65f.sx(w), 130f.sy(h))
    lineTo(57f.sx(w), 130f.sy(h))
    cubicTo(55f.sx(w), 120f.sy(h), 54f.sx(w), 110f.sy(h), 54f.sx(w), 100f.sy(h))
    close()
}

// ─────────────────────────────────────────────────────────────────────────────
//  Rear silhouette + muscle paths
// ─────────────────────────────────────────────────────────────────────────────

private fun rearSilhouette(w: Float, h: Float) = Path().apply {
    // Simplified rear silhouette (same silhouette mirror)
    moveTo(50f.sx(w), 0f.sy(h))
    cubicTo(44f.sx(w), 0f.sy(h), 40f.sx(w), 4f.sy(h), 40f.sx(w), 9f.sy(h))
    cubicTo(40f.sx(w), 14f.sy(h), 44f.sx(w), 17f.sy(h), 50f.sx(w), 17f.sy(h))
    cubicTo(56f.sx(w), 17f.sy(h), 60f.sx(w), 14f.sy(h), 60f.sx(w), 9f.sy(h))
    cubicTo(60f.sx(w), 4f.sy(h), 56f.sx(w), 0f.sy(h), 50f.sx(w), 0f.sy(h))
    close()
    moveTo(45f.sx(w), 16f.sy(h))
    lineTo(45f.sx(w), 20f.sy(h))
    lineTo(55f.sx(w), 20f.sy(h))
    lineTo(55f.sx(w), 16f.sy(h))
    close()
    moveTo(45f.sx(w), 19f.sy(h))
    cubicTo(38f.sx(w), 19f.sy(h), 20f.sx(w), 18f.sy(h), 18f.sx(w), 26f.sy(h))
    cubicTo(16f.sx(w), 32f.sy(h), 18f.sx(w), 42f.sy(h), 18f.sx(w), 48f.sy(h))
    cubicTo(18f.sx(w), 56f.sy(h), 18f.sx(w), 66f.sy(h), 20f.sx(w), 70f.sy(h))
    lineTo(28f.sx(w), 72f.sy(h))
    lineTo(30f.sx(w), 64f.sy(h))
    lineTo(32f.sx(w), 48f.sy(h))
    lineTo(34f.sx(w), 65f.sy(h))
    lineTo(33f.sx(w), 131f.sy(h))
    lineTo(43f.sx(w), 131f.sy(h))
    lineTo(46f.sx(w), 90f.sy(h))
    lineTo(50f.sx(w), 88f.sy(h))
    lineTo(54f.sx(w), 90f.sy(h))
    lineTo(57f.sx(w), 131f.sy(h))
    lineTo(67f.sx(w), 131f.sy(h))
    lineTo(66f.sx(w), 65f.sy(h))
    lineTo(68f.sx(w), 48f.sy(h))
    lineTo(70f.sx(w), 64f.sy(h))
    lineTo(72f.sx(w), 72f.sy(h))
    lineTo(80f.sx(w), 70f.sy(h))
    cubicTo(82f.sx(w), 66f.sy(h), 82f.sx(w), 56f.sy(h), 82f.sx(w), 48f.sy(h))
    cubicTo(82f.sx(w), 42f.sy(h), 84f.sx(w), 32f.sy(h), 82f.sx(w), 26f.sy(h))
    cubicTo(80f.sx(w), 18f.sy(h), 62f.sx(w), 19f.sy(h), 55f.sx(w), 19f.sy(h))
    close()
}

private fun rearNeck(w: Float, h: Float) = Path().apply {
    moveTo(45f.sx(w), 15f.sy(h))
    lineTo(45f.sx(w), 21f.sy(h))
    lineTo(55f.sx(w), 21f.sy(h))
    lineTo(55f.sx(w), 15f.sy(h))
    close()
}

private fun rearTrapsLeft(w: Float, h: Float) = Path().apply {
    moveTo(45f.sx(w), 20f.sy(h))
    cubicTo(40f.sx(w), 21f.sy(h), 35f.sx(w), 24f.sy(h), 33f.sx(w), 30f.sy(h))
    cubicTo(36f.sx(w), 32f.sy(h), 42f.sx(w), 31f.sy(h), 49f.sx(w), 29f.sy(h))
    lineTo(49f.sx(w), 20f.sy(h))
    close()
}

private fun rearTrapsRight(w: Float, h: Float) = Path().apply {
    moveTo(55f.sx(w), 20f.sy(h))
    cubicTo(60f.sx(w), 21f.sy(h), 65f.sx(w), 24f.sy(h), 67f.sx(w), 30f.sy(h))
    cubicTo(64f.sx(w), 32f.sy(h), 58f.sx(w), 31f.sy(h), 51f.sx(w), 29f.sy(h))
    lineTo(51f.sx(w), 20f.sy(h))
    close()
}

private fun rearDeltLeft(w: Float, h: Float) = Path().apply {
    moveTo(22f.sx(w), 20f.sy(h))
    cubicTo(20f.sx(w), 24f.sy(h), 19f.sx(w), 30f.sy(h), 22f.sx(w), 35f.sy(h))
    cubicTo(26f.sx(w), 36f.sy(h), 30f.sx(w), 34f.sy(h), 32f.sx(w), 30f.sy(h))
    cubicTo(30f.sx(w), 24f.sy(h), 27f.sx(w), 20f.sy(h), 22f.sx(w), 20f.sy(h))
    close()
}

private fun rearDeltRight(w: Float, h: Float) = Path().apply {
    moveTo(78f.sx(w), 20f.sy(h))
    cubicTo(80f.sx(w), 24f.sy(h), 81f.sx(w), 30f.sy(h), 78f.sx(w), 35f.sy(h))
    cubicTo(74f.sx(w), 36f.sy(h), 70f.sx(w), 34f.sy(h), 68f.sx(w), 30f.sy(h))
    cubicTo(70f.sx(w), 24f.sy(h), 73f.sx(w), 20f.sy(h), 78f.sx(w), 20f.sy(h))
    close()
}

private fun rearTricepLeft(w: Float, h: Float) = Path().apply {
    moveTo(20f.sx(w), 35f.sy(h))
    cubicTo(18f.sx(w), 39f.sy(h), 18f.sx(w), 46f.sy(h), 20f.sx(w), 52f.sy(h))
    cubicTo(23f.sx(w), 54f.sy(h), 27f.sx(w), 53f.sy(h), 29f.sx(w), 50f.sy(h))
    cubicTo(30f.sx(w), 46f.sy(h), 29f.sx(w), 39f.sy(h), 27f.sx(w), 35f.sy(h))
    cubicTo(25f.sx(w), 34f.sy(h), 22f.sx(w), 34f.sy(h), 20f.sx(w), 35f.sy(h))
    close()
}

private fun rearTricepRight(w: Float, h: Float) = Path().apply {
    moveTo(80f.sx(w), 35f.sy(h))
    cubicTo(82f.sx(w), 39f.sy(h), 82f.sx(w), 46f.sy(h), 80f.sx(w), 52f.sy(h))
    cubicTo(77f.sx(w), 54f.sy(h), 73f.sx(w), 53f.sy(h), 71f.sx(w), 50f.sy(h))
    cubicTo(70f.sx(w), 46f.sy(h), 71f.sx(w), 39f.sy(h), 73f.sx(w), 35f.sy(h))
    cubicTo(75f.sx(w), 34f.sy(h), 78f.sx(w), 34f.sy(h), 80f.sx(w), 35f.sy(h))
    close()
}

private fun rearLatsLeft(w: Float, h: Float) = Path().apply {
    moveTo(33f.sx(w), 30f.sy(h))
    cubicTo(30f.sx(w), 36f.sy(h), 30f.sx(w), 46f.sy(h), 33f.sx(w), 56f.sy(h))
    cubicTo(36f.sx(w), 57f.sy(h), 43f.sx(w), 55f.sy(h), 48f.sx(w), 50f.sy(h))
    cubicTo(48f.sx(w), 40f.sy(h), 47f.sx(w), 32f.sy(h), 45f.sx(w), 28f.sy(h))
    cubicTo(41f.sx(w), 28f.sy(h), 36f.sx(w), 28f.sy(h), 33f.sx(w), 30f.sy(h))
    close()
}

private fun rearLatsRight(w: Float, h: Float) = Path().apply {
    moveTo(67f.sx(w), 30f.sy(h))
    cubicTo(70f.sx(w), 36f.sy(h), 70f.sx(w), 46f.sy(h), 67f.sx(w), 56f.sy(h))
    cubicTo(64f.sx(w), 57f.sy(h), 57f.sx(w), 55f.sy(h), 52f.sx(w), 50f.sy(h))
    cubicTo(52f.sx(w), 40f.sy(h), 53f.sx(w), 32f.sy(h), 55f.sx(w), 28f.sy(h))
    cubicTo(59f.sx(w), 28f.sy(h), 64f.sx(w), 28f.sy(h), 67f.sx(w), 30f.sy(h))
    close()
}

private fun rearLowerBack(w: Float, h: Float) = Path().apply {
    moveTo(37f.sx(w), 56f.sy(h))
    lineTo(37f.sx(w), 68f.sy(h))
    lineTo(63f.sx(w), 68f.sy(h))
    lineTo(63f.sx(w), 56f.sy(h))
    cubicTo(58f.sx(w), 54f.sy(h), 42f.sx(w), 54f.sy(h), 37f.sx(w), 56f.sy(h))
    close()
}

private fun rearGluteLeft(w: Float, h: Float) = Path().apply {
    moveTo(34f.sx(w), 68f.sy(h))
    cubicTo(32f.sx(w), 72f.sy(h), 32f.sx(w), 79f.sy(h), 35f.sx(w), 85f.sy(h))
    cubicTo(38f.sx(w), 87f.sy(h), 44f.sx(w), 87f.sy(h), 48f.sx(w), 85f.sy(h))
    lineTo(49f.sx(w), 80f.sy(h))
    lineTo(49f.sx(w), 68f.sy(h))
    close()
}

private fun rearGluteRight(w: Float, h: Float) = Path().apply {
    moveTo(66f.sx(w), 68f.sy(h))
    cubicTo(68f.sx(w), 72f.sy(h), 68f.sx(w), 79f.sy(h), 65f.sx(w), 85f.sy(h))
    cubicTo(62f.sx(w), 87f.sy(h), 56f.sx(w), 87f.sy(h), 52f.sx(w), 85f.sy(h))
    lineTo(51f.sx(w), 80f.sy(h))
    lineTo(51f.sx(w), 68f.sy(h))
    close()
}

private fun rearHamstringLeft(w: Float, h: Float) = Path().apply {
    moveTo(34f.sx(w), 85f.sy(h))
    cubicTo(32f.sx(w), 92f.sy(h), 32f.sx(w), 106f.sy(h), 35f.sx(w), 118f.sy(h))
    lineTo(47f.sx(w), 118f.sy(h))
    cubicTo(48f.sx(w), 106f.sy(h), 48f.sx(w), 94f.sy(h), 47f.sx(w), 85f.sy(h))
    cubicTo(43f.sx(w), 87f.sy(h), 38f.sx(w), 87f.sy(h), 34f.sx(w), 85f.sy(h))
    close()
}

private fun rearHamstringRight(w: Float, h: Float) = Path().apply {
    moveTo(66f.sx(w), 85f.sy(h))
    cubicTo(68f.sx(w), 92f.sy(h), 68f.sx(w), 106f.sy(h), 65f.sx(w), 118f.sy(h))
    lineTo(53f.sx(w), 118f.sy(h))
    cubicTo(52f.sx(w), 106f.sy(h), 52f.sx(w), 94f.sy(h), 53f.sx(w), 85f.sy(h))
    cubicTo(57f.sx(w), 87f.sy(h), 62f.sx(w), 87f.sy(h), 66f.sx(w), 85f.sy(h))
    close()
}

private fun rearCalfLeft(w: Float, h: Float) = Path().apply {
    moveTo(35f.sx(w), 118f.sy(h))
    cubicTo(33f.sx(w), 124f.sy(h), 33f.sx(w), 132f.sy(h), 36f.sx(w), 140f.sy(h))
    lineTo(45f.sx(w), 140f.sy(h))
    cubicTo(47f.sx(w), 132f.sy(h), 47f.sx(w), 124f.sy(h), 47f.sx(w), 118f.sy(h))
    close()
}

private fun rearCalfRight(w: Float, h: Float) = Path().apply {
    moveTo(65f.sx(w), 118f.sy(h))
    cubicTo(67f.sx(w), 124f.sy(h), 67f.sx(w), 132f.sy(h), 64f.sx(w), 140f.sy(h))
    lineTo(55f.sx(w), 140f.sy(h))
    cubicTo(53f.sx(w), 132f.sy(h), 53f.sx(w), 124f.sy(h), 53f.sx(w), 118f.sy(h))
    close()
}
