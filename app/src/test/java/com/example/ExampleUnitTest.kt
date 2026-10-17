package com.example

import com.example.astro.calculator.ExposureCalculator
import com.example.astro.ephemeris.AstroEphemerisEngine
import com.example.astro.model.CameraSensor
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun testExposureNPFAndRule500() {
        val sensor = CameraSensor.PRESETS[0] // Full Frame 24MP
        val result = ExposureCalculator.calculateExposure(
            focalLengthMm = 24f,
            aperture = 1.8f,
            sensor = sensor,
            megapixels = 24f,
            declinationDegrees = 0f
        )

        // Rule of 500 for 24mm FF should be 500 / 24 = 20.8s
        assertEquals(20.8f, result.rule500Seconds, 0.5f)

        // NPF rule should be positive and realistic (around 4.8s for 24mm f/1.8 on 24MP Full Frame)
        assertTrue(result.npfRuleSeconds in 3.0f..15.0f)

        // Hyperfocal distance should be positive
        assertTrue(result.hyperfocalDistanceMeters > 5f)
    }

    @Test
    fun testTimelapseCalculator() {
        val timelapse = ExposureCalculator.calculateTimelapse(
            targetClipSeconds = 10f,
            fps = 24,
            shutterSpeedSeconds = 15f,
            bufferIntervalSeconds = 3f,
            rawFileSizeMb = 30f
        )

        // 10s * 24fps = 240 photos
        assertEquals(240, timelapse.totalPhotos)
        // Interval = 15 + 3 = 18s. Total seconds = 240 * 18 = 4320s = 72 minutes
        assertEquals(72f, timelapse.shootingDurationMinutes, 1.0f)
    }

    @Test
    fun testMoonEphemerisCalculation() {
        val cal = Calendar.getInstance()
        val moon = AstroEphemerisEngine.calculateMoonDetails(cal)

        assertNotNull(moon.phaseType)
        assertTrue(moon.illuminationPercent in 0..100)
        assertTrue(moon.ageDays in 0f..30f)
        assertTrue(moon.astroSummary.isNotBlank())
    }

    @Test
    fun testTwilightCalculation() {
        val twilight = AstroEphemerisEngine.calculateTwilight(
            latitude = 55.75, // Moscow
            longitude = 37.61
        )

        assertNotNull(twilight.goldenHourStart)
        assertNotNull(twilight.blueHourStart)
        assertNotNull(twilight.astroTwilightStart)
        assertTrue(twilight.darkSkyDurationHours >= 0f)
    }
}
