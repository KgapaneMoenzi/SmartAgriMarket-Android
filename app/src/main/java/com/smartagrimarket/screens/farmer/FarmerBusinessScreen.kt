package com.smartagrimarket.screens.farmer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.BusinessDashboardDto
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*
import com.smartagrimarket.utils.BusinessPassportPdfGenerator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerBusinessScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var dashboardData by remember { mutableStateOf<BusinessDashboardDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showPassport by remember { mutableStateOf(false) }

    val tabs = listOf("Overview", "Cash Flow", "Get Paid")

    LaunchedEffect(Unit) {
        when (val result = FarmerRepository.fetchBusinessDashboard()) {
            is ApiResult.Success -> dashboardData = result.data
            is ApiResult.Error -> { /* Handle error silently */ }
        }
        isLoading = false
    }

    if (showPassport) {
        BusinessPassportView(
            data = dashboardData,
            onBack = { showPassport = false }
        )
        return
    }

    Scaffold(
        containerColor = AgriCreamBg,
        topBar = {
            TopAppBar(
                title = { Text("Business", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AgriCreamBg,
                contentColor = AgriGreen,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriGreen)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    when (selectedTab) {
                        0 -> BusinessOverviewTab(dashboardData, onViewPassport = { showPassport = true })
                        1 -> CashFlowTab(dashboardData)
                        2 -> GetPaidTab()
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BUSINESS PASSPORT VIEW
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessPassportView(data: BusinessDashboardDto?, onBack: () -> Unit) {
    val context = LocalContext.current
    val readiness = data?.fundingReadiness ?: 65
    val orders = data?.totalOrders ?: 0
    val revenue = data?.totalRevenue ?: 0.00
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AgriCreamBg,
        topBar = {
            TopAppBar(
                title = { Text("Business Passport", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AgriCreamBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Funding & support readiness",
                color = AgriTextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                CircularProgressIndicator(
                    progress = { readiness / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = AgriGreen,
                    trackColor = AgriMintLight,
                    strokeWidth = 12.dp
                )
                Text(
                    "$readiness%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Text(
                    "You have $orders completed orders and R${String.format(Locale.US, "%.2f", revenue)} in verified sales.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AgriTextPrimary,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            PassportChecklistRow("Profile photo added", "Done")
            HorizontalDivider(color = AgriBorderLight)
            PassportChecklistRow("Bank account linked", "Done")
            HorizontalDivider(color = AgriBorderLight)
            PassportChecklistRow("Completed orders", "$orders / 18")

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    isExporting = true
                    val farmerName = TokenStore.currentUser?.fullName ?: "Farmer"
                    val success = BusinessPassportPdfGenerator.generateAndSharePdf(context, data, farmerName)
                    if (!success) {
                        Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                    }
                    isExporting = false
                },
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreen)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Generating...", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                } else {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Business Passport (PDF)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Attach it to government or funding applications.",
                color = AgriTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PassportChecklistRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = AgriTextPrimary)
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (value == "Done") AgriGreen else AgriTextPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 1: OVERVIEW
// ─────────────────────────────────────────────────────────────
@Composable
private fun BusinessOverviewTab(data: BusinessDashboardDto?, onViewPassport: () -> Unit) {
    var selectedProgram by remember { mutableStateOf<String?>(null) }
    var isAllTime by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Select data based on toggle
    val netProfit = if (isAllTime) data?.totalNetProfit ?: 0.0 else data?.netProfit ?: 0.0
    val revenue = if (isAllTime) data?.totalRevenue ?: 0.0 else data?.revenueThisWeek ?: 0.0
    val fee = if (isAllTime) data?.totalPlatformFee ?: 0.0 else data?.platformFee ?: 0.0
    val orders = if (isAllTime) data?.totalOrders ?: 0 else data?.weeklyOrderCount ?: 0
    val avgOrder = if (isAllTime) data?.totalAvgOrderValue ?: 0.0 else data?.avgOrderValue ?: 0.0

    val pending = data?.pendingPayout ?: 0.0
    val readiness = data?.fundingReadiness ?: 0
    val topProducts = data?.topProducts ?: emptyList()

    // Dynamic text based on score
    val candidateText = when {
        readiness >= 80 -> "Excellent! Your $readiness% score makes you highly eligible for major grants."
        readiness >= 65 -> "Your $readiness% score makes you a strong candidate."
        readiness >= 40 -> "You are making progress! Complete a few more orders to unlock full funding."
        else -> "Keep trading! Build up your score to qualify for government support."
    }

    // ── Toggle Switch ──
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AgriCardBg)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (!isAllTime) AgriTextPrimary else Color.Transparent)
                    .clickable { isAllTime = false }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("This Week", color = if (!isAllTime) Color.White else AgriTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isAllTime) AgriTextPrimary else Color.Transparent)
                    .clickable { isAllTime = true }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("All Time", color = if (isAllTime) Color.White else AgriTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }

    // ── Hero Card ──
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AgriGreen)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                if (isAllTime) "Net profit (All Time)" else "Net profit this week",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            Text("R${String.format(Locale.US, "%.2f", netProfit)}", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("R${String.format(Locale.US, "%.2f", revenue)} revenue · 15% fee (R${String.format(Locale.US, "%.2f", fee)})", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pending payout", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    Text("R${String.format(Locale.US, "%.2f", pending)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$orders Orders", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    Text("R${String.format(Locale.US, "%.2f", avgOrder)} Avg. order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewPassport() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Funding Readiness", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("$readiness%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AgriGreen)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { readiness / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = AgriGreen,
                trackColor = AgriMintLight
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View Business Passport", color = AgriGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.width(4.dp))
                Text("→", color = AgriGreen, fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Text("Government Support & Grants", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AgriTextPrimary)
    Text(candidateText, fontSize = 12.sp, color = AgriTextSecondary, modifier = Modifier.padding(bottom = 12.dp))

    GovernmentProgramCard("Ilima/Letsema (Free Seeds)", "Get free seeds, fertilizer, and production inputs from DALRRD.", "🌱") { selectedProgram = "ilima" }
    GovernmentProgramCard("CASP (Ploughing & Mechanisation)", "Apply for free ploughing, discing, and farming equipment.", "🚜") { selectedProgram = "casp" }
    GovernmentProgramCard("Livestock Feed Support", "Free animal feed and starter packs for livestock and meat farmers.", "🐄") { selectedProgram = "livestock" }

    Spacer(Modifier.height(24.dp))

    Text("Top products", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(Modifier.height(10.dp))

    if (topProducts.isEmpty()) {
        Text("No sales yet.", fontSize = 13.sp, color = AgriTextSecondary)
    } else {
        topProducts.forEachIndexed { index, prod ->
            ProductRow("${index + 1}", prod.name, "${prod.qty} sold")
        }
    }

    if (selectedProgram != null) {
        val (title, body) = when (selectedProgram) {
            "ilima" -> "Ilima/Letsema: Free Seeds" to
                    "The Department of Agriculture provides free input packs (seeds, fertilizer, tools) to smallholder farmers, women, and youth.\n\n" +
                    "Requirements:\n" +
                    "• SA Citizen (18+)\n" +
                    "• Proof of land tenure\n" +
                    "• Access to water\n" +
                    "• Extension officer report\n\n" +
                    "How to apply: Contact your provincial Ilima/Letsema coordinator or visit your local Department of Agriculture office."

            "casp" -> "CASP: Mechanisation Support" to
                    "The Comprehensive Agricultural Support Programme provides free ploughing, discing, and planting services.\n\n" +
                    "Requirements:\n" +
                    "• Legal access to land (Title Deed/Lease)\n" +
                    "• Water rights\n" +
                    "• Signed business plan & financial records\n" +
                    "• Potential to create jobs\n\n" +
                    "How to apply: Submit your application at your local Department of Agriculture office or via the provincial CASP coordinator."

            "livestock" -> "Livestock Feed Support" to
                    "Livestock and meat farmers can receive free animal feed, medication, and starter packs to improve herd health.\n\n" +
                    "Requirements:\n" +
                    "• Proof of land rights or legal access\n" +
                    "• Livestock branding certificate\n" +
                    "• Business plan (for commercial support)\n\n" +
                    "How to apply: Contact your local extension officer or the provincial Livestock Development unit."

            else -> "" to ""
        }

        AlertDialog(
            onDismissRequest = { selectedProgram = null },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(body, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Official Portal:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgriGreen)
                    Text(
                        "www.gov.za",
                        fontSize = 14.sp,
                        color = Color.Blue,
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gov.za"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProgram = null }) {
                    Text("Got it", fontWeight = FontWeight.Bold, color = AgriGreen)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 2: CASH FLOW
// ─────────────────────────────────────────────────────────────
@Composable
private fun CashFlowTab(data: BusinessDashboardDto?) {
    val revenue = data?.revenueThisWeek ?: 0.0
    val fee = data?.platformFee ?: 0.0
    val netProfit = data?.netProfit ?: 0.0
    val chartData = data?.last7DaysChart ?: List(7) { 0 }
    val maxChart = chartData.maxOrNull()?.coerceAtLeast(1) ?: 1

    Text("Cash Flow", fontWeight = FontWeight.Bold, fontSize = 20.sp)
    Text("Where your money goes", color = AgriTextSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(24.dp))

    CashFlowRow("Money in", "R${String.format(Locale.US, "%.2f", revenue)}", AgriTextPrimary)
    CashFlowRow("Money out (15% platform fee)", "-R${String.format(Locale.US, "%.2f", fee)}", AgriRedAlert)
    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AgriBorderLight)
    CashFlowRow("Left over", "R${String.format(Locale.US, "%.2f", netProfit)}", AgriGreen, isBold = true)

    Spacer(Modifier.height(32.dp))
    Text("Orders, last 7 days", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        chartData.forEachIndexed { index, count ->
            BarChartItem(
                heightFraction = count.toFloat() / maxChart,
                label = if (count > 0) count.toString() else "0"
            )
        }
    }

    Spacer(Modifier.height(32.dp))

    Card(colors = CardDefaults.cardColors(containerColor = AgriOrange.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Heads up", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgriOrange)
            Spacer(Modifier.height(4.dp))
            Text("Fees take 15% of each sale. Price your products with the fee in mind.", fontSize = 12.sp, color = AgriTextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 3: GET PAID
// ─────────────────────────────────────────────────────────────
@Composable
private fun GetPaidTab() {
    var amountText by remember { mutableStateOf("50.00") }
    val context = LocalContext.current
    val message = "Hi! Please pay me R$amountText for my farm products via SmartAgriMarket."

    val qrText = "SMARTAGRIMARKET_PAYMENT|Amount:R$amountText|Merchant:${TokenStore.currentUser?.fullName ?: "Farmer"}"

    val qrBitmap = remember(amountText) {
        generateQrCode(qrText, 500)
    }

    Text("Get Paid", fontWeight = FontWeight.Bold, fontSize = 20.sp)
    Text("Take payment from a customer in front of you", color = AgriTextSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(24.dp))

    OutlinedTextField(
        value = amountText,
        onValueChange = { newValue -> if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amountText = newValue },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
        prefix = { Text("R ", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Spacer(Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAmountBtn("R20", amountText == "20.00" || amountText == "20") { amountText = "20.00" }
        QuickAmountBtn("R50", amountText == "50.00" || amountText == "50") { amountText = "50.00" }
        QuickAmountBtn("R100", amountText == "100.00" || amountText == "100") { amountText = "100.00" }
    }

    Spacer(Modifier.height(32.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Payment QR Code",
                modifier = Modifier.size(220.dp)
            )
        } else {
            Text("Could not generate QR code", color = Color.Red)
        }
    }

    Spacer(Modifier.height(16.dp))

    Text(
        "Scan to pay R$amountText\nSmartAgriMarket",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = AgriTextSecondary
    )

    Spacer(Modifier.height(32.dp))

    Text("Share on", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Payment Link", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Payment link copied!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AgriCardBg, contentColor = AgriTextPrimary)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Copy link")
        }

        Button(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                        setPackage("com.whatsapp")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    context.startActivity(Intent.createChooser(fallbackIntent, "Share via"))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White)
        ) {
            Text("WhatsApp")
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HELPER COMPOSABLES
// ─────────────────────────────────────────────────────────────
@Composable
private fun GovernmentProgramCard(title: String, description: String, emoji: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AgriMintLight.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(50)).background(Color.White), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AgriTextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 12.sp, color = AgriTextSecondary, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun ProductRow(rank: String, name: String, tag: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(rank, fontWeight = FontWeight.Bold, color = AgriTextSecondary, modifier = Modifier.width(24.dp))
        Text(name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (tag.isNotEmpty()) {
            Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(AgriMintLight).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(tag, fontSize = 10.sp, color = AgriGreenDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CashFlowRow(label: String, amount: String, color: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(amount, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun BarChartItem(heightFraction: Float, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Box(modifier = Modifier.width(28.dp).fillMaxHeight(fraction = if (heightFraction == 0f) 0.05f else heightFraction).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(if (heightFraction == 0f) AgriBorderLight else AgriGreen))
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, color = AgriTextSecondary)
    }
}

@Composable
private fun QuickAmountBtn(amount: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSelected) AgriGreen else AgriCardBg).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(amount, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else AgriTextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────
// QR CODE GENERATOR HELPER
// ─────────────────────────────────────────────────────────────
fun generateQrCode(text: String, size: Int = 500): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}