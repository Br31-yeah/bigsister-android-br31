package com.smwu.bigsister.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.smwu.bigsister.ui.theme.InputBg
import com.smwu.bigsister.ui.theme.MutedForeground
import com.smwu.bigsister.ui.theme.Primary

@Composable
fun FigmaInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            // ⭐⭐⭐ 이 한 줄이 핵심 ⭐⭐⭐
            .pointerInteropFilter { false },
        placeholder = { Text(placeholder, color = MutedForeground) },
        shape = RoundedCornerShape(8.dp),
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            focusedBorderColor = Primary,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Primary
        )
    )
}