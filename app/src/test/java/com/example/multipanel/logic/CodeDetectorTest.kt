package com.example.multipanel.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodeDetectorTest {

    @Test
    fun testDetectCSS() {
        val cssCode = """
            body {
                margin: 0;
                padding: 0;
            }
        """.trimIndent()
        assertEquals("css", CodeDetector.detectLanguage(cssCode))
    }

    @Test
    fun testDetectJS() {
        val jsCode = """
            const hitung = () => {
                return 1 + 1;
            }
        """.trimIndent()
        assertEquals("js", CodeDetector.detectLanguage(jsCode))
    }

    @Test
    fun testDetectHTML() {
        val htmlCode = "<div><h1>Halo</h1></div>"
        assertEquals("html", CodeDetector.detectLanguage(htmlCode))
    }

    @Test
    fun testDetectProsa() {
        val prosa = "Tentu, ini adalah kode untuk membuat tampilan web yang keren."
        assertNull(CodeDetector.detectLanguage(prosa))
    }

    @Test
    fun testSuggestNameCSS() {
        val cssCode = ".navbar { color: red; }"
        assertEquals("navbar", CodeDetector.suggestName(cssCode, "css"))
    }

    @Test
    fun testSuggestNameJS() {
        val jsCode = "function hitungTotal(a, b) { return a + b; }"
        assertEquals("hitungTotal", CodeDetector.suggestName(jsCode, "js"))
    }

    @Test
    fun testMarkdownClean() {
        val markdown = """
            ```css
            .navbar { color: red; }
            ```
        """.trimIndent()
        assertEquals("css", CodeDetector.detectLanguage(markdown))
        assertEquals("navbar", CodeDetector.suggestName(markdown, "css"))
    }
}
