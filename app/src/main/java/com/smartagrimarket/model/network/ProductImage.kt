package com.smartagrimarket.model.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.ui.theme.AgriCardBg
import com.smartagrimarket.ui.theme.AgriTextSecondary

@Composable
fun ProductImage(
    imageUrl: String?,
    productName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackFontSize: TextUnit = 24.sp
) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AgriCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = productName.take(1).uppercase(),
                fontSize = fallbackFontSize,
                color = AgriTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}