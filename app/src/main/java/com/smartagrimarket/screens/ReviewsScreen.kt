package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.RatingBreakdownDto
import com.smartagrimarket.model.network.ReviewDto
import com.smartagrimarket.model.network.ReviewsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ReviewsScreen(
    productId: String,
    productName: String,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0.0) }
    var totalReviews by remember { mutableStateOf(0) }
    var breakdown by remember { mutableStateOf<List<RatingBreakdownDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var eligible by remember { mutableStateOf(false) }
    var eligibilityReason by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = ReviewsRepository.fetchReviews(productId)) {
                is ApiResult.Success -> {
                    reviews = result.data.reviews
                    averageRating = result.data.averageRating
                    totalReviews = result.data.totalReviews
                    breakdown = result.data.breakdown
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            when (val r = ReviewsRepository.checkEligibility(productId)) {
                is ApiResult.Success -> {
                    eligible = r.data.eligible
                    eligibilityReason = r.data.reason
                }
                is ApiResult.Error -> eligible = false
            }
            isLoading = false
        }
    }

    LaunchedEffect(productId) { load() }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Header ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Reviews",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Text(
                            productName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreen)
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(
                            errorMessage!!,
                            color = AgriRedAlert,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                // ── Summary card ──
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "%.1f".format(averageRating),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary,
                                    fontSize = 44.sp
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    StarRowRating(averageRating)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "$totalReviews review${if (totalReviews == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AgriTextSecondary
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            // Breakdown bars
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                breakdown.sortedByDescending { it.stars }.forEach { b ->
                                    val fraction =
                                        if (totalReviews > 0) b.count.toFloat() / totalReviews else 0f
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${b.stars}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AgriTextSecondary,
                                            modifier = Modifier.width(14.dp)
                                        )
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = AgriRating,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(AgriBorderLight)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(fraction)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(AgriGreen)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "${b.count}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AgriTextSecondary,
                                            modifier = Modifier.width(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Write review button ──
                if (eligible && !showForm) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Button(
                                onClick = { showForm = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreen,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Write a review", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (showForm) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            ReviewForm(
                                isSubmitting = isSubmitting,
                                errorMessage = submitError,
                                onSubmit = { rating, comment ->
                                    scope.launch {
                                        isSubmitting = true
                                        submitError = null
                                        when (val result =
                                            ReviewsRepository.submitReview(productId, rating, comment)) {
                                            is ApiResult.Success -> {
                                                showForm = false
                                                load()
                                            }
                                            is ApiResult.Error -> submitError = result.message
                                        }
                                        isSubmitting = false
                                    }
                                },
                                onCancel = { showForm = false }
                            )
                        }
                    }
                }

                if (!eligible && eligibilityReason == "not_purchased") {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AgriMintLight)
                                .padding(14.dp)
                        ) {
                            Text(
                                "You can leave a review after you've purchased this product.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriGreenDark
                            )
                        }
                    }
                }

                // ── Recent reviews heading ──
                item {
                    Text(
                        "Recent reviews",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                if (reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AgriCardBg)
                                .padding(20.dp)
                        ) {
                            Text(
                                "No reviews yet. Be the first to leave one!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AgriTextSecondary
                            )
                        }
                    }
                } else {
                    items(reviews) { review ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            ReviewCard(review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewForm(
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onCancel: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Write a review",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(12.dp))

            Text("Your rating", style = MaterialTheme.typography.labelSmall, color = AgriTextSecondary)
            Spacer(Modifier.height(6.dp))
            Row {
                for (i in 1..5) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "$i star",
                        tint = if (i <= rating) AgriRating else AgriBorderLight,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { rating = i }
                            .padding(2.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Share your experience…") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCreamBg,
                    focusedContainerColor = AgriCreamBg,
                    unfocusedBorderColor = AgriBorderLight,
                    focusedBorderColor = AgriGreen,
                    cursorColor = AgriGreen
                )
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriTextPrimary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        if (comment.isNotBlank()) onSubmit(rating, comment.trim())
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = comment.isNotBlank() && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AgriMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.reviewerName.firstOrNull()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            color = AgriGreenDark,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        review.reviewerName,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    shortDate(review.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = AgriTextTertiary
                )
            }

            Spacer(Modifier.height(10.dp))
            StarRowRating(review.rating.toDouble())
            Spacer(Modifier.height(8.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = AgriTextPrimary
            )
        }
    }
}

@Composable
private fun StarRowRating(rating: Double) {
    val full = rating.toInt()
    val hasHalf = (rating - full) >= 0.5
    Row {
        repeat(full) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = AgriRating,
                modifier = Modifier.size(16.dp)
            )
        }
        if (hasHalf) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = AgriRating.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
        val remaining = 5 - full - if (hasHalf) 1 else 0
        repeat(remaining) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = AgriBorderLight,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun shortDate(iso: String): String {
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso) ?: return ""
        val formatter = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.US)
        formatter.format(date)
    } catch (_: Exception) { "" }
}