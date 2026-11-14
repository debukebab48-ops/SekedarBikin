package com.example.multipanel.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel : ViewModel() {
    // Nyimpen file yang lagi aktif dibuka
    var currentFile by mutableStateOf<File?>(null)
    
    // Nyimpen isi teks dari file
    var fileContent by mutableStateOf("")

    fun openFile(file: File) {
        currentFile = file
        fileContent = if (file.exists()) file.readText() else ""
    }

    fun saveFile() {
        currentFile?.let { file ->
            file.writeText(fileContent)
        }
    }
}
