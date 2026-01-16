package hu.odevs.utilitymetertracker.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import hu.odevs.utilitymetertracker.data.ProviderEntity
import hu.odevs.utilitymetertracker.viewmodel.MeterReadingViewModel
import hu.odevs.utilitymetertracker.viewmodel.ProviderViewModel
import hu.odevs.utilitymetertracker.ui.components.FancyButton
import java.time.LocalDate
import hu.odevs.utilitymetertracker.ui.screens.ReadingsListScreen
import androidx.compose.foundation.background



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val meterReadingViewModel: MeterReadingViewModel = viewModel()
    val providerViewModel: ProviderViewModel = viewModel()

    var value by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<ProviderEntity?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val providers by providerViewModel.providers.observeAsState(emptyList())
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        providerViewModel.loadProviders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0).copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Reading Value") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedProvider?.name ?: "",
                onValueChange = {},
                label = { Text("Provider") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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

        Spacer(modifier = Modifier.height(12.dp))

        FancyButton(
            icon = Icons.Default.DateRange,
            text = "Date selection: $selectedDate",
            backgroundColor = Color(0xFFABC270),
            onClick = {
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    },
                    selectedDate.year,
                    selectedDate.monthValue - 1,
                    selectedDate.dayOfMonth
                )
                datePicker.show()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        FancyButton(
            icon = Icons.Default.Save,
            text = "Save",
            backgroundColor = Color(0xFFFDA769),
            onClick = {
                val v = value.toDoubleOrNull()
                if (v != null && selectedProvider != null) {
                    meterReadingViewModel.saveReading(v, selectedProvider!!.id, selectedDate)
                    Toast.makeText(context, "Save completed!", Toast.LENGTH_SHORT).show()
                    value = ""
                } else {
                    Toast.makeText(context, "All fields must be filled!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        FancyButton(
            icon = Icons.Default.PersonAdd,
            text = "Add Provider",
            backgroundColor = Color(0xFFFEC868),
            onClick = { navController.navigate("providers") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FancyButton(
            icon = Icons.Default.TableChart,
            text = "Meter Readings Overview",
            backgroundColor = Color(0xFF76C7C0),
            onClick = { navController.navigate("readings_table") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FancyButton(
            icon = Icons.Default.FilterList,
            text = "Filtered Consumption Table",
            backgroundColor = Color(0xFFB39DDB),
            onClick = { navController.navigate("filtered_readings_table") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FancyButton(
            icon = Icons.Default.List,
            text = "Reading List",
            backgroundColor = Color(0xFFABC270),
            onClick = { navController.navigate("reading_list_screen") }
        )
    }
}
