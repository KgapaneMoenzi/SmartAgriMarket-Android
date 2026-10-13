package com.smartagrimarket.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.CartState
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.network.AddressDto
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.DeliveryQuoteItem
import com.smartagrimarket.model.network.DeliveryQuoteRepository
import com.smartagrimarket.model.network.DeliveryQuoteResponse
import com.smartagrimarket.ui.theme.*
import com.smartagrimarket.util.LocationHelper
import com.smartagrimarket.util.LocationResult
import kotlinx.coroutines.launch

enum class DeliveryMethod(val label: String, val emoji: String, val backendValue: String) {
    HOME_DELIVERY("Home delivery", "🚚", "HOME_DELIVERY")
}

data class DeliveryCoordinates(
    val latitude: Double,
    val longitude: Double,
)

@Composable
fun CheckoutScreen(
    cart: CartState,
    onBack: () -> Unit,
    onPayWithPayfast: (
        cartProducts: List<Pair<Product, Int>>,
        total: Double,
        deliveryMethod: DeliveryMethod,
        deliveryAddress: AddressDto?,
        deliveryCoordinates: DeliveryCoordinates?,
    ) -> Unit = { _, _, _, _, _ -> },
    deliveryAddress: AddressDto? = null,
    isLoadingAddress: Boolean = false,
    onEditAddressClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cartProducts: List<Pair<Product, Int>> = cart.lines.values.map {
        it.product to it.quantity
    }
    val subtotal = cartProducts.sumOf { (product, qty) -> product.price * qty }

    val deliveryMethod = DeliveryMethod.HOME_DELIVERY

    var hasPermission by remember { mutableStateOf(LocationHelper.hasPermission(context)) }
    var isCapturing by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }

    // Seed coordinates from the saved address pin, if there is one
    var coordinates by remember {
        mutableStateOf(
            if (deliveryAddress?.latitude != null && deliveryAddress.longitude != null) {
                DeliveryCoordinates(deliveryAddress.latitude, deliveryAddress.longitude)
            } else null
        )
    }

    LaunchedEffect(deliveryAddress?.latitude, deliveryAddress?.longitude) {
        if (deliveryAddress?.latitude != null && deliveryAddress.longitude != null) {
            coordinates = DeliveryCoordinates(
                deliveryAddress.latitude,
                deliveryAddress.longitude
            )
        }
    }

    var quote by remember { mutableStateOf<DeliveryQuoteResponse?>(null) }
    var isQuoting by remember { mutableStateOf(false) }
    var quoteError by remember { mutableStateOf<String?>(null) }

    val deliveryFee: Double = quote?.fee ?: 50.0
    val total = subtotal + deliveryFee

    fun beginCapture() {
        isCapturing = true
        captureError = null
        scope.launch {
            when (val result = LocationHelper.getLocation(context, timeoutMs = 30_000L)) {
                is LocationResult.Success -> {
                    coordinates = DeliveryCoordinates(
                        result.location.lat,
                        result.location.lng
                    )
                }
                is LocationResult.Failure -> {
                    captureError = result.message
                }
            }
            isCapturing = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted =
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasPermission = granted
        if (granted) {
            beginCapture()
        } else {
            captureError = "Location permission denied. A delivery location is required to place your order."
        }
    }

    fun captureLocation() {
        captureError = null
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            return
        }
        beginCapture()
    }

    LaunchedEffect(coordinates, cartProducts.size) {
        val coords = coordinates
        if (coords == null || cartProducts.isEmpty()) {
            quote = null
            quoteError = null
            return@LaunchedEffect
        }
        isQuoting = true
        quoteError = null
        val items = cartProducts.map { (product, _) ->
            DeliveryQuoteItem(farmerEmail = product.farmerEmail, productId = product.id)
        }
        when (val r = DeliveryQuoteRepository.quote(items, coords.latitude, coords.longitude)) {
            is ApiResult.Success -> quote = r.data
            is ApiResult.Error -> quoteError = r.message
        }
        isQuoting = false
    }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                Text(
                    "Checkout",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                StepIndicator(currentStep = 1)
                Spacer(Modifier.height(20.dp))

                Text(
                    "Shipping Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(12.dp))

                if (isLoadingAddress) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = AgriCardBg
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = AgriGreen
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Loading your delivery address…",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                    }
                } else if (deliveryAddress == null) {
                    AddressCard(
                        icon = Icons.Filled.Home,
                        label = "Home",
                        line1 = "No address saved",
                        line2 = "Tap to add your delivery address",
                        onChangeClick = onEditAddressClick,
                        onClick = onEditAddressClick
                    )
                } else {
                    AddressCard(
                        icon = Icons.Filled.Home,
                        label = "Home",
                        line1 = deliveryAddress.line1.orEmpty(),
                        line2 = listOfNotNull(
                            deliveryAddress.city,
                            deliveryAddress.province,
                            deliveryAddress.postalCode
                        ).joinToString(", "),
                        onChangeClick = onEditAddressClick,
                        onClick = onEditAddressClick
                    )
                }

                Spacer(Modifier.height(12.dp))

                LocationCaptureCard(
                    coordinates = coordinates,
                    isCapturing = isCapturing,
                    errorMessage = captureError,
                    hasPinFromAddress = deliveryAddress?.latitude != null &&
                            deliveryAddress.longitude != null,
                    onCaptureClick = { captureLocation() },
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "Order Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AgriCardBg
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        cartProducts.forEach { (product, qty) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${product.name} × $qty",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                                Text(
                                    "R${"%.2f".format(product.price * qty)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextPrimary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = AgriBorderLight)
                        Spacer(Modifier.height(10.dp))

                        SummaryRow("Subtotal", subtotal)
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Delivery fee",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                            when {
                                isQuoting -> Text(
                                    "Calculating…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                                else -> Text(
                                    "R${"%.2f".format(deliveryFee)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextPrimary
                                )
                            }
                        }

                        quote?.distanceKm?.let { km ->
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (quote?.tier == "long")
                                    "$km km away · long-distance rate"
                                else
                                    "$km km away · standard rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriTextSecondary,
                            )
                        }
                        if (quote == null && coordinates == null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Pin your location to see the exact fee.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriOrange,
                            )
                        }
                        quoteError?.let { err ->
                            Spacer(Modifier.height(2.dp))
                            Text(
                                err,
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriRedAlert,
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = AgriBorderLight)
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Cost",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary
                            )
                            Text(
                                "R${"%.2f".format(total)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenDark
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                val hasAddress = deliveryAddress != null
                val hasItems = cartProducts.isNotEmpty()
                val hasCoordinates = coordinates != null

                // Pay is only enabled when we have items, an address,
                // AND a pinned location. This mirrors the server-side
                // LOCATION_REQUIRED guard in /api/payments/create.
                val canPay = hasItems && hasAddress && hasCoordinates &&
                        !isCapturing && !isQuoting

                // ── Missing address banner ──
                if (!hasAddress && !isLoadingAddress) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AgriRedAlert.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "⚠️  Add a delivery address before you can pay.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriRedAlert,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ── Missing location banner ──
                if (hasAddress && !hasCoordinates && !isCapturing) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { captureLocation() },
                        shape = RoundedCornerShape(12.dp),
                        color = AgriOrange.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📍",
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Pin your delivery location to continue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Tap here to use your current location.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AgriOrange.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        onPayWithPayfast(
                            cartProducts, total, deliveryMethod,
                            deliveryAddress, coordinates,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreen,
                        contentColor = Color.White,
                        disabledContainerColor = AgriMintLight,
                        disabledContentColor = AgriGreenDark,
                    ),
                    enabled = canPay
                ) {
                    when {
                        isCapturing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Getting location…", fontWeight = FontWeight.Bold)
                        }
                        isQuoting -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Calculating fee…", fontWeight = FontWeight.Bold)
                        }
                        !hasCoordinates && hasAddress -> {
                            Text(
                                "Pin your location to pay",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        else -> {
                            Text(
                                "Payment · R${"%.2f".format(total)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AgriGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "Shipping",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AgriTextPrimary
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(1.dp)
                .background(AgriBorderLight)
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AgriCardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = AgriTextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "Review and Payments",
            style = MaterialTheme.typography.bodySmall,
            color = AgriTextSecondary
        )
    }
}

@Composable
private fun AddressCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    line1: String,
    line2: String,
    onChangeClick: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AgriCardBg
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AgriMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = AgriGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                }
                Text(
                    "Change",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriGreen,
                    modifier = Modifier.clickable(onClick = onChangeClick)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = AgriTextTertiary,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        line1,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextPrimary
                    )
                    if (line2.isNotBlank()) {
                        Text(
                            line2,
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCaptureCard(
    coordinates: DeliveryCoordinates?,
    isCapturing: Boolean,
    errorMessage: String?,
    hasPinFromAddress: Boolean,
    onCaptureClick: () -> Unit,
) {
    val isMissing = coordinates == null && !isCapturing

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isMissing) AgriOrange.copy(alpha = 0.08f) else AgriCardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isMissing) AgriOrange.copy(alpha = 0.15f) else AgriMintLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = if (isMissing) AgriOrange else AgriGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Delivery location",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        if (isMissing) "Required to place your order" else "Used for the delivery fee",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isMissing) AgriOrange else AgriTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when {
                coordinates != null -> {
                    Text(
                        "✓ Location pinned",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AgriGreen
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "%.5f, %.5f".format(coordinates.latitude, coordinates.longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextSecondary
                    )
                    if (hasPinFromAddress) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "From your saved delivery address",
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriTextTertiary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Update location",
                        color = AgriGreen,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable(enabled = !isCapturing, onClick = onCaptureClick)
                    )
                }
                isCapturing -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AgriGreen
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Locating… (up to 30 seconds)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    }
                }
                else -> {
                    Text(
                        "Tap below to share your current location. This is required so the driver knows where to deliver.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onCaptureClick),
                        shape = RoundedCornerShape(12.dp),
                        color = AgriOrange
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Pin my location",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
        Text("R${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall, color = AgriTextPrimary)
    }
}