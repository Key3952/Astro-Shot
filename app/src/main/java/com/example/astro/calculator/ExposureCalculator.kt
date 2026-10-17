package com.example.astro.calculator

import com.example.astro.model.CameraSensor
import com.example.astro.model.ExposureCalculationResult
import com.example.astro.model.TimelapseResult
import kotlin.math.*

object ExposureCalculator {

    /**
     * Calculates the maximum shutter speed before stars begin to trail.
     *
     * @param focalLengthMm Focal length of the lens in millimeters (e.g. 14, 24, 50)
     * @param aperture F-stop number (e.g. 1.4, 2.8)
     * @param sensor Camera sensor specs
     * @param megapixels Sensor resolution in MP
     * @param declinationDegrees Declination of target in degrees (0 = Celestial Equator, 89 = Polaris)
     */
    fun calculateExposure(
        focalLengthMm: Float,
        aperture: Float,
        sensor: CameraSensor,
        megapixels: Float = sensor.defaultMegapixels,
        declinationDegrees: Float = 0f
    ): ExposureCalculationResult {
        val f = max(1f, focalLengthMm)
        val n = max(0.7f, aperture)
        val p = sensor.pixelPitchMicrons(megapixels)

        // Rule of 500: T = 500 / (f * crop)
        val rule500 = 500f / (f * sensor.cropFactor)

        // Rule of 400 (safer for modern high-res sensors): T = 400 / (f * crop)
        val rule400 = 400f / (f * sensor.cropFactor)

        // Precise NPF Rule formula:
        // T = (16.9 * N + 0.10 * f + 13.7 * p) / (f * cos(declination))
        // Declination in radians
        val decRad = Math.toRadians(declinationDegrees.toDouble().coerceIn(-89.0, 89.0))
        val cosDec = max(0.05, cos(decRad))

        val npfNumerator = (16.9f * n) + (0.10f * f) + (13.7f * p)
        val npfSeconds = (npfNumerator / (f * cosDec)).toFloat()

        // Hyperfocal distance: H = (f^2 / (N * c)) + f
        // in millimeters, converted to meters
        val c = sensor.circleOfConfusionMm
        val hyperfocalMm = ((f * f) / (n * c)) + f
        val hyperfocalMeters = hyperfocalMm / 1000f

        // Recommended ISO guide based on aperture and sensor
        val (recIsoMin, recIsoMax) = when {
            n <= 1.8f -> 1600 to 3200
            n <= 2.8f -> 3200 to 6400
            else -> 6400 to 12800
        }

        val warning = when {
            npfSeconds < 2.0f -> "Короткая выдержка: для слабоосвещенных туманностей потребуется трекер!"
            abs(rule500 - npfSeconds) > 8f -> "Внимание: Правило 500 даст заметный смаз звезд на этом сенсоре, используйте NPF."
            else -> "Оптимальный баланс четкости звезд и накопления фотонов."
        }

        return ExposureCalculationResult(
            npfRuleSeconds = (npfSeconds * 10f).roundToInt() / 10f,
            rule500Seconds = (rule500 * 10f).roundToInt() / 10f,
            rule400Seconds = (rule400 * 10f).roundToInt() / 10f,
            hyperfocalDistanceMeters = (hyperfocalMeters * 100f).roundToInt() / 100f,
            recommendedIsoMin = recIsoMin,
            recommendedIsoMax = recIsoMax,
            starTrailWarning = warning
        )
    }

    /**
     * Calculates timelapse shooting parameters
     *
     * @param targetClipSeconds Desired video duration in seconds (e.g. 15s for Instagram/Reels, 30s for YouTube)
     * @param fps Framerate of final video (24, 30, 60)
     * @param shutterSpeedSeconds Exposure time per photo (e.g. 15s)
     * @param bufferIntervalSeconds Extra buffer between shots for sensor readout and cooling (default 3s)
     * @param rawFileSizeMb Average size per RAW/JPEG file in MB
     */
    fun calculateTimelapse(
        targetClipSeconds: Float,
        fps: Int = 24,
        shutterSpeedSeconds: Float = 15f,
        bufferIntervalSeconds: Float = 3f,
        rawFileSizeMb: Float = 35f
    ): TimelapseResult {
        val photosNeeded = ceil(targetClipSeconds * fps).toInt()
        val intervalSeconds = shutterSpeedSeconds + bufferIntervalSeconds
        val totalShootingSeconds = photosNeeded * intervalSeconds
        val totalDurationMinutes = totalShootingSeconds / 60f
        val storageMb = photosNeeded * rawFileSizeMb

        return TimelapseResult(
            totalPhotos = photosNeeded,
            shootingDurationMinutes = (totalDurationMinutes * 10f).roundToInt() / 10f,
            finalClipDurationSeconds = targetClipSeconds,
            estimatedStorageMb = storageMb,
            recommendedIntervalSeconds = intervalSeconds
        )
    }

    /**
     * Field of View (FOV) calculation
     */
    fun calculateFieldOfView(focalLengthMm: Float, sensor: CameraSensor): Pair<Float, Float> {
        val hFov = 2f * atan(sensor.widthMm / (2f * focalLengthMm)) * (180f / Math.PI.toFloat())
        val vFov = 2f * atan(sensor.heightMm / (2f * focalLengthMm)) * (180f / Math.PI.toFloat())
        return hFov to vFov
    }
}
