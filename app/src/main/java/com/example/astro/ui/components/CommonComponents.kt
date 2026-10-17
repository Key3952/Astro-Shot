package com.example.astro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astro.model.ObservationLocation
import com.example.astro.ui.theme.LocalAstroPalette
import kotlin.math.cos

@Composable
fun AstroCard(
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalAstroPalette.current
    val shape = RoundedCornerShape(20.dp)
    val finalBorder = borderStroke ?: BorderStroke(1.dp, palette.cardBorder)

    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = palette.cardBg,
        border = finalBorder
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailingAction: @Composable (() -> Unit)? = null
) {
    val palette = LocalAstroPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }
        }
        trailingAction?.invoke()
    }
}

@Composable
fun MetricBadge(
    label: String,
    value: String,
    unit: String = "",
    statusColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalAstroPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = palette.surface,
        border = BorderStroke(0.5.dp, palette.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (statusColor != null) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * Realistic Canvas rendering of the Moon illumination
 */
@Composable
fun MoonPhaseCanvas(
    illuminationPercent: Int,
    ageDays: Float,
    modifier: Modifier = Modifier.size(80.dp)
) {
    val palette = LocalAstroPalette.current
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Dark side base
        val darkMoonColor = if (palette.isRedMode) Color(0xFF1E0404) else Color(0xFF1A1F2E)
        drawCircle(
            color = darkMoonColor,
            radius = radius,
            center = center
        )

        // Moon subtle outer glow ring
        val glowColor = if (palette.isRedMode) Color(0x33FF3333) else Color(0x33FBBF24)
        drawCircle(
            color = glowColor,
            radius = radius + 2.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Moon illuminated portion
        val lightMoonColor = if (palette.isRedMode) Color(0xFFFF5555) else Color(0xFFFFF7ED)
        val phaseFraction = (illuminationPercent / 100f).coerceIn(0f, 1f)

        // Draw illuminated crescent or gibbous path
        if (phaseFraction > 0.02f) {
            val isWaxing = ageDays < 15.0f
            val path = Path()

            // Arc for the outer illuminated limb
            val sweep = if (isWaxing) 180f else -180f
            val startAngle = if (isWaxing) 270f else 90f

            path.arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - radius, center.y - radius,
                    center.x + radius, center.y + radius
                ),
                startAngleDegrees = startAngle,
                sweepAngleDegrees = sweep,
                forceMoveTo = false
            )

            // Inner terminator ellipse: width scales by cos(phaseAngle)
            val termRadiusX = radius * (1f - 2f * phaseFraction).coerceIn(-radius, radius)
            path.arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - kotlin.math.abs(termRadiusX), center.y - radius,
                    center.x + kotlin.math.abs(termRadiusX), center.y + radius
                ),
                startAngleDegrees = if (isWaxing) 90f else 270f,
                sweepAngleDegrees = if (isWaxing) 180f else -180f,
                forceMoveTo = false
            )
            path.close()

            drawPath(
                path = path,
                color = lightMoonColor,
                style = Fill
            )
        }

        // Maria / crater accents
        if (!palette.isRedMode && phaseFraction > 0.25f) {
            drawCircle(
                color = Color(0x18000000),
                radius = radius * 0.25f,
                center = Offset(center.x + radius * 0.2f, center.y - radius * 0.1f)
            )
            drawCircle(
                color = Color(0x15000000),
                radius = radius * 0.18f,
                center = Offset(center.x - radius * 0.15f, center.y + radius * 0.25f)
            )
        }
    }
}

/**
 * Sky Quality circular score meter
 */
@Composable
fun SkyQualityScoreRing(
    score: Int,
    modifier: Modifier = Modifier.size(90.dp)
) {
    val palette = LocalAstroPalette.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background circle
            drawArc(
                color = palette.surface,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth)
            )

            // Active progress arc
            val progressSweep = (score / 100f).coerceIn(0f, 1f) * 270f
            val arcColor = when {
                palette.isRedMode -> palette.primary
                score >= 75 -> palette.primary
                score >= 45 -> palette.accent
                else -> Color(0xFFEF4444)
            }

            drawArc(
                color = arcColor,
                startAngle = 135f,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary
            )
            Text(
                text = "Индекс",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Dialog to choose location preset or enter coordinates
 */
@Composable
fun LocationSelectorDialog(
    currentLocation: ObservationLocation,
    onSelectLocation: (ObservationLocation) -> Unit,
    onSetCustomCoords: (lat: Double, lon: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalAstroPalette.current
    var isCustomMode by remember { mutableStateOf(false) }
    var latInput by remember { mutableStateOf(currentLocation.latitude.toString()) }
    var lonInput by remember { mutableStateOf(currentLocation.longitude.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.cardBg,
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier.fillMaxWidth().testTag("location_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Выбор локации съёмки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = "Координаты определяют высоту Солнца, сумерки, погоду и вероятность сияния.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (!isCustomMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ObservationLocation.DEFAULT_LOCATIONS.forEach { loc ->
                            val isSelected = loc.id == currentLocation.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) palette.surface else Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) palette.primary else palette.cardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectLocation(loc)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = loc.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = palette.textPrimary
                                        )
                                        Text(
                                            text = "${loc.regionRu} • Шкала Бортля: ${loc.bortleScale}/9",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = palette.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { isCustomMode = true },
                        modifier = Modifier.fillMaxWidth().testTag("custom_coords_button")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ввести точные GPS координаты")
                    }
                } else {
                    OutlinedTextField(
                        value = latInput,
                        onValueChange = { latInput = it },
                        label = { Text("Широта (Latitude, например 55.75)") },
                        modifier = Modifier.fillMaxWidth().testTag("latitude_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lonInput,
                        onValueChange = { lonInput = it },
                        label = { Text("Долгота (Longitude, например 37.61)") },
                        modifier = Modifier.fillMaxWidth().testTag("longitude_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { isCustomMode = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Назад")
                        }
                        Button(
                            onClick = {
                                val lat = latInput.toDoubleOrNull() ?: currentLocation.latitude
                                val lon = lonInput.toDoubleOrNull() ?: currentLocation.longitude
                                onSetCustomCoords(lat, lon)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).testTag("save_coords_button")
                        ) {
                            Text("Применить")
                        }
                    }
                }
            }
        }
    }
}
