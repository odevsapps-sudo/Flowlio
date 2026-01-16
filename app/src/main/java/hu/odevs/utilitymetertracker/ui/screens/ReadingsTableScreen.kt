package hu.odevs.utilitymetertracker.ui.screens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import hu.odevs.utilitymetertracker.viewmodel.MeterReadingViewModel
import hu.odevs.utilitymetertracker.viewmodel.ProviderViewModel
import hu.odevs.utilitymetertracker.data.MeterReadingEntity
import java.io.File
import java.io.FileOutputStream
import java.time.temporal.ChronoUnit
import java.util.*
import androidx.compose.foundation.background


@Composable
fun ReadingsTableScreen() {
    val meterReadingViewModel: MeterReadingViewModel = viewModel()
    val providerViewModel: ProviderViewModel = viewModel()
    val context = LocalContext.current

    val providers by providerViewModel.providers.observeAsState(emptyList())
    val readings by meterReadingViewModel.readings.observeAsState(emptyList())

    var selectedProviderId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedProvider = providers.find { it.id == selectedProviderId }

    LaunchedEffect(Unit) {
        providerViewModel.loadProviders()
        meterReadingViewModel.loadReadings()
    }

    val rowColors = listOf(
        Color(0xFFFFF8E1),
        Color(0xFFE1F5FE),
        Color(0xFFE8F5E9),
        Color(0xFFF3E5F5)
    )

    var exportMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFFF3E0).copy(alpha = 0.95f))
        .padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Table of values", style = MaterialTheme.typography.titleLarge)
            Box {
                IconButton(onClick = { exportMenuExpanded = true }) {
                    Icon(Icons.Default.Share, contentDescription = "Export")
                }
                DropdownMenu(
                    expanded = exportMenuExpanded,
                    onDismissRequest = { exportMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Save to CSV") },
                        onClick = {
                            exportMenuExpanded = false
                            selectedProvider?.let {
                                val filtered = readings.filter { r -> r.providerId == it.id }
                                exportToCsv(context, it.name, filtered)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.TableChart, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Save to PDF") },
                        onClick = {
                            exportMenuExpanded = false
                            selectedProvider?.let {
                                val filtered = readings.filter { r -> r.providerId == it.id }
                                exportToPdf(context, it.name, filtered)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp)
            ) {
                Text(text = selectedProvider?.name ?: "Select a Provider")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                providers.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider.name) },
                        onClick = {
                            selectedProviderId = provider.id
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedProvider?.let { provider ->
            val filteredReadings = readings
                .filter { it.providerId == provider.id }
                .sortedBy { it.date }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Date", "Provider", "Value", "Term usage value", "Monthly average YtD", "Daily average in last term", "bill amount").forEach {
                            Text(
                                text = it,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                itemsIndexed(filteredReadings) { index, current ->
                    val previous = filteredReadings.getOrNull(index - 1)
                    val consumption = if (previous != null) current.value - previous.value else 0.0
                    val days = if (previous != null) ChronoUnit.DAYS.between(previous.date, current.date).toDouble() else 0.0
                    val dailyAvg = if (days > 0) consumption / days else 0.0
                    val monthlyAvg = if (index > 0) (current.value - filteredReadings.first().value) / index else 0.0

                    val rowColor = rowColors[index % rowColors.size]

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(rowColor)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(current.date.toString(), Modifier.weight(1f))
                        Text(provider.name, Modifier.weight(1f))
                        Text(current.value.toString(), Modifier.weight(1f))
                        Text(String.format("%.1f", consumption), Modifier.weight(1f))
                        Text(String.format("%.1f", monthlyAvg), Modifier.weight(1f))
                        Text(String.format("%.2f", dailyAvg), Modifier.weight(1f))
                        Text(current.billAmount?.toString() ?: "-", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


fun exportToCsv(context: Context, providerName: String, readings: List<MeterReadingEntity>) {
    val fileName = "${providerName}_readings.csv"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

    try {
        val csvText = StringBuilder()
        // Fejlécek
        csvText.append("Date,Provider,Value,Term,Monthly average,Daily average,Bill\n")

        val sortedReadings = readings.sortedBy { it.date }

        sortedReadings.forEachIndexed { index, current ->
            val previous = sortedReadings.getOrNull(index - 1)
            val consumption = if (previous != null) current.value - previous.value else 0.0
            val days = if (previous != null) ChronoUnit.DAYS.between(previous.date, current.date).toDouble() else 0.0
            val dailyAvg = if (days > 0) consumption / days else 0.0
            val monthlyAvg = if (index > 0) (current.value - sortedReadings.first().value) / index else 0.0

            // Minden mezőt idézőjelek közé teszünk, a számokat ponttal formázzuk
            csvText.append("\"${current.date}\",")
            csvText.append("\"$providerName\",")
            csvText.append("\"${String.format("%.2f", current.value)}\",")
            csvText.append("\"${String.format("%.2f", consumption)}\",")
            csvText.append("\"${String.format("%.2f", monthlyAvg)}\",")
            csvText.append("\"${String.format("%.2f", dailyAvg)}\",")
            csvText.append("\"${current.billAmount?.let { String.format("%.2f", it) } ?: "-"}\"")
            csvText.append("\n")
        }

        FileOutputStream(file).use { it.write(csvText.toString().toByteArray()) }

        Toast.makeText(context, "CSV mentve: ${file.absolutePath}", Toast.LENGTH_LONG).show()

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Megosztás"))

    } catch (e: Exception) {
        Toast.makeText(context, "Hiba történt a mentésnél: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
fun exportToPdf(context: Context, providerName: String, readings: List<MeterReadingEntity>) {
    val fileName = "${providerName}_readings.pdf"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

    try {
        val pdfWriter = PdfWriter(file)
        val pdfDoc = PdfDocument(pdfWriter)
        val document = Document(pdfDoc)

        document.add(Paragraph("Olvasások – $providerName").setFontSize(16f).setBold())

        val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 2f, 2f, 2f, 2f, 2f, 2f)))
            .setWidth(UnitValue.createPercentValue(100f))

        val headers = listOf("Date", "Provider", "Value", "Term", "Monthly avg", "Daily avg", "Bill")
        headers.forEach { header ->
            table.addHeaderCell(Paragraph(header).setFontSize(12f).setBold())
        }

        val sortedReadings = readings.sortedBy { it.date }
        sortedReadings.forEachIndexed { index, current ->
            val previous = sortedReadings.getOrNull(index - 1)
            val consumption = if (previous != null) current.value - previous.value else 0.0
            val days = if (previous != null) ChronoUnit.DAYS.between(previous.date, current.date).toDouble() else 0.0
            val dailyAvg = if (days > 0) consumption / days else 0.0
            val monthlyAvg = if (index > 0) (current.value - sortedReadings.first().value) / index else 0.0

            table.addCell(current.date.toString())
            table.addCell(providerName)
            table.addCell(String.format("%.2f", current.value))
            table.addCell(String.format("%.2f", consumption))
            table.addCell(String.format("%.2f", monthlyAvg))
            table.addCell(String.format("%.2f", dailyAvg))
            table.addCell(current.billAmount?.let { String.format("%.2f", it) } ?: "-")
        }

        document.add(table)
        document.close()

        Toast.makeText(context, "PDF mentve: ${file.absolutePath}", Toast.LENGTH_LONG).show()

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "PDF megosztása"))

    } catch (e: Exception) {
        Toast.makeText(context, "Hiba a PDF mentésekor: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
