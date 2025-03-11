package com.sinarowa.e_bus_ticket.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuComponent(
    label: String,
    items: List<String>,
    selectedItem: String,
    onSelectionChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp, // Elevation parameter
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(selectedItem) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation), // Apply elevation via Card
        shape = RoundedCornerShape(12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                label = { Text(label) },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = colors,
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            selectedText = item
                            onSelectionChanged(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}