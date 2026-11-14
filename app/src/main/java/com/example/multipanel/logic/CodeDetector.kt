package com.example.multipanel.logic

object CodeDetector {
    
    // Nentuin bahasa berdasarkan isi teks
    fun detectLanguage(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // 1. Cek HTML
        if (trimmed.startsWith("<") && (trimmed.contains("</") || trimmed.contains("/>"))) {
            return "html"
        }

        // 2. Cek CSS (karakteristik: ada kurung kurawal, titik dua, dan titik koma)
        if (trimmed.contains("{") && trimmed.contains(":") && trimmed.contains("}")) {
            // Pastiin bukan JS (JS biasanya ada 'function', 'const', dll, atau kurung '()')
            val jsKeywords = Regex("\\b(function|const|let|var|return|if|for|while)\\b")
            if (!jsKeywords.containsMatchIn(trimmed) && !trimmed.contains("(")) {
                return "css"
            }
        }

        // 3. Cek JS (karakteristik: ada keyword atau kurung)
        val jsKeywords = Regex("\\b(function|const|let|var|document|window|import|export)\\b")
        if (jsKeywords.containsMatchIn(trimmed) || (trimmed.contains("(") && trimmed.contains(")"))) {
            return "js"
        }

        // Kalau ragu atau teksnya cuma prosa (bukan kode), balikin null
        return null
    }

    // Nentuin nama file berdasarkan fungsi/class di dalam kode
    fun suggestName(text: String, lang: String?): String? {
        if (lang == null) return null

        if (lang == "css") {
            // Coba ambil class/id pertama di CSS, misal .navbar { ... } atau #header { ... }
            val cssMatch = Regex("""[.#]([a-zA-Z0-9_-]+)\s*\{""").find(text)
            if (cssMatch != null) {
                return cssMatch.groupValues[1] // ngambil 'navbar' dari '.navbar'
            }
        } 
        
        if (lang == "js") {
            // Coba cari nama fungsi
            val funcMatch = Regex("""function\s+([a-zA-Z0-9_]+)""").find(text)
            if (funcMatch != null) return funcMatch.groupValues[1]

            // Atau nama variabel const/let/var
            val varMatch = Regex("""(?:const|let|var)\s+([a-zA-Z0-9_]+)""").find(text)
            if (varMatch != null) return varMatch.groupValues[1]
        }

        return null // Kalau nggak nemu nama
    }
}
