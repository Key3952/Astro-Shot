package com.example.astro.model

/**
 * Camera sensor presets with sensor dimensions and crop factor
 */
data class CameraSensor(
    val id: String,
    val name: String,
    val cropFactor: Float,
    val widthMm: Float,
    val heightMm: Float,
    val defaultMegapixels: Float,
    val circleOfConfusionMm: Float
) {
    /**
     * Approximate pixel pitch in micrometers (µm)
     */
    fun pixelPitchMicrons(megapixels: Float = defaultMegapixels): Float {
        val sensorArea = widthMm * heightMm
        val pixelArea = sensorArea / (megapixels * 1_000_000f)
        return kotlin.math.sqrt(pixelArea) * 1000f
    }

    companion object {
        val PRESETS = listOf(
            CameraSensor("ff", "Full Frame (35mm)", 1.0f, 36.0f, 24.0f, 24.0f, 0.030f),
            CameraSensor("ff_high", "Full Frame Hi-Res (45-61 MP)", 1.0f, 36.0f, 24.0f, 50.0f, 0.025f),
            CameraSensor("apsc_sony", "APS-C Sony / Nikon / Fuji (1.5x)", 1.53f, 23.5f, 15.6f, 24.0f, 0.020f),
            CameraSensor("apsc_canon", "APS-C Canon (1.6x)", 1.62f, 22.3f, 14.9f, 24.0f, 0.019f),
            CameraSensor("mft", "Micro Four Thirds (2.0x)", 2.0f, 17.3f, 13.0f, 20.0f, 0.015f),
            CameraSensor("one_inch", "1-Inch Sensor (2.7x)", 2.72f, 13.2f, 8.8f, 20.0f, 0.011f),
            CameraSensor("phone", "Смартфон (1/1.3\" - 1/1.5\")", 3.8f, 9.6f, 7.2f, 12.0f, 0.008f)
        )
    }
}

/**
 * Result of exposure calculations
 */
data class ExposureCalculationResult(
    val npfRuleSeconds: Float,
    val rule500Seconds: Float,
    val rule400Seconds: Float,
    val hyperfocalDistanceMeters: Float,
    val recommendedIsoMin: Int,
    val recommendedIsoMax: Int,
    val starTrailWarning: String
)

/**
 * Result of timelapse calculation
 */
data class TimelapseResult(
    val totalPhotos: Int,
    val shootingDurationMinutes: Float,
    val finalClipDurationSeconds: Float,
    val estimatedStorageMb: Float,
    val recommendedIntervalSeconds: Float
)

/**
 * Moon phase and ephemeris
 */
enum class MoonPhaseType(val titleRu: String, val illuminationApprox: String) {
    NEW_MOON("Новолуние", "0%"),
    WAXING_CRESCENT("Молодая Луна", "1% – 49%"),
    FIRST_QUARTER("Первая четверть", "50%"),
    WAXING_GIBBOUS("Прибывающая Луна", "51% – 99%"),
    FULL_MOON("Полнолуние", "100%"),
    WANING_GIBBOUS("Убывающая Луна", "99% – 51%"),
    LAST_QUARTER("Последняя четверть", "50%"),
    WANING_CRESCENT("Старая Луна", "49% – 1%")
}

data class MoonDetails(
    val phaseType: MoonPhaseType,
    val illuminationPercent: Int,
    val ageDays: Float,
    val moonriseTime: String,
    val moonsetTime: String,
    val isGoodForAstro: Boolean,
    val astroSummary: String
)

data class TwilightDetails(
    val goldenHourStart: String,
    val goldenHourEnd: String,
    val blueHourStart: String,
    val blueHourEnd: String,
    val sunsetTime: String,
    val sunriseTime: String,
    val astroTwilightStart: String,
    val astroTwilightEnd: String,
    val darkSkyDurationHours: Float
)

/**
 * Multi-layer cloud and condensation weather
 */
data class AstroWeather(
    val temperatureC: Float,
    val relativeHumidityPercent: Int,
    val dewPointC: Float,
    val totalCloudCoverPercent: Int,
    val lowCloudsPercent: Int,
    val midCloudsPercent: Int,
    val highCloudsPercent: Int,
    val windSpeedKmh: Float,
    val visibilityKm: Float,
    val condensationRisk: CondensationRisk,
    val skyQualityScore: Int, // 0 - 100
    val hourlyForecast: List<HourlyAstroWeather>
)

enum class CondensationRisk(val labelRu: String, val severityColorHex: Long) {
    SAFE("Безопасно (росы нет)", 0xFF4ADE80),
    MODERATE("Внимание (возможно запотевание)", 0xFFFBBF24),
    CRITICAL("Опасность росы! Нужен обогрев", 0xFFF87171)
}

data class HourlyAstroWeather(
    val timeLabel: String,
    val cloudCover: Int,
    val lowClouds: Int,
    val midClouds: Int,
    val highClouds: Int,
    val dewPointDiff: Float,
    val skyScore: Int
)

/**
 * Aurora geomagnetic activity
 */
data class AuroraActivity(
    val kpIndex: Float, // 0.0 to 9.0
    val stormLevel: String, // G0 to G5
    val stormDescriptionRu: String,
    val visibilityLatitudeThreshold: Float,
    val observationChanceAtUserLat: Int, // 0 to 100%
    val recommendedSettings: String
)

/**
 * Preset observation location
 */
/**
 * Preset observation location
 */
data class ObservationLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val bortleScale: Int, // 1 to 9
    val regionRu: String,
    val isNorthWest: Boolean = false
) {
    companion object {
        val DEFAULT_LOCATIONS = listOf(
            // Северо-Запад России (СЗФО)
            ObservationLocation("spb", "Санкт-Петербург (Дворцовая)", 59.93, 30.33, 7, "Северо-Запад", true),
            ObservationLocation("teriberka", "Териберка (Кольский п-ов, Сияния)", 69.16, 35.14, 2, "Мурманская обл.", true),
            ObservationLocation("murmansk", "Мурманск", 68.96, 33.08, 6, "Мурманская обл.", true),
            ObservationLocation("khibiny", "Кировск / Хибины (Горы)", 67.61, 33.67, 3, "Мурманская обл.", true),
            ObservationLocation("petrozavodsk", "Петрозаводск (Онежское оз.)", 61.79, 34.36, 5, "Карелия", true),
            ObservationLocation("sortavala", "Сортавала (Ладожские шхеры)", 61.70, 30.68, 3, "Карелия", true),
            ObservationLocation("arhangelsk", "Архангельск (Северная Двина)", 64.54, 40.53, 5, "Архангельская обл.", true),
            ObservationLocation("severodvinsk", "Северодвинск (Белое море)", 64.56, 39.83, 5, "Архангельская обл.", true),
            ObservationLocation("novgorod", "Великий Новгород (Ильмень)", 58.52, 31.27, 5, "Новгородская обл.", true),
            ObservationLocation("pskov", "Псков (Псковское оз.)", 57.82, 28.33, 5, "Псковская обл.", true),
            ObservationLocation("vyborg", "Выборг (Финский залив)", 60.71, 28.75, 4, "Ленинградская обл.", true),
            ObservationLocation("vologda", "Вологда", 59.22, 39.89, 5, "Вологодская обл.", true),
            ObservationLocation("syktyvkar", "Сыктывкар", 61.67, 50.81, 5, "Республика Коми", true),
            ObservationLocation("kaliningrad", "Калининград (Балтика)", 54.71, 20.51, 6, "Калининградская обл.", true),

            // Топовые астроклиматические локации России
            ObservationLocation("caucasus", "САО РАН (Архыз, Кавказ)", 43.65, 41.44, 2, "Кавказ"),
            ObservationLocation("altai", "Чемал / Актру (Алтай)", 51.40, 86.00, 1, "Алтай"),
            ObservationLocation("baikal", "Остров Ольхон (Байкал)", 53.15, 107.38, 2, "Байкал"),
            ObservationLocation("crimea", "КрАО (Научный, Крым)", 44.72, 34.01, 3, "Крым"),
            ObservationLocation("msk", "Москва и Подмосковье", 55.75, 37.61, 7, "Центральный"),
            ObservationLocation("novosib", "Новосибирск (НСО)", 55.00, 82.93, 6, "Сибирь")
        )
    }
}

/**
 * Interactive Weather Map layers and providers
 */
enum class WeatherMapLayer(val labelRu: String) {
    CLOUDS("Облачность"),
    RADAR_PRECIPITATION("Радар осадков"),
    WIND("Потоки ветра"),
    TEMPERATURE("Температура")
}

enum class WeatherMapProvider(val titleRu: String, val requiresKey: Boolean, val freeDescriptionRu: String) {
    OPEN_METEO("Open-Meteo", false, "Бесплатно, без ключа. Полный послойный расчет."),
    RAINVIEWER("RainViewer Radar", false, "Бесплатно, без ключа. Живой радар осадков и облачности."),
    OPENWEATHERMAP("OpenWeatherMap", true, "Бесплатный ключ на 1 млн запросов/мес. Глобальные тайлы.")
}

data class WeatherComparison(
    val openMeteoCloudCover: Int,
    val openWeatherCloudCover: Int?,
    val rainViewerIntensity: String,
    val averageCloudCover: Int,
    val verdictRu: String
)

/**
 * Hourly night timeline slice for dark sky visualization
 */
data class NightTimelineHour(
    val hourString: String,
    val hourOfDay: Int,
    val sunAltitudeDeg: Double,
    val moonAltitudeDeg: Double,
    val moonIlluminationPercent: Int,
    val milkyWayAltitudeDeg: Double,
    val twilightPhaseRu: String,
    val cloudCoverPercent: Int,
    val qualityScore: Int,
    val isBestDarkWindow: Boolean,
    val isMilkyWayVisible: Boolean
)

/**
 * Celestial target in deep-sky catalog (Messier & famous targets)
 */
data class CelestialTarget(
    val id: String,
    val nameRu: String,
    val catalogId: String,
    val constellationRu: String,
    val categoryRu: String,
    val apparentMagnitude: Float,
    val angularSize: String,
    val rightAscensionHours: Double,
    val declinationDeg: Double,
    val recommendedFocalLength: String,
    val recommendedFilters: String,
    val difficultyRu: String,
    val descriptionRu: String,
    val currentAltitudeDeg: Double = 0.0,
    val currentAzimuthDeg: Double = 0.0,
    val visibilityStatusRu: String = ""
)

/**
 * Meteor shower calendar event
 */
data class MeteorShower(
    val id: String,
    val nameRu: String,
    val peakDateRu: String,
    val activeRangeRu: String,
    val zhr: Int, // Zenithal Hourly Rate
    val radiantConstellation: String,
    val velocityKmS: Int,
    val parentBody: String,
    val currentStatusRu: String,
    val shootingTipsRu: String
)

/**
 * Real-time Milky Way galactic core ephemeris & status
 */
data class MilkyWayStatus(
    val coreAltitudeDeg: Double,
    val coreAzimuthDeg: Double,
    val cardinalDirectionRu: String,
    val maxAltitudeTonightDeg: Double,
    val riseTime: String,
    val setTime: String,
    val bestWindowTime: String,
    val visibilitySummary: String,
    val isCoreVisible: Boolean,
    val northernBranchZenith: Boolean
)

/**
 * Field of view calculation result
 */
data class FovFramingResult(
    val horizontalFovDeg: Float,
    val verticalFovDeg: Float,
    val diagonalFovDeg: Float,
    val equivalent35mmFocalMm: Float,
    val skyCoverageDescription: String
)

/**
 * Depth of field and hyperfocal calculation result
 */
data class HyperfocalResult(
    val hyperfocalMeters: Float,
    val nearLimitMeters: Float,
    val farLimitDescription: String,
    val depthOfFieldDescription: String
)

/**
 * Weather & atmospheric data provider options
 */
enum class WeatherDataSource(
    val titleRu: String,
    val subtitleRu: String,
    val requiresKey: Boolean
) {
    ENSEMBLE("Ансамбль (Консенсус)", "Сводка всех доступных источников", false),
    OPEN_METEO("Open-Meteo", "DWD ICON / ECMWF модели (Без ключа)", false),
    OPEN_WEATHER_MAP("OpenWeatherMap", "OWM 2.5 / 3.0 OneCall (По ключу)", true),
    WEATHER_API("WeatherAPI.com", "Астрономический прогноз (По ключу)", true),
    SEVEN_TIMER("7Timer! ASTRO", "Seeing & Transparency для астрономов", false),
    TOMORROW_IO("Tomorrow.io", "Высокое разрешение облачности", true)
}

/**
 * Solar & geomagnetic space weather sources
 */
enum class SpaceWeatherSource(
    val titleRu: String,
    val subtitleRu: String
) {
    NOAA_SWPC_LIVE("NOAA SWPC Live", "Прямой спутниковый поток США (Kp-индекс)"),
    SIMULATED_CYCLE_25("Модель Солнечного цикла 25", "Автономный расчет геомагнитных бурь")
}

/**
 * API Keys bundle for all connected external services
 */
data class ApiKeysConfig(
    val openWeatherKey: String = "",
    val weatherApiKey: String = "",
    val nasaApiKey: String = "",
    val tomorrowKey: String = "",
    val customTileUrl: String = "",
    val useNasaDemoKey: Boolean = true
)

/**
 * User-configurable measurement units & calculation preferences
 */
data class AppUnitSettings(
    val temperatureUnit: String = "°C",
    val windSpeedUnit: String = "км/ч",
    val starRuleMode: String = "NPF",
    val lightPollutionUnit: String = "Bortle",
    val nightVisionColor: String = "Красный (650 нм)"
)

/**
 * Live connectivity and status diagnostic for an external data source
 */
data class DataSourcePingResult(
    val id: String,
    val nameRu: String,
    val categoryRu: String,
    val isOnline: Boolean,
    val latencyMs: Long,
    val statusMessageRu: String,
    val requiresKey: Boolean,
    val hasKey: Boolean
)

/**
 * NASA Astronomy Picture of the Day (APOD)
 */
data class NasaApodData(
    val title: String,
    val date: String,
    val explanation: String,
    val url: String,
    val hdUrl: String? = null,
    val mediaType: String = "image"
)

