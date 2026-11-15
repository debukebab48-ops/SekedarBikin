package com.example.multipanel.ui.panels

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.multipanel.viewmodel.MainViewModel

@Composable
fun LogPanel(viewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(8.dp)) {
        // Header Log & Tombol Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LOG:", color = Color.Gray, fontSize = 10.sp)
            Button(
                onClick = { viewModel.runPreview() },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("▶️ Preview", fontSize = 11.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Pesan Log
        Text(
            text = viewModel.logMessage,
            color = if (viewModel.logMessage.startsWith("❌") || viewModel.logMessage.startsWith("⚠️")) Color.Red else Color.Green,
            fontSize = 12.sp
        )
    }

    // Tampilan Dialog Preview
    if (viewModel.isPreviewVisible) {
        Dialog(
            onDismissRequest = { viewModel.isPreviewVisible = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // WebView buat nampilin hasil kode
                PreviewWebView(viewModel)
                
                // Tombol Tutup
                Button(
                    onClick = { viewModel.isPreviewVisible = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text("Tutup (X)")
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun PreviewWebView(viewModel: MainViewModel) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.WHITE)
                settings.javaScriptEnabled = true
                
                // Tangkap error JS dan kirim ke ViewModel
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun logError(errorMsg: String) {
                        viewModel.logMessage = "❌ JS Error: $errorMsg"
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Suntik script buat nangkep error console
                        val script = """
                            window.onerror = function(msg, url, line) {
                                Android.logError(msg + " (baris " + line + ")");
                                return false;
                            };
                        """.trimIndent()
                        evaluateJavascript(script, null)
                    }
                }
                
                // Load HTML gabungan dari ViewModel
                loadDataWithBaseURL(null, viewModel.previewHtml, "text/html", "UTF-8", null)
            }
        }
    )
}
