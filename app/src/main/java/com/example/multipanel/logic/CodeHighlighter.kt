package com.example.multipanel.logic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

object CodeHighlighter {
    
    fun highlight(text: String, fileExtension: String): AnnotatedString {
        // Pake AnnotatedString.Builder biar addStyle-nya kebaca
        val builder = AnnotatedString.Builder(text)
        
        // Warna dasar syntax
        val keywordColor = Color(0xFF569CD6) // Biru
        val stringColor = Color(0xFFCE9178) // Orange/Hijau
        val commentColor = Color(0xFF6A9955) // Hijau gelap

        // 1. Highlight String (di antara tanda kutip)
        val stringRegex = "\"([^\"]*)\"|'([^']*)'".toRegex()
        stringRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
        }

        // 2. Highlight Komentar (// atau /* */)
        val commentRegex = "//.*|/\\*[\\s\\S]*?\\*/".toRegex()
        commentRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = commentColor), match.range.first, match.range.last + 1)
        }

        // 3. Highlight Keyword (khusus JS/HTML/CSS)
        if (fileExtension == "js" || fileExtension == "html") {
            val keywords = "\\b(function|const|let|var|if|else|return|import|export|class|new|this|document|window)\\b".toRegex()
            keywords.findAll(text).forEach { match ->
                builder.addStyle(SpanStyle(color = keywordColor), match.range.first, match.range.last + 1)
            }
        }

        return builder.toAnnotatedString()
    }
}
