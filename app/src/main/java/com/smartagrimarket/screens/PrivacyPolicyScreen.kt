package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.ui.theme.*

private data class PolicySection(val title: String, val body: String)

private val sections = listOf(
    PolicySection(
        "1. What we collect",
        "Name, contact details, delivery address, order history, and payment confirmation. We never store your card number — that's handled by our payment provider."
    ),
    PolicySection(
        "2. Why we collect it",
        "To process orders, arrange delivery, prevent fraud, and improve product recommendations."
    ),
    PolicySection(
        "3. Who we share it with",
        "Only the farmer fulfilling your order and the delivery driver assigned to it. We never sell your data to third parties."
    ),
    PolicySection(
        "4. Your rights under POPIA",
        "You may request a copy of your data, correct inaccurate details, or delete your account and associated data at any time."
    ),
    PolicySection(
        "5. Data retention",
        "Order records are kept for 5 years for tax purposes. All other personal data is deleted within 30 days of account closure."
    )
)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 20.sp,
                    modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp)
                )
                Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall)
            }

            Text(
                text = "Last updated: 1 August 2026 · Compliant with POPIA",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sections.forEach { section ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(14.dp)
                    ) {
                        Text(section.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(section.body, style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}