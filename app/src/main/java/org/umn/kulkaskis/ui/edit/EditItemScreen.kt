package org.umn.kulkaskis.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.ui.home.KChip
import org.umn.kulkaskis.ui.home.kFieldColors
import org.umn.kulkaskis.ui.theme.Kulkaskis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val categoryOptions = listOf("Dairy", "Meat", "Vegetables", "Fruits", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    item: FridgeItem?,
    onSave: (FridgeItem) -> Unit,
    onCancel: () -> Unit,
    title: String = "Edit Item"
) {
    if (item == null) {
        Text("Item not found", modifier = Modifier.padding(20.dp))
        return
    }

    var name by remember(item.id) { mutableStateOf(item.name) }
    var quantity by remember(item.id) { mutableStateOf(item.quantity.toString()) }
    var category by remember(item.id) { mutableStateOf(item.category) }
    var expiry by remember(item.id) { mutableStateOf(item.expiry) }
    var showPicker by remember { mutableStateOf(false) }

    // Validation state: errors appear once a field is edited, or after pressing Save
    var nameTouched by remember { mutableStateOf(false) }
    var quantityTouched by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val nameError: String? = when {
        name.isBlank() -> "Name is required"
        name.trim().length < 2 -> "Name must be at least 2 characters"
        else -> null
    }
    val quantityValue = quantity.toIntOrNull()
    val quantityError: String? = when {
        quantity.isBlank() -> "Quantity is required"
        quantityValue == null -> "Quantity must be a number"
        quantityValue < 1 -> "Quantity must be at least 1"
        quantityValue > 999 -> "Quantity cannot exceed 999"
        else -> null
    }
    val showNameError = (nameTouched || submitted) && nameError != null
    val showQuantityError = (quantityTouched || submitted) && quantityError != null

    val fieldShape = RoundedCornerShape(10.dp)
    val buttonShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40); nameTouched = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
            isError = showNameError,
            supportingText = { nameError?.let { if (showNameError) Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            shape = fieldShape,
            colors = kFieldColors()
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = quantity,
            // numeric-only: anything that is not a digit is rejected immediately
            onValueChange = { quantity = it.filter(Char::isDigit).take(3); quantityTouched = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Quantity") },
            singleLine = true,
            isError = showQuantityError,
            supportingText = { quantityError?.let { if (showQuantityError) Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            shape = fieldShape,
            colors = kFieldColors()
        )
        Spacer(Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categoryOptions) { option ->
                KChip(
                    label = option,
                    selected = option == category,
                    onClick = { category = option }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Expiry date", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = fieldShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(expiry.format(DateTimeFormatter.ofPattern("d MMM yyyy")))
        }
        if (expiry.isBefore(LocalDate.now())) {
            Spacer(Modifier.height(4.dp))
            Text(
                "This date has already passed, the item will be marked as expired.",
                style = MaterialTheme.typography.bodySmall,
                color = Kulkaskis.status.amberFg
            )
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                submitted = true
                // Submission is blocked until every field is valid
                if (nameError == null && quantityError == null && quantityValue != null) {
                    onSave(
                        item.copy(
                            name = name.trim(),
                            quantity = quantityValue,
                            category = category,
                            expiry = expiry
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = buttonShape
        ) {
            Text("Save", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = buttonShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Cancel", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiry
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        expiry = Instant.ofEpochMilli(it)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}