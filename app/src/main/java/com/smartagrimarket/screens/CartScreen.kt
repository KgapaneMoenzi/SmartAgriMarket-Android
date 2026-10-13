package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.CartState
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.CartValidateItem
import com.smartagrimarket.model.network.ProductImage
import com.smartagrimarket.model.network.ProductsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    cart: CartState,
    onCheckoutClick: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val cartLines: List<CartState.CartLine> = cart.lines.values.toList()
    val subtotal = cartLines.sumOf { it.product.price * it.quantity }
    val total = subtotal

    // ── Prune stale items on first mount ──
    // The buyer may have added something to the cart days ago. When
    // they come back to the cart screen, we re-check the current
    // product state before they reach checkout.
    var hasPruned by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (hasPruned) return@LaunchedEffect
        hasPruned = true

        val items = cart.lines.values.map {
            CartValidateItem(
                productId = it.product.id,
                quantity = it.quantity,
                name = it.product.name,
            )
        }
        if (items.isEmpty()) return@LaunchedEffect

        when (val result = ProductsRepository.validateCart(items)) {
            is ApiResult.Success -> {
                val unavailable = result.data.unavailable
                if (unavailable.isEmpty()) return@LaunchedEffect

                val removedIds = mutableSetOf<String>()
                val clampedTo = mutableMapOf<String, Int>()

                for (u in unavailable) {
                    when (u.reason) {
                        "removed" -> removedIds.add(u.productId)
                        "out_of_stock" -> removedIds.add(u.productId)
                        "insufficient_stock" -> {
                            val stock = u.availableStock ?: 0
                            clampedTo[u.productId] = stock
                        }
                    }
                }

                val pruneResult = cart.pruneStale(removedIds, clampedTo)

                if (pruneResult.removedCount > 0) {
                    val names = unavailable
                        .filter { it.productId in removedIds }
                        .map { it.name }
                        .distinct()
                        .take(2)
                        .joinToString(", ")
                    val extra = if (pruneResult.removedCount > 2) " and ${pruneResult.removedCount - 2} more" else ""
                    android.widget.Toast.makeText(
                        context,
                        "$names$extra no longer available — removed from cart.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                if (pruneResult.clampedCount > 0) {
                    android.widget.Toast.makeText(
                        context,
                        "Some quantities were reduced to the available stock.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
            is ApiResult.Error -> {
                // Silent — non-fatal. Checkout will surface the real
                // error if the state is actually stale.
            }
        }
    }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        if (cartLines.isEmpty()) {
            EmptyCart(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My Cart",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                }

                // ── Item list ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    cartLines.forEach { line ->
                        CartLineItem(
                            product = line.product,
                            quantity = line.quantity,
                            isAtMax = cart.isAtMax(line.product.id),
                            onIncrement = { cart.increment(line.product.id) },
                            onDecrement = { cart.decrement(line.product.id) },
                            onRemove = { cart.remove(line.product.id) }
                        )
                    }
                }

                // ── Summary ──
                CartSummaryCard(
                    subtotal = subtotal,
                    total = total,
                    onCheckoutClick = onCheckoutClick
                )
            }
        }
    }
}

@Composable
private fun EmptyCart(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(AgriMintLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = AgriGreen,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Your cart is empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Add some fresh produce to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = AgriTextSecondary
        )
    }
}

@Composable
private fun CartLineItem(
    product: Product,
    quantity: Int,
    isAtMax: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AgriMintSoft),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    imageUrl = product.imageUrl,
                    productName = product.name,
                    modifier = Modifier.fillMaxSize(),
                    fallbackFontSize = 22.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            // Name + price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    product.farmName.ifBlank { "Local farm" },
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "R${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenDark
                )
                if (isAtMax && product.stock != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Max ${product.stock} available",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriRedAlert
                    )
                }
            }

            // Stepper + remove
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.Delete, "Remove",
                        tint = AgriRedAlert,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                QuantityStepper(
                    quantity = quantity,
                    incrementEnabled = !isAtMax,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement
                )
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    incrementEnabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AgriMintSoft)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Filled.Remove, "Decrease",
                modifier = Modifier.size(16.dp),
                tint = AgriGreenDark
            )
        }
        Text(
            quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IconButton(
            onClick = onIncrement,
            enabled = incrementEnabled,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Filled.Add, "Increase",
                modifier = Modifier.size(16.dp),
                tint = if (incrementEnabled) AgriGreenDark
                else AgriTextTertiary
            )
        }
    }
}

@Composable
private fun CartSummaryCard(
    subtotal: Double,
    total: Double,
    onCheckoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AgriCardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = AgriTextSecondary)
                Text(
                    "R${"%.2f".format(subtotal)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = AgriBorderLight)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Text(
                    "R${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenDark
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Checkout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}