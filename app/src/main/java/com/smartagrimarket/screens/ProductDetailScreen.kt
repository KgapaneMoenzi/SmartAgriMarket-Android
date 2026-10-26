package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FavoritesRepository
import com.smartagrimarket.model.network.ProductImage
import com.smartagrimarket.model.network.ProductsRepository
import com.smartagrimarket.model.network.RecommendedProductDto
import com.smartagrimarket.model.network.RecommendationsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: String,
    productName: String,
    price: Double,
    farmName: String,
    averageRating: Double,
    reviewCount: Int,
    inStock: Boolean,
    unit: String = "item",
    packSize: String? = null,
    bulkMinQty1: Int? = null,
    bulkPrice1: Double? = null,
    bulkMinQty2: Int? = null,
    bulkPrice2: Double? = null,
    imageUrl: String? = null,
    description: String? = null,
    stock: Int? = null,
    currentCartQuantity: Int = 0,
    cartItemCount: Int = 0,
    onCartClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onRequestQuote: (Int) -> Unit = {},
    onReviewsClick: () -> Unit = {},
    onQAClick: () -> Unit = {},
    onBackInStockClick: () -> Unit = {},
    onSimilarProductClick: (RecommendedProductDto) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    var isTogglingFavorite by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    var showQuoteDialog by remember { mutableStateOf(false) }
    var quoteQuantity by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        ProductsRepository.logView(productId)
    }

    LaunchedEffect(productId) {
        when (val result = FavoritesRepository.checkFavorite(productId)) {
            is ApiResult.Success -> isFavorite = result.data
            is ApiResult.Error -> { }
        }
    }

    val maxStock = stock ?: Int.MAX_VALUE
    val remaining = (maxStock - currentCartQuantity).coerceAtLeast(0)
    val atMax = currentCartQuantity >= maxStock
    val lowStock = stock != null && stock in 1..3

    // ── THIS IS THE FIX FOR THE STOCK TEXT ──
    val stockText = if (stock != null) {
        if (packSize != null) {
            "$remaining pack${if (remaining == 1) "" else "s"} left"
        } else {
            "$remaining $unit left"
        }
    } else {
        "In stock"
    }
    // ───────────────────────────────────────

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(320.dp).background(AgriMintSoft)) {
                ProductImage(imageUrl = imageUrl, productName = productName, modifier = Modifier.fillMaxSize().padding(24.dp), fallbackFontSize = 100.sp)

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(AgriCardBg).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AgriTextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape).background(AgriCardBg).clickable(enabled = !isTogglingFavorite) {
                                isTogglingFavorite = true
                                scope.launch {
                                    val result = if (isFavorite) FavoritesRepository.removeFavorite(productId) else FavoritesRepository.addFavorite(productId)
                                    when (result) {
                                        is ApiResult.Success -> isFavorite = result.data
                                        is ApiResult.Error -> { }
                                    }
                                    isTogglingFavorite = false
                                }
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavorite) AgriRedAlert else AgriTextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Box {
                            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(AgriCardBg).clickable(onClick = onCartClick), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = AgriTextPrimary, modifier = Modifier.size(20.dp))
                            }
                            if (cartItemCount > 0) {
                                Box(modifier = Modifier.align(Alignment.TopEnd).size(18.dp).clip(CircleShape).background(AgriGreen), contentAlignment = Alignment.Center) {
                                    Text(if (cartItemCount > 9) "9+" else cartItemCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (packSize != null) "$productName ($packSize)" else productName,
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AgriTextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("R${"%.2f".format(price)} / $unit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AgriGreenDark)
                }

                Spacer(Modifier.height(6.dp))
                Text(farmName, style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                Spacer(Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StarRow(averageRating)
                        Spacer(Modifier.width(8.dp))
                        Text("$farmName · ($reviewCount reviews)", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary, maxLines = 1)
                    }
                    Box(modifier = Modifier.clip(CircleShape).background(AgriMintLight).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(text = stockText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AgriGreenDark) // <-- FIXED
                    }
                }

                if (!inStock) {
                    Spacer(Modifier.height(12.dp))
                    StockBadge("Out of stock", AgriRedAlert)
                } else if (atMax) {
                    Spacer(Modifier.height(12.dp))
                    StockBadge("Max ${stock ?: 0} in your cart", AgriRedAlert)
                } else if (lowStock) {
                    Spacer(Modifier.height(12.dp))
                    StockBadge("Only $stockText", AgriOrange) // <-- FIXED
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    TabLabel(text = "Description", isSelected = selectedTab == 0, onClick = { selectedTab = 0 })
                    TabLabel(text = "Reviews", isSelected = selectedTab == 1, onClick = { selectedTab = 1; onReviewsClick() })
                }

                Spacer(Modifier.height(14.dp))
                Text(text = description ?: "Fresh from $farmName. Straight from the farm to your kitchen — no middlemen, no cold storage.", style = MaterialTheme.typography.bodyMedium, color = AgriTextSecondary, lineHeight = 20.sp)

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AgriCardBg).clickable(onClick = onQAClick).padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Questions & answers", style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AgriTextTertiary, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(24.dp))

                if (inStock && !atMax) {
                    Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AgriGreen, contentColor = Color.White)) {
                        Text(if (stock != null) "Add To Cart · $stockText" else "Add To Cart", fontWeight = FontWeight.Bold, fontSize = 15.sp) // <-- FIXED
                    }
                } else if (inStock && atMax) {
                    Text("You already have the maximum available quantity in your cart.", style = MaterialTheme.typography.bodySmall, color = AgriRedAlert)
                } else {
                    OutlinedButton(onClick = onBackInStockClick, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = AgriGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Notify me when back in stock", fontWeight = FontWeight.Bold, color = AgriGreen)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("We'll alert you the moment the farmer restocks this product.", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                }

                if (bulkMinQty1 != null && bulkPrice1 != null) {
                    Spacer(Modifier.height(32.dp))
                    Text("Buying for a business?", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = AgriTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AgriMintLight.copy(alpha = 0.5f))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Bulk pricing", fontWeight = FontWeight.Bold, color = AgriGreenDark)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$bulkMinQty1+ packs", fontSize = 14.sp) // <-- Changed to packs for consistency
                                Text("R${"%.2f".format(bulkPrice1)} each", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            if (bulkMinQty2 != null && bulkPrice2 != null) {
                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("$bulkMinQty2+ packs", fontSize = 14.sp) // <-- Changed to packs for consistency
                                    Text("R${"%.2f".format(bulkPrice2)} each", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showQuoteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AgriGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriGreen)
                    ) {
                        Text("Request a quote", fontWeight = FontWeight.Bold)
                    }
                    Text("The farmer replies with a final price and delivery date", fontSize = 11.sp, color = AgriTextSecondary, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }

                SimilarProductsCarousel(productId = productId, onProductClick = onSimilarProductClick)
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showQuoteDialog) {
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text("Request Bulk Quote", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You are requesting a quote for $productName.")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = quoteQuantity,
                        onValueChange = { quoteQuantity = it.filter { char -> char.isDigit() } },
                        label = { Text("How many packs do you need?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quoteQuantity.toIntOrNull()
                        if (qty != null && qty > 0) {
                            onRequestQuote(qty)
                            showQuoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreen)
                ) { Text("Send Request") }
            },
            dismissButton = { TextButton(onClick = { showQuoteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StarRow(rating: Double) {
    val fullStars = rating.toInt()
    val hasHalf = (rating - fullStars) >= 0.5
    Row {
        repeat(fullStars) { Icon(Icons.Filled.Star, contentDescription = null, tint = AgriRating, modifier = Modifier.size(16.dp)) }
        if (hasHalf) { Icon(Icons.Filled.StarHalf, contentDescription = null, tint = AgriRating, modifier = Modifier.size(16.dp)) }
        val remaining = 5 - fullStars - if (hasHalf) 1 else 0
        repeat(remaining) { Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = AgriRating.copy(alpha = 0.4f), modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun TabLabel(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isSelected) AgriTextPrimary else AgriTextSecondary)
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.width(if (isSelected) 40.dp else 0.dp).height(2.dp).background(if (isSelected) AgriGreen else Color.Transparent))
    }
}

@Composable
private fun StockBadge(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SimilarProductsCarousel(productId: String, onProductClick: (RecommendedProductDto) -> Unit) {
    var products by remember { mutableStateOf<List<RecommendedProductDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(productId) {
        isLoading = true
        when (val r = RecommendationsRepository.fetchSimilar(productId)) {
            is ApiResult.Success -> products = r.data.products
            is ApiResult.Error -> { }
        }
        isLoading = false
    }

    if (isLoading || products.isEmpty()) return

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text("You might also like ✨", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AgriTextPrimary, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.id }) { product -> SimilarProductCard(product, onClick = { onProductClick(product) }) }
        }
    }
}

@Composable
private fun SimilarProductCard(product: RecommendedProductDto, onClick: () -> Unit) {
    Card(modifier = Modifier.width(140.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AgriCardBg), elevation = CardDefaults.cardElevation(0.dp), onClick = onClick) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(AgriMintSoft), contentAlignment = Alignment.Center) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = product.imageUrl, contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text("🌱", fontSize = 32.sp)
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AgriTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("R${"%.2f".format(product.price)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AgriGreenDark)
            }
        }
    }
}