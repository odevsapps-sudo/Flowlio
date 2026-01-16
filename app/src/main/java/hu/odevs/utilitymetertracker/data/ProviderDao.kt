package hu.odevs.utilitymetertracker.data

import androidx.room.*

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers")
    suspend fun getAll(): List<ProviderEntity>

    @Insert
    suspend fun insert(provider: ProviderEntity)

    @Delete
    suspend fun delete(provider: ProviderEntity)
}
