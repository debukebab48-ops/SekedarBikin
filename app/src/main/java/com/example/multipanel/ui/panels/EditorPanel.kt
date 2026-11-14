package com.example.multipanel.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multipanel.logic.CodeHighlighter
import com.example.multipanel.viewmodel.MainViewModel

@Composable
fun EditorPanel(viewModel: MainViewModel) {
    val file = viewModel.currentFile

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        // Header: Nama file & Tombol Save
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = file?.name ?: "Tidak ada file yang dibuka",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            if (file != null) {
                Button(
                    onClick = { viewModel.saveFile() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Simpan", fontSize = 10.sp)
                }
            }
        }

        // Area Text Editor
        if (file != null) {
            val fileExt = file.extension.lowercase()

            BasicTextField(
                value = viewModel.fileContent,
                onValueChange = { viewModel.fileContent = it },
                modifier = Modifier.fillMaxSize().padding(8.dp),
                textStyle = TextStyle(
                    color = Color(0xFFD4D4D4),
                    fontSize = 13.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                cursorBrush = SolidColor(Color.White),
                // Panggil fungsi highlighter dibungkus TransformedText
                visualTransformation = VisualTransformation { text ->
                    TransformedText(
                        CodeHighlighter.highlight(text.text, fileExt),
                        OffsetMapping.Identity
                    )
                }
            )
        }
    }
}
