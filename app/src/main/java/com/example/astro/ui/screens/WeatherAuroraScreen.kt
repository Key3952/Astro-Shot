package com.example.astro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import com.example.astro.ui.components.AstroCard
import com.example.astro.ui.components.MetricBadge
import com.example.astro.ui.components.SectionHeader
import com.example.astro.ui.theme.LocalAstroPalette

@Composable
fun WeatherAuroraScreen(
    viewModel: AstroViewModel
) {
    val palette = LocalAstroPalette.current
    val scrollState = rememberScrollState()

    val weather by viewModel.weatherState.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
    val aurora by viewModel.auroraState.collectAsState()
    val location by viewModel.selectedLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("weather_aurora_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action Bar with Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Погода и Сияния",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = "${location.name} (${String.format("%.2f", location.latitude)}° N)",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }

            IconButton(
                onClick = { viewModel.refreshWeather() },
                modifier = Modifier.testTag("refresh_weather_button")
            ) {
                if (isWeatherLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = palette.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить погоду",
                        tint = palette.primary
                    )
                }
            }
        }

        // Multi-Layer Cloud Breakdown Card
        AstroCard(modifier = Modifier.fillMaxWidth().testTag("clouds_card")) {
            SectionHeader(
                title = "Послойная облачность неба",
                subtitle = "Общая: ${weather?.totalCloudCoverPercent ?: 15}%",
                icon = Icons.Default.Cloud
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Cloud layers breakdown
            CloudLayerBar(
                title = "Высокие облака (Перистые)",
                percent = weather?.highCloudsPercent ?: 10,
                hint = "Размывают звёзды и создают гало",
                palette = palette
            )
            Spacer(modifier = Modifier.height(8.dp))
            CloudLayerBar(
                title = "Средние облака (Высокослоистые)",
                percent = weather?.midCloudsPercent ?: 8,
                hint = "Блокируют свет слабоосвещенных туманностей",
                palette = palette
            )
            Spacer(modifier = Modifier.height(8.dp))
            CloudLayerBar(
                title = "Низкие облака и туман",
                percent = weather?.lowCloudsPercent ?: 5,
                hint = "Полностью закрывают горизонт и пейзаж",
                palette = palette
            )
        }

        // Condensation Risk & Dew Point Alert
        val dewDiff = (weather?.temperatureC ?: 10f) - (weather?.dewPointC ?: 5f)
        val condensationRisk = weather?.condensationRisk ?: CondensationRisk.SAFE
        AstroCard(
            modifier = Modifier.fillMaxWidth().testTag("dew_point_card"),
            borderStroke = if (condensationRisk == CondensationRisk.CRITICAL) {
                androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444))
            } else null
        ) {
            SectionHeader(
                title = "Риск запотевания оптики (Точка росы)",
                subtitle = "Разница: ${String.format("%.1f", dewDiff)}°C",
                icon = Icons.Default.WaterDrop
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBadge(
                    label = "Температура воздуха",
                    value = "${weather?.temperatureC ?: 10}°C",
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Точка росы",
                    value = "${weather?.dewPointC ?: 6}°C",
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Влажность",
                    value = "${weather?.relativeHumidityPercent ?: 70}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (condensationRisk) {
                    CondensationRisk.CRITICAL -> Color(0x33EF4444)
                    CondensationRisk.MODERATE -> Color(0x33F59E0B)
                    CondensationRisk.SAFE -> Color(0x2210B981)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (condensationRisk == CondensationRisk.CRITICAL) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(condensationRisk.severityColorHex),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = condensationRisk.labelRu,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = when (condensationRisk) {
                                CondensationRisk.CRITICAL -> "Срочно включите USB-грелку для объектива! Линза запотеет через 15-20 минут."
                                CondensationRisk.MODERATE -> "Следите за передней линзой, держите бленду надетой."
                                CondensationRisk.SAFE -> "Опасности конденсата нет, оптика останется сухой."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }

        // 24-Hour Timeline Horizon
        AstroCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(
                title = "Почасовой прогноз чистоты неба",
                subtitle = "Ближайшие 24 часа",
                icon = Icons.Default.Timeline
            )
            Spacer(modifier = Modifier.height(10.dp))

            val hourly = weather?.hourlyForecast ?: emptyList()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hourly.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.surface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, palette.cardBorder),
                        modifier = Modifier.width(68.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.timeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Mini sky score ring / badge
                            val col = when {
                                item.cloudCover < 20 -> Color(0xFF10B981)
                                item.cloudCover < 50 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.cloudCover}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = col,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Δ ${item.dewPointDiff}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Aurora Borealis Predictor
        AstroCard(modifier = Modifier.fillMaxWidth().testTag("aurora_radar_card")) {
            SectionHeader(
                title = "Прогноз Северных сияний (Aurora)",
                subtitle = "Геомагнитная активность и Kp-индекс",
                icon = Icons.Default.WbTwilight
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Планетарный Kp-индекс:",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${aurora.kpIndex}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (palette.isRedMode) palette.primary else Color(0xFF34D399)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "/ 9.0 (${aurora.stormLevel})",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (aurora.observationChanceAtUserLat > 40) Color(0x3310B981) else palette.surface,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${aurora.observationChanceAtUserLat}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (aurora.observationChanceAtUserLat > 40) Color(0xFF34D399) else palette.textPrimary
                        )
                        Text(
                            text = "Шанс здесь",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = aurora.stormDescriptionRu,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = palette.cardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Граница видимости овала: от ${aurora.visibilityLatitudeThreshold}° N (Ваша широта: ${String.format("%.1f", location.latitude)}° N)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Параметры съёмки сияния:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                    Text(
                        text = aurora.recommendedSettings,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CloudLayerBar(
    title: String,
    percent: Int,
    hint: String,
    palette: com.example.astro.ui.theme.AstroPalette
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textPrimary
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (percent > 40) Color(0xFFEF4444) else palette.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (palette.isRedMode) palette.primary else if (percent > 40) Color(0xFFEF4444) else palette.primary,
            trackColor = palette.surface
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
