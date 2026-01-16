package hu.odevs.utilitymetertracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProviderEntity::class], version = 1, exportSchema = false)
abstract class ProvidersDatabase : RoomDatabase() {
    abstract fun providerDao(): ProviderDao

    companion object {
        @Volatile
        private var INSTANCE: ProvidersDatabase? = null

        fun getDatabase(context: Context): ProvidersDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProvidersDatabase::class.java,
                    "providers_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
