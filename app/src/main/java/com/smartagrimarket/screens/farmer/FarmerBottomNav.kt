package com.smartagrimarket.screens.farmer

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.ui.theme.AgriCardBg
import com.smartagrimarket.ui.theme.AgriYellow

enum class FarmerTab(val label: String, val emoji: String) {
    HOME("Home", "🏠"),        // Renamed from DASHBOARD to HOME
    ORDERS("Orders", "📦"),
    WEATHER("Weather", "☀️"),
    BUSINESS("Business", "📊"), // The new FNB Financial Dashboard
    ACCOUNT("Account", "👤")
}

@Composable
fun FarmerBottomNav(
    selectedTab: FarmerTab,
    onTabSelected: (FarmerTab) -> Unit
) {
    NavigationBar(
        containerColor = AgriCardBg,
        tonalElevation = 0.dp
    ) {
        FarmerTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Text(
                        text = tab.emoji,
                        fontSize = 20.sp
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AgriYellow,
                    selectedTextColor = AgriYellow,
                    indicatorColor = AgriYellow.copy(alpha = 0.15f)
                )
            )
        }
    }
}