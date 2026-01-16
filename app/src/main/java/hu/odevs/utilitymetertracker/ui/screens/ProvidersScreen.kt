package hu.odevs.utilitymetertracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import hu.odevs.utilitymetertracker.viewmodel.ProviderViewModel
import hu.odevs.utilitymetertracker.data.ProviderEntity
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background

@Composable
fun ProvidersScreen(navController: NavController) {
    val providerViewModel: ProviderViewModel = viewModel()
    val providers by providerViewModel.providers.observeAsState(emptyList())
    var newProviderName by remember { mutableStateOf("") }
    var selectedProviderForDeletion by remember { mutableStateOf<ProviderEntity?>(null) }

    LaunchedEffect(Unit) {
        providerViewModel.loadProviders()
    }

    Column(modifier = Modifier
        .background(Color(0xFFFFF3E0).copy(alpha = 0.95f))
        .padding(16.dp)) {
        Text(text = "List of Providers", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newProviderName,
                onValueChange = { newProviderName = it },
                label = { Text("Name of the new Provider") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newProviderName.isNotBlank()) {
                    providerViewModel.addProvider(newProviderName)
                    newProviderName = ""
                }
            }) {
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(providers) { provider: ProviderEntity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = provider.name, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = {
                        selectedProviderForDeletion = provider
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.popBackStack()
        }, modifier = Modifier.align(Alignment.End)) {
            Text("Back")
        }
    }

    selectedProviderForDeletion?.let { provider ->
        AlertDialog(
            onDismissRequest = { selectedProviderForDeletion = null },
            confirmButton = {
                TextButton(onClick = {
                    providerViewModel.deleteProvider(provider)
                    selectedProviderForDeletion = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedProviderForDeletion = null
                }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Provider") },
            text = { Text("Are you sure you want to delete: \"${provider.name}\"?") }
        )
    }
}
