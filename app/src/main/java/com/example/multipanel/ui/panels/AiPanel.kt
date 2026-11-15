package com.example.multipanel.ui.panels

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiPanel(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // Konfigurasi WebView Client
                webViewClient = WebViewClient()
                
                // Konfigurasi Settings
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE // Bypass cache lama
                
                // Bersihin cache yang error
                clearCache(true)
                clearHistory()
                
                // Load URL
                loadUrl("https://chat.deepseek.com/")
            }
        }
    )
}
