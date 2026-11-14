package com.example.multipanel.data

import java.io.File

class FileRepository {
    // Fungsi buat baca isi folder, urutin folder dulu baru file
    fun getFiles(path: String): List<File> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        return dir.listFiles()?.sortedWith(
            compareBy({ !it.isDirectory }, { it.name.lowercase() })
        ) ?: emptyList()
    }
}
