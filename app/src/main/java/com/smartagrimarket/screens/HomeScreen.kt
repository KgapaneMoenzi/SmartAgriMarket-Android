package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.ProductCategory
import com.smartagrimarket.model.SampleData
import com.smartagrimarket.model.network.ProductImage
import com.smartagrimarket.model.network.RecommendedProductDto
import com.smartagrimarket.screens.components.RecommendedCarousel
import com.smartagrimarket.ui.theme.*

@Composable
fun HomeScreen(
    isLoggedIn: Boolean = false,
    buyerFirstName: String? = null,
    products: List<Product> = SampleData.marketplace,
    cartItemCount: Int = 0,
    unreadNotificationCount: Int = 0,
    onProductClick: (Product) -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onRemoveFromCart: (Product) -> Unit = {},
    cartQuantityFor: (String) -> Int = { 0 },
    onCartClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onRecommendationClick: (RecommendedProductDto) -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(ProductCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val trimmedQuery = searchQuery.trim()
    val isSearching = trimmedQuery.isNotBlank()

    val filteredProducts = remember(selectedCategory, trimmedQuery, products) {
        val byCategory = if (selectedCategory == ProductCategory.ALL) {
            products
        } else {
            products.filter { it.category == selectedCategory }
        }
        if (trimmedQuery.isBlank()) byCategory
        else byCategory.filter { p ->
            p.name.contains(trimmedQuery, ignoreCase = true) ||
                    p.category.label.contains(trimmedQuery, ignoreCase = true) ||
                    p.farmName.contains(trimmedQuery, ignoreCase = true)
        }
    }

    val newListings = remember(products) { products.take(5) }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Greeting header ──
            item(span = { GridItemSpan(2) }) {
                StoreGreetingHeader(
                    firstName = if (isLoggedIn) buyerFirstName else "Guest",
                    unreadCount = unreadNotificationCount,
                    cartCount = cartItemCount,
                    onCartClick = onCartClick,
                    onNotificationsClick = onNotificationsClick
                )
            }

            // ── Search bar ──
            item(span = { GridItemSpan(2) }) {
                StoreSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            // ── Categories ──
            if (!isSearching) {
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            lazyRowItems(ProductCategory.values().toList()) { category ->
                                CategoryChip(
                                    label = category.label,
                                    isSelected = category == selectedCategory,
                                    onClick = { selectedCategory = category }
                                )
                            }
                        }
                    }
                }

                // ── Promo banner ──
                item(span = { GridItemSpan(2) }) {
                    StorePromoBanner()
                }
            }

            // ── Section heading ──
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = if (isSearching) {
                        if (filteredProducts.isEmpty()) "No results for \"$trimmedQuery\""
                        else "Results for \"$trimmedQuery\" (${filteredProducts.size})"
                    } else "Fresh Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            // ── Product grid ──
            if (filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyProductsState(
                        query = trimmedQuery,
                        onClearSearch = { searchQuery = "" }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    val qty = cartQuantityFor(product.id)
                    StoreProductCard(
                        product = product,
                        cartQuantity = qty,
                        onClick = { onProductClick(product) },
                        onIncrement = { onAddToCart(product) },
                        onDecrement = { onRemoveFromCart(product) }
                    )
                }
            }

            // ── Recommended carousel ──
            if (!isSearching) {
                item(span = { GridItemSpan(2) }) {
                    RecommendedCarousel(
                        onProductClick = { rec ->
                            val matching = products.find { it.id == rec.id }
                            if (matching != null) onProductClick(matching)
                            else onRecommendationClick(rec)
                        }
                    )
                }

                if (newListings.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        NewListingsSection(products = newListings)
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    SeasonalTipCard()
                }
            }

            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Greeting header
// ─────────────────────────────────────────────────────────────
@Composable
private fun StoreGreetingHeader(
    firstName: String?,
    unreadCount: Int,
    cartCount: Int,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Hi, ${firstName ?: "there"}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Fresh from the farm, every day",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
        }

        Box {
            IconButton(onClick = onCartClick) {
                Icon(Icons.Filled.ShoppingCart, "Cart", tint = AgriTextPrimary)
            }
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AgriRedAlert),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (cartCount > 9) "9+" else cartCount.toString(),
                        color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box {
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Filled.Notifications, "Notifications", tint = AgriTextPrimary)
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AgriRedAlert),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Search bar
// ─────────────────────────────────────────────────────────────
@Composable
private fun StoreSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        color = AgriCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = AgriTextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search fresh produce…",
                        color = AgriTextTertiary,
                        fontSize = 14.sp
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = AgriTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AgriGreen
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Category chip
// ─────────────────────────────────────────────────────────────
@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) AgriGreen else AgriCardBg
    val fg = if (isSelected) Color.White else AgriTextPrimary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            color = fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Promo banner
// ─────────────────────────────────────────────────────────────
@Composable
private fun StorePromoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriMintLight),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Fresh Products from the Farmer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenDark
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Handpicked, locally grown, delivered to your door.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriGreenDark.copy(alpha = 0.75f)
                )
            }
            Text("🧺", fontSize = 68.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Product card — real ratings, no clock
// ─────────────────────────────────────────────────────────────
@Composable
private fun StoreProductCard(
    product: Product,
    cartQuantity: Int,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val isOutOfStock = product.stock != null && product.stock <= 0
    val atMax = product.stock != null && cartQuantity >= product.stock
    val inCart = cartQuantity > 0

    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (isOutOfStock) 0.6f else 1f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Column {
            // ── Image area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(AgriMintSoft)
            ) {
                ProductImage(
                    imageUrl = product.imageUrl,
                    productName = product.name,
                    modifier = Modifier.fillMaxSize(),
                    fallbackFontSize = 42.sp
                )

                // "+" add button — only when not in cart
                if (!inCart) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isOutOfStock) Color(0xFFB0BEC5) else AgriGreen)
                            .clickable(enabled = !isOutOfStock, onClick = onIncrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add, "Add to cart",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Info ──
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                // ── Real ratings only (clock removed) ──
                if (product.reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star, null,
                            tint = AgriRating,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "%.1f".format(product.averageRating),
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "(${product.reviewCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriTextTertiary
                        )
                    }
                } else {
                    Text(
                        "No reviews yet",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextTertiary
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Price + stepper ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "R${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenDark
                    )

                    if (inCart) {
                        QuantityStepper(
                            quantity = cartQuantity,
                            incrementEnabled = !atMax,
                            onIncrement = onIncrement,
                            onDecrement = onDecrement
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Quantity stepper
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuantityStepper(
    quantity: Int,
    incrementEnabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AgriGreen)
            .padding(horizontal = 3.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Decrease quantity",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            quantity.toString(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (incrementEnabled) 0.25f else 0.10f))
                .clickable(enabled = incrementEnabled, onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase quantity",
                tint = Color.White.copy(alpha = if (incrementEnabled) 1f else 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// New listings
// ─────────────────────────────────────────────────────────────
@Composable
private fun NewListingsSection(products: List<Product>) {
    Column {
        Text(
            "New listings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AgriCardBg)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.forEach { product ->
                val isOut = product.stock != null && product.stock <= 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductImage(
                            imageUrl = product.imageUrl,
                            productName = product.name,
                            modifier = Modifier.size(32.dp),
                            fallbackFontSize = 14.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(product.name, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        if (isOut) "Out of stock" else "R${"%.2f".format(product.price)}",
                        color = if (isOut) AgriRedAlert else AgriGreenDark,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonalTipCard() {
    val tip = SampleData.seasonalTip
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(tip.emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Text(tip.message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyProductsState(query: String, onClearSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 36.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            if (query.isBlank()) "No products yet" else "No matches for \"$query\"",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Try a different search or category",
            style = MaterialTheme.typography.bodyMedium,
            color = AgriTextSecondary
        )
        if (query.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClearSearch, shape = RoundedCornerShape(12.dp)) {
                Text("Clear search", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}