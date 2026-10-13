package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.ui.theme.*

enum class BuyerTab(val label: String) {
    HOME("Home"),
    CART("Cart"),
    ACCOUNT("Account")
}

@Composable
fun SmartAgriMarketBottomNav(
    selectedTab: BuyerTab,
    onTabSelected: (BuyerTab) -> Unit,
    cartItemCount: Int = 0,
    isGuest: Boolean = false,
) {
    NavigationBar(
        containerColor = AgriCardBg,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == BuyerTab.HOME,
            onClick = { onTabSelected(BuyerTab.HOME) },
            icon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = BuyerTab.HOME.label,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(BuyerTab.HOME.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = navColors()
        )

        NavigationBarItem(
            selected = selectedTab == BuyerTab.CART,
            onClick = { onTabSelected(BuyerTab.CART) },
            icon = {
                Box {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = BuyerTab.CART.label,
                        modifier = Modifier.size(24.dp)
                    )
                    if (cartItemCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-6).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(AgriGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (cartItemCount > 9) "9+" else cartItemCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = { Text(BuyerTab.CART.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = navColors()
        )

        NavigationBarItem(
            selected = selectedTab == BuyerTab.ACCOUNT && !isGuest,
            onClick = { onTabSelected(BuyerTab.ACCOUNT) },
            icon = {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = if (isGuest) "Sign in" else BuyerTab.ACCOUNT.label,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    if (isGuest) "Sign in" else BuyerTab.ACCOUNT.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = navColors()
        )
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AgriGreen,
    selectedTextColor = AgriGreenDark,
    unselectedIconColor = AgriTextTertiary,
    unselectedTextColor = AgriTextTertiary,
    indicatorColor = Color.Transparent
)