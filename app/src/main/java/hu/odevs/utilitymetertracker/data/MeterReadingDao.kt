package hu.odevs.utilitymetertracker.data

import androidx.room.*

@Dao
interface MeterReadingDao {
    @Insert
    suspend fun insert(reading: MeterReadingEntity)

    @Update
    suspend fun update(reading: MeterReadingEntity)

    @Query("SELECT * FROM meter_readings")
    suspend fun getAll(): List<MeterReadingEntity>

    @Query("UPDATE meter_readings SET billAmount = :amount WHERE id = :readingId")
    suspend fun updateBillAmount(readingId: Int, amount: Double?)

    @Delete
    suspend fun delete(reading: MeterReadingEntity)

    @Query("SELECT * FROM meter_readings WHERE id = :id")
    suspend fun getById(id: Int): MeterReadingEntity?

    @Query("UPDATE meter_readings SET imagePath = :imagePath WHERE id = :id")
    suspend fun updateImagePath(id: Int, imagePath: String)
}
