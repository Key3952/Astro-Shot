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
data class ObservationLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val bortleScale: Int, // 1 to 9
    val regionRu: String
) {
    companion object {
        val DEFAULT_LOCATIONS = listOf(
            ObservationLocation("caucasus", "САО РАН (Архыз, Кавказ)", 43.65, 41.44, 2, "Кавказ"),
            ObservationLocation("altai", "Чемал / Актру (Алтай)", 51.40, 86.00, 1, "Алтай"),
            ObservationLocation("baikal", "Остров Ольхон (Байкал)", 53.15, 107.38, 2, "Байкал"),
            ObservationLocation("teriberka", "Териберка (Сияния, Заполярье)", 69.16, 35.14, 2, "Мурманская обл."),
            ObservationLocation("crimea", "КрАО (Научный, Крым)", 44.72, 34.01, 3, "Крым"),
            ObservationLocation("spb", "Санкт-Петербург / ЛО", 59.93, 30.33, 6, "Северо-Запад"),
            ObservationLocation("msk", "Москва и Подмосковье", 55.75, 37.61, 7, "Центральный"),
            ObservationLocation("novosib", "Новосибирск (НСО)", 55.00, 82.93, 6, "Сибирь")
        )
    }
}
