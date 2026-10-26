package com.smartagrimarket.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.ProductCategory
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.BulkQuoteDto
import com.smartagrimarket.model.network.ProductsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerQuotesScreen(
    onBack: () -> Unit = {},
    onCheckoutQuote: (Product, Int, Double) -> Unit, // Product, Quantity, Final Price
    bottomBar: @Composable () -> Unit = {}
) {
    var quotes by remember { mutableStateOf<List<BulkQuoteDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            when (val res = ProductsRepository.fetchMyQuotes()) {
                is ApiResult.Success -> quotes = res.data
                is ApiResult.Error -> { }
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = AgriCreamBg,
        topBar = {
            TopAppBar(
                title = { Text("My Quotes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AgriCreamBg)
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriGreen)
            }
        } else if (quotes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No replies yet.", color = AgriTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quotes) { quote ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = if (quote.product.packSize != null) "${quote.product.name} (${quote.product.packSize})" else quote.product.name,
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                                )
                                Text("R${"%.2f".format(quote.farmerReplyPrice ?: 0.0)}", fontWeight = FontWeight.Bold, color = AgriGreen)
                            }
                            Text("Qty: ${quote.quantity} packs", fontSize = 14.sp)
                            if (!quote.farmerNote.isNullOrBlank()) {
                                Text("Note: ${quote.farmerNote}", fontSize = 12.sp, color = AgriTextSecondary)
                            }
                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val product = Product(
                                        id = quote.product.id,
                                        name = quote.product.name,
                                        price = quote.farmerReplyPrice ?: 0.0,
                                        farmName = "Farm",
                                        category = ProductCategory.ALL,
                                        stock = quote.quantity,
                                        packSize = quote.product.packSize,
                                        farmerEmail = quote.product.farmerEmail // <-- ADDED THIS LINE
                                    )
                                    onCheckoutQuote(product, quote.quantity, quote.farmerReplyPrice ?: 0.0)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreen)
                            ) {
                                Text("Accept & Checkout", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}