package com.example.astro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.StrokeCap
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
 * Multi-segment circular ring diagram (Кольцевая диаграмма)
 * Displays breakdown of clear sky, clouds, and moon illumination.
 * Strictly constrained to 96dp size to completely prevent full-screen expansion.
 */
@Composable
fun SkyQualityScoreRing(
    score: Int,
    cloudCoverPercent: Int = 15,
    moonIlluminationPercent: Int = 10,
    modifier: Modifier = Modifier
) {
    val palette = LocalAstroPalette.current
    val ringSize = 92.dp

    Box(
        modifier = modifier
            .size(ringSize)
            .wrapContentSize(Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokeWidth = 7.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)

            // Base track circle
            drawArc(
                color = palette.surface,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Donut segments calculation
            val total = (score + cloudCoverPercent + moonIlluminationPercent).coerceAtLeast(100).toFloat()
            val skySweep = (score / total) * 360f
            val cloudSweep = (cloudCoverPercent / total) * 360f
            val moonSweep = (moonIlluminationPercent / total) * 360f

            val skyColor = if (palette.isRedMode) palette.primary else Color(0xFF10B981)
            val cloudColor = if (palette.isRedMode) Color(0xFF661111) else Color(0xFF64748B)
            val moonColor = if (palette.isRedMode) Color(0xFFAA2222) else Color(0xFFFBBF24)

            var currentAngle = -90f // Start from top 12 o'clock

            // 1. Clear sky segment
            if (skySweep > 3f) {
                drawArc(
                    color = skyColor,
                    startAngle = currentAngle + 2f,
                    sweepAngle = (skySweep - 4f).coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                currentAngle += skySweep
            }

            // 2. Cloud segment
            if (cloudSweep > 3f) {
                drawArc(
                    color = cloudColor,
                    startAngle = currentAngle + 2f,
                    sweepAngle = (cloudSweep - 4f).coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                currentAngle += cloudSweep
            }

            // 3. Moon illumination segment
            if (moonSweep > 3f) {
                drawArc(
                    color = moonColor,
                    startAngle = currentAngle + 2f,
                    sweepAngle = (moonSweep - 4f).coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Centered percentage and label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary,
                fontSize = 18.sp
            )
            Text(
                text = "Индекс",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Dialog to choose location preset or enter coordinates / GPS
 */
@Composable
fun LocationSelectorDialog(
    currentLocation: ObservationLocation,
    isGpsSearching: Boolean = false,
    onTriggerGps: () -> Unit,
    onSelectLocation: (ObservationLocation) -> Unit,
    onSetCustomCoords: (lat: Double, lon: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalAstroPalette.current
    var isCustomMode by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(0) } // 0: Северо-Запад, 1: Все города, 2: Тёмное небо
    var latInput by remember { mutableStateOf(currentLocation.latitude.toString()) }
    var lonInput by remember { mutableStateOf(currentLocation.longitude.toString()) }

    val filteredLocations = remember(selectedCategory) {
        when (selectedCategory) {
            0 -> ObservationLocation.DEFAULT_LOCATIONS.filter { it.isNorthWest }
            1 -> ObservationLocation.DEFAULT_LOCATIONS
            2 -> ObservationLocation.DEFAULT_LOCATIONS.filter { it.bortleScale <= 3 }
            else -> ObservationLocation.DEFAULT_LOCATIONS
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.cardBg,
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("location_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Выбор локации",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = palette.textMuted)
                    }
                }

                Text(
                    text = "Координаты определяют высоту Солнца, сумерки, облака и полярные сияния.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // GPS Search Button
                FilledTonalButton(
                    onClick = onTriggerGps,
                    enabled = !isGpsSearching,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (palette.isRedMode) palette.surface else Color(0x3338BDF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gps_search_button")
                ) {
                    if (isGpsSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = palette.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Определяем GPS координаты...", color = palette.primary)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = palette.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Определить по GPS (моё место)", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isCustomMode) {
                    // Category filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == 0,
                            onClick = { selectedCategory = 0 },
                            label = { Text("Северо-Запад (${ObservationLocation.DEFAULT_LOCATIONS.count { it.isNorthWest }})", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedCategory == 1,
                            onClick = { selectedCategory = 1 },
                            label = { Text("Все города", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedCategory == 2,
                            onClick = { selectedCategory = 2 },
                            label = { Text("Тёмное небо", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredLocations.forEach { loc ->
                            val isSelected = loc.id == currentLocation.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
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
                                    modifier = Modifier.padding(10.dp),
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
                                            text = "${loc.regionRu} • Бортль: ${loc.bortleScale}/9",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = palette.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { isCustomMode = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_coords_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ввести координаты вручную")
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
