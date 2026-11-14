package com.example.multipanel.data

import android.os.Environment
import java.io.File

class FileRepository {
    fun getFiles(path: String): List<File> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        return dir.listFiles()?.sortedWith(
            compareBy({ !it.isDirectory }, { it.name.lowercase() })
        ) ?: emptyList()
    }

    // Fungsi baru buat nyimpen file tanpa nimpa yang lama
    fun saveNewFile(folderName: String, fileName: String, content: String): File {
        val root = Environment.getExternalStorageDirectory()
        val folder = File(root, folderName)
        if (!folder.exists()) folder.mkdirs()

        var file = File(folder, fileName)
        var counter = 1
        val nameWithoutExt = fileName.substringBeforeLast(".")
        val ext = fileName.substringAfterLast(".", "")

        // Cek kalau file udah ada, tambahin _1, _2, dst
        while (file.exists()) {
            val newName = if (ext.isNotEmpty()) "${nameWithoutExt}_$counter.$ext" else "${nameWithoutExt}_$counter"
            file = File(folder, newName)
            counter++
        }

        file.writeText(content)
        return file
    }
}
