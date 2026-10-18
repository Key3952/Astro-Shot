package com.example.astro.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.ui.components.LocationSelectorDialog
import com.example.astro.ui.screens.*
import com.example.astro.ui.theme.LocalAstroPalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroMainScreen(viewModel: AstroViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val palette = LocalAstroPalette.current
    val currentTab by viewModel.currentTab.collectAsState()
    val isRedMode by viewModel.isRedNightMode.collectAsState()
    val currentLocation by viewModel.selectedLocation.collectAsState()
    val isGpsSearching by viewModel.isGpsSearching.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Runtime Permission Launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.requestGpsLocation { success, msg ->
                scope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Доступ к GPS отклонен. Выберите город вручную из списка.")
            }
        }
    }

    fun startGpsSearch() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // If in Settings or on a secondary tab, pressing Back handles navigation smoothly
    BackHandler(enabled = isSettingsOpen || currentTab != 0) {
        if (isSettingsOpen) {
            isSettingsOpen = false
        } else {
            viewModel.setTab(0)
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { isSettingsOpen = false }
        )
    } else {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isRedMode) Color(0x33FF3333) else Color(0x2238BDF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AstroShot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = if (isRedMode) "Красный ночной режим" else "Полевой ассистент",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isRedMode) palette.primary else palette.textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Location button (Opens Dialog with GPS button & NW cities)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = palette.surface,
                        border = BorderStroke(0.5.dp, palette.cardBorder),
                        onClick = { showLocationDialog = true },
                        modifier = Modifier.padding(end = 6.dp).testTag("top_location_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGpsSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = palette.primary,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Локация",
                                    tint = palette.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLocation.name.take(13),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    // Night-Vision Red Mode Toggle Button
                    FilledIconToggleButton(
                        checked = isRedMode,
                        onCheckedChange = { viewModel.toggleRedMode() },
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor = palette.surface,
                            checkedContainerColor = Color(0xFFFF2E2E),
                            contentColor = palette.textSecondary,
                            checkedContentColor = Color.Black
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("red_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isRedMode) Icons.Default.Visibility else Icons.Default.Nightlight,
                            contentDescription = "Красный ночной режим",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Settings & API Keys Button
                    IconButton(
                        onClick = { isSettingsOpen = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("settings_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки приложения и API",
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background,
                    titleContentColor = palette.textPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = palette.background,
                contentColor = palette.textPrimary,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                val items = listOf(
                    NavigationItem("Обзор", Icons.Default.Dashboard, 0),
                    NavigationItem("Оптика", Icons.Default.CameraAlt, 1),
                    NavigationItem("Погода", Icons.Default.Cloud, 2),
                    NavigationItem("Луна", Icons.Default.NightlightRound, 3),
                    NavigationItem("План", Icons.Default.DateRange, 4)
                )

                items.forEach { item ->
                    val selected = currentTab == item.tabIndex
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setTab(item.tabIndex) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) {
                                    if (isRedMode) Color.Black else Color.Black
                                } else palette.textMuted
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) palette.primary else palette.textMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = palette.primary
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.tabIndex}")
                    )
                }
            }
        },
        containerColor = palette.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { viewModel.setTab(it) },
                        onOpenSettings = { isSettingsOpen = true }
                    )
                    1 -> OpticsCalculatorScreen(viewModel = viewModel)
                    2 -> WeatherAuroraScreen(
                        viewModel = viewModel,
                        onOpenSettings = { isSettingsOpen = true }
                    )
                    3 -> MoonEphemerisScreen(viewModel = viewModel)
                    4 -> PlannerScreen(viewModel = viewModel)
                }
            }
        }
    }
    }

    if (showLocationDialog) {
        LocationSelectorDialog(
            currentLocation = currentLocation,
            isGpsSearching = isGpsSearching,
            onTriggerGps = {
                startGpsSearch()
                showLocationDialog = false
            },
            onSelectLocation = { viewModel.selectLocation(it) },
            onSetCustomCoords = { lat, lon -> viewModel.setCustomCoordinates(lat, lon) },
            onDismiss = { showLocationDialog = false }
        )
    }
}

private data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tabIndex: Int
)
