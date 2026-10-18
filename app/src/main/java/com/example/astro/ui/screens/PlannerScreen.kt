package com.example.astro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.astro.data.ChecklistItemEntity
import com.example.astro.data.TargetSessionEntity
import com.example.astro.model.CelestialTarget
import com.example.astro.model.MeteorShower
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.components.*
import com.example.astro.ui.theme.LocalAstroPalette

@Composable
fun PlannerScreen(
    viewModel: AstroViewModel
) {
    val palette = LocalAstroPalette.current

    val timeline by viewModel.nightTimeline.collectAsState()
    val milkyWay by viewModel.milkyWayStatus.collectAsState()
    val targets by viewModel.celestialCatalog.collectAsState()
    val meteorShowers by viewModel.meteorShowers.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val checklist by viewModel.checklistItems.collectAsState()
    val currentLocation by viewModel.selectedLocation.collectAsState()
    val currentExposure by viewModel.exposureResult.collectAsState()
    val currentFocal by viewModel.focalLengthMm.collectAsState()
    val currentAperture by viewModel.aperture.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    // 0: Таймлайн ночи, 1: Каталог Мессье, 2: Метеорные потоки, 3: Мои сессии, 4: Чеклист

    var showAddDialog by remember { mutableStateOf(false) }
    var prefillTitle by remember { mutableStateOf("") }
    var prefillType by remember { mutableStateOf("Deep Sky") }
    var prefillNotes by remember { mutableStateOf("") }
    var prefillFocal by remember { mutableStateOf(currentFocal) }

    var targetCategoryFilter by remember { mutableStateOf("Все") }
    var onlyVisibleFilter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("planner_screen")
    ) {
        // Top Filter Tabs (Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = { Text("Таймлайн ночи") },
                modifier = Modifier.testTag("planner_tab_timeline")
            )
            FilterChip(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = { Text("Каталог Мессье (${targets.size})") },
                modifier = Modifier.testTag("planner_tab_messier")
            )
            FilterChip(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                label = { Text("Метеоры (${meteorShowers.size})") },
                modifier = Modifier.testTag("planner_tab_meteors")
            )
            FilterChip(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                label = { Text("Мои сессии (${sessions.size})") },
                modifier = Modifier.testTag("planner_tab_sessions")
            )
            FilterChip(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                label = { Text("Чеклист") },
                modifier = Modifier.testTag("planner_tab_checklist")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Night Timeline & Milky Way Core
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NightTimelineWidget(timeline = timeline)
                    MilkyWayStatusCard(status = milkyWay)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            1 -> {
                // Messier and Deep Sky Catalog
                val filteredTargets = remember(targets, targetCategoryFilter, onlyVisibleFilter) {
                    targets.filter { target ->
                        val matchesCat = when (targetCategoryFilter) {
                            "Все" -> true
                            "Галактики" -> target.categoryRu.contains("галактика", ignoreCase = true)
                            "Туманности" -> target.categoryRu.contains("туманность", ignoreCase = true)
                            "Скопления" -> target.categoryRu.contains("скопление", ignoreCase = true)
                            else -> true
                        }
                        val matchesVisible = if (onlyVisibleFilter) target.currentAltitudeDeg > 5.0 else true
                        matchesCat && matchesVisible
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Все", "Галактики", "Туманности", "Скопления").forEach { cat ->
                        FilterChip(
                            selected = targetCategoryFilter == cat,
                            onClick = { targetCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Только видимые над горизонтом",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Switch(
                        checked = onlyVisibleFilter,
                        onCheckedChange = { onlyVisibleFilter = it }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTargets, key = { it.id }) { target ->
                        CelestialItemCard(
                            target = target,
                            onPlanTarget = { t ->
                                prefillTitle = t.nameRu
                                prefillType = t.categoryRu
                                prefillNotes = "${t.catalogId}, созвездие ${t.constellationRu}. Оптика: ${t.recommendedFocalLength}. Фильтры: ${t.recommendedFilters}"
                                val parsedFocal = t.recommendedFocalLength.filter { it.isDigit() }.take(3).toFloatOrNull() ?: currentFocal
                                prefillFocal = parsedFocal
                                showAddDialog = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            2 -> {
                // Meteor Showers
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(meteorShowers, key = { it.id }) { shower ->
                        MeteorShowerCard(
                            shower = shower,
                            onPlanShower = { s ->
                                prefillTitle = "Звездопад ${s.nameRu}"
                                prefillType = "Метеорный поток"
                                prefillNotes = "Пик: ${s.peakDateRu}. ZHR: ${s.zhr} мет/ч. Радиант: ${s.radiantConstellation}. Совет: ${s.shootingTipsRu}"
                                prefillFocal = 14f
                                showAddDialog = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            3 -> {
                // User Sessions & Targets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Мои запланированные сессии",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Button(
                        onClick = {
                            prefillTitle = ""
                            prefillType = "Deep Sky"
                            prefillNotes = ""
                            prefillFocal = currentFocal
                            showAddDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                        modifier = Modifier.testTag("add_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Новая цель", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = palette.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нет запланированных съёмок",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Выберите объект из каталога Мессье или нажмите «Новая цель»",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            AstroCard(
                                modifier = Modifier.fillMaxWidth().testTag("session_card_${session.id}"),
                                borderStroke = if (session.isCompleted) BorderStroke(1.dp, Color(0xFF10B981)) else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = session.isCompleted,
                                            onCheckedChange = { viewModel.toggleSessionCompleted(session) }
                                        )
                                        Column {
                                            Text(
                                                text = session.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (session.isCompleted) palette.textMuted else palette.textPrimary
                                            )
                                            Text(
                                                text = "${session.targetType} • ${session.targetDate}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.textMuted
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteSession(session) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Удалить",
                                            tint = palette.textMuted
                                        )
                                    }
                                }

                                if (session.notes.isNotBlank()) {
                                    Text(
                                        text = session.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 6.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MetricBadge(
                                        label = "Оптика",
                                        value = "${session.focalLength.toInt()}mm f/${session.aperture}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricBadge(
                                        label = "Кадры",
                                        value = "${session.subCount} × ${session.subExposureSeconds}s",
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    MetricBadge(
                                        label = "ISO",
                                        value = "${session.iso}",
                                        modifier = Modifier.weight(0.8f)
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            4 -> {
                // Checklist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Чеклист подготовки к выезду",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    TextButton(onClick = { viewModel.resetChecklist(false) }) {
                        Text("Сбросить", color = palette.primary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(checklist, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.surface,
                            border = BorderStroke(0.5.dp, palette.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleChecklistItem(item) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { viewModel.toggleChecklistItem(item) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                                        color = if (item.isChecked) palette.textMuted else palette.textPrimary
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSessionDialog(
            initialTitle = prefillTitle,
            initialType = prefillType,
            initialNotes = prefillNotes,
            initialFocal = prefillFocal,
            currentAperture = currentAperture,
            defaultExposure = currentExposure.npfRuleSeconds,
            onDismiss = { showAddDialog = false },
            onSave = { title, type, notes, date, focal, ap, iso, subExp, subCount ->
                viewModel.addSession(
                    title = title,
                    targetType = type,
                    notes = notes,
                    targetDate = date,
                    focal = focal,
                    aperture = ap,
                    iso = iso,
                    subExposure = subExp,
                    subCount = subCount
                )
                showAddDialog = false
                selectedTab = 3 // Jump to sessions tab
            }
        )
    }
}

@Composable
private fun AddSessionDialog(
    initialTitle: String,
    initialType: String,
    initialNotes: String,
    initialFocal: Float,
    currentAperture: Float,
    defaultExposure: Float,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        type: String,
        notes: String,
        date: String,
        focal: Float,
        ap: Float,
        iso: Int,
        subExp: Float,
        subCount: Int
    ) -> Unit
) {
    val palette = LocalAstroPalette.current

    var title by remember { mutableStateOf(initialTitle) }
    var targetType by remember { mutableStateOf(initialType) }
    var notes by remember { mutableStateOf(initialNotes) }
    var targetDate by remember { mutableStateOf("Ближайшая ясная ночь") }
    var focalText by remember { mutableStateOf(initialFocal.toInt().toString()) }
    var subExpText by remember { mutableStateOf(defaultExposure.toInt().toString()) }
    var subCountText by remember { mutableStateOf("30") }
    var iso by remember { mutableStateOf(3200) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.cardBg,
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier.fillMaxWidth().testTag("add_target_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Запланировать съёмку",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название объекта / цели") },
                    placeholder = { Text("например: Туманность Андромеды (M31)") },
                    modifier = Modifier.fillMaxWidth().testTag("session_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Дата съёмки") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = focalText,
                        onValueChange = { focalText = it },
                        label = { Text("Фокусное (мм)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subExpText,
                        onValueChange = { subExpText = it },
                        label = { Text("Выдержка (с)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subCountText,
                        onValueChange = { subCountText = it },
                        label = { Text("Кадров") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text(
                    text = "Рекомендуемый ISO: $iso",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(800, 1600, 3200, 6400).forEach { isoVal ->
                        FilterChip(
                            selected = iso == isoVal,
                            onClick = { iso = isoVal },
                            label = { Text("ISO $isoVal", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки, фильтры, координаты") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                        onClick = {
                            val f = focalText.toFloatOrNull() ?: initialFocal
                            val exp = subExpText.toFloatOrNull() ?: defaultExposure
                            val count = subCountText.toIntOrNull() ?: 30
                            onSave(title, targetType, notes, targetDate, f, currentAperture, iso, exp, count)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                        modifier = Modifier.weight(1f).testTag("save_session_button")
                    ) {
                        Text("Сохранить", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
