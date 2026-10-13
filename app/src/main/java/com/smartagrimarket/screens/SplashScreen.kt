package com.smartagrimarket.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "Onboarding"

// ─────────────────────────────────────────────────────────────
// Onboarding slide data
// ─────────────────────────────────────────────────────────────
data class OnboardingSlide(
    val emoji: String,
    val title: String,
    val subtitle: String,
)

private val onboardingSlides = listOf(
    OnboardingSlide(
        emoji = "💰",
        title = "Support local growers",
        subtitle = "Every purchase puts money directly into a farmer's pocket."
    ),
    OnboardingSlide(
        emoji = "🇿🇦",
        title = "Proudly South African",
        subtitle = "Built for farmers and buyers across Mzansi."
    ),
)

// ─────────────────────────────────────────────────────────────
// Branded splash screen — shows your logo, 1.5s
// ─────────────────────────────────────────────────────────────
@Composable
fun BrandedSplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        Log.d(TAG, "Splash screen launched — waiting 1.5s")
        delay(1500)
        Log.d(TAG, "Splash timer done — calling onFinished")
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAF5)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.agrimarket_logo),
                contentDescription = "AgriMarket logo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(40.dp))

            CircularProgressIndicator(
                color = Color(0xFF1B7A3D),
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Onboarding slideshow — no skip button, users must page through
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size + 1 })
    val scope = rememberCoroutineScope()
    val lastIndex = onboardingSlides.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAF5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Extra top spacing so the content isn't pushed by the missing skip button
            Spacer(Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                if (page == 0) {
                    // ─── Slide 0: Logo intro ───
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.agrimarket_logo),
                            contentDescription = "AgriMarket logo",
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .heightIn(max = 180.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(40.dp))
                        Text(
                            "Welcome to AgriMarket",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "The marketplace connecting South African farmers with buyers everywhere.",
                            fontSize = 15.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                } else {
                    // ─── Slides 1..N: feature highlights ───
                    val slide = onboardingSlides[page - 1]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(slide.emoji, fontSize = 96.sp)
                        Spacer(Modifier.height(32.dp))
                        Text(
                            slide.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            slide.subtitle,
                            fontSize = 15.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Pagination dots
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(onboardingSlides.size + 1) { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color(0xFF1B7A3D)
                                else Color(0xFFB0BEC5)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage == lastIndex) {
                        Log.d(TAG, "Get Started tapped — calling onFinished")
                        onFinished()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B7A3D),
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (pagerState.currentPage == lastIndex) "Get Started" else "Next",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}