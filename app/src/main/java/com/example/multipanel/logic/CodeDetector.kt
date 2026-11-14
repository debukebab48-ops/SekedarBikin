package com.example.multipanel.logic

object CodeDetector {
    
    // Fungsi buat ngebersihin backtick (```) bawaan Markdown AI
    fun cleanText(text: String): String {
        val regex = "```[a-zA-Z]*\\n?".toRegex()
        return text.replace(regex, "").replace("```", "").trim()
    }

    fun detectLanguage(originalText: String): String? {
        val text = cleanText(originalText)
        if (text.isEmpty()) return null

        // 1. Cek HTML
        if (text.contains("<html", true) || text.contains("<div", true) || 
            text.contains("<body", true) || text.contains("<!DOCTYPE", true)) {
            return "html"
        }

        // 2. Cek CSS (Ada kurung kurawal, titik dua, dan titik koma)
        if (text.contains("{") && text.contains("}") && text.contains(":")) {
            val jsKeywords = Regex("\\b(function|const|let|var|return|import|export|class|=>)\\b")
            // Pastiin bukan JS
            if (!jsKeywords.containsMatchIn(text) && !text.contains("()")) {
                return "css"
            }
        }

        // 3. Cek JS
        val jsKeywords = Regex("\\b(function|const|let|var|return|import|export|class|=>|console\\.log|document\\.)\\b")
        if (jsKeywords.containsMatchIn(text) || text.contains("function") || text.contains("=>")) {
            return "js"
        }

        return null
    }

    fun suggestName(originalText: String, lang: String?): String? {
        val text = cleanText(originalText)
        if (lang == null) return null

        // Coba cari komentar nama file misal: /* style.css */ atau // app.js
        val fileCommentRegex = Regex("(?:/\\*|//)\\s*([a-zA-Z0-9_-]+)\\.[a-zA-Z0-9]+")
        val match = fileCommentRegex.find(text)
        if (match != null) {
            return match.groupValues[1]
        }

        if (lang == "css") {
            val cssMatch = Regex("[.#]([a-zA-Z0-9_-]+)\\s*\\{").find(text)
            if (cssMatch != null) return cssMatch.groupValues[1]
        }
        
        if (lang == "js") {
            val funcMatch = Regex("function\\s+([a-zA-Z0-9_]+)").find(text)
            if (funcMatch != null) return funcMatch.groupValues[1]

            val varMatch = Regex("(?:const|let|var)\\s+([a-zA-Z0-9_]+)\\s*=").find(text)
            if (varMatch != null) return varMatch.groupValues[1]
            
            val classMatch = Regex("class\\s+([a-zA-Z0-9_]+)").find(text)
            if (classMatch != null) return classMatch.groupValues[1]
        }

        if (lang == "html") {
            val titleMatch = Regex("<title>([^<]+)</title>", RegexOption.IGNORE_CASE).find(text)
            if (titleMatch != null) return titleMatch.groupValues[1].trim().replace(" ", "_").lowercase()
        }

        return null
    }
}
