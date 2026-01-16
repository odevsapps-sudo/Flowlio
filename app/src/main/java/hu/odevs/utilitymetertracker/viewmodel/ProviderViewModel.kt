package hu.odevs.utilitymetertracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hu.odevs.utilitymetertracker.data.ProviderEntity
import hu.odevs.utilitymetertracker.data.ProvidersDatabase
import kotlinx.coroutines.launch

class ProviderViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ProvidersDatabase.getDatabase(application).providerDao()
    private val _providers = MutableLiveData<List<ProviderEntity>>(emptyList())
    val providers: LiveData<List<ProviderEntity>> = _providers

    fun loadProviders() {
        viewModelScope.launch {
            _providers.value = dao.getAll()
        }
    }

    fun addProvider(name: String) {
        viewModelScope.launch {
            dao.insert(ProviderEntity(name = name))
            loadProviders()
        }
    }

    fun deleteProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            dao.delete(provider)
            loadProviders()
        }
    }
}
