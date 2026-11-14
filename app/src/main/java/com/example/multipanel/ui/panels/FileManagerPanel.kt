package com.example.multipanel.ui.panels

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.multipanel.data.FileRepository
import com.example.multipanel.viewmodel.MainViewModel
import java.io.File

@Composable
fun FileManagerPanel(modifier: Modifier = Modifier, viewModel: com.example.multipanel.viewmodel.MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { FileRepository() }

    var hasPermission by remember { mutableStateOf(Environment.isExternalStorageManager()) }
    var currentPath by remember { mutableStateOf(Environment.getExternalStorageDirectory().absolutePath) }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = Environment.isExternalStorageManager()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Baca file tiap kali path berubah ATAU ada file baru disimpan
    LaunchedEffect(hasPermission, currentPath, viewModel.refreshTrigger) {
        if (hasPermission) {
            files = repository.getFiles(currentPath)
        }
    }

    // AUTO-PINDAH FOLDER: Begitu file disimpan, File Manager pindah ke folder itu
    LaunchedEffect(viewModel.lastSavedFolder) {
        viewModel.lastSavedFolder?.let { newPath ->
            currentPath = newPath
            viewModel.lastSavedFolder = null // Reset biar nggak loop
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF252526))) {
        if (!hasPermission) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Butuh akses penyimpanan", color = Color.White, fontSize = 12.sp)
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Beri Izin")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = currentPath,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1
                )
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (currentPath != Environment.getExternalStorageDirectory().absolutePath) {
                        // Tombol Ke Root (Penyimpanan Utama)
                        item {
                            Text(
                                text = "🏠 Ke Penyimpanan Utama",
                                color = Color(0xFF4CAF50), // Warna hijau
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentPath = Environment.getExternalStorageDirectory().absolutePath
                                    }
                                    .padding(12.dp)
                            )
                        }

                        // Tombol kembali
                        item {
                            Text(
                                text = "⬆️ .. (Kembali)",
                                color = Color.Cyan,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val parent = File(currentPath).parent
                                        if (parent != null) currentPath = parent
                                    }
                                    .padding(12.dp)
                            )
                        }
                    }

                    // List file/folder
                    items(files) { file ->
                        Text(
                            text = (if (file.isDirectory) "📁 " else "📄 ") + file.name,
                            color = if (file.isDirectory) Color(0xFFDCB67A) else Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (file.isDirectory) {
                                        currentPath = file.absolutePath
                                    } else {
                                        viewModel.openFile(file)
                                    }
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
