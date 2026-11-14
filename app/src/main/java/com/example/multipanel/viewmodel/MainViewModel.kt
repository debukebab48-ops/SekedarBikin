package com.example.multipanel.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.multipanel.data.FileRepository
import com.example.multipanel.logic.CodeDetector
import java.io.File

// State buat nentuin pop-up (dialog) apa yang mau muncul
sealed class SaveState {
    object Idle : SaveState()
    data class NeedFolder(val code: String) : SaveState()
    data class NeedName(val code: String, val folder: String) : SaveState()
}

class MainViewModel : ViewModel() {
    private val repository = FileRepository()

    var currentFile by mutableStateOf<File?>(null)
    var fileContent by mutableStateOf("")

    // State buat log & clipboard
    var logMessage by mutableStateOf("Siap menerima copy-an...")
    var lastClipboardText by mutableStateOf("")
    var saveState by mutableStateOf<SaveState>(SaveState.Idle)

    fun openFile(file: File) {
        currentFile = file
        fileContent = if (file.exists()) file.readText() else ""
    }

    fun saveFile() {
        currentFile?.let { file ->
            file.writeText(fileContent)
            logMessage = "✅ Disimpan manual: ${file.name}"
        }
    }

    // Dipanggil tiap ada perubahan clipboard
    fun processClipboard(text: String) {
        if (text == lastClipboardText || text.isBlank()) return
        lastClipboardText = text

        val lang = CodeDetector.detectLanguage(text)
        val name = CodeDetector.suggestName(text, lang)

        if (lang == null) {
            // Ragu bahasa -> Tanya Folder
            saveState = SaveState.NeedFolder(text)
            logMessage = "⚠️ Bahasa tidak dikenal, pilih folder manual."
        } else if (name == null) {
            // Yakin bahasa, ragu nama -> Tanya Nama
            saveState = SaveState.NeedName(text, lang)
            logMessage = "⚠️ Nama file tidak ditemukan, ketik manual."
        } else {
            // Yakin semuanya -> Langsung Simpan
            doSave(lang, "$name.$lang", text)
        }
    }

    // Kalau user milih folder manual dari dialog
    fun handleFolderSelected(folder: String, code: String) {
        val name = CodeDetector.suggestName(code, folder)
        if (name == null) {
            saveState = SaveState.NeedName(code, folder)
        } else {
            doSave(folder, "$name.$folder", code)
        }
    }

    // Fungsi eksekusi simpan
    fun doSave(folder: String, fileName: String, content: String) {
        try {
            val file = repository.saveNewFile(folder, fileName, content)
            logMessage = "✅ Auto-save: ${file.name} ke folder /$folder"
            saveState = SaveState.Idle
        } catch (e: Exception) {
            logMessage = "❌ Gagal simpan: ${e.message}"
            saveState = SaveState.Idle
        }
    }
}
