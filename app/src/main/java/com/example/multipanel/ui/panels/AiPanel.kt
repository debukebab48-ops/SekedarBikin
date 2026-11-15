package com.example.multipanel.ui.panels

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // 1. Bungkus instansiasi WebView pakai remember
    val webView = remember {
        WebView(context).apply {
            // 4. LayoutParams yang valid
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // 3. Set Client & ChromeClient
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            
            clearCache(true)
            // loadUrl dihapus dari sini, dipindah ke blok update
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            // 2. Load URL di blok update
            // Guard (view.url == null) MENCEGAH reload berulang tiap recomposition
            if (view.url == null) {
                view.loadUrl("https://chat.deepseek.com/")
            }
        }
    )

    // 5. Lifecycle cleanup pakai DisposableEffect
    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }
}
