package se.ac.kmp_playground

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import se.ac.kmp_playground.data.Product
import se.ac.kmp_playground.data.ProductRepository
import se.ac.kmp_playground.data.Store
import se.ac.kmp_playground.data.StoreRepository
import se.ac.kmp_playground.data.SelectedStore
import se.ac.kmp_playground.data.ShoppingList
import se.ac.kmp_playground.data.ShoppingListRepository
import se.ac.kmp_playground.data.EnrichedShoppingList
import se.ac.kmp_playground.data.User
import se.ac.kmp_playground.data.UserRepository
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

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
    onSelectionChanged: (Set<String>) -> Unit,
    storeRepository: StoreRepository = remember { StoreRepository() },
    userRepository: UserRepository = remember { UserRepository() },
    shoppingListRepository: ShoppingListRepository = remember { ShoppingListRepository() },
    currentUserId: String = "usr_a1b2c3d4"
) {
    var showContent by remember { mutableStateOf(false) }
    var storesUiState by remember { mutableStateOf<StoresUiState>(StoresUiState.Loading) }
    var shoppingListsUiState by remember { mutableStateOf<ShoppingListsUiState>(ShoppingListsUiState.Loading) }
    var enrichedListUiState by remember { mutableStateOf<EnrichedShoppingListUiState>(EnrichedShoppingListUiState.Idle) }
    var selectedShoppingListId by remember { mutableStateOf<String?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var hasInitializedSelection by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val selectionCount = selectedSupermarkets.size
    val isValidSelection = selectionCount in 1..5

    LaunchedEffect(Unit) {
        // Load stores and user in parallel
        storeRepository.getAllStores()
            .onSuccess { stores -> storesUiState = StoresUiState.Success(stores) }
            .onFailure { error -> storesUiState = StoresUiState.Error(error.message ?: "Unknown error") }

        userRepository.getUserById(currentUserId)
            .onSuccess { loadedUser -> user = loadedUser }
            .onFailure { /* User not found is OK, just won't pre-select */ }
    }

    // Load shopping lists when user is loaded
    LaunchedEffect(user) {
        user?.let { loadedUser ->
            if (loadedUser.shoppingListIds.isNotEmpty()) {
                shoppingListRepository.getShoppingListsByIds(loadedUser.shoppingListIds)
                    .onSuccess { lists -> shoppingListsUiState = ShoppingListsUiState.Success(lists) }
                    .onFailure { error -> shoppingListsUiState = ShoppingListsUiState.Error(error.message ?: "Unknown error") }
            } else {
                shoppingListsUiState = ShoppingListsUiState.Success(emptyList())
            }
        }
    }

    // Pre-select stores based on user's selectedStores when both are loaded
    LaunchedEffect(storesUiState, user) {
        if (!hasInitializedSelection && user != null && storesUiState is StoresUiState.Success) {
            val stores = (storesUiState as StoresUiState.Success).stores
            val userStoreIds = user!!.selectedStores.map { it.storeId }.toSet()
            val preSelectedNames = stores
                .filter { it.id in userStoreIds }
                .map { it.name }
                .toSet()
            if (preSelectedNames.isNotEmpty()) {
                onSelectionChanged(preSelectedNames)
            }
            hasInitializedSelection = true
        }
    }

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

        when (val state = storesUiState) {
            is StoresUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is StoresUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Failed to load stores",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = {
                                storesUiState = StoresUiState.Loading
                                scope.launch {
                                    storeRepository.getAllStores()
                                        .onSuccess { stores -> storesUiState = StoresUiState.Success(stores) }
                                        .onFailure { error -> storesUiState = StoresUiState.Error(error.message ?: "Unknown error") }
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is StoresUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.stores) { store ->
                        val isSelected = store.name in selectedSupermarkets
                        val canSelect = selectionCount < 5 || isSelected

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    val newSelection = if (checked && canSelect) {
                                        selectedSupermarkets + store.name
                                    } else {
                                        selectedSupermarkets - store.name
                                    }
                                    onSelectionChanged(newSelection)

                                    // Update user profile in backend
                                    user?.let { currentUser ->
                                        scope.launch {
                                            val updatedSelectedStores = if (checked && canSelect) {
                                                // Add store with current timestamp
                                                currentUser.selectedStores + SelectedStore(
                                                    storeId = store.id,
                                                    addedAt = getPlatformTimestamp(),
                                                    isPreferred = false
                                                )
                                            } else {
                                                // Remove store
                                                currentUser.selectedStores.filter { it.storeId != store.id }
                                            }
                                            val updatedUser = currentUser.copy(selectedStores = updatedSelectedStores)
                                            userRepository.updateUser(updatedUser)
                                                .onSuccess { user = it }
                                        }
                                    }
                                },
                                enabled = canSelect
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = store.name,
                                    color = if (canSelect)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                                Text(
                                    text = "${store.address}, ${store.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        when (val enrichedState = enrichedListUiState) {
            is EnrichedShoppingListUiState.Idle -> {
                Text(
                    text = "Your Shopping Lists",
                    style = MaterialTheme.typography.titleLarge
                )

                when (val listsState = shoppingListsUiState) {
                    is ShoppingListsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    is ShoppingListsUiState.Error -> {
                        Text(
                            text = "Failed to load shopping lists",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    is ShoppingListsUiState.Success -> {
                        if (listsState.shoppingLists.isEmpty()) {
                            Text(
                                text = "No shopping lists yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                listsState.shoppingLists.forEach { shoppingList ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedShoppingListId = shoppingList.id
                                                enrichedListUiState = EnrichedShoppingListUiState.Loading
                                                scope.launch {
                                                    shoppingListRepository.getEnrichedShoppingList(shoppingList.id)
                                                        .onSuccess { enrichedList ->
                                                            enrichedListUiState = EnrichedShoppingListUiState.Success(enrichedList)
                                                        }
                                                        .onFailure { error ->
                                                            enrichedListUiState = EnrichedShoppingListUiState.Error(error.message ?: "Unknown error")
                                                        }
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = shoppingList.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is EnrichedShoppingListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EnrichedShoppingListUiState.Error -> {
                Column {
                    Text(
                        text = "Failed to load shopping list details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = {
                            enrichedListUiState = EnrichedShoppingListUiState.Idle
                            selectedShoppingListId = null
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Back to Lists")
                    }
                }
            }
            is EnrichedShoppingListUiState.Success -> {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                enrichedListUiState = EnrichedShoppingListUiState.Idle
                                selectedShoppingListId = null
                            }
                        ) {
                            Text("< Back")
                        }
                        Text(
                            text = enrichedState.enrichedList.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Text(
                        text = "${enrichedState.enrichedList.items.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(enrichedState.enrichedList.items) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (!item.productImageUrl.isNullOrBlank())
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.name.take(2).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (!item.productImageUrl.isNullOrBlank())
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Qty: ${item.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!item.productBrands.isNullOrBlank()) {
                                            Text(
                                                text = item.productBrands,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (item.isChecked) {
                                        Text(
                                            text = "✓",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class ProductsUiState {
    data object Loading : ProductsUiState()
    data class Success(val products: List<Product>) : ProductsUiState()
    data class Error(val message: String) : ProductsUiState()
}

sealed class StoresUiState {
    data object Loading : StoresUiState()
    data class Success(val stores: List<Store>) : StoresUiState()
    data class Error(val message: String) : StoresUiState()
}

sealed class ShoppingListsUiState {
    data object Loading : ShoppingListsUiState()
    data class Success(val shoppingLists: List<ShoppingList>) : ShoppingListsUiState()
    data class Error(val message: String) : ShoppingListsUiState()
}

sealed class EnrichedShoppingListUiState {
    data object Idle : EnrichedShoppingListUiState()
    data object Loading : EnrichedShoppingListUiState()
    data class Success(val enrichedList: EnrichedShoppingList) : EnrichedShoppingListUiState()
    data class Error(val message: String) : EnrichedShoppingListUiState()
}

@Composable
fun SearchTab(repository: ProductRepository = remember { ProductRepository() }) {
    var uiState by remember { mutableStateOf<ProductsUiState>(ProductsUiState.Loading) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.getAllProducts()
            .onSuccess { products -> uiState = ProductsUiState.Success(products) }
            .onFailure { error -> uiState = ProductsUiState.Error(error.message ?: "Unknown error") }
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

        when (val state = uiState) {
            is ProductsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProductsUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Failed to load products",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = {
                                uiState = ProductsUiState.Loading
                                scope.launch {
                                    repository.getAllProducts()
                                        .onSuccess { products -> uiState = ProductsUiState.Success(products) }
                                        .onFailure { error -> uiState = ProductsUiState.Error(error.message ?: "Unknown error") }
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is ProductsUiState.Success -> {
                val filteredProducts = remember(searchQuery, state.products) {
                    if (searchQuery.isBlank()) {
                        state.products
                    } else {
                        state.products.filter {
                            it.productName.contains(searchQuery, ignoreCase = true) ||
                            it.brands.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                Text(
                    text = "${filteredProducts.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = product.productName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (product.brands.isNotBlank()) {
                                    Text(
                                        text = product.brands,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (filteredProducts.isEmpty()) {
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
