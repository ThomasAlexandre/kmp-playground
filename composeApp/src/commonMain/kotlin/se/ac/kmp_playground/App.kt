package se.ac.kmp_playground

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmp_playground.composeapp.generated.resources.Res
import kmp_playground.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

val availableSupermarkets = listOf(
    "Lidl",
    "Aldi",
    "Whole Foods",
    "Trader Joe's",
    "Kroger",
    "Safeway",
    "Costco",
    "Walmart",
    "Target",
    "Publix"
)

val shoppingListItems = listOf(
    "Milk", "Bread", "Eggs", "Butter", "Cheese",
    "Chicken breast", "Ground beef", "Rice", "Pasta", "Tomatoes",
    "Onions", "Garlic", "Potatoes", "Carrots", "Broccoli",
    "Apples", "Bananas", "Orange juice", "Yogurt", "Cereal",
    "Coffee", "Tea", "Olive oil", "Salt", "Pepper"
)

val productPrices: Map<String, Map<String, Double>> = mapOf(
    "Milk" to mapOf("Lidl" to 2.49, "Aldi" to 2.39, "Whole Foods" to 4.99, "Trader Joe's" to 3.49, "Kroger" to 2.99, "Safeway" to 3.29, "Costco" to 2.19, "Walmart" to 2.78, "Target" to 2.89, "Publix" to 3.49),
    "Bread" to mapOf("Lidl" to 1.29, "Aldi" to 1.19, "Whole Foods" to 4.49, "Trader Joe's" to 2.99, "Kroger" to 1.99, "Safeway" to 2.49, "Costco" to 3.99, "Walmart" to 1.48, "Target" to 1.79, "Publix" to 2.79),
    "Eggs" to mapOf("Lidl" to 2.99, "Aldi" to 2.89, "Whole Foods" to 5.99, "Trader Joe's" to 3.99, "Kroger" to 3.29, "Safeway" to 3.79, "Costco" to 4.99, "Walmart" to 2.98, "Target" to 3.19, "Publix" to 3.99),
    "Butter" to mapOf("Lidl" to 3.49, "Aldi" to 3.29, "Whole Foods" to 6.49, "Trader Joe's" to 3.99, "Kroger" to 3.99, "Safeway" to 4.49, "Costco" to 5.99, "Walmart" to 3.48, "Target" to 3.79, "Publix" to 4.49),
    "Cheese" to mapOf("Lidl" to 2.99, "Aldi" to 2.79, "Whole Foods" to 5.99, "Trader Joe's" to 3.99, "Kroger" to 3.49, "Safeway" to 3.99, "Costco" to 6.99, "Walmart" to 2.98, "Target" to 3.29, "Publix" to 3.99),
    "Chicken breast" to mapOf("Lidl" to 5.99, "Aldi" to 5.49, "Whole Foods" to 9.99, "Trader Joe's" to 6.99, "Kroger" to 6.49, "Safeway" to 7.49, "Costco" to 4.99, "Walmart" to 5.98, "Target" to 6.29, "Publix" to 7.99),
    "Ground beef" to mapOf("Lidl" to 4.99, "Aldi" to 4.79, "Whole Foods" to 8.99, "Trader Joe's" to 5.99, "Kroger" to 5.49, "Safeway" to 6.49, "Costco" to 4.49, "Walmart" to 4.98, "Target" to 5.29, "Publix" to 6.49),
    "Rice" to mapOf("Lidl" to 1.99, "Aldi" to 1.89, "Whole Foods" to 4.49, "Trader Joe's" to 2.99, "Kroger" to 2.49, "Safeway" to 2.99, "Costco" to 8.99, "Walmart" to 1.98, "Target" to 2.29, "Publix" to 2.99),
    "Pasta" to mapOf("Lidl" to 0.99, "Aldi" to 0.89, "Whole Foods" to 2.49, "Trader Joe's" to 1.49, "Kroger" to 1.29, "Safeway" to 1.49, "Costco" to 4.99, "Walmart" to 0.98, "Target" to 1.19, "Publix" to 1.69),
    "Tomatoes" to mapOf("Lidl" to 1.49, "Aldi" to 1.29, "Whole Foods" to 3.99, "Trader Joe's" to 2.49, "Kroger" to 1.99, "Safeway" to 2.29, "Costco" to 3.99, "Walmart" to 1.48, "Target" to 1.79, "Publix" to 2.49),
    "Onions" to mapOf("Lidl" to 0.79, "Aldi" to 0.69, "Whole Foods" to 1.99, "Trader Joe's" to 0.99, "Kroger" to 0.89, "Safeway" to 0.99, "Costco" to 2.49, "Walmart" to 0.78, "Target" to 0.89, "Publix" to 1.29),
    "Garlic" to mapOf("Lidl" to 0.49, "Aldi" to 0.39, "Whole Foods" to 0.99, "Trader Joe's" to 0.59, "Kroger" to 0.49, "Safeway" to 0.59, "Costco" to 1.99, "Walmart" to 0.48, "Target" to 0.49, "Publix" to 0.79),
    "Potatoes" to mapOf("Lidl" to 2.99, "Aldi" to 2.79, "Whole Foods" to 4.99, "Trader Joe's" to 3.49, "Kroger" to 3.29, "Safeway" to 3.49, "Costco" to 4.49, "Walmart" to 2.98, "Target" to 3.19, "Publix" to 3.99),
    "Carrots" to mapOf("Lidl" to 0.99, "Aldi" to 0.89, "Whole Foods" to 2.49, "Trader Joe's" to 1.49, "Kroger" to 1.29, "Safeway" to 1.49, "Costco" to 2.49, "Walmart" to 0.98, "Target" to 1.19, "Publix" to 1.79),
    "Broccoli" to mapOf("Lidl" to 1.49, "Aldi" to 1.39, "Whole Foods" to 2.99, "Trader Joe's" to 1.99, "Kroger" to 1.79, "Safeway" to 1.99, "Costco" to 3.49, "Walmart" to 1.48, "Target" to 1.69, "Publix" to 2.29),
    "Apples" to mapOf("Lidl" to 1.99, "Aldi" to 1.79, "Whole Foods" to 3.99, "Trader Joe's" to 2.49, "Kroger" to 2.29, "Safeway" to 2.49, "Costco" to 4.99, "Walmart" to 1.98, "Target" to 2.19, "Publix" to 2.99),
    "Bananas" to mapOf("Lidl" to 0.59, "Aldi" to 0.49, "Whole Foods" to 0.79, "Trader Joe's" to 0.19, "Kroger" to 0.59, "Safeway" to 0.69, "Costco" to 1.49, "Walmart" to 0.58, "Target" to 0.59, "Publix" to 0.69),
    "Orange juice" to mapOf("Lidl" to 2.49, "Aldi" to 2.29, "Whole Foods" to 5.99, "Trader Joe's" to 3.49, "Kroger" to 2.99, "Safeway" to 3.49, "Costco" to 6.49, "Walmart" to 2.48, "Target" to 2.79, "Publix" to 3.79),
    "Yogurt" to mapOf("Lidl" to 0.79, "Aldi" to 0.69, "Whole Foods" to 1.49, "Trader Joe's" to 0.99, "Kroger" to 0.89, "Safeway" to 0.99, "Costco" to 4.99, "Walmart" to 0.78, "Target" to 0.89, "Publix" to 1.19),
    "Cereal" to mapOf("Lidl" to 2.49, "Aldi" to 2.29, "Whole Foods" to 4.99, "Trader Joe's" to 2.99, "Kroger" to 2.99, "Safeway" to 3.49, "Costco" to 5.99, "Walmart" to 2.48, "Target" to 2.79, "Publix" to 3.49),
    "Coffee" to mapOf("Lidl" to 4.99, "Aldi" to 4.49, "Whole Foods" to 9.99, "Trader Joe's" to 5.99, "Kroger" to 5.49, "Safeway" to 6.49, "Costco" to 8.99, "Walmart" to 4.98, "Target" to 5.29, "Publix" to 6.99),
    "Tea" to mapOf("Lidl" to 2.49, "Aldi" to 2.29, "Whole Foods" to 4.99, "Trader Joe's" to 2.99, "Kroger" to 2.79, "Safeway" to 2.99, "Costco" to 7.99, "Walmart" to 2.48, "Target" to 2.69, "Publix" to 3.49),
    "Olive oil" to mapOf("Lidl" to 4.99, "Aldi" to 4.49, "Whole Foods" to 9.99, "Trader Joe's" to 5.99, "Kroger" to 5.99, "Safeway" to 6.99, "Costco" to 11.99, "Walmart" to 4.98, "Target" to 5.49, "Publix" to 7.49),
    "Salt" to mapOf("Lidl" to 0.49, "Aldi" to 0.39, "Whole Foods" to 1.49, "Trader Joe's" to 0.99, "Kroger" to 0.69, "Safeway" to 0.79, "Costco" to 1.99, "Walmart" to 0.48, "Target" to 0.59, "Publix" to 0.99),
    "Pepper" to mapOf("Lidl" to 1.99, "Aldi" to 1.79, "Whole Foods" to 4.99, "Trader Joe's" to 2.49, "Kroger" to 2.29, "Safeway" to 2.49, "Costco" to 3.99, "Walmart" to 1.98, "Target" to 2.19, "Publix" to 2.99)
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        val tabs = listOf("Home", "Search", "Profile")
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        var selectedSupermarkets by rememberSaveable { mutableStateOf(setOf<String>()) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column {
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> HomeTab(selectedSupermarkets = selectedSupermarkets)
                    1 -> SearchTab()
                    2 -> ProfileTab(
                        selectedSupermarkets = selectedSupermarkets,
                        onSelectionChanged = { selectedSupermarkets = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(
    selectedSupermarkets: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    val selectionCount = selectedSupermarkets.size
    val isValidSelection = selectionCount in 1..5

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(onClick = { showContent = !showContent }) {
            Text("Select on Map!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
        Text(
            text = "Select Your Supermarkets",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Choose 1-5 supermarkets in your area ($selectionCount selected)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isValidSelection || selectionCount == 0)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(availableSupermarkets) { supermarket ->
                val isSelected = supermarket in selectedSupermarkets
                val canSelect = selectionCount < 5 || isSelected

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            val newSelection = if (checked && canSelect) {
                                selectedSupermarkets + supermarket
                            } else {
                                selectedSupermarkets - supermarket
                            }
                            onSelectionChanged(newSelection)
                        },
                        enabled = canSelect
                    )
                    Text(
                        text = supermarket,
                        modifier = Modifier.padding(start = 8.dp),
                        color = if (canSelect)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTab() {
    val weeklyItems = listOf(
        "Milk",
        "Bread",
        "Eggs",
        "Butter",
        "Cheese",
        "Chicken breast",
        "Ground beef",
        "Rice",
        "Pasta",
        "Tomatoes",
        "Onions",
        "Garlic",
        "Potatoes",
        "Carrots",
        "Broccoli",
        "Apples",
        "Bananas",
        "Orange juice",
        "Yogurt",
        "Cereal",
        "Coffee",
        "Tea",
        "Olive oil",
        "Salt",
        "Pepper"
    )

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            weeklyItems
        } else {
            weeklyItems.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Weekly Shopping List",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            placeholder = { Text("Search items...") },
            singleLine = true
        )

        Text(
            text = "${filteredItems.size} items",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    Text(
                        text = "No items found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTab(selectedSupermarkets: Set<String>) {
    val supermarketTotals = remember(selectedSupermarkets) {
        selectedSupermarkets.associateWith { supermarket ->
            shoppingListItems.sumOf { item ->
                productPrices[item]?.get(supermarket) ?: 0.0
            }
        }.toList().sortedBy { it.second }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Price Dashboard",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Total cost for ${shoppingListItems.size} items",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (selectedSupermarkets.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Select supermarkets in the Profile tab to see price comparisons",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(supermarketTotals) { (supermarket, total) ->
                    val isCheapest = supermarketTotals.firstOrNull()?.first == supermarket

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCheapest)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = supermarket,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCheapest) {
                                    Text(
                                        text = "Best price",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "$${((total * 100).toInt() / 100.0)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isCheapest)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
