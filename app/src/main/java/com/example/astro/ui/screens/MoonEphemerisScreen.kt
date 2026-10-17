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
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.components.AstroCard
import com.example.astro.ui.components.MetricBadge
import com.example.astro.ui.components.MoonPhaseCanvas
import com.example.astro.ui.components.SectionHeader
import com.example.astro.ui.theme.LocalAstroPalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MoonEphemerisScreen(
    viewModel: AstroViewModel
) {
    val palette = LocalAstroPalette.current
    val scrollState = rememberScrollState()

    val moon by viewModel.moonDetails.collectAsState()
    val twilight by viewModel.twilightDetails.collectAsState()
    val moonCalendar by viewModel.moonCalendar.collectAsState()
    val location by viewModel.selectedLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("moon_ephemeris_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Moon Disc Card
        AstroCard(modifier = Modifier.fillMaxWidth().testTag("moon_hero_card")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoonPhaseCanvas(
                    illuminationPercent = moon.illuminationPercent,
                    ageDays = moon.ageDays,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = moon.phaseType.titleRu,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Освещенность: ${moon.illuminationPercent}% • Возраст: ${moon.ageDays} дн",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = moon.astroSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = palette.cardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBadge(
                    label = "Восход Луны",
                    value = moon.moonriseTime,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Заход Луны",
                    value = moon.moonsetTime,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "Статус для DeepSky",
                    value = if (moon.isGoodForAstro) "Отлично" else "Засветка",
                    statusColor = if (moon.isGoodForAstro) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Twilight and Solar Ephemeris
        AstroCard(modifier = Modifier.fillMaxWidth().testTag("twilight_card")) {
            SectionHeader(
                title = "Эфемериды и сумерки",
                subtitle = "Локация: ${location.name}",
                icon = Icons.Default.WbSunny
            )
            Spacer(modifier = Modifier.height(10.dp))

            TwilightRowItem(
                title = "Золотой час (Golden Hour)",
                timeRange = "${twilight.goldenHourStart} – ${twilight.goldenHourEnd}",
                description = "Мягкий золотистый свет на горах и пейзаже перед закатом",
                color = Color(0xFFFBBF24),
                palette = palette
            )
            Spacer(modifier = Modifier.height(8.dp))
            TwilightRowItem(
                title = "Синий час (Blue Hour)",
                timeRange = "${twilight.blueHourStart} – ${twilight.blueHourEnd}",
                description = "Глубокое синее небо, подсветка переднего плана",
                color = Color(0xFF38BDF8),
                palette = palette
            )
            Spacer(modifier = Modifier.height(8.dp))
            TwilightRowItem(
                title = "Астрономическая темнота",
                timeRange = "${twilight.astroTwilightStart} – ${twilight.astroTwilightEnd}",
                description = "Полное отсутствие солнечного света (${twilight.darkSkyDurationHours} ч)",
                color = if (palette.isRedMode) palette.primary else Color(0xFFA855F7),
                palette = palette
            )
            Spacer(modifier = Modifier.height(8.dp))
            TwilightRowItem(
                title = "Восход / Заход Солнца",
                timeRange = "${twilight.sunriseTime} / ${twilight.sunsetTime}",
                description = "Официальное время появления и захода диска за горизонт",
                color = palette.textMuted,
                palette = palette
            )
        }

        // 30-Day Lunar Calendar
        AstroCard(modifier = Modifier.fillMaxWidth().testTag("moon_calendar_card")) {
            SectionHeader(
                title = "Лунный календарь на месяц",
                subtitle = "Ищите дни Новолуния (0-10%) для съемки Млечного Пути",
                icon = Icons.Default.CalendarMonth
            )
            Spacer(modifier = Modifier.height(12.dp))

            val dateFormat = SimpleDateFormat("d MMM", Locale("ru"))
            val dayNameFormat = SimpleDateFormat("E", Locale("ru"))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moonCalendar.forEach { (cal, dayMoon) ->
                    val isNewMoon = dayMoon.illuminationPercent <= 10
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNewMoon) {
                            if (palette.isRedMode) Color(0x33FF3333) else Color(0x2238BDF8)
                        } else palette.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isNewMoon) palette.primary else palette.cardBorder
                        ),
                        modifier = Modifier.width(74.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayNameFormat.format(cal.time),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = dateFormat.format(cal.time),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            MoonPhaseCanvas(
                                illuminationPercent = dayMoon.illuminationPercent,
                                ageDays = dayMoon.ageDays,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dayMoon.illuminationPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isNewMoon) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNewMoon) palette.primary else palette.textSecondary,
                                fontSize = 10.sp
                            )
                            if (isNewMoon) {
                                Text(
                                    text = "★ Темно",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.accent,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TwilightRowItem(
    title: String,
    timeRange: String,
    description: String,
    color: Color,
    palette: com.example.astro.ui.theme.AstroPalette
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = palette.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
            }
        }
    }
}
