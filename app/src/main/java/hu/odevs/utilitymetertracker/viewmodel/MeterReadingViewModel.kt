package hu.odevs.utilitymetertracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hu.odevs.utilitymetertracker.data.AppDatabase
import hu.odevs.utilitymetertracker.data.MeterReadingEntity
import kotlinx.coroutines.launch
import java.time.LocalDate

class MeterReadingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).meterReadingDao()
    private val _readings = MutableLiveData<List<MeterReadingEntity>>(emptyList())
    val readings: LiveData<List<MeterReadingEntity>> = _readings

    init {
        loadReadings()
    }

    fun loadReadings() {
        viewModelScope.launch {
            _readings.value = dao.getAll()
        }
    }

    fun updateBillAmount(id: Int, amount: Double?) {
        viewModelScope.launch {
            dao.updateBillAmount(id, amount)
            loadReadings()
        }
    }

    fun deleteReading(reading: MeterReadingEntity) {
        viewModelScope.launch {
            dao.delete(reading)
            loadReadings()
        }
    }

    fun saveReading(value: Double, providerId: Int, date: LocalDate) {
        viewModelScope.launch {
            val newReading = MeterReadingEntity(
                value = value,
                providerId = providerId,
                date = date
            )
            dao.insert(newReading)
            loadReadings()
        }
    }

    fun updateImagePath(id: Int, path: String?) {
        viewModelScope.launch {
            val reading = dao.getById(id)
            if (reading != null) {
                val updated = reading.copy(imagePath = path)
                dao.update(updated)
                loadReadings()
            }
        }
    }
}
