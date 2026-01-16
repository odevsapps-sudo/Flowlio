package hu.odevs.utilitymetertracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.odevs.utilitymetertracker.viewmodel.MeterReadingViewModel
import hu.odevs.utilitymetertracker.viewmodel.ProviderViewModel
import hu.odevs.utilitymetertracker.data.MeterReadingEntity
import hu.odevs.utilitymetertracker.data.ProviderEntity
import androidx.compose.runtime.livedata.observeAsState
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background

@Composable
fun FilteredReadingsTableScreen() {
    val readingViewModel: MeterReadingViewModel = viewModel()
    val providerViewModel: ProviderViewModel = viewModel()

    val readings by readingViewModel.readings.observeAsState(emptyList())
    val providers by providerViewModel.providers.observeAsState(emptyList())

    var selectedProvider by remember { mutableStateOf<ProviderEntity?>(null) }

    LaunchedEffect(Unit) {
        providerViewModel.loadProviders()
        readingViewModel.loadReadings()
    }

    Column(modifier = Modifier
        .background(Color(0xFFFFF3E0).copy(alpha = 0.95f))
        .padding(16.dp)) {
        Text("Meter Readings Analysis", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Szolgáltató választás
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedProvider?.name ?: "Select a Provider",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                label = { Text("Provider") },
                enabled = false
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                providers.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider.name) },
                        onClick = {
                            selectedProvider = provider
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedProvider?.let { provider ->
            val filtered = readings.filter { it.providerId == provider.id }.sortedBy { it.date }

            var previousValue: Double? = null
            var previousDate = filtered.firstOrNull()?.date

            LazyColumn {
                items(filtered) { reading ->
                    val fogyasztas = if (previousValue != null) reading.value - previousValue!! else null
                    val napok = if (previousDate != null) ChronoUnit.DAYS.between(previousDate, reading.date).toInt() else null
                    val napiAtlag = if (fogyasztas != null && napok != null && napok > 0) fogyasztas / napok else null

                    val elozmenyek = filtered.filter { it.date <= reading.date }
                    val datumtol = elozmenyek.firstOrNull()?.date
                    val honapok = if (datumtol != null) ChronoUnit.MONTHS.between(datumtol, reading.date).coerceAtLeast(1) else 1
                    val haviAtlag = (elozmenyek.last().value - elozmenyek.first().value) / honapok.toDouble()

                    previousValue = reading.value
                    previousDate = reading.date

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Date: ${reading.date}")
                        Text("Provider: ${provider.name}")
                        Text("Value: ${reading.value}")
                        Text("Period Consumption: ${fogyasztas?.let { String.format("%.2f", it) } ?: "-"}")
                        Text("Monthly average: ${String.format("%.2f", haviAtlag)}")
                        Text("Daily average: ${napiAtlag?.let { String.format("%.2f", it) } ?: "-"}")
                        Text("Sum: ${reading.billAmount?.let { "${it} Ft" } ?: "-"}")
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
