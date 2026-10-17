package com.example.astro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astro.model.ObservationLocation
import com.example.astro.model.WeatherComparison
import com.example.astro.model.WeatherMapLayer
import com.example.astro.model.WeatherMapProvider
import com.example.astro.ui.theme.LocalAstroPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherMapViewer(
    currentLocation: ObservationLocation,
    selectedLayer: WeatherMapLayer,
    selectedProvider: WeatherMapProvider,
    openWeatherApiKey: String,
    isAggregationMode: Boolean,
    weatherComparison: WeatherComparison,
    onSelectLayer: (WeatherMapLayer) -> Unit,
    onSelectProvider: (WeatherMapProvider) -> Unit,
    onToggleAggregation: () -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    val palette = LocalAstroPalette.current

    var zoom by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().testTag("weather_map_viewer")) {
        // Map Top Bar: Layer Chips + Provider Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Интерактивная карта неба",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Key settings button
                IconButton(
                    onClick = { showApiKeyDialog = true },
                    modifier = Modifier.size(34.dp).testTag("api_key_dialog_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "API Ключи",
                        tint = if (openWeatherApiKey.isNotBlank()) palette.accent else palette.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset zoom button
                IconButton(
                    onClick = {
                        zoom = 1.0f
                        offset = Offset.Zero
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterCenterFocus,
                        contentDescription = "Центрировать",
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Layer selector chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WeatherMapLayer.values().forEach { layer ->
                FilterChip(
                    selected = selectedLayer == layer,
                    onClick = { onSelectLayer(layer) },
                    label = { Text(layer.labelRu, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Provider switcher and comparison mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WeatherMapProvider.values().forEach { provider ->
                    val isSelected = selectedProvider == provider && !isAggregationMode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) palette.primary else palette.surface,
                        border = BorderStroke(0.5.dp, palette.cardBorder),
                        onClick = { onSelectProvider(provider) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = provider.titleRu,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else palette.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Aggregation button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isAggregationMode) palette.accent else palette.surface,
                border = BorderStroke(0.5.dp, palette.cardBorder),
                onClick = onToggleAggregation
            ) {
                Text(
                    text = "Сравнение источников",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAggregationMode) Color.Black else palette.textMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Map View Surface with Pinch-to-Zoom & Pan Gesture
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0A0F1D),
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(0.7f, 4.0f)
                        offset += pan
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f + offset.x
                    val centerY = size.height / 2f + offset.y

                    // Map grid lines
                    val gridSpacing = 40.dp.toPx() * zoom
                    val numVerticals = (size.width / gridSpacing).toInt() + 4
                    val numHorizontals = (size.height / gridSpacing).toInt() + 4

                    for (i in -numVerticals..numVerticals) {
                        val x = centerX + i * gridSpacing
                        drawLine(
                            color = Color(0x1538BDF8),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (j in -numHorizontals..numHorizontals) {
                        val y = centerY + j * gridSpacing
                        drawLine(
                            color = Color(0x1538BDF8),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Simulated radar cloud and precipitation fronts
                    val cloudColor = when (selectedLayer) {
                        WeatherMapLayer.CLOUDS -> Color(0x55FFFFFF)
                        WeatherMapLayer.RADAR_PRECIPITATION -> Color(0x6610B981)
                        WeatherMapLayer.WIND -> Color(0x5538BDF8)
                        WeatherMapLayer.TEMPERATURE -> Color(0x44F59E0B)
                    }

                    // Render weather front wave polygons
                    val path = Path()
                    val waveWidth = size.width * zoom
                    val waveHeight = size.height * zoom
                    path.moveTo(centerX - waveWidth * 0.4f, centerY - waveHeight * 0.3f)
                    path.cubicTo(
                        centerX - waveWidth * 0.1f, centerY - waveHeight * 0.5f,
                        centerX + waveWidth * 0.2f, centerY - waveHeight * 0.1f,
                        centerX + waveWidth * 0.4f, centerY - waveHeight * 0.2f
                    )
                    path.cubicTo(
                        centerX + waveWidth * 0.5f, centerY + waveHeight * 0.2f,
                        centerX + waveWidth * 0.1f, centerY + waveHeight * 0.3f,
                        centerX - waveWidth * 0.3f, centerY + waveHeight * 0.2f
                    )
                    path.close()

                    drawPath(path = path, color = cloudColor)

                    // Dense precipitation core in radar mode
                    if (selectedLayer == WeatherMapLayer.RADAR_PRECIPITATION) {
                        drawCircle(
                            color = Color(0x66F59E0B),
                            radius = 28.dp.toPx() * zoom,
                            center = Offset(centerX + 30.dp.toPx() * zoom, centerY - 20.dp.toPx() * zoom)
                        )
                        drawCircle(
                            color = Color(0x77EF4444),
                            radius = 14.dp.toPx() * zoom,
                            center = Offset(centerX + 30.dp.toPx() * zoom, centerY - 20.dp.toPx() * zoom)
                        )
                    }

                    // Wind vectors arrows
                    if (selectedLayer == WeatherMapLayer.WIND) {
                        for (k in -2..2) {
                            val startX = centerX + k * 50.dp.toPx() * zoom
                            val startY = centerY + k * 35.dp.toPx() * zoom
                            drawLine(
                                color = Color(0x9938BDF8),
                                start = Offset(startX, startY),
                                end = Offset(startX + 25.dp.toPx() * zoom, startY - 15.dp.toPx() * zoom),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    // Center Target Marker (Observation Location)
                    drawCircle(
                        color = Color(0xFFFF2E2E),
                        radius = 6.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color(0x44FF2E2E),
                        radius = 16.dp.toPx() * zoom,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Map Overlay Info Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "${currentLocation.name}: ${selectedLayer.labelRu}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Источник: ${if (isAggregationMode) "Агрегатор (Open-Meteo + OWM)" else selectedProvider.titleRu}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.primary,
                            fontSize = 9.sp
                        )
                    }
                }

                // Zoom control overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Зум: ${String.format("%.1f", zoom)}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Cross-validation and source comparison verdict
        if (isAggregationMode || openWeatherApiKey.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Open-Meteo: ${weatherComparison.openMeteoCloudCover}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.primary
                        )
                        if (weatherComparison.openWeatherCloudCover != null) {
                            Text(
                                text = "OpenWeather: ${weatherComparison.openWeatherCloudCover}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accent
                            )
                        }
                        Text(
                            text = "Радар: ${weatherComparison.rainViewerIntensity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Итог: ${weatherComparison.verdictRu} (Средняя облачность: ${weatherComparison.averageCloudCover}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeySetupDialog(
            currentKey = openWeatherApiKey,
            onSave = {
                onSaveApiKey(it)
                showApiKeyDialog = false
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }
}

@Composable
private fun ApiKeySetupDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalAstroPalette.current
    var keyInput by remember { mutableStateOf(currentKey) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.cardBg,
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier.fillMaxWidth().testTag("api_key_modal")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Подключение API карт",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = palette.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "По умолчанию приложение работает бесплатно через Open-Meteo и RainViewer без всяких ключей. Чтобы подключить персональные тайлы OpenWeatherMap:",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Как получить бесплатный ключ OpenWeatherMap:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = palette.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1. Откройте сайт openweathermap.org в браузере\n2. Нажмите «Sign Up» (регистрация бесплатна)\n3. В личном кабинете откройте вкладку «API Keys»\n4. Скопируйте 32-значный ключ и вставьте ниже", style = MaterialTheme.typography.bodySmall, color = palette.textMuted, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("OpenWeatherMap API Key") },
                    placeholder = { Text("например: e8a2...3b1f") },
                    modifier = Modifier.fillMaxWidth().testTag("owm_key_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = { onSave(keyInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                        modifier = Modifier.weight(1f).testTag("save_key_button")
                    ) {
                        Text("Сохранить", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
