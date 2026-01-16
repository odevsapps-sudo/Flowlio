package hu.odevs.utilitymetertracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "meter_readings")
data class MeterReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val value: Double,
    val providerId: Int,
    val date: LocalDate,
    val billAmount: Double? = null,
    val imagePath: String? = null
)
