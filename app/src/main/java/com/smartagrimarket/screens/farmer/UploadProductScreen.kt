package com.smartagrimarket.screens.farmer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.model.ProductCategory
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.model.network.UploadRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UploadProductScreen(
    onBack: () -> Unit,
    isUploading: Boolean = false,
    errorMessage: String? = null,
    onUpload: (
        name: String, price: Double, stock: Int, unit: String, packSize: String?,
        imageUrl: String?, bulkMin1: Int?, bulkPrice1: Double?, bulkMin2: Int?, bulkPrice2: Double?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> }
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("item") }
    var packSize by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.VEGETABLES) }
    var showValidationError by remember { mutableStateOf(false) }

    var bulkMin1 by remember { mutableStateOf("") }
    var bulkPrice1 by remember { mutableStateOf("") }
    var bulkMin2 by remember { mutableStateOf("") }
    var bulkPrice2 by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    var isButcherVerified by remember { mutableStateOf(false) }
    var isUploadingCert by remember { mutableStateOf(false) }
    var certMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val result = FarmerRepository.getButcherStatus()) {
            is ApiResult.Success -> isButcherVerified = result.data
            is ApiResult.Error -> {}
        }
    }

    val pickCertLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingCert = true
            certMessage = null
            scope.launch {
                when (val result = FarmerRepository.uploadButcherCertificate(context, uri)) {
                    is ApiResult.Success -> {
                        certMessage = "Certificate uploaded! Pending admin approval."
                        isUploadingCert = false
                    }
                    is ApiResult.Error -> {
                        certMessage = result.message
                        isUploadingCert = false
                    }
                }
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            isUploadingImage = true
            imageError = null
            scope.launch {
                val bearer = TokenStore.token?.let { "Bearer $it" }
                if (bearer.isNullOrBlank()) {
                    imageError = "You are not signed in."
                    isUploadingImage = false
                    return@launch
                }
                when (val result = UploadRepository.uploadProductImage(context, uri, bearer)) {
                    is ApiResult.Success -> {
                        imageUrl = result.data
                        isUploadingImage = false
                    }
                    is ApiResult.Error -> {
                        imageError = result.message
                        isUploadingImage = false
                    }
                }
            }
        }
    }

    val priceValue = price.toDoubleOrNull()
    val stockValue = stock.toIntOrNull()

    val isValid = name.isNotBlank() &&
            priceValue != null && priceValue > 0 &&
            stockValue != null && stockValue > 0 &&
            (category != ProductCategory.MEAT || isButcherVerified)

    val validationMessage: String? = when {
        !showValidationError || isValid -> null
        name.isBlank() -> "Add a product name."
        priceValue == null || priceValue <= 0 -> "Enter a price greater than 0."
        stockValue == null || stockValue <= 0 -> "Enter a stock quantity of at least 1."
        category == ProductCategory.MEAT && !isButcherVerified -> "You need a verified Meat Safety Certificate to sell meat."
        else -> "Please fill in all fields."
    }

    Scaffold(containerColor = AgriCreamBg) { padding ->
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", fontSize = 20.sp, color = AgriTextPrimary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Upload product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "It'll appear in the marketplace right away",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
            Spacer(Modifier.height(10.dp))

            PhotoPicker(
                imageUri = imageUri,
                imageUrl = imageUrl,
                isUploadingImage = isUploadingImage,
                onPick = { pickImageLauncher.launch("image/*") }
            )

            imageError?.let {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AgriRedAlert.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Product details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
            Spacer(Modifier.height(12.dp))

            LabeledField(label = "Product name", value = name, onValueChange = { name = it }, placeholder = "e.g. Organic kale")

            Spacer(Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.labelSmall, color = AgriTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ProductCategory.values().filter { it != ProductCategory.ALL }) { c ->
                    val selected = category == c
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) AgriYellow else AgriCardBg)
                            .clickable { category = c }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(c.label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = AgriTextPrimary)
                    }
                }
            }

            if (category == ProductCategory.MEAT && !isButcherVerified) {
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AgriRedAlert.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Meat Safety Certificate Required", fontWeight = FontWeight.Bold, color = AgriRedAlert, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("To sell meat on SmartAgriMarket, you must upload a clear picture of your Meat Safety Certificate. An admin will verify it.", fontSize = 12.sp, color = AgriTextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { pickCertLauncher.launch("image/*") },
                            enabled = !isUploadingCert,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriRedAlert),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isUploadingCert) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Upload Certificate Photo", fontWeight = FontWeight.Bold)
                            }
                        }
                        certMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = AgriGreenDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (category == ProductCategory.MEAT && isButcherVerified) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✅ Verified Butcher", color = AgriGreenDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            LabeledField(
                label = "Price per unit (R)",
                value = price,
                onValueChange = { input -> if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) price = input },
                placeholder = "e.g. 28.00",
                keyboardType = KeyboardType.Decimal
            )

            Spacer(Modifier.height(16.dp))

            LabeledField(
                label = "How many units/packs are you listing?",
                value = stock,
                onValueChange = { input -> stock = input.filter { it.isDigit() } },
                placeholder = "e.g. 20",
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(16.dp))
            Text("Unit of Measurement", style = MaterialTheme.typography.labelSmall, color = AgriTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("item", "kg", "g", "L", "ml").forEach { u ->
                    val selected = unit == u
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) AgriGreen else AgriCardBg)
                            .clickable { unit = u }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(u, color = if (selected) Color.White else AgriTextPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // NEW PACK SIZE FIELD
            Spacer(Modifier.height(16.dp))
            LabeledField(
                label = "Pack Size / Description (e.g. 5kg bag, 1L bottle)",
                value = packSize,
                onValueChange = { packSize = it },
                placeholder = "e.g. 5kg bag"
            )

            Spacer(Modifier.height(24.dp))
            Text("Bulk Pricing (Optional)", fontWeight = FontWeight.Bold, color = AgriTextPrimary)
            Text("Offer discounts for Spaza shops and caterers.", fontSize = 12.sp, color = AgriTextSecondary)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = bulkMin1, onValueChange = { bulkMin1 = it.filter { c -> c.isDigit() } }, label = { Text("Min Qty (e.g., 50)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = bulkPrice1, onValueChange = { bulkPrice1 = it }, label = { Text("Price (R)") }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = bulkMin2, onValueChange = { bulkMin2 = it.filter { c -> c.isDigit() } }, label = { Text("Min Qty (e.g., 100)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = bulkPrice2, onValueChange = { bulkPrice2 = it }, label = { Text("Price (R)") }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Text("Buyers can't order once this reaches 0.", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary, modifier = Modifier.padding(start = 4.dp))

            if (stockValue != null && stockValue in 1..2) {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AgriRedAlert.copy(alpha = 0.10f)).padding(14.dp), verticalAlignment = Alignment.Top) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("That's a low quantity — your listing will show a low-stock warning. Buyers may miss it.", color = AgriRedAlert, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            validationMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AgriRedAlert.copy(alpha = 0.12f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(msg, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AgriRedAlert.copy(alpha = 0.12f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(msg, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (isValid) {
                        onUpload(
                            name.trim(), priceValue!!, stockValue!!, unit, packSize.ifBlank { null }, imageUrl,
                            bulkMin1.toIntOrNull(), bulkPrice1.toDoubleOrNull(), bulkMin2.toIntOrNull(), bulkPrice2.toDoubleOrNull()
                        )
                    } else {
                        showValidationError = true
                    }
                },
                enabled = !isUploading && !isUploadingImage,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriYellow, contentColor = AgriTextPrimary)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AgriTextPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Uploading…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Text("Upload product", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhotoPicker(imageUri: Uri?, imageUrl: String?, isUploadingImage: Boolean, onPick: () -> Unit) {
    val preview: Any? = imageUrl ?: imageUri
    if (preview == null) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(AgriCardBg).clickable(enabled = !isUploadingImage, onClick = onPick).padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(AgriMintLight), contentAlignment = Alignment.Center) { Text("📷", fontSize = 28.sp) }
            Spacer(Modifier.height(14.dp))
            Text("Add a photo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Listings with photos sell faster", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)).background(AgriCardBg)) {
                AsyncImage(model = preview, contentDescription = "Product photo preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                if (isUploadingImage) {
                    Box(modifier = Modifier.fillMaxSize().background(AgriTextPrimary.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AgriCardBg, strokeWidth = 3.dp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onPick,
                enabled = !isUploadingImage,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriTextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriTextPrimary.copy(alpha = 0.15f))
            ) {
                Text("Change photo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = AgriTextSecondary, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AgriTextTertiary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AgriCardBg,
            focusedContainerColor = AgriCardBg,
            unfocusedBorderColor = AgriTextPrimary.copy(alpha = 0.15f),
            focusedBorderColor = AgriGreen,
            cursorColor = AgriGreen
        )
    )
}