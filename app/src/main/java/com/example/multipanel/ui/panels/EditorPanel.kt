package com.example.multipanel.ui.panels

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.multipanel.viewmodel.MainViewModel
import androidx.compose.ui.platform.LocalContext
@Composable
fun EditorPanel(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var showPreviewDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        
        // --- TOP BAR EDITOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nama File yang lagi diedit
            Text(
                text = viewModel.currentFile?.name ?: "Tidak ada file yang dibuka",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1
            )

            // Tombol Run/Preview
            if (viewModel.currentFile != null) {
                IconButton(onClick = { showPreviewDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run/Preview",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- AREA TEXT EDITOR ---
        Box(modifier = Modifier.weight(1f).padding(8.dp)) {
            BasicTextField(
                value = viewModel.fileContent,
                onValueChange = { viewModel.fileContent = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                // (Nanti di step berikutnya kita tambahin line numbers & no wrap)
            )
        }
    }

    // --- PREVIEW DIALOG ---
    if (showPreviewDialog) {
        val currentExtension = viewModel.currentFile?.extension?.lowercase()
        val content = viewModel.fileContent
        
        // 3. Penanganan HTML/CSS/JS
        val htmlToRender = remember(content, currentExtension) {
            generateHtmlPreview(content, currentExtension)
        }

        Dialog(
            onDismissRequest = { showPreviewDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen dialog
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black // Atau MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Dialog kecil buat nutup
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF333333)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Preview", color = Color.White, fontSize = 14.sp)
                        TextButton(onClick = { showPreviewDialog = false }) {
                            Text("Tutup", color = Color.Red)
                        }
                    }

                    // WebView Preview
                    PreviewWebView(
                        htmlContent = htmlToRender,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Fungsi Helper buat bungkus kode sesuai ekstensi
fun generateHtmlPreview(content: String, extension: String?): String {
    return when (extension) {
        "html" -> content
        "css" -> """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>$content</style>
            </head>
            <body>
                <h2>CSS Preview</h2>
                <p>Ini adalah paragraf dummy untuk ngetes styling.</p>
                <button>Tombol Dummy</button>
            </body>
            </html>
        """.trimIndent()
        "js" -> """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body { font-family: sans-serif; padding: 16px; }</style>
            </head>
            <body>
                <h2>JS Preview</h2>
                <p>Cek Logcat / console buat liat output JS.</p>
                <div id="output"></div>
                <script>
                    try {
                        $content
                    } catch (e) {
                        document.getElementById('output').innerText = "Error: " + e.message;
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        else -> "<html><body><h2>Preview tidak tersedia untuk tipe file ini.</h2></body></html>"
    }
}

// Composable terpisah buat WebView biar lifecycle-nya rapi
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewWebView(htmlContent: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
        update = { view ->
            // 2. Pake loadDataWithBaseURL sesuai request
            view.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    )

    // 4. Lifecycle: Destroy WebView pas dialog ditutup
    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }
}
