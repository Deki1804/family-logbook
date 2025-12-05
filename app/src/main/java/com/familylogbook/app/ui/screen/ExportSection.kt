package com.familylogbook.app.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportSection(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showImportDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<String?>(null) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                        val jsonString = inputStream?.bufferedReader().use { it?.readText() ?: "" }
                        
                        if (jsonString.isNotEmpty()) {
                            val result = viewModel.importFromJson(jsonString)
                            when (result) {
                                is SettingsViewModel.ImportResult.Success -> {
                                    importResult = "Uspješno uvezeno ${result.childrenAdded} djece i ${result.entriesAdded} zapisa"
                                }
                                is SettingsViewModel.ImportResult.Error -> {
                                    importResult = "Greška: ${result.message}"
                                }
                            }
                            showImportDialog = true
                        }
                    } catch (e: Exception) {
                        importResult = "Greška pri čitanju datoteke: ${e.message}"
                        showImportDialog = true
                    }
                }
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Izvoz i uvoz",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Izvezi podatke svog dnevnika u JSON ili CSV formatu, ili uvezi iz JSON sigurnosne kopije",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            exportAndShare(context, viewModel.exportToJson(), "family-logbook.json", "application/json")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JSON", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            exportAndShare(context, viewModel.exportToCsv(), "family-logbook.csv", "text/csv")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontSize = 12.sp)
                }
            }
            
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "application/json"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    filePickerLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Uvezi iz JSON")
            }
        }
    }
    
    // Import result dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Rezultat uvoza") },
            text = { Text(importResult ?: "Nepoznat rezultat") },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("U redu")
                }
            }
        )
    }
}

private fun exportAndShare(context: Context, content: String, filename: String, mimeType: String) {
    try {
        // Create file in cache directory
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "${filename.replace(".", "_$timestamp.")}")
        FileWriter(file).use { it.write(content) }
        
        // Get URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        // Share file
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Izvoz Obiteljskog dnevnika")
            putExtra(Intent.EXTRA_TEXT, "Izvoz podataka Obiteljskog dnevnika")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Izvezi Obiteljski dnevnik"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
