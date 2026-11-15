package com.example.multipanel.viewmodel

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.multipanel.data.FileRepository
import com.example.multipanel.logic.CodeDetector
import java.io.File

sealed class SaveState {
    object Idle : SaveState()
    data class NeedFolder(val code: String) : SaveState()
    data class NeedName(val code: String, val folder: String) : SaveState()
}

enum class PanelType { AI, EDITOR, FILE_MANAGER, LOG }

class MainViewModel : ViewModel() {
    private val repository = FileRepository()

    // --- Preview state ---
    var isPreviewVisible by mutableStateOf(false)
    var previewHtml by mutableStateOf("")

    // --- Focus mode state ---
    var focusedPanel by mutableStateOf<PanelType?>(null)
        private set

    fun toggleFocus(panel: PanelType) {
        focusedPanel = if (focusedPanel == panel) null else panel
    }

    // --- Editor state ---
    var currentFile by mutableStateOf<File?>(null)
    var fileContent by mutableStateOf("")
    var logMessage by mutableStateOf("Siap menerima copy-an...")
    var lastClipboardText by mutableStateOf("")
    var saveState by mutableStateOf<SaveState>(SaveState.Idle)
    var refreshTrigger by mutableIntStateOf(0)
    var lastSavedFolder by mutableStateOf<String?>(null)
    var currentFolderPath by mutableStateOf(Environment.getExternalStorageDirectory().absolutePath)

    fun runPreview() {
        try {
            val dir = File(currentFolderPath)
            if (!dir.exists() || !dir.isDirectory) {
                logMessage = "❌ Folder tidak valid."
                return
            }

            val htmlFiles = dir.listFiles { file -> file.extension.lowercase() == "html" }
            val cssFiles = dir.listFiles { file -> file.extension.lowercase() == "css" }
            val jsFiles = dir.listFiles { file -> file.extension.lowercase() == "js" }

            val htmlContent = htmlFiles?.firstOrNull()?.readText() ?: "<html><body><h2>Preview Kosong</h2></body></html>"
            val cssContent = cssFiles?.joinToString("\n") { it.readText() } ?: ""
            val jsContent = jsFiles?.joinToString("\n") { it.readText() } ?: ""

            val combinedHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>$cssContent</style>
                </head>
                <body>
                    $htmlContent
                    <script>
                        try {
                            $jsContent
                        } catch (e) {
                            Android.logError(e.message);
                        }
                    </script>
                </body>
                </html>
            """.trimIndent()

            previewHtml = combinedHtml
            isPreviewVisible = true
            logMessage = "▶️ Preview dijalankan. Memuat file dari: /${dir.name}"
        } catch (e: Exception) {
            logMessage = "❌ Gagal memuat preview: ${e.message}"
        }
    }

    fun updateCurrentFolder(path: String) {
        currentFolderPath = path
    }

    fun openFile(file: File) {
        currentFile = file
        fileContent = if (file.exists()) file.readText() else ""
    }

    fun saveFile() {
        currentFile?.let { file ->
            file.writeText(fileContent)
            logMessage = "✅ Disimpan manual: ${file.name}"
            refreshTrigger++
        }
    }

    fun processClipboard(text: String) {
        if (text == lastClipboardText || text.isBlank()) return
        lastClipboardText = text

        val lang = CodeDetector.detectLanguage(text)
        val name = CodeDetector.suggestName(text, lang)

        if (lang == null) {
            saveState = SaveState.NeedFolder(text)
            logMessage = "⚠️ Bahasa tidak dikenal, pilih folder manual."
        } else if (name == null) {
            saveState = SaveState.NeedName(text, lang)
            logMessage = "⚠️ Nama file tidak ditemukan, ketik manual."
        } else {
            doSave(lang, "$name.$lang", text)
        }
    }

    fun handleFolderSelected(folder: String, code: String) {
        val name = CodeDetector.suggestName(code, folder)
        if (name == null) {
            saveState = SaveState.NeedName(code, folder)
        } else {
            doSave(folder, "$name.$folder", code)
        }
    }

    fun doSave(lang: String, fileName: String, content: String) {
        try {
            val currentDir = File(currentFolderPath)

            val targetDirPath = if (currentDir.name == lang || currentDir.name == "${lang}s") {
                currentDir.absolutePath
            } else {
                File(currentDir, lang).absolutePath
            }

            val file = repository.saveNewFile(targetDirPath, fileName, content)

            logMessage = "✅ Tersimpan: ${file.name}\n📁 Di: ${file.parent}"
            saveState = SaveState.Idle
            refreshTrigger++

            lastSavedFolder = file.parent
        } catch (e: Exception) {
            logMessage = "❌ Gagal simpan: ${e.message}"
            saveState = SaveState.Idle
        }
    }
}
