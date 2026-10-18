package com.example.astro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.model.CelestialTarget
import com.example.astro.model.MeteorShower
import com.example.astro.model.MilkyWayStatus
import com.example.astro.model.NightTimelineHour
import com.example.astro.ui.theme.LocalAstroPalette
import kotlin.math.roundToInt

/**
 * Interactive Night Timeline Widget with darkness windows and hourly conditions
 */
@Composable
fun NightTimelineWidget(
    timeline: List<NightTimelineHour>,
    modifier: Modifier = Modifier
) {
    val palette = LocalAstroPalette.current
    var selectedHour by remember { mutableStateOf<NightTimelineHour?>(null) }

    LaunchedEffect(timeline) {
        if (selectedHour == null && timeline.isNotEmpty()) {
            selectedHour = timeline.firstOrNull { it.isBestDarkWindow } ?: timeline.firstOrNull()
        }
    }

    AstroCard(modifier = modifier.testTag("night_timeline_widget")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (palette.isRedMode) palette.surface else Color(0x3338BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Почасовой таймлайн ночи",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Фазы сумерек, окна темноты и ядро Млечного Пути",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal scrollable hourly pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeline.forEach { hour ->
                val isSelected = selectedHour?.hourOfDay == hour.hourOfDay
                val hourBorder = if (isSelected) {
                    BorderStroke(2.dp, palette.primary)
                } else if (hour.isBestDarkWindow) {
                    BorderStroke(1.dp, if (palette.isRedMode) palette.primary else Color(0xFF38BDF8))
                } else {
                    BorderStroke(0.5.dp, palette.cardBorder)
                }

                val scoreColor = when {
                    hour.qualityScore >= 75 -> if (palette.isRedMode) palette.primary else Color(0xFF10B981)
                    hour.qualityScore >= 45 -> if (palette.isRedMode) Color(0xFFAA2222) else Color(0xFFF59E0B)
                    else -> if (palette.isRedMode) Color(0xFF661111) else Color(0xFFEF4444)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) palette.surface else palette.cardBg,
                    border = hourBorder,
                    modifier = Modifier
                        .width(74.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedHour = hour }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = hour.hourString,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Darkness or Milky Way Icon
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hour.isBestDarkWindow) {
                                        if (palette.isRedMode) Color(0x33FF2E2E) else Color(0x3310B981)
                                    } else {
                                        Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    hour.sunAltitudeDeg > -6.0 -> Icons.Default.WbSunny
                                    hour.isBestDarkWindow -> Icons.Default.Stars
                                    hour.isMilkyWayVisible -> Icons.Default.Flare
                                    else -> Icons.Default.Nightlight
                                },
                                contentDescription = null,
                                tint = if (hour.isBestDarkWindow) palette.primary else palette.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Score Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = scoreColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${hour.qualityScore}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = scoreColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Обл. ${hour.cloudCoverPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected hour detailed card
        selectedHour?.let { hour ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.surface,
                border = BorderStroke(0.5.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Время: ${hour.hourString}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (hour.isBestDarkWindow) Color(0x3310B981) else palette.cardBg
                            ) {
                                Text(
                                    text = hour.twilightPhaseRu,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hour.isBestDarkWindow) Color(0xFF10B981) else palette.textSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "Индекс: ${hour.qualityScore}/100",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Солнце",
                            value = "${hour.sunAltitudeDeg}°",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Млечный Путь",
                            value = "${hour.milkyWayAltitudeDeg}°",
                            statusColor = if (hour.milkyWayAltitudeDeg > 5) palette.primary else null,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Луна",
                            value = "${hour.moonAltitudeDeg}° (${hour.moonIlluminationPercent}%)",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Облака",
                            value = "${hour.cloudCoverPercent}%",
                            statusColor = if (hour.cloudCoverPercent < 30) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val hourAdvice = when {
                        hour.isBestDarkWindow && hour.isMilkyWayVisible ->
                            "🌟 Идеальный час: полная темнота без Луны, ядро Млечного Пути над горизонтом! Снимайте панорамы и ядро Галактики."
                        hour.isBestDarkWindow ->
                            "🌌 Окно 100% темноты: лучшее время для накопления света на туманностях Мессье и далеких галактиках."
                        hour.sunAltitudeDeg > -12.0 ->
                            "🌅 Сумерки: отличный свет для силуэтов гор, рельефа переднего плана и синего часа."
                        hour.moonAltitudeDeg > 10.0 ->
                            "🌕 Лунный свет: освещает передний план и снежные вершины, но гасит слабые звезды и туманности."
                        else ->
                            "Хорошие ночные условия для астрофотографии."
                    }

                    Text(
                        text = hourAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Milky Way status and planning card
 */
@Composable
fun MilkyWayStatusCard(
    status: MilkyWayStatus,
    modifier: Modifier = Modifier
) {
    val palette = LocalAstroPalette.current

    AstroCard(modifier = modifier.testTag("milky_way_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (palette.isRedMode) palette.surface else Color(0x33A855F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (palette.isRedMode) palette.primary else Color(0xFFC084FC),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ядро Млечного Пути (Sagittarius A*)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Координаты центра Галактики: RA 17h45m, Dec -29°",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (status.isCoreVisible) Color(0x3310B981) else palette.surface
            ) {
                Text(
                    text = if (status.isCoreVisible) "На небе" else "Под горизонтом",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (status.isCoreVisible) Color(0xFF10B981) else palette.textMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadge(
                label = "Высота сейчас",
                value = "${status.coreAltitudeDeg}°",
                statusColor = if (status.coreAltitudeDeg > 5) palette.primary else null,
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Сторона света",
                value = status.cardinalDirectionRu,
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Макс. высота",
                value = "${status.maxAltitudeTonightDeg}°",
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Окно съёмки",
                value = status.bestWindowTime,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = palette.surface,
            border = BorderStroke(0.5.dp, palette.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = status.visibilitySummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💡 Совет для панорам: фокусное 14–24 мм, камера вертикально, перекрытие кадров 35–40%, ISO 3200–6400.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
            }
        }
    }
}

/**
 * Celestial target card in Messier & Deep Sky catalog
 */
@Composable
fun CelestialItemCard(
    target: CelestialTarget,
    onPlanTarget: (CelestialTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAstroPalette.current

    val isHigh = target.currentAltitudeDeg > 30.0
    val isVisible = target.currentAltitudeDeg > 0.0

    AstroCard(modifier = modifier.testTag("target_card_${target.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = target.nameRu,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }
                Text(
                    text = "${target.catalogId} • Созвездие: ${target.constellationRu} • ${target.categoryRu}",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isHigh -> Color(0x3310B981)
                    isVisible -> Color(0x33F59E0B)
                    else -> palette.surface
                }
            ) {
                Text(
                    text = "${target.currentAltitudeDeg}°",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isHigh -> Color(0xFF10B981)
                        isVisible -> Color(0xFFF59E0B)
                        else -> palette.textMuted
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = target.descriptionRu,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricBadge(
                label = "Яркость",
                value = "${target.apparentMagnitude}m",
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Размер",
                value = target.angularSize,
                modifier = Modifier.weight(1.2f)
            )
            MetricBadge(
                label = "Оптика",
                value = target.recommendedFocalLength,
                modifier = Modifier.weight(1.4f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = target.visibilityStatusRu,
                style = MaterialTheme.typography.labelSmall,
                color = if (isVisible) palette.primary else palette.textMuted,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { onPlanTarget(target) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("plan_target_${target.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("В план", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Meteor shower event card
 */
@Composable
fun MeteorShowerCard(
    shower: MeteorShower,
    onPlanShower: (MeteorShower) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAstroPalette.current

    AstroCard(modifier = modifier.testTag("meteor_card_${shower.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (palette.isRedMode) palette.surface else Color(0x33F59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (palette.isRedMode) palette.primary else Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Звездопад ${shower.nameRu}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Пик: ${shower.peakDateRu} • Активность: ${shower.activeRangeRu}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x33F59E0B)
            ) {
                Text(
                    text = "${shower.zhr} мет/ч",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadge(
                label = "Радиант",
                value = shower.radiantConstellation,
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Скорость",
                value = "${shower.velocityKmS} км/с",
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Источник",
                value = shower.parentBody,
                modifier = Modifier.weight(1.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "📷 ${shower.shootingTipsRu}",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { onPlanShower(shower) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.surface),
            border = BorderStroke(1.dp, palette.primary),
            modifier = Modifier.fillMaxWidth().testTag("plan_shower_${shower.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Запланировать выезд на пик (${shower.peakDateRu})", color = palette.primary, fontWeight = FontWeight.Bold)
        }
    }
}
