package com.example.multipanel.ui.panels

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiPanel(modifier: Modifier = Modifier) {
    // Simpen instance WebView biar bisa diakses dari luar factory
    var webViewInstance: WebView? = null

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        
        // Header Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Web AI (DeepSeek)",
                color = Color.LightGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        // WebView
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // Kalau panel digeser, kirim sinyal 'resize' ke WebView
                .onSizeChanged { 
                    webViewInstance?.evaluateJavascript("window.dispatchEvent(new Event('resize'));", null)
                },
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.Black.toArgb())
                    
                    // Konfigurasi dasar
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    
                    // --- SETTINGAN KUNCI BIAR RESPONSIF ---
                    // Paksa lebar web ngikutin panel, bukan ngikutin layar HP
                    settings.useWideViewPort = false
                    settings.loadWithOverviewMode = false
                    
                    // Paksa User Agent jadi Mobile
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"

                    // Matiin zoom biar gak muncul tombol aneh
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Suntik JavaScript: Paksa meta viewport biar web-nya ngecil (reflow)
                            val js = """
                                var meta = document.querySelector('meta[name=viewport]');
                                if (!meta) {
                                    meta = document.createElement('meta');
                                    meta.name = 'viewport';
                                    document.getElementsByTagName('head')[0].appendChild(meta);
                                }
                                meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                            """.trimIndent()
                            view?.evaluateJavascript(js, null)
                        }
                    }
                    
                    loadUrl("https://chat.deepseek.com/")
                    webViewInstance = this // Simpen instance
                }
            }
        )
    }
}
