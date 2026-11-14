package com.example.multipanel.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multipanel.ui.panels.AiPanel
import com.example.multipanel.ui.panels.EditorPanel
import com.example.multipanel.ui.panels.FileManagerPanel
import com.example.multipanel.viewmodel.MainViewModel
import com.example.multipanel.viewmodel.SaveState
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MultiPanelScreen(mainViewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    var leftWeight by rememberSaveable { mutableFloatStateOf(0.4f) }
    var topRightWeight by rememberSaveable { mutableFloatStateOf(0.6f) }
    var bottomRightTopWeight by rememberSaveable { mutableFloatStateOf(0.7f) }

    val dividerColor = Color(0xFF333333)
    val bgColor1 = Color(0xFF1E1E1E)
    val bgColor2 = Color(0xFF252526)

    // --- PEMANTAU CLIPBOARD OTOMATIS ---
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    LaunchedEffect(Unit) {
        while (true) {
            if (clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString() ?: ""
                    if (text.isNotBlank()) {
                        mainViewModel.processClipboard(text)
                    }
                }
            }
            delay(1500) // Cek tiap 1,5 detik
        }
    }

    // --- DIALOG POP-UP KALAU SISTEM RAGU ---
    when (val state = mainViewModel.saveState) {
        is SaveState.NeedFolder -> {
            AlertDialog(
                onDismissRequest = { mainViewModel.saveState = SaveState.Idle },
                title = { Text("Pilih Folder") },
                text = { Text("Sistem tidak yakin ini kode apa. Simpan di folder mana?") },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { mainViewModel.handleFolderSelected("css", state.code) }) { Text("CSS") }
                        Button(onClick = { mainViewModel.handleFolderSelected("js", state.code) }) { Text("JS") }
                        Button(onClick = { mainViewModel.handleFolderSelected("html", state.code) }) { Text("HTML") }
                    }
                }
            )
        }
        is SaveState.NeedName -> {
            var inputName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { mainViewModel.saveState = SaveState.Idle },
                title = { Text("Nama File") },
                text = {
                    Column {
                        Text("Nama file tidak ditemukan di kode. Ketik nama filenya (tanpa titik):")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            singleLine = true,
                            placeholder = { Text("contoh: style") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            if (inputName.isNotBlank()) {
                                mainViewModel.doSave(state.folder, "$inputName.${state.folder}", state.code) 
                            }
                        }
                    ) { Text("Simpan") }
                }
            )
        }
        else -> {}
    }

    // --- LAYOUT UTAMA ---
    Row(Modifier.fillMaxSize().background(bgColor1)) {
        // 1. AI Panel
        Box(Modifier.weight(leftWeight).fillMaxHeight().background(bgColor2)) {
            AiPanel(modifier = Modifier.fillMaxSize())
        }

        Box(
            Modifier.fillMaxHeight().width(4.dp).background(dividerColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        leftWeight = (leftWeight + (dragAmount.x / screenWidth)).coerceIn(0.15f, 0.85f)
                    }
                }
        )

        Column(Modifier.weight(1f - leftWeight).fillMaxHeight()) {
            // 3. Editor Panel
            Box(Modifier.weight(topRightWeight).fillMaxWidth()) {
                EditorPanel(viewModel = mainViewModel)
            }

            Box(
                Modifier.fillMaxWidth().height(4.dp).background(dividerColor)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            topRightWeight = (topRightWeight + (dragAmount.y / screenHeight)).coerceIn(0.2f, 0.8f)
                        }
                    }
            )

            Column(Modifier.weight(1f - topRightWeight)) {
                // 2. File Manager
                Box(Modifier.weight(bottomRightTopWeight).fillMaxWidth().background(bgColor2)) {
                    FileManagerPanel(modifier = Modifier.fillMaxSize(), viewModel = mainViewModel)
                }

                Box(
                    Modifier.fillMaxWidth().height(4.dp).background(dividerColor)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val delta = dragAmount.y / (screenHeight * (1f - topRightWeight))
                                bottomRightTopWeight = (bottomRightTopWeight + delta).coerceIn(0.1f, 0.9f)
                            }
                        }
                )

                // 4. Log Panel
                Box(Modifier.weight(1f - bottomRightTopWeight).fillMaxWidth().background(bgColor1)) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Text("LOG:", color = Color.Gray, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mainViewModel.logMessage,
                            color = if (mainViewModel.logMessage.startsWith("❌")) Color.Red else Color.Green,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
