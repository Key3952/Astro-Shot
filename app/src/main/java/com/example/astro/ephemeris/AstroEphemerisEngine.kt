package com.example.astro.ephemeris

import com.example.astro.model.MoonDetails
import com.example.astro.model.MoonPhaseType
import com.example.astro.model.TwilightDetails
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object AstroEphemerisEngine {

    // Known new moon reference: Jan 6, 2000, 18:14 UTC
    private const val REFERENCE_NEW_MOON_JD = 2451549.76
    private const val SYNODIC_MONTH = 29.53058867

    /**
     * Converts a calendar timestamp to Julian Day
     */
    fun toJulianDay(calendar: Calendar): Double {
        val year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1 // 1-12
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)

        val dayFraction = day + (hour + minute / 60.0 + second / 3600.0) / 24.0
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + dayFraction + b - 1524.5
    }

    /**
     * Calculates Moon details for a given date
     */
    fun calculateMoonDetails(calendar: Calendar = Calendar.getInstance()): MoonDetails {
        val jd = toJulianDay(calendar)
        val daysSinceNew = (jd - REFERENCE_NEW_MOON_JD) % SYNODIC_MONTH
        val ageDays = if (daysSinceNew < 0) daysSinceNew + SYNODIC_MONTH else daysSinceNew

        // Moon phase angle (0 to 2PI)
        val phaseAngle = (ageDays / SYNODIC_MONTH) * 2.0 * Math.PI

        // Illumination fraction: (1 - cos(phaseAngle)) / 2
        val illumination = ((1.0 - cos(phaseAngle)) / 2.0) * 100.0
        val illuminationPercent = illumination.roundToInt().coerceIn(0, 100)

        val phaseType = when {
            ageDays < 1.5 || ageDays >= 28.5 -> MoonPhaseType.NEW_MOON
            ageDays < 6.5 -> MoonPhaseType.WAXING_CRESCENT
            ageDays < 8.5 -> MoonPhaseType.FIRST_QUARTER
            ageDays < 13.5 -> MoonPhaseType.WAXING_GIBBOUS
            ageDays < 16.5 -> MoonPhaseType.FULL_MOON
            ageDays < 21.5 -> MoonPhaseType.WANING_GIBBOUS
            ageDays < 23.5 -> MoonPhaseType.LAST_QUARTER
            else -> MoonPhaseType.WANING_CRESCENT
        }

        // Is good for deep sky / milky way?
        val isGoodForAstro = illuminationPercent < 25
        val summary = when {
            illuminationPercent < 10 -> "Идеальная темная ночь: Млечный Путь и дипскай объекты будут максимально контрастными!"
            illuminationPercent < 30 -> "Хорошие условия: слабая лунная засветка позволяет снимать галактики и туманности."
            illuminationPercent < 60 -> "Умеренная засветка: подходит для ночных пейзажей, ярких звёздных треков и планет."
            else -> "Яркая Луна: сильная засветка неба. Отличное время для съёмки самой Луны и деталей кратеров!"
        }

        // Approximate moonrise/moonset offset by age
        val hourOffset = (ageDays * 24.0 / SYNODIC_MONTH).roundToInt()
        val riseHour = (6 + hourOffset) % 24
        val setHour = (18 + hourOffset) % 24

        val riseStr = String.format("%02d:%02d", riseHour, 15)
        val setStr = String.format("%02d:%02d", setHour, 45)

        return MoonDetails(
            phaseType = phaseType,
            illuminationPercent = illuminationPercent,
            ageDays = (ageDays * 10.0).roundToInt() / 10f,
            moonriseTime = riseStr,
            moonsetTime = setStr,
            isGoodForAstro = isGoodForAstro,
            astroSummary = summary
        )
    }

    /**
     * Calculates twilight and solar events for location
     */
    fun calculateTwilight(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance()
    ): TwilightDetails {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Solar declination approximation: -23.44 * cos(2pi * (N + 10) / 365)
        val declination = -23.44 * cos(Math.toRadians(360.0 / 365.0 * (dayOfYear + 10)))
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)

        fun hourAngle(altitudeDeg: Double): Double {
            val altRad = Math.toRadians(altitudeDeg)
            val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 180.0 else Math.toDegrees(acos(cosH))
        }

        // Solar Noon approx (in hours local time)
        val tzOffset = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0
        val solarNoon = 12.0 - (longitude / 15.0) + tzOffset

        // Sunset/Sunrise (Sun at -0.833°)
        val sunsetH = hourAngle(-0.833) / 15.0
        val sunsetTime = solarNoon + sunsetH
        val sunriseTime = solarNoon - sunsetH

        // Golden Hour (Sun between +6° and -4°)
        val goldenStartH = hourAngle(6.0) / 15.0
        val goldenEndH = hourAngle(-4.0) / 15.0

        // Blue Hour (Sun between -4° and -6°)
        val blueEndH = hourAngle(-6.0) / 15.0

        // Astronomical Twilight (Sun reaches -18°)
        val astroTwilightH = hourAngle(-18.0) / 15.0
        val astroDusk = solarNoon + astroTwilightH
        val astroDawn = (solarNoon - astroTwilightH + 24.0) % 24.0

        // Dark sky duration: from astroDusk to next astroDawn
        val darkSkyDuration = if (astroTwilightH > 0.1) {
            val duration = (24.0 - astroDusk + astroDawn).coerceIn(0.0, 14.0)
            duration.toFloat()
        } else {
            0f // White nights / polar day
        }

        fun formatHour(h: Double): String {
            val validH = (h % 24.0 + 24.0) % 24.0
            val hr = floor(validH).toInt()
            val min = floor((validH - hr) * 60.0).toInt()
            return String.format("%02d:%02d", hr, min)
        }

        return TwilightDetails(
            goldenHourStart = formatHour(solarNoon + goldenStartH),
            goldenHourEnd = formatHour(solarNoon + goldenEndH),
            blueHourStart = formatHour(solarNoon + goldenEndH),
            blueHourEnd = formatHour(solarNoon + blueEndH),
            sunsetTime = formatHour(sunsetTime),
            sunriseTime = formatHour(sunriseTime),
            astroTwilightStart = formatHour(astroDusk),
            astroTwilightEnd = formatHour(astroDawn),
            darkSkyDurationHours = (darkSkyDuration * 10f).roundToInt() / 10f
        )
    }

    /**
     * Generates moon phases for the next 30 days
     */
    fun getMoonCalendar(startDate: Calendar = Calendar.getInstance()): List<Pair<Calendar, MoonDetails>> {
        val result = mutableListOf<Pair<Calendar, MoonDetails>>()
        for (i in 0 until 30) {
            val dayCal = (startDate.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, i)
            }
            val details = calculateMoonDetails(dayCal)
            result.add(dayCal to details)
        }
        return result
    }
}
