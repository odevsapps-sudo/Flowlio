package hu.odevs.utilitymetertracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
