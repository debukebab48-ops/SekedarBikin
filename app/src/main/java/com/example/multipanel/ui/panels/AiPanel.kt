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
import androidx.compose.ui.graphics.toArgb // TAMBAHAN BUAT WEBVIEW
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiPanel(modifier: Modifier = Modifier) {
    // Root layout wajib fillMaxSize
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
                modifier = Modifier.weight(1f) // Pake weight biar fleksibel
            )
        }

        // WebView dikasih weight(1f) biar ngisi sisa ruang di bawah header
        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(1f),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // WebView butuh Int buat warna background, jadi pake .toArgb()
                    setBackgroundColor(Color.Black.toArgb())
                    
                    // Konfigurasi dasar
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    
                    // KONFIGURASI RESPONSIF:
                    // Biar halaman web ngecil dan nyesuaiin lebar panel (nggak overflow/gepeng)
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true

                    webViewClient = WebViewClient()
                    loadUrl("https://chat.deepseek.com/")
                }
            }
        )
    }
}
