package com.example.astro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TargetSessionEntity::class, ChecklistItemEntity::class], version = 1, exportSchema = false)
abstract class AstroDatabase : RoomDatabase() {

    abstract fun astroDao(): AstroDao

    companion object {
        @Volatile
        private var INSTANCE: AstroDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AstroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroDatabase::class.java,
                    "astroshot_database"
                )
                    .addCallback(AstroDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AstroDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.astroDao())
                }
            }
        }

        suspend fun populateInitialData(dao: AstroDao) {
            // Initial sessions
            dao.insertSession(
                TargetSessionEntity(
                    title = "Ядро Млечного Пути и туманность Лагуна",
                    targetType = "Млечный Путь",
                    locationName = "Архыз (САО РАН, Кавказ)",
                    targetDate = "Новолуние, Июль",
                    notes = "Широкий угол, 15 кадров для стека в Sequator / Siril. Фокус по яркой звезде Вега.",
                    focalLength = 14f,
                    aperture = 2.8f,
                    iso = 3200,
                    subExposureSeconds = 20f,
                    subCount = 20,
                    isCompleted = false
                )
            )
            dao.insertSession(
                TargetSessionEntity(
                    title = "Галактика Андромеды (M31)",
                    targetType = "Галактика",
                    locationName = "Чемал (Алтай)",
                    targetDate = "Ясная ночь без Луны",
                    notes = "Светосильный телеобъектив 135мм или 200мм. Снять калибровочные кадры (Dark, Flat, Bias).",
                    focalLength = 135f,
                    aperture = 2.0f,
                    iso = 1600,
                    subExposureSeconds = 60f,
                    subCount = 60,
                    isCompleted = false
                )
            )
            dao.insertSession(
                TargetSessionEntity(
                    title = "Северное сияние над Териберкой",
                    targetType = "Сияние",
                    locationName = "Териберка (Баренцево море)",
                    targetDate = "Октябрь (Kp > 4.5)",
                    notes = "Широкий угол 20мм f/1.8, быстрая серия кадров для фиксации лучей и корон сияния.",
                    focalLength = 20f,
                    aperture = 1.8f,
                    iso = 2500,
                    subExposureSeconds = 4f,
                    subCount = 120,
                    isCompleted = false
                )
            )

            // Essential astrophotography field checklist
            val checklist = listOf(
                ChecklistItemEntity(title = "Запасные аккумуляторы для камеры (минимум 3 шт)", category = "Питание"),
                ChecklistItemEntity(title = "Грелка для объектива (Dew Heater) + Powerbank", category = "Оптика"),
                ChecklistItemEntity(title = "Устойчивый штатив и утяжелитель на крюк", category = "Монтировка"),
                ChecklistItemEntity(title = "Пульт ДУ / интервалометр с запасной батарейкой", category = "Оптика"),
                ChecklistItemEntity(title = "Салфетки из микрофибры и груша для продувки", category = "Оптика"),
                ChecklistItemEntity(title = "Снять защитный UV/Sky-фильтр (во избежание бликов)", category = "Оптика"),
                ChecklistItemEntity(title = "Красный налобный фонарик (сохраняет ночное зрение)", category = "Комфорт"),
                ChecklistItemEntity(title = "Тёплая одежда, перчатки с откидными пальцами, термос", category = "Комфорт"),
                ChecklistItemEntity(title = "Быстрые SD-карты памяти с запасом свободного места", category = "Питание")
            )
            dao.insertChecklistItems(checklist)
        }
    }
}
