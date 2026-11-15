package com.example.multipanel.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multipanel.ui.panels.AiPanel
import com.example.multipanel.ui.panels.EditorPanel
import com.example.multipanel.ui.panels.FileManagerPanel
import com.example.multipanel.ui.theme.MultiPanelTheme
import com.example.multipanel.viewmodel.MainViewModel
import com.example.multipanel.viewmodel.PanelType
import com.example.multipanel.viewmodel.SaveState
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MultiPanelScreen(mainViewModel: MainViewModel = viewModel()) {
    MultiPanelTheme {
        MultiPanelContent(mainViewModel)
    }
}

@Composable
private fun MultiPanelContent(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    var leftWeight by rememberSaveable { mutableFloatStateOf(0.4f) }
    var topRightWeight by rememberSaveable { mutableFloatStateOf(0.6f) }
    var bottomRightTopWeight by rememberSaveable { mutableFloatStateOf(0.7f) }

    val bgColor1 = MaterialTheme.colorScheme.background

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
            delay(1500)
        }
    }

    // --- DIALOG POP-UP ---
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

    // --- FOCUS MODE LOGIC ---
    val focused = mainViewModel.focusedPanel

    if (focused != null) {
        Box(Modifier.fillMaxSize().background(bgColor1)) {
            when (focused) {
                PanelType.AI -> PanelWrapper("Web AI (DeepSeek)", onDoubleTap = { mainViewModel.toggleFocus(PanelType.AI) }) {
                    AiPanel(modifier = Modifier.fillMaxSize())
                }
                PanelType.EDITOR -> PanelWrapper("Editor", onDoubleTap = { mainViewModel.toggleFocus(PanelType.EDITOR) }) {
                    EditorPanel(viewModel = mainViewModel)
                }
                PanelType.FILE_MANAGER -> PanelWrapper("File Manager", onDoubleTap = { mainViewModel.toggleFocus(PanelType.FILE_MANAGER) }) {
                    FileManagerPanel(modifier = Modifier.fillMaxSize(), viewModel = mainViewModel)
                }
                PanelType.LOG -> PanelWrapper("Log", onDoubleTap = { mainViewModel.toggleFocus(PanelType.LOG) }) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Text(mainViewModel.logMessage, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    } else {
        // Layout 4 Panel Normal
        Row(Modifier.fillMaxSize().background(bgColor1)) {
            // 1. AI Panel
            Box(Modifier.weight(leftWeight).fillMaxHeight()) {
                PanelWrapper("Web AI (DeepSeek)", onDoubleTap = { mainViewModel.toggleFocus(PanelType.AI) }) {
                    AiPanel(modifier = Modifier.fillMaxSize())
                }
            }

            // VERTICAL DIVIDER (Fixed)
            SmoothDivider(
                isVertical = true,
                modifier = Modifier.fillMaxHeight().width(4.dp),
                onDrag = { delta ->
                    leftWeight = (leftWeight + (delta / screenWidth)).coerceIn(0.15f, 0.85f)
                }
            )

            Column(Modifier.weight(1f - leftWeight).fillMaxHeight()) {
                // 3. Editor Panel
                Box(Modifier.weight(topRightWeight).fillMaxWidth()) {
                    PanelWrapper("Editor", onDoubleTap = { mainViewModel.toggleFocus(PanelType.EDITOR) }) {
                        EditorPanel(viewModel = mainViewModel)
                    }
                }

                // HORIZONTAL DIVIDER (Fixed)
                SmoothDivider(
                    isVertical = false,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    onDrag = { delta ->
                        topRightWeight = (topRightWeight + (delta / screenHeight)).coerceIn(0.2f, 0.8f)
                    }
                )

                Column(Modifier.weight(1f - topRightWeight)) {
                    // 2. File Manager
                    Box(Modifier.weight(bottomRightTopWeight).fillMaxWidth()) {
                        PanelWrapper("File Manager", onDoubleTap = { mainViewModel.toggleFocus(PanelType.FILE_MANAGER) }) {
                            FileManagerPanel(modifier = Modifier.fillMaxSize(), viewModel = mainViewModel)
                        }
                    }

                    // HORIZONTAL DIVIDER (Fixed)
                    SmoothDivider(
                        isVertical = false,
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        onDrag = { delta ->
                            val actualDelta = delta / (screenHeight * (1f - topRightWeight))
                            bottomRightTopWeight = (bottomRightTopWeight + actualDelta).coerceIn(0.1f, 0.9f)
                        }
                    )

                    // 4. Log Panel
                    Box(Modifier.weight(1f - bottomRightTopWeight).fillMaxWidth()) {
                        PanelWrapper("Log", onDoubleTap = { mainViewModel.toggleFocus(PanelType.LOG) }) {
                            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                Text(mainViewModel.logMessage, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PanelWrapper(title: String, onDoubleTap: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

// FIX: Pindahin modifier ukuran ke parameter biar dinamis
@Composable
fun SmoothDivider(
    isVertical: Boolean,
    modifier: Modifier = Modifier,
    onDrag: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(if (isVertical) dragAmount.x else dragAmount.y)
                }
            }
    )
}
