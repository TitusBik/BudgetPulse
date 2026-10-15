package com.example.budgetpulse.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.budgetpulse.data.model.CategoryItem

@Composable
fun EditAmountDialog(
    category: CategoryItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var amountText by remember { mutableStateOf(category.amount.replace("RM", "").trim()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Amount for ${category.name}") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                prefix = { Text("RM ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(amountText) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
