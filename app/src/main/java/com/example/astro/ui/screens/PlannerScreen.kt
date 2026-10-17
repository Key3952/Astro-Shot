package com.example.astro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.components.AstroCard
import com.example.astro.ui.components.MetricBadge
import com.example.astro.ui.components.SectionHeader
import com.example.astro.ui.theme.LocalAstroPalette

@Composable
fun PlannerScreen(
    viewModel: AstroViewModel
) {
    val palette = LocalAstroPalette.current

    val sessions by viewModel.sessions.collectAsState()
    val checklist by viewModel.checklistItems.collectAsState()
    val currentLocation by viewModel.selectedLocation.collectAsState()
    val currentExposure by viewModel.exposureResult.collectAsState()
    val currentFocal by viewModel.focalLengthMm.collectAsState()
    val currentAperture by viewModel.aperture.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSectionTab by remember { mutableStateOf(0) } // 0: Сессии, 1: Чеклист

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("planner_screen")
    ) {
        // Section tabs: Сессии съёмок vs Чеклист экипировки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSectionTab == 0,
                onClick = { selectedSectionTab = 0 },
                label = { Text("Сессии и цели (${sessions.size})") },
                modifier = Modifier.weight(1f).testTag("tab_sessions")
            )
            FilterChip(
                selected = selectedSectionTab == 1,
                onClick = { selectedSectionTab = 1 },
                label = { Text("Чеклист выезда") },
                modifier = Modifier.weight(1f).testTag("tab_checklist")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSectionTab == 0) {
            // Header + Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "План ночных съёмок",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Button(
                    onClick = { showAddDialog = true },
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
                            text = "Нажмите «Новая цель», чтобы запланировать экспозицию",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        SessionItemCard(
                            session = session,
                            onToggleCompleted = { viewModel.toggleSessionCompleted(session) },
                            onDelete = { viewModel.deleteSession(session) },
                            palette = palette
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        } else {
            // Field Gear Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Чеклист полевой экипировки",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                TextButton(onClick = { viewModel.resetChecklist(false) }) {
                    Text("Сбросить", color = palette.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(checklist, key = { it.id }) { item ->
                    ChecklistRow(
                        item = item,
                        onToggle = { viewModel.toggleChecklistItem(item) },
                        palette = palette
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddSessionDialog(
            defaultLocation = currentLocation.name,
            defaultFocal = currentFocal,
            defaultAperture = currentAperture,
            defaultSubExposure = currentExposure.npfRuleSeconds,
            onDismiss = { showAddDialog = false },
            onSave = { title, targetType, date, notes, focal, ap, iso, exp, count ->
                viewModel.addSession(
                    title = title,
                    targetType = targetType,
                    notes = notes,
                    targetDate = date,
                    focal = focal,
                    aperture = ap,
                    iso = iso,
                    subExposure = exp,
                    subCount = count
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SessionItemCard(
    session: TargetSessionEntity,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    palette: com.example.astro.ui.theme.AstroPalette
) {
    AstroCard(
        modifier = Modifier.fillMaxWidth().testTag("session_card_${session.id}"),
        borderStroke = if (session.isCompleted) BorderStroke(1.dp, Color(0xFF10B981)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = session.isCompleted,
                onCheckedChange = { onToggleCompleted() },
                colors = CheckboxDefaults.colors(
                    checkedColor = palette.primary,
                    uncheckedColor = palette.cardBorder
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (session.isCompleted) palette.textMuted else palette.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.surface
                    ) {
                        Text(
                            text = session.targetType,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${session.locationName} • ${session.targetDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить цель",
                    tint = palette.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = palette.cardBorder)
        Spacer(modifier = Modifier.height(10.dp))

        // Exposure info pills
        val totalIntegrationMin = (session.subExposureSeconds * session.subCount) / 60f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricBadge(
                label = "Оптика",
                value = "${session.focalLength.toInt()}mm",
                unit = "f/${session.aperture}",
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Экспозиция",
                value = "${session.subExposureSeconds}s",
                unit = "x${session.subCount}",
                modifier = Modifier.weight(1f)
            )
            MetricBadge(
                label = "Стек",
                value = "${String.format("%.1f", totalIntegrationMin)}",
                unit = "мин",
                modifier = Modifier.weight(1f)
            )
        }

        if (session.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Заметка: ${session.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItemEntity,
    onToggle: () -> Unit,
    palette: com.example.astro.ui.theme.AstroPalette
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.cardBg,
        border = BorderStroke(1.dp, if (item.isChecked) palette.primary else palette.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = palette.primary,
                    uncheckedColor = palette.cardBorder
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (item.isChecked) palette.textMuted else palette.textPrimary
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AddSessionDialog(
    defaultLocation: String,
    defaultFocal: Float,
    defaultAperture: Float,
    defaultSubExposure: Float,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        targetType: String,
        date: String,
        notes: String,
        focal: Float,
        aperture: Float,
        iso: Int,
        subExposure: Float,
        subCount: Int
    ) -> Unit
) {
    val palette = LocalAstroPalette.current
    var title by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("Млечный Путь") }
    var date by remember { mutableStateOf("Новолуние") }
    var notes by remember { mutableStateOf("") }
    var focalStr by remember { mutableStateOf(defaultFocal.toInt().toString()) }
    var apStr by remember { mutableStateOf(defaultAperture.toString()) }
    var isoStr by remember { mutableStateOf("3200") }
    var expStr by remember { mutableStateOf(defaultSubExposure.toString()) }
    var countStr by remember { mutableStateOf("30") }

    val targetTypes = listOf("Млечный Путь", "Галактика", "Туманность", "Сияние", "Метеоры", "Луна")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.cardBg,
            border = BorderStroke(1.dp, palette.cardBorder),
            modifier = Modifier.fillMaxWidth().testTag("add_session_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Запланировать съёмку",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название цели (напр. Центр Млечного Пути)") },
                    modifier = Modifier.fillMaxWidth().testTag("session_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Категория объекта:",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    targetTypes.take(3).forEach { type ->
                        FilterChip(
                            selected = targetType == type,
                            onClick = { targetType = type },
                            label = { Text(type, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    targetTypes.drop(3).forEach { type ->
                        FilterChip(
                            selected = targetType == type,
                            onClick = { targetType = type },
                            label = { Text(type, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = focalStr,
                        onValueChange = { focalStr = it },
                        label = { Text("Фокус (мм)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = apStr,
                        onValueChange = { apStr = it },
                        label = { Text("Диафрагма f/") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expStr,
                        onValueChange = { expStr = it },
                        label = { Text("Выдержка (с)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = isoStr,
                        onValueChange = { isoStr = it },
                        label = { Text("ISO") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = countStr,
                        onValueChange = { countStr = it },
                        label = { Text("Кадров для стека") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки (фильтры, калибровка, ориентиры)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
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
                        onClick = {
                            val f = focalStr.toFloatOrNull() ?: defaultFocal
                            val a = apStr.toFloatOrNull() ?: defaultAperture
                            val i = isoStr.toIntOrNull() ?: 3200
                            val e = expStr.toFloatOrNull() ?: defaultSubExposure
                            val c = countStr.toIntOrNull() ?: 30
                            onSave(title, targetType, date, notes, f, a, i, e, c)
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
