package com.example.multipanel.ui.panels

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // Inisialisasi WebView cuma sekali pake remember
    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            
            clearCache(true)
            loadUrl("https://chat.deepseek.com/")
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
        update = { view ->
            // Handle state update kalau perlu (misal resume/pause), 
            // tapi biarin view-nya tetep sama.
            view.settings.javaScriptEnabled = true
            view.settings.domStorageEnabled = true
        }
    )
}
