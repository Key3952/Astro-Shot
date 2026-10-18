package com.example.astro.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.astro.model.*
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.components.AstroCard
import com.example.astro.ui.components.SectionHeader
import com.example.astro.ui.theme.AstroPalette
import com.example.astro.ui.theme.LocalAstroPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AstroViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val palette = LocalAstroPalette.current
    val scrollState = rememberScrollState()

    val apiKeys by viewModel.apiKeysConfig.collectAsState()
    val weatherSource by viewModel.selectedWeatherSource.collectAsState()
    val spaceSource by viewModel.selectedSpaceSource.collectAsState()
    val unitSettings by viewModel.unitSettings.collectAsState()
    val pingResults by viewModel.pingResults.collectAsState()
    val isPinging by viewModel.isPinging.collectAsState()
    val apodData by viewModel.nasaApod.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }

    // Local state for editable keys
    var owmInput by remember(apiKeys) { mutableStateOf(apiKeys.openWeatherKey) }
    var wapiInput by remember(apiKeys) { mutableStateOf(apiKeys.weatherApiKey) }
    var nasaInput by remember(apiKeys) { mutableStateOf(apiKeys.nasaApiKey) }
    var tomorrowInput by remember(apiKeys) { mutableStateOf(apiKeys.tomorrowKey) }
    var tileUrlInput by remember(apiKeys) { mutableStateOf(apiKeys.customTileUrl) }
    var useNasaDemo by remember(apiKeys) { mutableStateOf(apiKeys.useNasaDemoKey) }

    // Password visibility toggles
    var showOwmKey by remember { mutableStateOf(false) }
    var showWapiKey by remember { mutableStateOf(false) }
    var showNasaKey by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .testTag("settings_screen")
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Настройки и API",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Управление внешними источниками данных",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted,
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = palette.primary
                    )
                }
            },
            actions = {
                Button(
                    onClick = {
                        viewModel.saveApiKeys(
                            owmKey = owmInput,
                            wapiKey = wapiInput,
                            nasaKey = nasaInput,
                            tomorrowKey = tomorrowInput,
                            tileUrl = tileUrlInput,
                            useDemo = useNasaDemo
                        )
                        Toast.makeText(context, "Ключи и настройки успешно сохранены!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.padding(end = 8.dp).testTag("save_all_settings_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Сохранить", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.background)
        )

        // Settings Tabs row
        val tabs = listOf("Ключи API", "Источники", "Параметры", "Диагностика")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { activeTab = index },
                    label = { Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.primary,
                        selectedLabelColor = Color.Black,
                        containerColor = palette.surface,
                        labelColor = palette.textSecondary
                    ),
                    border = BorderStroke(0.5.dp, if (isSelected) palette.primary else palette.cardBorder),
                    modifier = Modifier.testTag("settings_tab_$index")
                )
            }
        }

        // Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // API Keys tab
                    ApiKeysTabContent(
                        palette = palette,
                        owmInput = owmInput,
                        onOwmChange = { owmInput = it },
                        showOwmKey = showOwmKey,
                        onToggleShowOwm = { showOwmKey = !showOwmKey },
                        wapiInput = wapiInput,
                        onWapiChange = { wapiInput = it },
                        showWapiKey = showWapiKey,
                        onToggleShowWapi = { showWapiKey = !showWapiKey },
                        nasaInput = nasaInput,
                        onNasaChange = { nasaInput = it },
                        showNasaKey = showNasaKey,
                        onToggleShowNasa = { showNasaKey = !showNasaKey },
                        useNasaDemo = useNasaDemo,
                        onToggleUseNasaDemo = { useNasaDemo = it },
                        tomorrowInput = tomorrowInput,
                        onTomorrowChange = { tomorrowInput = it },
                        tileUrlInput = tileUrlInput,
                        onTileUrlChange = { tileUrlInput = it },
                        onRunDiagnostic = {
                            viewModel.saveApiKeys(owmInput, wapiInput, nasaInput, tomorrowInput, tileUrlInput, useNasaDemo)
                            viewModel.runDataSourcesDiagnostic()
                            activeTab = 3
                        }
                    )
                }
                1 -> {
                    // Data sources configuration
                    DataSourcesTabContent(
                        palette = palette,
                        weatherSource = weatherSource,
                        onSelectWeatherSource = { viewModel.setWeatherDataSource(it) },
                        spaceSource = spaceSource,
                        onSelectSpaceSource = { viewModel.setSpaceWeatherSource(it) },
                        apodData = apodData,
                        onReloadApod = { viewModel.fetchNasaApod() }
                    )
                }
                2 -> {
                    // Unit settings and preferences
                    UnitsSettingsTabContent(
                        palette = palette,
                        unitSettings = unitSettings,
                        onUpdateUnits = { viewModel.setUnitSettings(it) }
                    )
                }
                3 -> {
                    // Diagnostics & ping tab
                    DiagnosticsTabContent(
                        palette = palette,
                        isPinging = isPinging,
                        pingResults = pingResults,
                        onRunDiagnostic = { viewModel.runDataSourcesDiagnostic() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ApiKeysTabContent(
    palette: AstroPalette,
    owmInput: String,
    onOwmChange: (String) -> Unit,
    showOwmKey: Boolean,
    onToggleShowOwm: () -> Unit,
    wapiInput: String,
    onWapiChange: (String) -> Unit,
    showWapiKey: Boolean,
    onToggleShowWapi: () -> Unit,
    nasaInput: String,
    onNasaChange: (String) -> Unit,
    showNasaKey: Boolean,
    onToggleShowNasa: () -> Unit,
    useNasaDemo: Boolean,
    onToggleUseNasaDemo: (Boolean) -> Unit,
    tomorrowInput: String,
    onTomorrowChange: (String) -> Unit,
    tileUrlInput: String,
    onTileUrlChange: (String) -> Unit,
    onRunDiagnostic: () -> Unit
) {
    // OpenWeatherMap Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("owm_key_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("OpenWeatherMap API", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Text("Погода, облачность и радарные слои", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
            }
            StatusPill(
                isActive = owmInput.isNotBlank(),
                activeText = "Ключ введен",
                inactiveText = "Не задан",
                palette = palette
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = owmInput,
            onValueChange = onOwmChange,
            label = { Text("OpenWeatherMap API Key (32 знака)") },
            placeholder = { Text("например: e8a2b5c7...3f1d") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("owm_key_input_field"),
            visualTransformation = if (showOwmKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleShowOwm) {
                    Icon(
                        imageVector = if (showOwmKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Показать/скрыть",
                        tint = palette.textMuted
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Инструкция: Зарегистрируйтесь на openweathermap.org -> Раздел «API Keys» -> Скопируйте ключ (активация занимает 10-15 минут).",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted,
            fontSize = 11.sp
        )
    }

    // WeatherAPI.com Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("weatherapi_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("WeatherAPI.com", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Text("Астрономический прогноз, прозрачность и луна", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
            }
            StatusPill(
                isActive = wapiInput.isNotBlank(),
                activeText = "Ключ введен",
                inactiveText = "Не задан",
                palette = palette
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = wapiInput,
            onValueChange = onWapiChange,
            label = { Text("WeatherAPI Key") },
            placeholder = { Text("например: 9283f6a...110c") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("weather_api_key_field"),
            visualTransformation = if (showWapiKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleShowWapi) {
                    Icon(
                        imageVector = if (showWapiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Показать/скрыть",
                        tint = palette.textMuted
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Инструкция: Получите бесплатный ключ на weatherapi.com для астрономических эфемерид и прогноза прозрачности атмосферы.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted,
            fontSize = 11.sp
        )
    }

    // NASA Open API Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("nasa_key_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("NASA Open APIs", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Text("Астрономическое фото дня (APOD) и солнечные вспышки", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
            }
            StatusPill(
                isActive = useNasaDemo || nasaInput.isNotBlank(),
                activeText = if (nasaInput.isNotBlank()) "Личный ключ" else "DEMO_KEY",
                inactiveText = "Отключено",
                palette = palette
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Использовать ключ по умолчанию (DEMO_KEY)",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textPrimary
            )
            Switch(
                checked = useNasaDemo,
                onCheckedChange = onToggleUseNasaDemo,
                colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
            )
        }
        if (!useNasaDemo) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nasaInput,
                onValueChange = onNasaChange,
                label = { Text("Персональный NASA API Key") },
                placeholder = { Text("вставьте ключ с api.nasa.gov") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("nasa_key_input_field"),
                visualTransformation = if (showNasaKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleShowNasa) {
                        Icon(
                            imageVector = if (showNasaKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Показать/скрыть",
                            tint = palette.textMuted
                        )
                    }
                }
            )
        }
    }

    // Tomorrow.io Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("tomorrow_key_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tomorrow.io (ClimaCell)", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Text("Высота нижней кромки облаков и видимость", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
            }
            StatusPill(
                isActive = tomorrowInput.isNotBlank(),
                activeText = "Ключ введен",
                inactiveText = "Не задан",
                palette = palette
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = tomorrowInput,
            onValueChange = onTomorrowChange,
            label = { Text("Tomorrow.io API Key (Необязательно)") },
            placeholder = { Text("например: 4k8d...91j2") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("tomorrow_key_field")
        )
    }

    // Custom Light Pollution Tiles
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("custom_tiles_card")) {
        Text("Кастомная карта засветки (Bortle / Tiles)", fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Text("Пользовательский шаблон XYZ тайлового сервера светового загрязнения", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = tileUrlInput,
            onValueChange = onTileUrlChange,
            label = { Text("URL шаблона тайлов") },
            placeholder = { Text("https://darksitefinder.com/tiles/{z}/{x}/{y}.png") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_tiles_field")
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick = { onTileUrlChange("https://darksitefinder.com/maps/tiles/{z}/{x}/{y}.png") },
                label = { Text("DarkSiteFinder", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = { onTileUrlChange("") },
                label = { Text("Сброс (VIIRS)", fontSize = 11.sp) }
            )
        }
    }

    // Diagnostic trigger button
    Button(
        onClick = onRunDiagnostic,
        colors = ButtonDefaults.buttonColors(containerColor = palette.surface),
        border = BorderStroke(1.dp, palette.primary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("check_all_keys_button")
    ) {
        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = palette.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Проверить доступность всех источников", color = palette.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DataSourcesTabContent(
    palette: AstroPalette,
    weatherSource: WeatherDataSource,
    onSelectWeatherSource: (WeatherDataSource) -> Unit,
    spaceSource: SpaceWeatherSource,
    onSelectSpaceSource: (SpaceWeatherSource) -> Unit,
    apodData: NasaApodData?,
    onReloadApod: () -> Unit
) {
    // Primary Weather Provider Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("weather_source_card")) {
        SectionHeader(
            title = "Основной источник метеоданных",
            subtitle = "Алгоритм прогноза облачности для планирования ночи",
            icon = Icons.Default.CloudQueue
        )
        Spacer(modifier = Modifier.height(12.dp))

        WeatherDataSource.entries.forEach { src ->
            val isSelected = weatherSource == src
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) palette.primary.copy(alpha = 0.15f) else palette.surface,
                border = BorderStroke(1.dp, if (isSelected) palette.primary else palette.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectWeatherSource(src) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectWeatherSource(src) },
                        colors = RadioButtonDefaults.colors(selectedColor = palette.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = src.titleRu,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = src.subtitleRu,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    if (src.requiresKey) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x33FFB300),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "Ключ API",
                                color = Color(0xFFFFB300),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Space Weather Provider Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("space_source_card")) {
        SectionHeader(
            title = "Геомагнетизм и Полярные сияния",
            subtitle = "Источник Kp-индекса и прогноза видимости сияний",
            icon = Icons.Default.Storm
        )
        Spacer(modifier = Modifier.height(12.dp))

        SpaceWeatherSource.entries.forEach { src ->
            val isSelected = spaceSource == src
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) palette.primary.copy(alpha = 0.15f) else palette.surface,
                border = BorderStroke(1.dp, if (isSelected) palette.primary else palette.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectSpaceSource(src) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectSpaceSource(src) },
                        colors = RadioButtonDefaults.colors(selectedColor = palette.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = src.titleRu,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = src.subtitleRu,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // NASA APOD Preview Widget Card
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("apod_preview_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = "NASA Астрономическое фото дня",
                subtitle = apodData?.date ?: "Свежий снимок из космоса",
                icon = Icons.Default.AutoAwesome
            )
            IconButton(onClick = onReloadApod) {
                Icon(Icons.Default.Refresh, contentDescription = "Обновить APOD", tint = palette.primary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (apodData != null) {
            if (apodData.mediaType == "image" && apodData.url.isNotBlank()) {
                AsyncImage(
                    model = apodData.url,
                    contentDescription = apodData.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = apodData.title,
                fontWeight = FontWeight.Bold,
                color = palette.accent,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = apodData.explanation.take(240) + if (apodData.explanation.length > 240) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        } else {
            Text(
                text = "Загрузка астрономического снимка NASA...",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted
            )
        }
    }
}

@Composable
private fun UnitsSettingsTabContent(
    palette: AstroPalette,
    unitSettings: AppUnitSettings,
    onUpdateUnits: (AppUnitSettings) -> Unit
) {
    // Temperature Unit
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("temp_units_card")) {
        Text("Единицы температуры", fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val isC = unitSettings.temperatureUnit == "°C"
            FilterChip(
                selected = isC,
                onClick = { onUpdateUnits(unitSettings.copy(temperatureUnit = "°C")) },
                label = { Text("Цельсий (°C)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.primary, selectedLabelColor = Color.Black)
            )
            FilterChip(
                selected = !isC,
                onClick = { onUpdateUnits(unitSettings.copy(temperatureUnit = "°F")) },
                label = { Text("Фаренгейт (°F)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.primary, selectedLabelColor = Color.Black)
            )
        }
    }

    // Wind Speed Unit
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("wind_units_card")) {
        Text("Скорость ветра", fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("км/ч", "м/с", "миль/ч").forEach { unit ->
                val isSel = unitSettings.windSpeedUnit == unit
                FilterChip(
                    selected = isSel,
                    onClick = { onUpdateUnits(unitSettings.copy(windSpeedUnit = unit)) },
                    label = { Text(unit) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.primary, selectedLabelColor = Color.Black)
                )
            }
        }
    }

    // Star Trail Rule Mode
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("star_rules_card")) {
        Text("Правило выдержки звезд (Без смаза)", fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Text("Формула допустимой выдержки для точечных звезд", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
        Spacer(modifier = Modifier.height(8.dp))
        val rules = listOf("NPF (Рекомендуется)", "Правило 500", "Правило 400", "Правило 300 (Строгое)")
        rules.forEach { rule ->
            val isSel = unitSettings.starRuleMode == rule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdateUnits(unitSettings.copy(starRuleMode = rule)) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSel,
                    onClick = { onUpdateUnits(unitSettings.copy(starRuleMode = rule)) },
                    colors = RadioButtonDefaults.colors(selectedColor = palette.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = rule, color = palette.textPrimary, fontSize = 13.sp)
            }
        }
    }

    // Night vision tone
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("night_vision_tone_card")) {
        Text("Спектр ночного видения", fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Text("Цвет интерфейса для сохранения темновой адаптации глаз", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Красный (650 нм)", "Янтарный (590 нм)").forEach { tone ->
                val isSel = unitSettings.nightVisionColor == tone
                FilterChip(
                    selected = isSel,
                    onClick = { onUpdateUnits(unitSettings.copy(nightVisionColor = tone)) },
                    label = { Text(tone) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.primary, selectedLabelColor = Color.Black)
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsTabContent(
    palette: AstroPalette,
    isPinging: Boolean,
    pingResults: List<DataSourcePingResult>,
    onRunDiagnostic: () -> Unit
) {
    AstroCard(modifier = Modifier.fillMaxWidth().testTag("diagnostics_header_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Состояние всех внешних API", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Text("Тестирование соединения и пинга в реальном времени", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
            }

            Button(
                onClick = onRunDiagnostic,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                enabled = !isPinging,
                modifier = Modifier.testTag("run_ping_button")
            ) {
                if (isPinging) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Тест", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    if (pingResults.isEmpty() && !isPinging) {
        AstroCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = palette.primary, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Нажмите «Тест» для проверки всех 7 источников данных", color = palette.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        pingResults.forEach { res ->
            AstroCard(modifier = Modifier.fillMaxWidth().testTag("ping_card_${res.id}")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (res.isOnline) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(res.nameRu, fontWeight = FontWeight.Bold, color = palette.textPrimary, fontSize = 14.sp)
                            Text(res.categoryRu, style = MaterialTheme.typography.labelSmall, color = palette.textMuted, fontSize = 10.sp)
                        }
                    }

                    if (res.isOnline && res.latencyMs > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = palette.surface,
                            border = BorderStroke(0.5.dp, palette.cardBorder)
                        ) {
                            Text(
                                text = "${res.latencyMs} ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = res.statusMessageRu,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (res.isOnline) palette.textSecondary else Color(0xFFFF6B6B),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    isActive: Boolean,
    activeText: String,
    inactiveText: String,
    palette: AstroPalette
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0x2210B981) else Color(0x2294A3B8),
        border = BorderStroke(0.5.dp, if (isActive) Color(0xFF10B981) else Color(0xFF64748B))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFF10B981) else Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isActive) activeText else inactiveText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) Color(0xFF10B981) else palette.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
