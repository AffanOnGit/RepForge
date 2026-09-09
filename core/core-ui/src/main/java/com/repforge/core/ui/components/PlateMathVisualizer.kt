package com.repforge.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.core.domain.model.PlateMath
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateLight
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary

/**
 * Visual representation of barbell plates loaded per side.
 */
@Composable
fun PlateMathVisualizer(
    totalWeightKg: Double,
    barWeightKg: Double = 20.0,
    unitSystem: UnitSystem = UnitSystem.Metric,
    modifier: Modifier = Modifier
) {
    val platesPerSide = PlateMath.calculatePlatesPerSide(totalWeightKg, barWeightKg, unitSystem)
    val unitLabel = if (unitSystem is UnitSystem.Metric) "kg" else "lb"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BARBELL PLATE LOADER",
                color = ForgeAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Bar: ${barWeightKg.toInt()} kg",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Barbell Sleeve Representation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonSlateSurface)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bar collar / stopper
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF8E99A8))
            )

            // Bar shaft sleeve line
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(18.dp)
                    .background(Color(0xFFB0B8C8))
            )

            // Plates loaded from inside collar outward
            if (platesPerSide.isEmpty()) {
                Text(
                    text = "Empty Bar",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 16.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    platesPerSide.forEach { plateWeight ->
                        val plateHeight = when {
                            plateWeight >= 20.0 || plateWeight >= 45.0 -> 68.dp
                            plateWeight >= 15.0 || plateWeight >= 35.0 -> 58.dp
                            plateWeight >= 10.0 || plateWeight >= 25.0 -> 48.dp
                            plateWeight >= 5.0 || plateWeight >= 10.0 -> 38.dp
                            else -> 28.dp
                        }

                        val plateColor = when {
                            plateWeight >= 25.0 -> Color(0xFFE53935) // Red 25kg
                            plateWeight >= 20.0 || plateWeight >= 45.0 -> Color(0xFF1E88E5) // Blue 20kg / 45lb
                            plateWeight >= 15.0 || plateWeight >= 35.0 -> Color(0xFFFDD835) // Yellow 15kg
                            plateWeight >= 10.0 || plateWeight >= 25.0 -> Color(0xFF43A047) // Green 10kg
                            else -> Color(0xFF757575)
                        }

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(plateHeight)
                                .clip(RoundedCornerShape(4.dp))
                                .background(plateColor)
                                .border(1.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (plateWeight % 1.0 == 0.0) "%.0f".format(plateWeight) else "%.1f".format(plateWeight),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Breakdown Summary
        if (platesPerSide.isNotEmpty()) {
            val plateCounts = platesPerSide.groupingBy { it }.eachCount()
            Text(
                text = "Per side: " + plateCounts.entries.joinToString(", ") { "${it.value} × ${if (it.key % 1.0 == 0.0) it.key.toInt() else it.key} $unitLabel" },
                color = KineticLime,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
