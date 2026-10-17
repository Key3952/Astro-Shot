package com.example.astro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.model.CondensationRisk
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.components.*
import com.example.astro.ui.theme.LocalAstroPalette

@Composable
fun DashboardScreen(
    viewModel: AstroViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val palette = LocalAstroPalette.current
    val scrollState = rememberScrollState()

    val location by viewModel.selectedLocation.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
    val moon by viewModel.moonDetails.collectAsState()
    val twilight by viewModel.twilightDetails.collectAsState()
    val aurora by viewModel.auroraState.collectAsState()
    val exposure by viewModel.exposureResult.collectAsState()
    val focal by viewModel.focalLengthMm.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location & Sky Quality Hero Banner
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("sky_quality_hero_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (palette.isRedMode) palette.primary else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Шкала Бортля: ${location.bortleScale} • ${location.regionRu}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    val qualityText = when {
                        weather == null -> "Загрузка метеоданных..."
                        weather!!.skyQualityScore >= 80 -> "Отличная ночь для съёмки звёзд"
                        weather!!.skyQualityScore >= 50 -> "Приемлемые условия с облачностью"
                        else -> "Плотная облачность или сильная засветка"
                    }
                    Text(
                        text = qualityText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (palette.isRedMode) palette.primary else palette.primary
                    )
                }

                SkyQualityScoreRing(
                    score = weather?.skyQualityScore ?: 80,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = palette.cardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Quick metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBadge(
                    label = "Облачность",
                    value = "${weather?.totalCloudCoverPercent ?: 15}%",
                    statusColor = if ((weather?.totalCloudCoverPercent ?: 15) < 30) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Температура",
                    value = "${weather?.temperatureC ?: 10}°C",
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Риск росы",
                    value = if (weather?.condensationRisk == CondensationRisk.CRITICAL) "Высокий" else "Низкий",
                    statusColor = Color(weather?.condensationRisk?.severityColorHex ?: 0xFF4ADE80),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Moon Status Card
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("moon_dashboard_card"),
            onClick = { onNavigateToTab(3) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoonPhaseCanvas(
                    illuminationPercent = moon.illuminationPercent,
                    ageDays = moon.ageDays,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = moon.phaseType.titleRu,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.surface
                        ) {
                            Text(
                                text = "${moon.illuminationPercent}% свет",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = moon.astroSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Восход: ${moon.moonriseTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                        Text(
                            text = "Заход: ${moon.moonsetTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                    }
                }
            }
        }

        // Astronomical Darkness Window
        AstroCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title = "Окно астрономической темноты",
                subtitle = "Солнце ниже -18° (идеально для Млечного Пути)",
                icon = Icons.Default.NightlightRound
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Начало темноты",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                    Text(
                        text = twilight.astroTwilightStart,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "${twilight.darkSkyDurationHours} ч темноты",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Конец темноты",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                    Text(
                        text = twilight.astroTwilightEnd,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }
            }
        }

        // Aurora Borealis Snapshot
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("aurora_dashboard_card"),
            onClick = { onNavigateToTab(2) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (palette.isRedMode) palette.surface else Color(0x2234D399)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storm,
                            contentDescription = null,
                            tint = if (palette.isRedMode) palette.primary else Color(0xFF34D399),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Полярные сияния",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Kp-индекс: ${aurora.kpIndex} • Уровень ${aurora.stormLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (aurora.observationChanceAtUserLat > 50) Color(0x3310B981) else palette.surface
                ) {
                    Text(
                        text = "Шанс: ${aurora.observationChanceAtUserLat}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (aurora.observationChanceAtUserLat > 50) Color(0xFF34D399) else palette.textMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = aurora.stormDescriptionRu,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted
            )
        }

        // Quick Optics Tool Preview
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("quick_optics_card"),
            onClick = { onNavigateToTab(1) }
        ) {
            SectionHeader(
                title = "Параметры камеры: ${focal.toInt()} мм",
                subtitle = "Макс. выдержка без треков звёзд",
                icon = Icons.Default.CameraAlt,
                trailingAction = {
                    TextButton(onClick = { onNavigateToTab(1) }) {
                        Text("Настроить >", color = palette.primary)
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBadge(
                    label = "Правило NPF (точно)",
                    value = "${exposure.npfRuleSeconds}",
                    unit = "сек",
                    statusColor = palette.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Правило 500 (классика)",
                    value = "${exposure.rule500Seconds}",
                    unit = "сек",
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Гиперфокал",
                    value = "${exposure.hyperfocalDistanceMeters}",
                    unit = "м",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
