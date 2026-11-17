package org.umn.kulkaskis.ui.fridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.data.sampleItems
import org.umn.kulkaskis.ui.home.FoodCard
import org.umn.kulkaskis.ui.home.KChip
import org.umn.kulkaskis.ui.home.kFieldColors
import org.umn.kulkaskis.ui.theme.KulkaskisTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    items: List<FridgeItem> = sampleItems,
    onItemClick: (String) -> Unit = {},
    onAdd: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }

    // Dihitung ulang hanya saat data/filter berubah, bukan di setiap recomposition
    val categories = remember(items) { listOf("All") + items.map { it.category }.distinct() }
    val filtered = remember(items, query, selected) {
        items.filter {
            (selected == "All" || it.category == selected) &&
                    it.name.contains(query, ignoreCase = true)
        }.sortedBy { it.expiry }   // yang paling cepat basi di paling atas
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Fridge",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )
            FilledIconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
        Text(
            "${items.size} items · soonest expiry first",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search food by name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = kFieldColors()
        )
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                KChip(
                    label = category,
                    selected = category == selected,
                    onClick = { selected = category }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Text(
                "No items found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { item ->
                    FoodCard(item, onClick = { onItemClick(item.id) })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FridgeScreenPreview() {
    KulkaskisTheme {
        FridgeScreen()
    }
}