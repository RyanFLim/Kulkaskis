package org.umn.kulkaskis.ui.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.data.Recipe
import org.umn.kulkaskis.data.sampleItems
import org.umn.kulkaskis.data.sampleRecipes
import org.umn.kulkaskis.ui.home.BadgeKind
import org.umn.kulkaskis.ui.home.StatusBadge
import org.umn.kulkaskis.ui.theme.Kulkaskis
import org.umn.kulkaskis.ui.theme.KulkaskisTheme

private data class RecipeMatch(
    val recipe: Recipe,
    val have: List<String>,
    val missing: List<String>,
    val expiringUsed: List<String>
)

@Composable
fun RecipesScreen(items: List<FridgeItem> = sampleItems) {
    val matches = remember(items) {
        val fresh = items.filter { it.daysLeft >= 0 }
        sampleRecipes
            .map { recipe ->
                val have = recipe.ingredients.filter { ing ->
                    fresh.any { it.name.contains(ing, ignoreCase = true) }
                }
                val missing = recipe.ingredients.filterNot { it in have }
                val expiringUsed = fresh
                    .filter { item ->
                        item.isExpiringSoon &&
                                recipe.ingredients.any { item.name.contains(it, ignoreCase = true) }
                    }
                    .map { it.name }
                RecipeMatch(recipe, have, missing, expiringUsed)
            }
            .filter { it.have.isNotEmpty() }
            .sortedWith(
                compareByDescending<RecipeMatch> { it.expiringUsed.size }
                    .thenByDescending { it.have.size.toFloat() / it.recipe.ingredients.size }
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Recipes", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ranked by ingredients expiring first",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        if (matches.isEmpty()) {
            Text(
                "No recipe matches your fridge yet. Add more items!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(matches, key = { it.recipe.id }) { match ->
                    RecipeCard(match)
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(match: RecipeMatch) {
    var expanded by remember { mutableStateOf(false) }
    val recipe = match.recipe
    val complete = match.missing.isEmpty()

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(recipe.emoji, fontSize = 26.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${recipe.minutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(
                    "${match.have.size}/${recipe.ingredients.size} ingredients",
                    if (complete) BadgeKind.OK else BadgeKind.WARN
                )
            }

            if (match.expiringUsed.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Uses expiring: ${match.expiringUsed.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Kulkaskis.status.amberFg,
                    fontWeight = FontWeight.Medium
                )
            }

            if (expanded) {
                if (match.have.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "You have",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        match.have.forEach { ing ->
                            StatusBadge(ing.replaceFirstChar { it.uppercase() }, BadgeKind.OK)
                        }
                    }
                }
                if (match.missing.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Missing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        match.missing.forEach { ing ->
                            StatusBadge(ing.replaceFirstChar { it.uppercase() }, BadgeKind.BAD)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Steps", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                recipe.steps.forEachIndexed { index, step ->
                    Text(
                        "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipesScreenPreview() {
    KulkaskisTheme {
        RecipesScreen()
    }
}