package com.example.astro.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.calculator.ExposureCalculator
import com.example.astro.data.AstroDatabase
import com.example.astro.data.ChecklistItemEntity
import com.example.astro.data.TargetSessionEntity
import com.example.astro.ephemeris.AstroEphemerisEngine
import com.example.astro.model.*
import com.example.astro.network.WeatherAndAuroraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AstroViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AstroDatabase.getDatabase(application, viewModelScope)
    private val dao = db.astroDao()
    private val weatherRepo = WeatherAndAuroraRepository()

    // Navigation and Mode
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _isRedNightMode = MutableStateFlow(false)
    val isRedNightMode: StateFlow<Boolean> = _isRedNightMode.asStateFlow()

    // Location
    private val _selectedLocation = MutableStateFlow(ObservationLocation.DEFAULT_LOCATIONS[0])
    val selectedLocation: StateFlow<ObservationLocation> = _selectedLocation.asStateFlow()

    // Weather & Aurora State
    private val _weatherState = MutableStateFlow<AstroWeather?>(null)
    val weatherState: StateFlow<AstroWeather?> = _weatherState.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _auroraState = MutableStateFlow(
        weatherRepo.getAuroraForecast(ObservationLocation.DEFAULT_LOCATIONS[0].latitude)
    )
    val auroraState: StateFlow<AuroraActivity> = _auroraState.asStateFlow()

    // Ephemeris & Moon State
    private val _moonDetails = MutableStateFlow(AstroEphemerisEngine.calculateMoonDetails())
    val moonDetails: StateFlow<MoonDetails> = _moonDetails.asStateFlow()

    private val _twilightDetails = MutableStateFlow(
        AstroEphemerisEngine.calculateTwilight(
            ObservationLocation.DEFAULT_LOCATIONS[0].latitude,
            ObservationLocation.DEFAULT_LOCATIONS[0].longitude
        )
    )
    val twilightDetails: StateFlow<TwilightDetails> = _twilightDetails.asStateFlow()

    private val _moonCalendar = MutableStateFlow(AstroEphemerisEngine.getMoonCalendar())
    val moonCalendar: StateFlow<List<Pair<Calendar, MoonDetails>>> = _moonCalendar.asStateFlow()

    // Optics & Calculator State
    private val _focalLengthMm = MutableStateFlow(24f)
    val focalLengthMm: StateFlow<Float> = _focalLengthMm.asStateFlow()

    private val _aperture = MutableStateFlow(1.8f)
    val aperture: StateFlow<Float> = _aperture.asStateFlow()

    private val _selectedSensor = MutableStateFlow(CameraSensor.PRESETS[0])
    val selectedSensor: StateFlow<CameraSensor> = _selectedSensor.asStateFlow()

    private val _megapixels = MutableStateFlow(24f)
    val megapixels: StateFlow<Float> = _megapixels.asStateFlow()

    private val _declination = MutableStateFlow(0f)
    val declination: StateFlow<Float> = _declination.asStateFlow()

    // Derived Exposure Result
    val exposureResult: StateFlow<ExposureCalculationResult> = combine(
        _focalLengthMm,
        _aperture,
        _selectedSensor,
        _megapixels,
        _declination
    ) { focal, ap, sensor, mp, dec ->
        ExposureCalculator.calculateExposure(focal, ap, sensor, mp, dec)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ExposureCalculator.calculateExposure(24f, 1.8f, CameraSensor.PRESETS[0], 24f, 0f)
    )

    // Timelapse parameters
    private val _timelapseClipSeconds = MutableStateFlow(15f)
    val timelapseClipSeconds: StateFlow<Float> = _timelapseClipSeconds.asStateFlow()

    private val _timelapseFps = MutableStateFlow(24)
    val timelapseFps: StateFlow<Int> = _timelapseFps.asStateFlow()

    val timelapseResult: StateFlow<TimelapseResult> = combine(
        _timelapseClipSeconds,
        _timelapseFps,
        exposureResult
    ) { clipSec, fps, exp ->
        ExposureCalculator.calculateTimelapse(
            targetClipSeconds = clipSec,
            fps = fps,
            shutterSpeedSeconds = exp.npfRuleSeconds
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ExposureCalculator.calculateTimelapse(15f, 24, 15f)
    )

    // Room Database Flows
    val sessions: StateFlow<List<TargetSessionEntity>> = dao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklistItems: StateFlow<List<ChecklistItemEntity>> = dao.getAllChecklistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshWeather()
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun toggleRedMode() {
        _isRedNightMode.value = !_isRedNightMode.value
    }

    fun selectLocation(location: ObservationLocation) {
        _selectedLocation.value = location
        // Recalculate ephemeris & aurora for new location
        _twilightDetails.value = AstroEphemerisEngine.calculateTwilight(
            location.latitude,
            location.longitude
        )
        _auroraState.value = weatherRepo.getAuroraForecast(location.latitude)
        refreshWeather()
    }

    fun setCustomCoordinates(lat: Double, lon: Double, name: String = "Пользовательская точка") {
        val customLoc = ObservationLocation(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            latitude = lat,
            longitude = lon,
            bortleScale = 3,
            regionRu = "Координаты: ${lat.formatCoord()}, ${lon.formatCoord()}"
        )
        selectLocation(customLoc)
    }

    fun refreshWeather() {
        val loc = _selectedLocation.value
        viewModelScope.launch {
            _isWeatherLoading.value = true
            val result = weatherRepo.getAstroWeather(loc.latitude, loc.longitude)
            _weatherState.value = result.getOrNull()
            _isWeatherLoading.value = false
        }
    }

    // Calculator updates
    fun updateFocalLength(focal: Float) {
        _focalLengthMm.value = focal.coerceIn(8f, 800f)
    }

    fun updateAperture(ap: Float) {
        _aperture.value = ap.coerceIn(0.95f, 22f)
    }

    fun updateSensor(sensor: CameraSensor) {
        _selectedSensor.value = sensor
        _megapixels.value = sensor.defaultMegapixels
    }

    fun updateMegapixels(mp: Float) {
        _megapixels.value = mp.coerceIn(10f, 150f)
    }

    fun updateDeclination(dec: Float) {
        _declination.value = dec.coerceIn(0f, 89f)
    }

    fun updateTimelapseClipSeconds(seconds: Float) {
        _timelapseClipSeconds.value = seconds.coerceIn(5f, 120f)
    }

    fun updateTimelapseFps(fps: Int) {
        _timelapseFps.value = fps
    }

    // Session Database actions
    fun addSession(
        title: String,
        targetType: String,
        notes: String,
        targetDate: String,
        focal: Float = _focalLengthMm.value,
        aperture: Float = _aperture.value,
        iso: Int = 3200,
        subExposure: Float = exposureResult.value.npfRuleSeconds,
        subCount: Int = 25
    ) {
        viewModelScope.launch {
            val session = TargetSessionEntity(
                title = title.ifBlank { "Съёмка $targetType" },
                targetType = targetType,
                locationName = _selectedLocation.value.name,
                targetDate = targetDate.ifBlank { "Ясная ночь" },
                notes = notes,
                focalLength = focal,
                aperture = aperture,
                iso = iso,
                subExposureSeconds = subExposure,
                subCount = subCount,
                isCompleted = false
            )
            dao.insertSession(session)
        }
    }

    fun toggleSessionCompleted(session: TargetSessionEntity) {
        viewModelScope.launch {
            dao.updateSession(session.copy(isCompleted = !session.isCompleted))
        }
    }

    fun deleteSession(session: TargetSessionEntity) {
        viewModelScope.launch {
            dao.deleteSession(session)
        }
    }

    fun toggleChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            dao.updateChecklistItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun resetChecklist(checkAll: Boolean) {
        viewModelScope.launch {
            dao.setAllChecklistChecked(checkAll)
        }
    }
}

private fun Double.formatCoord(): String = String.format("%.2f", this)
