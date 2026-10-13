package com.smartagrimarket.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartagrimarket.ui.theme.*

// ── Contact details (single source of truth) ──
private const val SUPPORT_WHATSAPP_E164 = "27825550134"   // +27 82 555 0134
private const val SUPPORT_PHONE_TEL    = "0135550100"
private const val SUPPORT_EMAIL        = "help@agrimarkethub.com"

@Composable
fun AboutSupportScreen(
    onBack: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onReturnsPolicyClick: () -> Unit = {},
    onFarmerTermsClick: () -> Unit = {},
    onFarmerTrainingPackagingClick: () -> Unit = {},
    onFarmerTrainingPricingClick: () -> Unit = {},
    onApplyVerifiedBadgeClick: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onBack)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("About & Support", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AgriCardBg)
                    .padding(16.dp)
            ) {
                Text(
                    "AgriMarket connects South African farmers directly to buyers — fresher produce, fairer prices, less middleman markup.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Get in touch")
            InfoGroup {
                ContactRow(
                    label = "WhatsApp support",
                    value = "082 555 0134",
                    emoji = "💬",
                    onClick = { openWhatsApp(context, SUPPORT_WHATSAPP_E164) }
                )
                InfoDivider()
                ContactRow(
                    label = "Call us",
                    value = "013 555 0100",
                    emoji = "📞",
                    onClick = { dialPhone(context, SUPPORT_PHONE_TEL) }
                )
                InfoDivider()
                ContactRow(
                    label = "Email",
                    value = SUPPORT_EMAIL,
                    emoji = "✉️",
                    onClick = { sendEmail(context, SUPPORT_EMAIL) }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Legal")
            LinkGroup {
                LinkRow("Terms & Conditions", onTermsClick)
                LinkDivider()
                LinkRow("Privacy Policy", onPrivacyClick)
                LinkDivider()
                LinkRow("Returns & Refunds Policy", onReturnsPolicyClick)
                LinkDivider()
                LinkRow("Farmer Terms of Service", onFarmerTermsClick)
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("For farmers")
            LinkGroup {
                LinkRow("Farmer training: packaging tips", onFarmerTrainingPackagingClick)
                LinkDivider()
                LinkRow("Farmer training: pricing your produce", onFarmerTrainingPricingClick)
                LinkDivider()
                LinkRow("Apply for Verified Farmer badge", onApplyVerifiedBadgeClick)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Intents
// ═══════════════════════════════════════════════════════════

private fun openWhatsApp(context: Context, e164: String) {
    // Primary: the wa.me universal link (works with or without WhatsApp installed)
    val waMe = Uri.parse("https://wa.me/$e164")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, waMe).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        return
    } catch (_: Exception) { /* fall through */ }

    // Fallback: use the whatsapp:// scheme directly
    try {
        val appUri = Uri.parse("whatsapp://send?phone=$e164")
        context.startActivity(Intent(Intent.ACTION_VIEW, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
        // No WhatsApp, no browser — nothing we can do
    }
}

private fun dialPhone(context: Context, tel: String) {
    val uri = Uri.parse("tel:$tel")
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) { /* no dialer */ }
}

private fun sendEmail(context: Context, address: String) {
    val uri = Uri.parse("mailto:$address")
    try {
        context.startActivity(Intent(Intent.ACTION_SENDTO, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
        // Fall back to Gmail compose URL
        try {
            val gmail = Uri.parse(
                "https://mail.google.com/mail/?view=cm&fs=1&to=$address"
            )
            context.startActivity(Intent(Intent.ACTION_VIEW, gmail).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) { /* nothing */ }
    }
}

// ═══════════════════════════════════════════════════════════
// Layout helpers
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun InfoGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AgriCardBg)
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
    emoji: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji)
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AgriTextSecondary
            )
        }
    }
}

@Composable
private fun InfoDivider() = HorizontalDivider(color = AgriCreamBg, thickness = 1.dp)

@Composable
private fun LinkGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AgriCardBg)
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AgriTextSecondary)
    }
}

@Composable
private fun LinkDivider() = HorizontalDivider(color = AgriCreamBg, thickness = 1.dp)