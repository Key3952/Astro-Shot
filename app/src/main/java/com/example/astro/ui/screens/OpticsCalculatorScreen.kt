package com.example.astro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.model.CameraSensor
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.components.AstroCard
import com.example.astro.ui.components.MetricBadge
import com.example.astro.ui.components.SectionHeader
import com.example.astro.ui.theme.LocalAstroPalette
import kotlin.math.roundToInt

@Composable
fun OpticsCalculatorScreen(
    viewModel: AstroViewModel
) {
    val palette = LocalAstroPalette.current
    val scrollState = rememberScrollState()

    val focalLength by viewModel.focalLengthMm.collectAsState()
    val aperture by viewModel.aperture.collectAsState()
    val selectedSensor by viewModel.selectedSensor.collectAsState()
    val megapixels by viewModel.megapixels.collectAsState()
    val declination by viewModel.declination.collectAsState()
    val exposureResult by viewModel.exposureResult.collectAsState()

    val timelapseClipSec by viewModel.timelapseClipSeconds.collectAsState()
    val timelapseFps by viewModel.timelapseFps.collectAsState()
    val timelapseResult by viewModel.timelapseResult.collectAsState()

    var showTimelapseTool by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("optics_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Switcher: Single Shot vs Timelapse
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showTimelapseTool,
                onClick = { showTimelapseTool = false },
                label = { Text("Одиночный кадр (NPF / 500)") },
                modifier = Modifier.weight(1f).testTag("tab_single_shot")
            )
            FilterChip(
                selected = showTimelapseTool,
                onClick = { showTimelapseTool = true },
                label = { Text("Таймлапс и треки звёзд") },
                modifier = Modifier.weight(1f).testTag("tab_timelapse")
            )
        }

        if (!showTimelapseTool) {
            // Main Exposure Output Card
            AstroCard(
                modifier = Modifier.fillMaxWidth().testTag("npf_result_card")
            ) {
                Text(
                    text = "Рекомендуемая выдержка (Правило NPF)",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${exposureResult.npfRuleSeconds}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "секунд",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = exposureResult.starTrailWarning,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = palette.cardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Comparative formulas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadge(
                        label = "Правило 500",
                        value = "${exposureResult.rule500Seconds} с",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "Правило 400",
                        value = "${exposureResult.rule400Seconds} с",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "ISO диап.",
                        value = "${exposureResult.recommendedIsoMin}-${exposureResult.recommendedIsoMax}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sensor Presets
            AstroCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "Размер матрицы (Сенсор)",
                    subtitle = "Кроп-фактор: ${selectedSensor.cropFactor}x • Пиксель: ${String.format("%.2f", selectedSensor.pixelPitchMicrons(megapixels))} мкм",
                    icon = Icons.Default.AspectRatio
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CameraSensor.PRESETS.forEach { sensor ->
                        val isSelected = sensor.id == selectedSensor.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSensor(sensor) },
                            label = { Text(sensor.name, fontSize = 12.sp) },
                            modifier = Modifier.testTag("sensor_chip_${sensor.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Разрешение матрицы: ${megapixels.toInt()} MP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                Slider(
                    value = megapixels,
                    onValueChange = { viewModel.updateMegapixels(it) },
                    valueRange = 12f..100f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth().testTag("megapixels_slider")
                )
            }

            // Lens & Focal Length Controls
            AstroCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "Оптика и фокусное расстояние",
                    subtitle = "Текущее значение: ${focalLength.toInt()} мм",
                    icon = Icons.Default.CameraAlt
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick focal presets
                val focalPresets = listOf(14f, 20f, 24f, 35f, 50f, 85f, 135f, 200f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    focalPresets.forEach { f ->
                        AssistChip(
                            onClick = { viewModel.updateFocalLength(f) },
                            label = { Text("${f.toInt()}mm") },
                            border = if (focalLength.roundToInt() == f.roundToInt()) {
                                BorderStroke(1.dp, palette.primary)
                            } else null
                        )
                    }
                }

                Slider(
                    value = focalLength,
                    onValueChange = { viewModel.updateFocalLength(it) },
                    valueRange = 10f..300f,
                    modifier = Modifier.fillMaxWidth().testTag("focal_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Aperture selector
                Text(
                    text = "Диафрагма: f/${aperture}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                val aperturePresets = listOf(1.4f, 1.8f, 2.0f, 2.8f, 4.0f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aperturePresets.forEach { a ->
                        FilterChip(
                            selected = aperture == a,
                            onClick = { viewModel.updateAperture(a) },
                            label = { Text("f/$a") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Hyperfocal Distance Guide
            AstroCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "Гиперфокальное расстояние",
                    subtitle = "Для резкости звёзд и переднего плана пейзажа",
                    icon = Icons.Default.Adjust
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Установите фокус на кольце:",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                        Text(
                            text = "${exposureResult.hyperfocalDistanceMeters} метров",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.accent
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.surface
                    ) {
                        Text(
                            text = "Резко от ${String.format("%.2f", exposureResult.hyperfocalDistanceMeters / 2f)}м до ∞",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Declination of celestial target
            AstroCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "Склонение объекта (Declination)",
                    subtitle = "Ближе к Полярной звезде (+89°) звёзды движутся медленнее",
                    icon = Icons.Default.Explore
                )
                Slider(
                    value = declination,
                    onValueChange = { viewModel.updateDeclination(it) },
                    valueRange = 0f..89f,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Небесный экватор: 0°", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Text("Текущее: ${declination.toInt()}°", style = MaterialTheme.typography.labelMedium, color = palette.primary)
                    Text("Полярная: 89°", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                }
            }
        } else {
            // TIMELAPSE & STAR TRAILS CALCULATOR
            AstroCard(modifier = Modifier.fillMaxWidth().testTag("timelapse_result_card")) {
                SectionHeader(
                    title = "Калькулятор таймлапса и Star Trails",
                    subtitle = "Расчет интервалометра, времени съёмки и карты памяти",
                    icon = Icons.Default.Timelapse
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadge(
                        label = "Всего кадров",
                        value = "${timelapseResult.totalPhotos}",
                        statusColor = palette.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "Время на съёмку",
                        value = "${timelapseResult.shootingDurationMinutes} мин",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "Объем RAW",
                        value = "${(timelapseResult.estimatedStorageMb / 1024f).let { String.format("%.1f", it) }} ГБ",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = palette.cardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Рекомендуемый интервал съемки: ${timelapseResult.recommendedIntervalSeconds.toInt()} сек",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.accent
                )
                Text(
                    text = "Включает выдержку ${exposureResult.npfRuleSeconds} сек + 3 сек буфера для сохранения на карту и охлаждения сенсора.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            AstroCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Желаемая длительность готового видео: ${timelapseClipSec.toInt()} сек",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                Slider(
                    value = timelapseClipSec,
                    onValueChange = { viewModel.updateTimelapseClipSeconds(it) },
                    valueRange = 5f..60f,
                    steps = 11,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Частота кадров (FPS): $timelapseFps fps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(24, 30, 60).forEach { fps ->
                        FilterChip(
                            selected = timelapseFps == fps,
                            onClick = { viewModel.updateTimelapseFps(fps) },
                            label = { Text("$fps FPS") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Star trails advice
            AstroCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "Советы по съёмке Star Trails (Звёздные следы)",
                    subtitle = "Стек снимков в StarStaX или Photoshop",
                    icon = Icons.Default.AutoAwesome
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Направьте камеру на Полярную звезду (север), чтобы получить концентрические круги, или на восток/запад для диагональных дуг.\n• Снимайте непрерывную серию кадров с минимальной паузой (1 сек между кадрами).\n• Не используйте одну сверхдлинную выдержку на 2 часа — сенсор перегреется и зальет кадр тепловым шумом. Снимайте 100-300 кадров по 20-30 секунд.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
