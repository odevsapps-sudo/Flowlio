package hu.odevs.utilitymetertracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ProviderEntity::class, MeterReadingEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun meterReadingDao(): MeterReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 🔁 Migráció 1 -> 2: új oszlop a számlakép elérési útjához
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE meter_readings ADD COLUMN imagePath TEXT")
                database.execSQL("ALTER TABLE meter_readings ADD COLUMN billAmount REAL") // 💡 EZ HIÁNYZOTT
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "utility_database"
                )
                    .addMigrations(MIGRATION_1_2) // 🧠 Itt alkalmazzuk a migrációt
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
