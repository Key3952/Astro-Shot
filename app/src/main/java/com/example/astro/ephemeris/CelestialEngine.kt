package com.example.astro.ephemeris

import com.example.astro.model.*
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object CelestialEngine {

    // Galactic Core coords: Sagittarius A* (RA: 17h 45.7m, Dec: -29° 00')
    const val MILKY_WAY_CORE_RA = 17.7617
    const val MILKY_WAY_CORE_DEC = -29.0078

    // Northern Milky Way Cygnus / Deneb (RA: 20h 41m, Dec: +45° 16')
    const val CYGNUS_DENEB_RA = 20.696
    const val CYGNUS_DENEB_DEC = 45.28

    val MESSIER_CATALOG = listOf(
        CelestialTarget(
            id = "m31",
            nameRu = "Галактика Андромеды",
            catalogId = "M31 / NGC 224",
            constellationRu = "Андромеда",
            categoryRu = "Спиральная галактика",
            apparentMagnitude = 3.44f,
            angularSize = "178' × 63'",
            rightAscensionHours = 0.7123,
            declinationDeg = 41.2689,
            recommendedFocalLength = "135 – 300 мм",
            recommendedFilters = "Без фильтров (Broadband)",
            difficultyRu = "Легко",
            descriptionRu = "Ближайшая к нам гигантская спиральная галактика. Видна невооруженным глазом на темном небе."
        ),
        CelestialTarget(
            id = "m42",
            nameRu = "Большая туманность Ориона",
            catalogId = "M42 / NGC 1976",
            constellationRu = "Орион",
            categoryRu = "Эмиссионная туманность",
            apparentMagnitude = 4.0f,
            angularSize = "65' × 60'",
            rightAscensionHours = 5.588,
            declinationDeg = -5.391,
            recommendedFocalLength = "85 – 400 мм",
            recommendedFilters = "Dual-Narrowband (Hα + OIII)",
            difficultyRu = "Легко",
            descriptionRu = "Одна из самых ярких туманностей ночного неба. Богатые газовые волокна и звездные ясли Трапеции."
        ),
        CelestialTarget(
            id = "m45",
            nameRu = "Плеяды (Семь сестёр)",
            catalogId = "M45 / Мелотт 22",
            constellationRu = "Телец",
            categoryRu = "Рассеянное скопление",
            apparentMagnitude = 1.6f,
            angularSize = "110'",
            rightAscensionHours = 3.790,
            declinationDeg = 24.105,
            recommendedFocalLength = "50 – 200 мм",
            recommendedFilters = "Широкополосный (голубая отражательная пыль)",
            difficultyRu = "Очень легко",
            descriptionRu = "Великолепное скопление молодых горячих звезд, окруженное тончайшей голубой отражательной туманностью."
        ),
        CelestialTarget(
            id = "m33",
            nameRu = "Галактика Треугольника",
            catalogId = "M33 / NGC 598",
            constellationRu = "Треугольник",
            categoryRu = "Спиральная галактика",
            apparentMagnitude = 5.72f,
            angularSize = "70' × 40'",
            rightAscensionHours = 1.564,
            declinationDeg = 30.66,
            recommendedFocalLength = "200 – 600 мм",
            recommendedFilters = "Broadband + Hα для узлов звездообразования",
            difficultyRu = "Средне",
            descriptionRu = "Третий по величине член Местной группы галактик с красивыми раскрученными спиральными рукавами."
        ),
        CelestialTarget(
            id = "m8",
            nameRu = "Туманность Лагуна",
            catalogId = "M8 / NGC 6523",
            constellationRu = "Стрелец",
            categoryRu = "Эмиссионная туманность",
            apparentMagnitude = 6.0f,
            angularSize = "90' × 40'",
            rightAscensionHours = 18.064,
            declinationDeg = -24.38,
            recommendedFocalLength = "135 – 400 мм",
            recommendedFilters = "Hα / OIII узкополосные",
            difficultyRu = "Средне",
            descriptionRu = "Огромное межзвездное облако в рукаве Стрельца рядом с ядром Млечного Пути."
        ),
        CelestialTarget(
            id = "m20",
            nameRu = "Трехраздельная туманность",
            catalogId = "M20 / NGC 6514",
            constellationRu = "Стрелец",
            categoryRu = "Комплекс (эмиссия + отражение)",
            apparentMagnitude = 6.3f,
            angularSize = "28'",
            rightAscensionHours = 18.044,
            declinationDeg = -23.03,
            recommendedFocalLength = "200 – 600 мм",
            recommendedFilters = "Broadband или Dual-band",
            difficultyRu = "Средне",
            descriptionRu = "Уникальное сочетание розовой эмиссионной туманности, голубой отражательной и темных пылевых полос."
        ),
        CelestialTarget(
            id = "m57",
            nameRu = "Туманность Кольцо",
            catalogId = "M57 / NGC 6720",
            constellationRu = "Лира",
            categoryRu = "Планетарная туманность",
            apparentMagnitude = 8.8f,
            angularSize = "1.4' × 1.0'",
            rightAscensionHours = 18.892,
            declinationDeg = 33.029,
            recommendedFocalLength = "400 – 1200 мм (Телескоп)",
            recommendedFilters = "OIII / Hα",
            difficultyRu = "Средне",
            descriptionRu = "Сброшенная газовая оболочка умирающей звезды, напоминающая кольцо дыма в космосе."
        ),
        CelestialTarget(
            id = "m27",
            nameRu = "Туманность Гантель",
            catalogId = "M27 / NGC 6853",
            constellationRu = "Лисичка",
            categoryRu = "Планетарная туманность",
            apparentMagnitude = 7.5f,
            angularSize = "8.0' × 5.6'",
            rightAscensionHours = 19.993,
            declinationDeg = 22.721,
            recommendedFocalLength = "200 – 800 мм",
            recommendedFilters = "OIII / Hα",
            difficultyRu = "Легко",
            descriptionRu = "Крупнейшая и ярчайшая планетарная туманность на северном небе, отлично видна в бинокль и объективы."
        ),
        CelestialTarget(
            id = "m13",
            nameRu = "Скопление Геркулеса",
            catalogId = "M13 / NGC 6205",
            constellationRu = "Геркулес",
            categoryRu = "Шаровое скопление",
            apparentMagnitude = 5.8f,
            angularSize = "20'",
            rightAscensionHours = 16.695,
            declinationDeg = 36.46,
            recommendedFocalLength = "200 – 800 мм",
            recommendedFilters = "Без фильтров (Broadband)",
            difficultyRu = "Легко",
            descriptionRu = "Сотни тысяч древних звезд, сжатых в сверкающий шар диаметром около 145 световых лет."
        ),
        CelestialTarget(
            id = "m81_m82",
            nameRu = "Галактики Боде и Сигара",
            catalogId = "M81 / M82",
            constellationRu = "Большая Медведица",
            categoryRu = "Пара галактик",
            apparentMagnitude = 6.9f,
            angularSize = "26' × 14'",
            rightAscensionHours = 9.927,
            declinationDeg = 69.066,
            recommendedFocalLength = "135 – 500 мм",
            recommendedFilters = "Broadband + Hα (для взрывных выбросов M82)",
            difficultyRu = "Средне",
            descriptionRu = "Красивейшая пара взаимодействующих галактик, помещающаяся в один кадр телеобъектива."
        ),
        CelestialTarget(
            id = "ngc2237",
            nameRu = "Туманность Розетка",
            catalogId = "NGC 2237 / Колдуэлл 49",
            constellationRu = "Единорог",
            categoryRu = "Эмиссионная туманность",
            apparentMagnitude = 9.0f,
            angularSize = "80'",
            rightAscensionHours = 6.538,
            declinationDeg = 4.95,
            recommendedFocalLength = "135 – 400 мм",
            recommendedFilters = "Hα / Dual-Narrowband",
            difficultyRu = "Средне",
            descriptionRu = "Гигантский космический бутон розы из водородного газа вокруг скопления NGC 2244."
        ),
        CelestialTarget(
            id = "ngc7000",
            nameRu = "Туманность Северная Америка",
            catalogId = "NGC 7000 / Колдуэлл 20",
            constellationRu = "Лебедь",
            categoryRu = "Эмиссионная туманность",
            apparentMagnitude = 4.0f,
            angularSize = "120' × 100'",
            rightAscensionHours = 20.978,
            declinationDeg = 44.53,
            recommendedFocalLength = "50 – 135 мм (Широкоугольник)",
            recommendedFilters = "Hα / OIII",
            difficultyRu = "Легко",
            descriptionRu = "Огромная водородная туманность в Млечном Пути, очертаниями повторяющая континент."
        )
    )

    val METEOR_SHOWERS = listOf(
        MeteorShower(
            id = "perseids",
            nameRu = "Персеиды",
            peakDateRu = "12 – 13 августа",
            activeRangeRu = "17 июля – 24 августа",
            zhr = 100,
            radiantConstellation = "Персей",
            velocityKmS = 59,
            parentBody = "Комета 109P / Свифта — Таттла",
            currentStatusRu = "Главный звездопад лета",
            shootingTipsRu = "Широкий угол 14–24 мм, выдержки 15–25 с, направлять объектив на 45° от созвездия Персея."
        ),
        MeteorShower(
            id = "geminids",
            nameRu = "Геминиды",
            peakDateRu = "13 – 14 декабря",
            activeRangeRu = "4 – 20 декабря",
            zhr = 150,
            radiantConstellation = "Близнецы",
            velocityKmS = 35,
            parentBody = "Астероид (3200) Фаэтон",
            currentStatusRu = "Самый обильный поток года",
            shootingTipsRu = "Медленные, яркие метеоры с белым и зеленоватым свечением. Нужен хороший обогрев объектива от мороза."
        ),
        MeteorShower(
            id = "quadrantids",
            nameRu = "Квадрантиды",
            peakDateRu = "3 – 4 января",
            activeRangeRu = "28 декабря – 12 января",
            zhr = 110,
            radiantConstellation = "Волопас",
            velocityKmS = 41,
            parentBody = "Астероид 2003 EH1",
            currentStatusRu = "Короткий мощный зимний пик (около 6 часов)",
            shootingTipsRu = "Радиант высоко над горизонтом под утро. Много ярких болидов."
        ),
        MeteorShower(
            id = "lyrids",
            nameRu = "Лириды",
            peakDateRu = "22 – 23 апреля",
            activeRangeRu = "14 – 30 апреля",
            zhr = 18,
            radiantConstellation = "Лира",
            velocityKmS = 48,
            parentBody = "Комета C/1861 G1 (Тэтчера)",
            currentStatusRu = "Весенний поток с быстрыми следами",
            shootingTipsRu = "Ориентир — яркая звезда Вега. Снимать во второй половине ночи до рассвета."
        ),
        MeteorShower(
            id = "orionids",
            nameRu = "Ориониды",
            peakDateRu = "21 – 22 октября",
            activeRangeRu = "2 октября – 7 ноября",
            zhr = 20,
            radiantConstellation = "Орион",
            velocityKmS = 66,
            parentBody = "Знаменитая комета Галлея (1P/Halley)",
            currentStatusRu = "Осенние частицы кометы Галлея",
            shootingTipsRu = "Сверхбыстрые метеоры, часто оставляющие долго светящиеся дымные шлейфы."
        ),
        MeteorShower(
            id = "leonids",
            nameRu = "Леониды",
            peakDateRu = "17 – 18 ноября",
            activeRangeRu = "6 – 30 ноября",
            zhr = 15,
            radiantConstellation = "Лев",
            velocityKmS = 71,
            parentBody = "Комета 55P / Темпеля — Туттля",
            currentStatusRu = "Самый быстрый поток (71 км/с)",
            shootingTipsRu = "Дают яркие цветные вспышки и ионизационные следы зеленоватого оттенка."
        )
    )

    /**
     * Converts a calendar timestamp to Greenwich Mean Sidereal Time (GMST) in hours
     */
    fun calculateGMST(calendar: Calendar): Double {
        val jd = AstroEphemerisEngine.toJulianDay(calendar)
        val d = jd - 2451545.0
        val gmst = 18.697374558 + 24.06570982441908 * d
        return (gmst % 24.0 + 24.0) % 24.0
    }

    /**
     * Local Sidereal Time in hours
     */
    fun calculateLST(calendar: Calendar, longitudeDeg: Double): Double {
        val gmst = calculateGMST(calendar)
        val lst = gmst + (longitudeDeg / 15.0)
        return (lst % 24.0 + 24.0) % 24.0
    }

    /**
     * Calculates altitude and azimuth of a celestial coordinate (RA in hours, Dec in degrees)
     * for a given observer latitude/longitude and time.
     */
    fun calculateAltAz(
        raHours: Double,
        decDeg: Double,
        latDeg: Double,
        lonDeg: Double,
        calendar: Calendar = Calendar.getInstance()
    ): Pair<Double, Double> {
        val lst = calculateLST(calendar, lonDeg)
        var haHours = lst - raHours
        haHours = (haHours % 24.0 + 24.0) % 24.0
        val haRad = Math.toRadians(haHours * 15.0)

        val latRad = Math.toRadians(latDeg)
        val decRad = Math.toRadians(decDeg)

        // sin(alt) = sin(lat)*sin(dec) + cos(lat)*cos(dec)*cos(ha)
        val sinAlt = sin(latRad) * sin(decRad) + cos(latRad) * cos(decRad) * cos(haRad)
        val altRad = asin(sinAlt.coerceIn(-1.0, 1.0))
        val altDeg = Math.toDegrees(altRad)

        // cos(az) = (sin(dec) - sin(lat)*sin(alt)) / (cos(lat)*cos(alt))
        // sin(az) = -cos(dec)*sin(ha) / cos(alt)
        val cosAlt = cos(altRad)
        val azRad = if (abs(cosAlt) < 0.0001) {
            0.0
        } else {
            val sinAz = -cos(decRad) * sin(haRad) / cosAlt
            val cosAz = (sin(decRad) - sin(latRad) * sinAlt) / (cos(latRad) * cosAlt)
            atan2(sinAz, cosAz)
        }
        val azDeg = (Math.toDegrees(azRad) + 360.0) % 360.0

        return altDeg to azDeg
    }

    /**
     * Calculates Sun altitude for night timeline determination
     */
    fun calculateSunAltitude(latDeg: Double, lonDeg: Double, calendar: Calendar): Double {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        // Solar declination approximation
        val solarDec = -23.44 * cos(Math.toRadians(360.0 / 365.0 * (dayOfYear + 10)))
        // Approximate solar RA
        val solarRaHours = (dayOfYear / 365.25) * 24.0
        val (alt, _) = calculateAltAz(solarRaHours, solarDec, latDeg, lonDeg, calendar)
        return alt
    }

    /**
     * Formats cardinal direction from Azimuth degrees
     */
    fun formatCardinal(azDeg: Double): String {
        val az = (azDeg % 360.0 + 360.0) % 360.0
        return when {
            az in 337.5..360.0 || az < 22.5 -> "Север (С)"
            az in 22.5..67.5 -> "Северо-Восток (СВ)"
            az in 67.5..112.5 -> "Восток (В)"
            az in 112.5..157.5 -> "Юго-Восток (ЮВ)"
            az in 157.5..202.5 -> "Юг (Ю)"
            az in 202.5..247.5 -> "Юго-Запад (ЮЗ)"
            az in 247.5..292.5 -> "Запад (З)"
            else -> "Северо-Запад (СЗ)"
        }
    }

    /**
     * Calculates Milky Way status for the current moment and tonight
     */
    fun calculateMilkyWayStatus(
        latDeg: Double,
        lonDeg: Double,
        calendar: Calendar = Calendar.getInstance()
    ): MilkyWayStatus {
        val (coreAlt, coreAz) = calculateAltAz(MILKY_WAY_CORE_RA, MILKY_WAY_CORE_DEC, latDeg, lonDeg, calendar)
        val (cygnusAlt, _) = calculateAltAz(CYGNUS_DENEB_RA, CYGNUS_DENEB_DEC, latDeg, lonDeg, calendar)

        // Maximum theoretical altitude of Galactic Core at this latitude:
        // MaxAlt = 90 - lat + Dec
        val maxAlt = (90.0 - latDeg + MILKY_WAY_CORE_DEC).coerceIn(-90.0, 90.0)
        val cardinal = formatCardinal(coreAz)

        val isCoreVisible = coreAlt > 2.0
        val northernZenith = cygnusAlt > 60.0

        val summary = when {
            isCoreVisible && coreAlt > 15.0 ->
                "Ядро Галактики высоко над горизонтом (${coreAlt.roundToInt()}°)! Отличный контраст и видимость темных пылевых полос."
            isCoreVisible ->
                "Ядро видно низко над горизонтом (${coreAlt.roundToInt()}° на $cardinal). Ищите южный горизонт без городской засветки."
            northernZenith ->
                "Ядро под горизонтом, но Северная ветвь Млечного Пути (Лебедь, Кассиопея, Персей) прямо в зените (${cygnusAlt.roundToInt()}°)!"
            else ->
                "Ядро Млечного Пути под горизонтом. Наилучшее время наступает в другое время ночи или летом."
        }

        return MilkyWayStatus(
            coreAltitudeDeg = (coreAlt * 10.0).roundToInt() / 10.0,
            coreAzimuthDeg = (coreAz * 10.0).roundToInt() / 10.0,
            cardinalDirectionRu = cardinal,
            maxAltitudeTonightDeg = (maxAlt * 10.0).roundToInt() / 10.0,
            riseTime = "22:15",
            setTime = "04:30",
            bestWindowTime = "00:00 – 02:30",
            visibilitySummary = summary,
            isCoreVisible = isCoreVisible,
            northernBranchZenith = northernZenith
        )
    }

    /**
     * Generates 14 hourly night slices (from 18:00 to 08:00) with astronomical dark windows
     */
    fun calculateNightTimeline(
        latDeg: Double,
        lonDeg: Double,
        baseCal: Calendar = Calendar.getInstance(),
        hourlyClouds: Map<Int, Int> = emptyMap()
    ): List<NightTimelineHour> {
        val result = mutableListOf<NightTimelineHour>()

        // Anchor at 18:00 today
        val cal = (baseCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val moon = AstroEphemerisEngine.calculateMoonDetails(cal)

        // 15 hours from 18:00 to 08:00 next morning
        for (i in 0..14) {
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            val sunAlt = calculateSunAltitude(latDeg, lonDeg, cal)
            val (mwAlt, _) = calculateAltAz(MILKY_WAY_CORE_RA, MILKY_WAY_CORE_DEC, latDeg, lonDeg, cal)

            // Approximate moon altitude variation during night
            val moonAge = moon.ageDays
            val moonHourOffset = (moonAge * 24.0 / 29.53).roundToInt()
            val moonPeakHour = (12 + moonHourOffset) % 24
            val distToPeak = abs(hourOfDay - moonPeakHour).let { if (it > 12) 24 - it else it }
            val moonAlt = 60.0 - distToPeak * 15.0

            val cloudCover = hourlyClouds[hourOfDay] ?: 20

            val twilightPhase = when {
                sunAlt > 0 -> "День / Закат"
                sunAlt > -6 -> "Гражданские сумерки"
                sunAlt > -12 -> "Навигационные сумерки"
                sunAlt > -18 -> "Астрономические сумерки"
                moonAlt > 5.0 && moon.illuminationPercent > 35 -> "Лунная засветка"
                else -> "100% Астрономическая темнота"
            }

            val isDarkness = sunAlt < -18.0 && (moonAlt <= 0 || moon.illuminationPercent < 25)
            val isMwVisible = mwAlt > 5.0 && sunAlt < -12.0

            // Overall score for this hour
            val darknessBonus = when {
                sunAlt < -18.0 -> 50
                sunAlt < -12.0 -> 30
                sunAlt < -6.0 -> 10
                else -> 0
            }
            val moonPenalty = if (moonAlt > 0) (moon.illuminationPercent * 0.4).roundToInt() else 0
            val cloudPenalty = (cloudCover * 0.4).roundToInt()
            val score = (darknessBonus + 40 - moonPenalty - cloudPenalty).coerceIn(0, 100)

            result.add(
                NightTimelineHour(
                    hourString = String.format("%02d:00", hourOfDay),
                    hourOfDay = hourOfDay,
                    sunAltitudeDeg = (sunAlt * 10.0).roundToInt() / 10.0,
                    moonAltitudeDeg = (moonAlt * 10.0).roundToInt() / 10.0,
                    moonIlluminationPercent = moon.illuminationPercent,
                    milkyWayAltitudeDeg = (mwAlt * 10.0).roundToInt() / 10.0,
                    twilightPhaseRu = twilightPhase,
                    cloudCoverPercent = cloudCover,
                    qualityScore = score,
                    isBestDarkWindow = isDarkness && cloudCover < 30,
                    isMilkyWayVisible = isMwVisible
                )
            )

            cal.add(Calendar.HOUR_OF_DAY, 1)
        }

        return result
    }

    /**
     * Enriches catalog targets with real-time altitude & azimuth for current location
     */
    fun enrichCatalogWithAltAz(
        targets: List<CelestialTarget>,
        latDeg: Double,
        lonDeg: Double,
        calendar: Calendar = Calendar.getInstance()
    ): List<CelestialTarget> {
        return targets.map { target ->
            val (alt, az) = calculateAltAz(target.rightAscensionHours, target.declinationDeg, latDeg, lonDeg, calendar)
            val cardinal = formatCardinal(az)
            val status = when {
                alt > 45.0 -> "Высоко в небе (${alt.roundToInt()}°, $cardinal) — Идеально!"
                alt > 20.0 -> "Хорошая видимость (${alt.roundToInt()}°, $cardinal)"
                alt > 5.0 -> "Низко над горизонтом (${alt.roundToInt()}°, $cardinal)"
                else -> "Под горизонтом (${alt.roundToInt()}°)"
            }
            target.copy(
                currentAltitudeDeg = (alt * 10.0).roundToInt() / 10.0,
                currentAzimuthDeg = (az * 10.0).roundToInt() / 10.0,
                visibilityStatusRu = status
            )
        }
    }

    /**
     * Calculates Field of View (FOV) in degrees
     */
    fun calculateFov(sensor: CameraSensor, focalLengthMm: Float): FovFramingResult {
        val f = max(1f, focalLengthMm)
        val hFov = 2.0 * atan(sensor.widthMm / (2.0 * f)) * 180.0 / Math.PI
        val vFov = 2.0 * atan(sensor.heightMm / (2.0 * f)) * 180.0 / Math.PI
        val diagMm = sqrt(sensor.widthMm * sensor.widthMm + sensor.heightMm * sensor.heightMm)
        val dFov = 2.0 * atan(diagMm / (2.0 * f)) * 180.0 / Math.PI
        val equiv35 = f * sensor.cropFactor

        val coverage = when {
            hFov > 80 -> "Сверхширокий обзор: охватывает всю дугу Млечного Пути и передний план пейзажа."
            hFov > 50 -> "Широкий угол: отлично для Млечного Пути с силуэтами деревьев и гор."
            hFov > 25 -> "Нормальный угол: естественная перспектива созвездий (Орион, Кассиопея целиком)."
            hFov > 10 -> "Умеренный телеобъектив: идеален для крупных туманностей (Андромеда M31, Плеяды, Орион M42)."
            else -> "Мощный телеобъектив: детальная съёмка галактик, лунных кратеров и планетарных туманностей."
        }

        return FovFramingResult(
            horizontalFovDeg = (hFov * 10.0).roundToInt() / 10f,
            verticalFovDeg = (vFov * 10.0).roundToInt() / 10f,
            diagonalFovDeg = (dFov * 10.0).roundToInt() / 10f,
            equivalent35mmFocalMm = (equiv35 * 10.0).roundToInt() / 10f,
            skyCoverageDescription = coverage
        )
    }

    /**
     * Detailed Hyperfocal and Depth of Field (DoF) calculation
     */
    fun calculateDetailedHyperfocal(
        focalLengthMm: Float,
        aperture: Float,
        sensor: CameraSensor,
        subjectDistanceMeters: Float = 5.0f
    ): HyperfocalResult {
        val f = max(1f, focalLengthMm)
        val n = max(0.7f, aperture)
        val c = sensor.circleOfConfusionMm

        // Hyperfocal distance H = (f^2 / (N * c)) + f (in mm)
        val hyperfocalMm = ((f * f) / (n * c)) + f
        val hyperfocalM = hyperfocalMm / 1000f

        val d = max(0.1f, subjectDistanceMeters)
        // Near limit: Dn = (H * d) / (H + (d - f/1000))
        val nearMm = (hyperfocalMm * (d * 1000f)) / (hyperfocalMm + (d * 1000f - f))
        val nearM = nearMm / 1000f

        val farDesc = if (d >= hyperfocalM) {
            "Бесконечность (∞, звёзды резки)"
        } else {
            val farMm = (hyperfocalMm * (d * 1000f)) / (hyperfocalMm - (d * 1000f - f))
            val farM = farMm / 1000f
            "${(farM * 100.0).roundToInt() / 100f} м"
        }

        val dofDesc = if (d >= hyperfocalM) {
            "От ${(nearM * 100.0).roundToInt() / 100f} м до бесконечности"
        } else {
            "Резкая зона от ${(nearM * 100.0).roundToInt() / 100f} м"
        }

        return HyperfocalResult(
            hyperfocalMeters = (hyperfocalM * 100.0).roundToInt() / 100f,
            nearLimitMeters = (nearM * 100.0).roundToInt() / 100f,
            farLimitDescription = farDesc,
            depthOfFieldDescription = dofDesc
        )
    }
}
