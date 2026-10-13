package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.ProductDto
import com.smartagrimarket.model.network.ProductsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FarmerMyProductsScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    // Restock dialog state
    var restockTarget by remember { mutableStateOf<ProductDto?>(null) }
    var isRestocking by remember { mutableStateOf(false) }
    var restockError by remember { mutableStateOf<String?>(null) }

    // Delete state
    var deleteTarget by remember { mutableStateOf<ProductDto?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var deletingProductId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        when (val result = ProductsRepository.fetchMyProductsRaw()) {
            is ApiResult.Success -> products = result.data
            is ApiResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Top bar ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AgriTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "My Products",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable { reloadKey++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔄", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Hint ──
            if (products.isNotEmpty() && !isLoading) {
                Text(
                    "Long-press a listing to delete it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AgriTextPrimary)
                    }
                }

                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AgriRedAlert.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚠️", fontSize = 28.sp)
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                errorMessage!!,
                                color = AgriRedAlert,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { reloadKey++ }) {
                                Text("Retry", fontWeight = FontWeight.Bold, color = AgriGreenDark)
                            }
                        }
                    }
                }

                products.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AgriMintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🌾", fontSize = 36.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No listings yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Head back to your dashboard to upload one.",
                                color = AgriTextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isDeleting = deletingProductId == product.id,
                                onRestock = { chosen ->
                                    restockError = null
                                    restockTarget = chosen
                                },
                                onLongPress = {
                                    if (deletingProductId == null) {
                                        deleteError = null
                                        deleteTarget = product
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Restock dialog ──
    restockTarget?.let { target ->
        RestockDialog(
            product = target,
            isSaving = isRestocking,
            errorMessage = restockError,
            onDismiss = {
                if (!isRestocking) {
                    restockTarget = null
                    restockError = null
                }
            },
            onConfirm = { amountToAdd ->
                isRestocking = true
                restockError = null
                scope.launch {
                    when (val result = ProductsRepository.addStock(
                        productId = target.id,
                        currentStock = target.stock ?: 0,
                        amountToAdd = amountToAdd
                    )) {
                        is ApiResult.Success -> {
                            restockTarget = null
                            restockError = null
                            reloadKey++
                        }
                        is ApiResult.Error -> {
                            restockError = result.message
                        }
                    }
                    isRestocking = false
                }
            }
        )
    }

    // ── Delete confirmation dialog ──
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    deleteTarget = null
                    deleteError = null
                }
            },
            title = { Text("Delete this listing?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "\"${target.name}\" will be permanently removed from the marketplace. " +
                                "Buyers will no longer see or be able to order it."
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This cannot be undone.",
                        color = AgriRedAlert,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (!deleteError.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AgriRedAlert.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                deleteError!!,
                                fontSize = 12.sp,
                                color = AgriRedAlert,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        deleteError = null
                        isDeleting = true
                        deletingProductId = target.id
                        scope.launch {
                            when (val result = ProductsRepository.deleteProduct(target.id)) {
                                is ApiResult.Success -> {
                                    products = products.filterNot { it.id == target.id }
                                    deleteTarget = null
                                    deletingProductId = null
                                    deleteError = null
                                }
                                is ApiResult.Error -> {
                                    deleteError = result.message
                                    deletingProductId = null
                                }
                            }
                            isDeleting = false
                        }
                    }
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AgriRedAlert
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Deleting…", color = AgriRedAlert)
                    } else {
                        Text(
                            "Delete",
                            color = AgriRedAlert,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        deleteTarget = null
                        deleteError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductCard(
    product: ProductDto,
    isDeleting: Boolean,
    onRestock: (ProductDto) -> Unit,
    onLongPress: () -> Unit
) {
    val stock = product.stock
    val isLowStock = stock != null && stock in 1..2
    val isOutOfStock = stock != null && stock <= 0

    val cardAlpha = if (isDeleting) 0.5f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AgriCardBg)
            .combinedClickable(
                onClick = { /* no-op — long-press is the action */ },
                onLongClick = if (isDeleting) null else onLongPress
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // ── Image / emoji badge ──
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AgriCreamBg),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🌾", fontSize = 26.sp)
                }
                if (isDeleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AgriRedAlert.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // ── Name + price ──
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AgriTextPrimary.copy(alpha = cardAlpha)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "R ${String.format("%.2f", product.price)}",
                    color = AgriGreenDark.copy(alpha = cardAlpha),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.width(8.dp))

            // ── Stock chip ──
            StockChip(
                stock = stock,
                isLow = isLowStock,
                isOut = isOutOfStock,
                alpha = cardAlpha
            )
        }

        // ── Warning strip ──
        if (isOutOfStock || isLowStock) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AgriRedAlert.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOutOfStock) "🚫" else "⚠️",
                    fontSize = 15.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isOutOfStock)
                        "Out of stock — buyers can't order this."
                    else
                        "Only $stock left — restock soon.",
                    color = AgriRedAlert,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Restock button ──
        OutlinedButton(
            onClick = { onRestock(product) },
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AgriGreenDark
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isOutOfStock) AgriRedAlert.copy(alpha = 0.4f)
                else AgriGreen.copy(alpha = 0.4f)
            )
        ) {
            Text("➕ Add stock", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Stock chip — compact right-aligned status.
// ─────────────────────────────────────────────────────────────
@Composable
private fun StockChip(
    stock: Int?,
    isLow: Boolean,
    isOut: Boolean,
    alpha: Float
) {
    val (bg, fg) = when {
        isOut -> AgriRedAlert.copy(alpha = 0.15f * alpha) to AgriRedAlert
        isLow -> AgriRedAlert.copy(alpha = 0.12f * alpha) to AgriRedAlert
        stock == null -> AgriTextSecondary.copy(alpha = 0.12f) to AgriTextSecondary
        else -> AgriGreen.copy(alpha = 0.12f * alpha) to AgriGreenDark
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stock?.toString() ?: "—",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
        Text(
            text = "in stock",
            fontSize = 10.sp,
            color = fg.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RestockDialog(
    product: ProductDto,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amountToAdd = amountText.toIntOrNull()
    val currentStock = product.stock ?: 0
    val isValid = amountToAdd != null && amountToAdd > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add stock to ${product.name}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Current stock: $currentStock",
                    fontSize = 13.sp,
                    color = AgriTextSecondary
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() }.take(6)
                    },
                    label = { Text("Amount to add") },
                    placeholder = { Text("e.g. 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                if (amountToAdd != null && amountToAdd > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AgriGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$currentStock + $amountToAdd = ",
                            fontSize = 13.sp,
                            color = AgriTextPrimary
                        )
                        Text(
                            "${currentStock + amountToAdd}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriGreenDark
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "(new total)",
                            fontSize = 11.sp,
                            color = AgriTextSecondary
                        )
                    }
                } else {
                    Text(
                        "This will be added to the current stock.",
                        fontSize = 12.sp,
                        color = AgriTextSecondary
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            errorMessage,
                            fontSize = 12.sp,
                            color = AgriRedAlert,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid) onConfirm(amountToAdd!!) },
                enabled = isValid && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Adding…")
                } else {
                    Text(
                        if (amountToAdd != null && amountToAdd > 0) "Add $amountToAdd"
                        else "Add",
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenDark
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}