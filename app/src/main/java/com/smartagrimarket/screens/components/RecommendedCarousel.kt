package com.smartagrimarket.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.RecommendedProductDto
import com.smartagrimarket.model.network.RecommendationsRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*

@Composable
fun RecommendedCarousel(
    title: String = "Recommended for you",
    onProductClick: (RecommendedProductDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    var products by remember { mutableStateOf<List<RecommendedProductDto>>(emptyList()) }
    var source by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Personalised for signed-in buyers; trending for guests.
        val hasSession = !TokenStore.token.isNullOrBlank()
        val result = if (hasSession) {
            RecommendationsRepository.fetchForMe()
        } else {
            RecommendationsRepository.fetchTrending()
        }
        when (result) {
            is ApiResult.Success -> {
                products = result.data.products
                source = result.data.source
            }
            is ApiResult.Error -> { /* silent — carousel just doesn't render */ }
        }
        isLoading = false
    }

    if (isLoading || products.isEmpty()) return

    val heading = if (source == "trending") "Trending now" else title

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                heading,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Text("✨", fontSize = 14.sp)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products, key = { it.id }) { product ->
                RecommendedCard(product = product, onClick = { onProductClick(product) })
            }
        }
    }
}

@Composable
private fun RecommendedCard(
    product: RecommendedProductDto,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AgriCardBg)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AgriCreamBg),
            contentAlignment = Alignment.Center
        ) {
            if (!product.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text("🌱", fontSize = 32.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            product.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            "R ${String.format("%.2f", product.price)}",
            style = MaterialTheme.typography.bodySmall,
            color = AgriGreen,
            fontWeight = FontWeight.Bold,
        )
        product.farmName?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary,
                maxLines = 1,
                fontSize = 10.sp,
            )
        }
    }
}