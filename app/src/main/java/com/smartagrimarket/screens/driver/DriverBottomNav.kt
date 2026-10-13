package com.smartagrimarket.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
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

enum class DriverTab(val label: String) {
    HOME("Home"),
    JOBS("Jobs"),
    ACCOUNT("Account")
}

@Composable
fun DriverBottomNav(
    selectedTab: DriverTab,
    onTabSelected: (DriverTab) -> Unit,
    activeJobCount: Int = 0
) {
    NavigationBar(
        containerColor = AgriCardBg,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == DriverTab.HOME,
            onClick = { onTabSelected(DriverTab.HOME) },
            icon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = DriverTab.HOME.label,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    DriverTab.HOME.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = navColors()
        )
        NavigationBarItem(
            selected = selectedTab == DriverTab.JOBS,
            onClick = { onTabSelected(DriverTab.JOBS) },
            icon = {
                Box {
                    Icon(
                        Icons.AutoMirrored.Filled.ListAlt,
                        contentDescription = DriverTab.JOBS.label,
                        modifier = Modifier.size(24.dp)
                    )
                    if (activeJobCount > 0) {
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
                                text = if (activeJobCount > 9) "9+" else activeJobCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = {
                Text(
                    DriverTab.JOBS.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = navColors()
        )
        NavigationBarItem(
            selected = selectedTab == DriverTab.ACCOUNT,
            onClick = { onTabSelected(DriverTab.ACCOUNT) },
            icon = {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = DriverTab.ACCOUNT.label,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    DriverTab.ACCOUNT.label,
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