package com.example.multipanel.logic

object CodeDetector {
    
    fun detectLanguage(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // 1. Cek HTML (Harus ada tag pembuka dan penutup beneran)
        if (Regex("""<([a-z]+)[^>]*>[\s\S]*</\1>""").containsMatchIn(trimmed) || 
            Regex("""<[a-z]+[^>]*/>""").containsMatchIn(trimmed)) {
            return "html"
        }

        // 2. Cek CSS (Harus ada selector { properti: nilai; })
        if (Regex("""[a-zA-Z0-9_\-.#]+\s*\{[^{}]*:[^{}]*\}""").containsMatchIn(trimmed)) {
            return "css"
        }

        // 3. Cek JS (Harus ada keyword JS yang jelas, bukan cuma kurung biasa)
        val jsKeywords = Regex("""\b(function|const|let|var|return|import|export)\b""")
        if (jsKeywords.containsMatchIn(trimmed) || trimmed.contains("=>") || trimmed.contains("console.log")) {
            return "js"
        }

        return null
    }

    fun suggestName(text: String, lang: String?): String? {
        if (lang == null) return null

        if (lang == "css") {
            val cssMatch = Regex("""[.#]([a-zA-Z0-9_-]+)\s*\{""").find(text)
            if (cssMatch != null) return cssMatch.groupValues[1] 
        } 
        
        if (lang == "js") {
            val funcMatch = Regex("""function\s+([a-zA-Z0-9_]+)""").find(text)
            if (funcMatch != null) return funcMatch.groupValues[1]

            // Ambil nama variabel const/let/var
            val varMatch = Regex("""(?:const|let|var)\s+([a-zA-Z0-9_]+)\s*=""")
.find(text)
            if (varMatch != null) return varMatch.groupValues[1]
        }

        return null 
    }
}
