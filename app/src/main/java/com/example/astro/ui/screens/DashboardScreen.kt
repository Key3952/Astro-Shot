package com.example.astro.ui.screens

import androidx.compose.foundation.BorderStroke
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
    onNavigateToTab: (Int) -> Unit,
    onOpenSettings: () -> Unit = {}
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
    val milkyWay by viewModel.milkyWayStatus.collectAsState()
    val weatherSource by viewModel.selectedWeatherSource.collectAsState()
    val apodData by viewModel.nasaApod.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Data Sources & API Keys Quick Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.surface,
            border = BorderStroke(0.5.dp, palette.cardBorder),
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth().testTag("dashboard_settings_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Источник: ${weatherSource.titleRu.take(20)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Настройки API",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        // Location & Sky Quality Card - Compact & Balanced
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
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (palette.isRedMode) palette.primary else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${location.regionRu} • Бортль: ${location.bortleScale}/9",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    // Donut chart color breakdown legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChartLegendDot(color = if (palette.isRedMode) palette.primary else Color(0xFF10B981), label = "Небо ${weather?.skyQualityScore ?: 80}%")
                        ChartLegendDot(color = if (palette.isRedMode) Color(0xFF661111) else Color(0xFF64748B), label = "Облака ${weather?.totalCloudCoverPercent ?: 15}%")
                        ChartLegendDot(color = if (palette.isRedMode) Color(0xFFAA2222) else Color(0xFFFBBF24), label = "Луна ${moon.illuminationPercent}%")
                    }
                }

                SkyQualityScoreRing(
                    score = weather?.skyQualityScore ?: 80,
                    cloudCoverPercent = weather?.totalCloudCoverPercent ?: 15,
                    moonIlluminationPercent = moon.illuminationPercent,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = palette.cardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Quick metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        // Moon Status Card - Compact & Clean
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
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
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
                            shape = RoundedCornerShape(6.dp),
                            color = palette.surface
                        ) {
                            Text(
                                text = "${moon.illuminationPercent}% свет",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = moon.astroSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

        // Astronomical Darkness & Milky Way Window Card
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("darkness_dashboard_card"),
            onClick = { onNavigateToTab(4) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Астрономическая темнота & Млечный Путь",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Окно темноты: ${twilight.astroTwilightStart} – ${twilight.astroTwilightEnd}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.surface
                ) {
                    Text(
                        text = "${twilight.darkSkyDurationHours} ч",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (milkyWay.isCoreVisible) "🌌 Ядро Галактики: ${milkyWay.coreAltitudeDeg}° (${milkyWay.cardinalDirectionRu})" else "🌌 " + milkyWay.visibilitySummary.take(45) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Таймлайн >",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Aurora Borealis Alert
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (palette.isRedMode) palette.surface else Color(0x2234D399)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storm,
                            contentDescription = null,
                            tint = if (palette.isRedMode) palette.primary else Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Полярные сияния",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Kp-индекс: ${aurora.kpIndex} • ${aurora.stormLevel}",
                            style = MaterialTheme.typography.labelSmall,
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Quick Optics Tool Preview
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("quick_optics_card"),
            onClick = { onNavigateToTab(1) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Параметры камеры: ${focal.toInt()} мм",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Выдержка без звёздных треков",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                    }
                }
                TextButton(
                    onClick = { onNavigateToTab(1) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Оптика >", color = palette.primary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricBadge(
                    label = "NPF (точно)",
                    value = "${exposure.npfRuleSeconds}",
                    unit = "с",
                    statusColor = palette.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Правило 500",
                    value = "${exposure.rule500Seconds}",
                    unit = "с",
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = LocalAstroPalette.current.textMuted
        )
    }
}
