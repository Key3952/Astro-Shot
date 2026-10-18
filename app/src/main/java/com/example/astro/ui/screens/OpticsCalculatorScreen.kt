package com.example.astro.ui.screens

import androidx.compose.foundation.BorderStroke
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

    val fovResult by viewModel.fovResult.collectAsState()
    val hyperfocalResult by viewModel.hyperfocalResult.collectAsState()
    val subjectDistance by viewModel.subjectDistanceMeters.collectAsState()

    // 0: NPF & 500, 1: Таймлапс, 2: Гиперфокал & ГРИП, 3: Поле зрения FOV
    var calculatorTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("optics_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = calculatorTab == 0,
                onClick = { calculatorTab = 0 },
                label = { Text("NPF & 500") },
                modifier = Modifier.testTag("calc_tab_npf")
            )
            FilterChip(
                selected = calculatorTab == 1,
                onClick = { calculatorTab = 1 },
                label = { Text("Таймлапс") },
                modifier = Modifier.testTag("calc_tab_timelapse")
            )
            FilterChip(
                selected = calculatorTab == 2,
                onClick = { calculatorTab = 2 },
                label = { Text("Гиперфокал & ГРИП") },
                modifier = Modifier.testTag("calc_tab_hyperfocal")
            )
            FilterChip(
                selected = calculatorTab == 3,
                onClick = { calculatorTab = 3 },
                label = { Text("Поле зрения (FOV)") },
                modifier = Modifier.testTag("calc_tab_fov")
            )
        }

        // Shared Sensor and Optics setup card
        AstroCard(modifier = Modifier.fillMaxWidth().testTag("sensor_optics_setup_card")) {
            SectionHeader(
                title = "Камера: ${selectedSensor.name}",
                subtitle = "Фокусное: ${focalLength.toInt()} мм • f/$aperture • Кроп: ${selectedSensor.cropFactor}x",
                icon = Icons.Default.CameraAlt
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sensor preset chips
            Text(
                text = "Размер сенсора:",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CameraSensor.PRESETS.forEach { sensor ->
                    val isSelected = sensor.id == selectedSensor.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateSensor(sensor) },
                        label = { Text(sensor.name, fontSize = 11.sp) },
                        modifier = Modifier.testTag("sensor_chip_${sensor.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick focal length buttons
            Text(
                text = "Фокусное расстояние: ${focalLength.toInt()} мм",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
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
                        label = { Text("${f.toInt()}mm", fontSize = 11.sp) },
                        border = if (focalLength.roundToInt() == f.roundToInt()) {
                            BorderStroke(1.dp, palette.primary)
                        } else null
                    )
                }
            }

            Slider(
                value = focalLength,
                onValueChange = { viewModel.updateFocalLength(it) },
                valueRange = 10f..400f,
                modifier = Modifier.fillMaxWidth().testTag("focal_slider")
            )

            // Aperture selection
            Text(
                text = "Диафрагма: f/$aperture",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            val aperturePresets = listOf(1.4f, 1.8f, 2.0f, 2.8f, 4.0f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                aperturePresets.forEach { a ->
                    FilterChip(
                        selected = aperture == a,
                        onClick = { viewModel.updateAperture(a) },
                        label = { Text("f/$a", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        when (calculatorTab) {
            0 -> {
                // NPF & 500 Rule Screen
                AstroCard(
                    modifier = Modifier.fillMaxWidth().testTag("npf_result_card")
                ) {
                    Text(
                        text = "Точная выдержка без смазывания звёзд (NPF-правило)",
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exposureResult.starTrailWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = palette.cardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

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

                // Sensor MP and Declination tuning
                AstroCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Тонкая настройка NPF: пиксель и склонение",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Разрешение матрицы: ${megapixels.toInt()} MP (размер пикселя: ${String.format("%.2f", selectedSensor.pixelPitchMicrons(megapixels))} мкм)",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Slider(
                        value = megapixels,
                        onValueChange = { viewModel.updateMegapixels(it) },
                        valueRange = 12f..100f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Склонение объекта (Dec): ${declination.toInt()}° (0° = небесный экватор, 89° = Полярная звезда)",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Slider(
                        value = declination,
                        onValueChange = { viewModel.updateDeclination(it) },
                        valueRange = 0f..85f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            1 -> {
                // Timelapse and Intervalometer Screen
                AstroCard(
                    modifier = Modifier.fillMaxWidth().testTag("timelapse_result_card")
                ) {
                    Text(
                        text = "Расчёт интервалометра для таймлапса",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

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
                            label = "Время съёмки",
                            value = "${timelapseResult.shootingDurationMinutes}",
                            unit = " мин",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Интервал",
                            value = "${timelapseResult.recommendedIntervalSeconds.toInt()}",
                            unit = " с",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Длина видео",
                            value = "${timelapseClipSec.toInt()}",
                            unit = " с",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Объём RAW",
                            value = "${(timelapseResult.estimatedStorageMb / 1024f * 10f).roundToInt() / 10f}",
                            unit = " ГБ",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = palette.cardBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Желаемая длительность ролика: ${timelapseClipSec.toInt()} с",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary
                    )
                    Slider(
                        value = timelapseClipSec,
                        onValueChange = { viewModel.updateTimelapseClipSeconds(it) },
                        valueRange = 5f..60f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Частота кадров (FPS готового видео): $timelapseFps fps",
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
                                label = { Text("$fps fps") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            2 -> {
                // Hyperfocal & Depth of Field (DoF)
                AstroCard(
                    modifier = Modifier.fillMaxWidth().testTag("hyperfocal_result_card")
                ) {
                    Text(
                        text = "Гиперфокальное расстояние и ГРИП",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Для ночных пейзажей: резкий передний план и бесконечно резкие звёзды",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Гиперфокал (H)",
                            value = "${hyperfocalResult.hyperfocalMeters} м",
                            statusColor = palette.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Ближняя граница",
                            value = "${hyperfocalResult.nearLimitMeters} м",
                            modifier = Modifier.weight(1f)
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
                                text = "Зона резкости: ${hyperfocalResult.depthOfFieldDescription}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Дальняя граница: ${hyperfocalResult.farLimitDescription}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 Правило: если сфокусироваться на расстоянии ${hyperfocalResult.hyperfocalMeters} м, всё от ${hyperfocalResult.nearLimitMeters} м до звёзд на бесконечности будет резким!",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Дистанция до объекта переднего плана: ${String.format("%.1f", subjectDistance)} м",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textPrimary
                    )
                    Slider(
                        value = subjectDistance,
                        onValueChange = { viewModel.updateSubjectDistance(it) },
                        valueRange = 0.5f..30f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            3 -> {
                // Field of View (FOV) Simulator
                AstroCard(
                    modifier = Modifier.fillMaxWidth().testTag("fov_result_card")
                ) {
                    Text(
                        text = "Поле зрения матрицы и оптики (FOV)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Эквивалент 35мм: ${fovResult.equivalent35mmFocalMm.toInt()} мм на сенсоре ${selectedSensor.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "По горизонт.",
                            value = "${fovResult.horizontalFovDeg}°",
                            statusColor = palette.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "По вертик.",
                            value = "${fovResult.verticalFovDeg}°",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "По диагонали",
                            value = "${fovResult.diagonalFovDeg}°",
                            modifier = Modifier.weight(1f)
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
                                text = fovResult.skyCoverageDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📐 Справка угловых размеров неба: Луна — 0.5°, ковш Большой Медведицы — 25°, пояс Ориона — 3°, Галактика Андромеды — 3°.",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
