package org.umn.kulkaskis.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.data.sampleItems
import org.umn.kulkaskis.ui.detail.emojiFor
import org.umn.kulkaskis.ui.theme.KulkaskisTheme
import java.time.LocalTime

@Composable
fun HomeScreen(
    items: List<FridgeItem> = sampleItems,
    userName: String = "Ryan",
    onScan: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onSeeAll: () -> Unit = {}
) {
    // Home = ringkasan: yang harus dipakai duluan + 3 item terbaru. Daftar lengkap ada di tab Fridge.
    val useFirst = items.filter { it.daysLeft in 0..3 }.sortedBy { it.daysLeft }
    val recent = items.sortedByDescending { it.purchaseDate }.take(3)
    val expiring = items.count { it.isExpiringSoon }
    val expired = items.count { it.daysLeft < 0 }
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                "$greeting,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item { ExpiringBanner(expiring = expiring, expired = expired) }
        item { ScanFridgeButton(onClick = onScan) }
        if (useFirst.isNotEmpty()) {
            item {
                Text(
                    "Use these first",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(useFirst, key = { it.id }) { item ->
                        MiniFoodCard(item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recently added",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onSeeAll) { Text("See all") }
            }
        }
        items(recent, key = { "recent-" + it.id }) { item ->
            FoodCard(item, onClick = { onItemClick(item.id) })
        }
    }
}

@Composable
fun FoodCard(item: FridgeItem, onClick: () -> Unit = {}) {
    val (statusText, kind) = when {
        item.daysLeft < 0 -> "Expired" to BadgeKind.BAD
        item.daysLeft == 0L -> "Today" to BadgeKind.WARN
        item.isExpiringSoon -> "${item.daysLeft}d left" to BadgeKind.WARN
        else -> "Fresh" to BadgeKind.OK
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiFor(item.category), fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Qty: ${item.quantity} · ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(statusText, kind)
        }
    }
}

/** Kartu kecil vertikal untuk bagian "Use these first" di Home. */
@Composable
fun MiniFoodCard(item: FridgeItem, onClick: () -> Unit = {}) {
    val (statusText, kind) = when {
        item.daysLeft == 0L -> "Today" to BadgeKind.WARN
        else -> "${item.daysLeft}d left" to BadgeKind.WARN
    }
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiFor(item.category), fontSize = 22.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.height(6.dp))
            StatusBadge(statusText, kind)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KulkaskisTheme {
        HomeScreen()
    }
}