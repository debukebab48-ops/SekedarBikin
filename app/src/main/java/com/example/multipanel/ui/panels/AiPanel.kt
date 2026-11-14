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
                // Tiap panel digeser, panggil JS 'fitToScreen'
                .onSizeChanged { 
                    webViewInstance?.evaluateJavascript("fitToScreen();", null)
                },
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.Black.toArgb())
                    
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    
                    // Paksa User Agent Mobile
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            
                            // JAVASCRIPT SAKTI: Nge-scale halaman web biar selalu pas sama lebar panel
                            val js = """
                                function fitToScreen() {
                                    var panelWidth = window.innerWidth;
                                    // Reset scale dulu biar ukurannya akurat
                                    document.body.style.transform = 'none';
                                    document.body.style.width = '100%';
                                    
                                    var bodyWidth = document.body.scrollWidth;
                                    
                                    // Kalau web lebih lebar dari panel, kita kecilin (zoom out)
                                    if (bodyWidth > panelWidth) {
                                        var scale = panelWidth / bodyWidth;
                                        document.body.style.transformOrigin = 'top left';
                                        document.body.style.transform = 'scale(' + scale + ')';
                                        // Lebarin body-nya biar pas abis di-scale
                                        document.body.style.width = (100 / scale) + '%'; 
                                    }
                                }
                                // Jalanin pas halaman pertama load
                                fitToScreen();
                                // Jalanin tiap layar di-resize (misal pas HP di-rotate)
                                window.addEventListener('resize', fitToScreen);
                            """.trimIndent()
                            view?.evaluateJavascript(js, null)
                        }
                    }
                    
                    loadUrl("https://chat.deepseek.com/")
                    webViewInstance = this
                }
            }
        )
    }
}
