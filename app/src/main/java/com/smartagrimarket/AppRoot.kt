package com.smartagrimarket

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.smartagrimarket.model.CartState
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.ProductCategory
import com.smartagrimarket.model.UserRole
import com.smartagrimarket.model.network.AddressDto
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.AuthRepository
import com.smartagrimarket.model.network.DriverRepository
import com.smartagrimarket.model.network.NotificationSettingsDto
import com.smartagrimarket.model.network.NotificationsRepository
import com.smartagrimarket.model.network.OrderDto
import com.smartagrimarket.model.network.PaymentItemPayload
import com.smartagrimarket.model.network.PaymentsRepository
import com.smartagrimarket.model.network.ProductsRepository
import com.smartagrimarket.model.network.RecommendedProductDto
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.model.network.VerificationRepository
import com.smartagrimarket.screens.AboutSupportScreen
import com.smartagrimarket.screens.AccountSettingsScreen
import com.smartagrimarket.screens.BackInStockScreen
import com.smartagrimarket.screens.BuyerTab
import com.smartagrimarket.screens.CartScreen
import com.smartagrimarket.screens.CheckoutScreen
import com.smartagrimarket.screens.DeliverySlotScreen
import com.smartagrimarket.screens.DownloadMyDataScreen
import com.smartagrimarket.screens.HelpCenterScreen
import com.smartagrimarket.screens.HomeScreen
import com.smartagrimarket.screens.NotificationSettingsScreen
import com.smartagrimarket.screens.NotificationsScreen
import com.smartagrimarket.screens.OrderConfirmationScreen
import com.smartagrimarket.screens.OrderTrackingScreen
import com.smartagrimarket.screens.PayFastWebViewScreen
import com.smartagrimarket.screens.PrivacyPolicyScreen
import com.smartagrimarket.screens.ProductDetailScreen
import com.smartagrimarket.screens.ProductQAScreen
import com.smartagrimarket.screens.RequestProductScreen
import com.smartagrimarket.screens.ReviewsScreen
import com.smartagrimarket.screens.SeasonalCalendarScreen
import com.smartagrimarket.screens.SmartAgriMarketBottomNav
import com.smartagrimarket.screens.account.ChangePasswordScreen
import com.smartagrimarket.screens.account.ComingSoonScreen
import com.smartagrimarket.screens.account.DeliveryAddressScreen
import com.smartagrimarket.screens.account.EditProfileScreen
import com.smartagrimarket.screens.account.OrderHistoryScreen
import com.smartagrimarket.screens.auth.ConfirmEmailScreen
import com.smartagrimarket.screens.auth.ForgotPasswordScreen
import com.smartagrimarket.screens.auth.SignInScreen
import com.smartagrimarket.screens.auth.SignUpScreen
import com.smartagrimarket.screens.auth.AdminRolePickerDialog
import com.smartagrimarket.screens.driver.DriverBottomNav
import com.smartagrimarket.screens.driver.DriverHomeScreen
import com.smartagrimarket.screens.driver.DriverJobsScreen
import com.smartagrimarket.screens.driver.DriverTab
import com.smartagrimarket.screens.driver.DriverVerificationScreen
import com.smartagrimarket.screens.driver.DriverWalletScreen
import com.smartagrimarket.screens.farmer.FarmerBottomNav
import com.smartagrimarket.screens.farmer.FarmerDashboardScreen
import com.smartagrimarket.screens.farmer.FarmerMyProductsScreen
import com.smartagrimarket.screens.farmer.FarmerOrderDetailScreen
import com.smartagrimarket.screens.farmer.FarmerOrdersScreen
import com.smartagrimarket.screens.farmer.FarmerQuestionsScreen
import com.smartagrimarket.screens.farmer.FarmerRequestsScreen
import com.smartagrimarket.screens.farmer.FarmerStatsScreen
import com.smartagrimarket.screens.farmer.FarmerTab
import com.smartagrimarket.screens.farmer.FarmerVerificationScreen
import com.smartagrimarket.screens.farmer.FarmerWalletScreen
import com.smartagrimarket.screens.farmer.FarmerWeatherScreen
import com.smartagrimarket.screens.farmer.UploadProductScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BASE_URL = "https://smartagrimarket-backend.onrender.com"

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}

private fun recommendedToProduct(rec: RecommendedProductDto): Product {
    return Product(
        id = rec.id,
        name = rec.name,
        price = rec.price,
        imageUrl = rec.imageUrl,
        stock = rec.stock,
        farmName = rec.farmName ?: "",
        farmerEmail = rec.farmerEmail ?: "",
        category = ProductCategory.ALL,
        lowStock = false,
        fromFarmerBadge = false
    )
}

private sealed class AppScreen {
    data object SignIn : AppScreen()
    data object SignUp : AppScreen()
    data object ForgotPassword : AppScreen()
    data class ConfirmEmail(val email: String, val role: UserRole) : AppScreen()
    data class Home(val role: UserRole, val isGuest: Boolean = false) : AppScreen()
}

private sealed class BuyerScreen {
    data object Tabs : BuyerScreen()
    data object Notifications : BuyerScreen()
    data object Checkout : BuyerScreen()
    data object OrderConfirmation : BuyerScreen()
    data class PayfastWebView(val paymentUrl: String, val fields: Map<String, String>) : BuyerScreen()
    data class ProductDetail(val product: Product) : BuyerScreen()
    data class Reviews(val product: Product) : BuyerScreen()
    data class QA(val product: Product) : BuyerScreen()
    data class BackInStock(val product: Product) : BuyerScreen()
    data object SeasonalCalendar : BuyerScreen()
    data class DeliverySlotPick(val order: OrderDto) : BuyerScreen()
    data class Tracking(val order: OrderDto) : BuyerScreen()
}

private sealed class FarmerScreen {
    data object Tabs : FarmerScreen()
    data class OrderDetail(val order: com.smartagrimarket.model.FarmerOrder) : FarmerScreen()
    data object UploadProduct : FarmerScreen()
    data object MyProducts : FarmerScreen()
}

// Driver no longer has a JobDetail sub-screen — batch queue is inline.
private sealed class DriverScreen {
    data object Tabs : DriverScreen()
    data object Verification : DriverScreen()
    data object Wallet : DriverScreen()
    data object AccountEditProfile : DriverScreen()
    data object AccountChangePassword : DriverScreen()
    data object AccountDownloadData : DriverScreen()
}

private sealed class AccountSubScreen {
    data object Main : AccountSubScreen()
    data object PrivacyPolicy : AccountSubScreen()
    data object DownloadData : AccountSubScreen()
    data object EditProfile : AccountSubScreen()
    data object ChangePassword : AccountSubScreen()
    data object AddressOrFarm : AccountSubScreen()
    data object NotificationSettings : AccountSubScreen()
    data object OrderHistory : AccountSubScreen()
    data object AboutSupport : AccountSubScreen()
    data object HelpCenter : AccountSubScreen()
    data object RequestProduct : AccountSubScreen()
    data object Wallet : AccountSubScreen()
}

@Composable
fun AppRoot() {
    val initialScreen: AppScreen = if (TokenStore.hasSession) {
        val role = when (TokenStore.currentUser?.role?.uppercase()) {
            "FARMER" -> UserRole.FARMER
            "DRIVER" -> UserRole.DRIVER
            else -> UserRole.BUYER
        }
        AppScreen.Home(role, isGuest = false)
    } else {
        AppScreen.Home(UserRole.BUYER, isGuest = true)
    }

    var screen by remember { mutableStateOf<AppScreen>(initialScreen) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteAccountError by remember { mutableStateOf<String?>(null) }
    var showAdminRolePicker by remember { mutableStateOf(false) }
    var adminName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        when (val current = screen) {
            is AppScreen.SignIn -> {
                SignInScreen(
                    onSignIn = { identifier, password ->
                        errorMessage = null
                        scope.launch {
                            isLoading = true
                            when (val result = AuthRepository.signIn(identifier, password)) {
                                is ApiResult.Success -> {
                                    TokenStore.token = result.data.token
                                    TokenStore.currentUser = result.data.user
                                    when (result.data.user.role.uppercase()) {
                                        "ADMIN" -> {
                                            adminName = result.data.user.fullName
                                            showAdminRolePicker = true
                                        }
                                        "FARMER" -> screen = AppScreen.Home(UserRole.FARMER, isGuest = false)
                                        "DRIVER" -> screen = AppScreen.Home(UserRole.DRIVER, isGuest = false)
                                        else -> screen = AppScreen.Home(UserRole.BUYER, isGuest = false)
                                    }
                                }
                                is ApiResult.Error -> {
                                    if (result.needsVerification) {
                                        screen = AppScreen.ConfirmEmail(identifier, UserRole.BUYER)
                                    } else {
                                        errorMessage = result.message
                                    }
                                }
                            }
                            isLoading = false
                        }
                    },
                    onForgotPassword = { screen = AppScreen.ForgotPassword },
                    onNavigateToSignUp = { screen = AppScreen.SignUp }
                )
            }

            is AppScreen.ForgotPassword -> {
                BackHandler { screen = AppScreen.SignIn }
                ForgotPasswordScreen(
                    onBack = { screen = AppScreen.SignIn },
                    onSendResetCode = { email ->
                        errorMessage = null
                        scope.launch {
                            isLoading = true
                            when (val result = AuthRepository.forgotPassword(email)) {
                                is ApiResult.Success<Unit> -> { }
                                is ApiResult.Error -> errorMessage = result.message
                            }
                            isLoading = false
                        }
                    },
                    onResetPassword = { email, code, newPassword ->
                        errorMessage = null
                        scope.launch {
                            isLoading = true
                            when (val result = AuthRepository.resetPassword(email, code, newPassword)) {
                                is ApiResult.Success<Unit> -> screen = AppScreen.SignIn
                                is ApiResult.Error -> errorMessage = result.message
                            }
                            isLoading = false
                        }
                    }
                )
            }

            is AppScreen.SignUp -> {
                BackHandler { screen = AppScreen.Home(UserRole.BUYER, isGuest = true) }
                SignUpScreen(
                    onCancel = { screen = AppScreen.Home(UserRole.BUYER, isGuest = true) },
                    onCreateAccount = { formState ->
                        errorMessage = null
                        scope.launch {
                            isLoading = true
                            when (val result = AuthRepository.signUp(
                                fullName = formState.fullName,
                                email = formState.email,
                                phone = formState.phone,
                                password = formState.password,
                                role = formState.role.name
                            )) {
                                is ApiResult.Success -> {
                                    screen = AppScreen.ConfirmEmail(formState.email, formState.role)
                                }
                                is ApiResult.Error -> {
                                    errorMessage = result.message
                                }
                            }
                            isLoading = false
                        }
                    },
                    onNavigateToSignIn = { screen = AppScreen.SignIn }
                )
            }

            is AppScreen.ConfirmEmail -> {
                BackHandler { screen = AppScreen.SignUp }
                ConfirmEmailScreen(
                    email = current.email,
                    onBack = { screen = AppScreen.SignUp },
                    onVerify = { code ->
                        errorMessage = null
                        scope.launch {
                            isLoading = true
                            when (val result = AuthRepository.verifyEmail(current.email, code)) {
                                is ApiResult.Success -> {
                                    TokenStore.token = result.data.token
                                    TokenStore.currentUser = result.data.user
                                    screen = AppScreen.Home(current.role, isGuest = false)
                                }
                                is ApiResult.Error -> {
                                    errorMessage = result.message
                                }
                            }
                            isLoading = false
                        }
                    },
                    onResendCode = {
                        scope.launch { AuthRepository.resendCode(current.email) }
                    },
                    onChangeEmail = { screen = AppScreen.SignUp }
                )
            }

            is AppScreen.Home -> {
                val isGuest = current.isGuest
                when (current.role) {
                    UserRole.BUYER -> {
                        var selectedTab by remember { mutableStateOf(BuyerTab.HOME) }
                        var buyerScreen by remember { mutableStateOf<BuyerScreen>(BuyerScreen.Tabs) }
                        var accountSubScreen by remember { mutableStateOf<AccountSubScreen>(AccountSubScreen.Main) }
                        var deliveryAddress by remember { mutableStateOf<AddressDto?>(null) }
                        var isLoadingAddress by remember { mutableStateOf(false) }
                        var returnToCheckoutAfterAddress by remember { mutableStateOf(false) }
                        var stockLimitMessage by remember { mutableStateOf<String?>(null) }
                        var showAddressPicker by remember { mutableStateOf(false) }
                        var pickedAddressCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

                        val cart = remember {
                            CartState(
                                context = context,
                                ownerKey = TokenStore.currentUser?.email ?: "guest"
                            )
                        }
                        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
                        var unreadNotificationCount by remember { mutableStateOf(0) }

                        val buyerCanGoBack = buyerScreen !is BuyerScreen.Tabs ||
                                (selectedTab == BuyerTab.ACCOUNT && accountSubScreen != AccountSubScreen.Main) ||
                                selectedTab != BuyerTab.HOME

                        BackHandler(enabled = buyerCanGoBack) {
                            when {
                                showAddressPicker -> showAddressPicker = false
                                buyerScreen !is BuyerScreen.Tabs -> {
                                    buyerScreen = when (val bs = buyerScreen) {
                                        is BuyerScreen.ProductDetail -> BuyerScreen.Tabs
                                        is BuyerScreen.Reviews -> BuyerScreen.ProductDetail(bs.product)
                                        is BuyerScreen.QA -> BuyerScreen.ProductDetail(bs.product)
                                        is BuyerScreen.BackInStock -> BuyerScreen.ProductDetail(bs.product)
                                        is BuyerScreen.Notifications -> BuyerScreen.Tabs
                                        BuyerScreen.Checkout -> BuyerScreen.Tabs
                                        is BuyerScreen.PayfastWebView -> BuyerScreen.Checkout
                                        BuyerScreen.OrderConfirmation -> BuyerScreen.Tabs
                                        BuyerScreen.SeasonalCalendar -> BuyerScreen.Tabs
                                        is BuyerScreen.DeliverySlotPick -> BuyerScreen.Tabs
                                        is BuyerScreen.Tracking -> BuyerScreen.Tabs
                                        BuyerScreen.Tabs -> BuyerScreen.Tabs
                                    }
                                }
                                selectedTab == BuyerTab.ACCOUNT && accountSubScreen != AccountSubScreen.Main -> {
                                    accountSubScreen = AccountSubScreen.Main
                                }
                                selectedTab != BuyerTab.HOME -> {
                                    selectedTab = BuyerTab.HOME
                                }
                            }
                        }

                        fun tryAddToCart(product: Product) {
                            if (isGuest) {
                                screen = AppScreen.SignIn
                                return
                            }
                            val inCart = cart.quantityOf(product.id)
                            val stock = product.stock
                            if (stock != null && inCart >= stock) {
                                stockLimitMessage = if (stock <= 0) {
                                    "${product.name} is out of stock."
                                } else {
                                    "You already have all $stock " +
                                            "${product.name} in your cart.\n\n" +
                                            "Only $stock available."
                                }
                            } else {
                                cart.add(product)
                            }
                        }

                        LaunchedEffect(selectedTab) {
                            if (selectedTab == BuyerTab.HOME || selectedTab == BuyerTab.CART) {
                                when (val result = ProductsRepository.fetchProducts()) {
                                    is ApiResult.Success -> products = result.data
                                    is ApiResult.Error -> { }
                                }
                            }
                        }

                        LaunchedEffect(selectedTab, buyerScreen) {
                            if (selectedTab == BuyerTab.HOME
                                && buyerScreen is BuyerScreen.Tabs
                                && !isGuest
                            ) {
                                when (val result = NotificationsRepository.fetchUnreadCount()) {
                                    is ApiResult.Success -> unreadNotificationCount = result.data
                                    is ApiResult.Error -> { }
                                }
                            }
                        }

                        LaunchedEffect(buyerScreen) {
                            if (buyerScreen is BuyerScreen.Checkout
                                && deliveryAddress == null
                                && !isGuest
                            ) {
                                isLoadingAddress = true
                                val token = TokenStore.token
                                if (token != null) {
                                    when (val r = AuthRepository.getAddress("Bearer $token")) {
                                        is ApiResult.Success -> deliveryAddress = r.data
                                        is ApiResult.Error -> { }
                                    }
                                }
                                isLoadingAddress = false
                            }
                        }

                        when (val bs = buyerScreen) {
                            BuyerScreen.Notifications -> {
                                NotificationsScreen(
                                    onBack = { buyerScreen = BuyerScreen.Tabs },
                                    onNotificationClick = { notif ->
                                        scope.launch {
                                            when (val r = NotificationsRepository.fetchUnreadCount()) {
                                                is ApiResult.Success -> unreadNotificationCount = r.data
                                                is ApiResult.Error -> { }
                                            }
                                        }
                                        when (notif.linkType) {
                                            "product" -> {
                                                val productId = notif.linkId
                                                val product = products.find { it.id == productId }
                                                if (product != null) {
                                                    buyerScreen = BuyerScreen.ProductDetail(product)
                                                }
                                            }
                                            "order" -> {
                                                val orderId = notif.linkId
                                                if (orderId != null) {
                                                    scope.launch {
                                                        val token = TokenStore.token
                                                        if (token != null) {
                                                            when (val r = AuthRepository.getOrderDetail("Bearer $token", orderId)) {
                                                                is ApiResult.Success -> {
                                                                    buyerScreen = BuyerScreen.Tracking(r.data)
                                                                }
                                                                is ApiResult.Error -> { }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            BuyerScreen.Checkout -> {
                                CheckoutScreen(
                                    cart = cart,
                                    onBack = { buyerScreen = BuyerScreen.Tabs },
                                    deliveryAddress = deliveryAddress,
                                    isLoadingAddress = isLoadingAddress,
                                    onEditAddressClick = {
                                        returnToCheckoutAfterAddress = true
                                        accountSubScreen = AccountSubScreen.AddressOrFarm
                                        buyerScreen = BuyerScreen.Tabs
                                        selectedTab = BuyerTab.ACCOUNT
                                    },
                                    onPayWithPayfast = { cartProducts, total, deliveryMethod, address, coordinates ->
                                        errorMessage = null
                                        scope.launch {
                                            isLoading = true
                                            val user = TokenStore.currentUser
                                            if (user == null) {
                                                errorMessage = "Please sign in to complete your order."
                                                isLoading = false
                                                return@launch
                                            }
                                            val items = cartProducts.map { (product, qty) ->
                                                PaymentItemPayload(
                                                    name = product.name,
                                                    quantity = qty,
                                                    price = product.price,
                                                    productId = product.id,
                                                    farmerEmail = product.farmerEmail
                                                )
                                            }
                                            when (val result = PaymentsRepository.createPayment(
                                                amount = total,
                                                itemName = "AgriMarket order (${cartProducts.size} items)",
                                                buyerEmail = user.email,
                                                buyerName = user.fullName,
                                                items = items,
                                                deliveryMethod = deliveryMethod.backendValue,
                                                address = address,
                                                deliveryLat = coordinates?.latitude,
                                                deliveryLng = coordinates?.longitude
                                            )) {
                                                is ApiResult.Success -> {
                                                    buyerScreen = BuyerScreen.PayfastWebView(
                                                        result.data.paymentUrl,
                                                        result.data.fields
                                                    )
                                                }
                                                is ApiResult.Error -> errorMessage = result.message
                                            }
                                            isLoading = false
                                        }
                                    }
                                )
                            }

                            is BuyerScreen.PayfastWebView -> {
                                PayFastWebViewScreen(
                                    paymentUrl = bs.paymentUrl,
                                    fields = bs.fields,
                                    onSuccess = {
                                        cart.clear()
                                        deliveryAddress = null
                                        buyerScreen = BuyerScreen.OrderConfirmation
                                    },
                                    onCancel = { buyerScreen = BuyerScreen.Checkout }
                                )
                            }

                            BuyerScreen.OrderConfirmation -> {
                                OrderConfirmationScreen(
                                    onContinueShopping = {
                                        selectedTab = BuyerTab.HOME
                                        buyerScreen = BuyerScreen.Tabs
                                    }
                                )
                            }

                            is BuyerScreen.ProductDetail -> {
                                ProductDetailScreen(
                                    productId = bs.product.id,
                                    productName = bs.product.name,
                                    price = bs.product.price,
                                    farmName = bs.product.farmName,
                                    averageRating = bs.product.averageRating,
                                    reviewCount = bs.product.reviewCount,
                                    inStock = bs.product.stock == null || bs.product.stock > 0,
                                    imageUrl = bs.product.imageUrl,
                                    stock = bs.product.stock,
                                    currentCartQuantity = cart.quantityOf(bs.product.id),
                                    cartItemCount = cart.totalItems,
                                    onCartClick = {
                                        buyerScreen = BuyerScreen.Tabs
                                        selectedTab = BuyerTab.CART
                                    },
                                    onBack = { buyerScreen = BuyerScreen.Tabs },
                                    onAddToCart = { tryAddToCart(bs.product) },
                                    onReviewsClick = { buyerScreen = BuyerScreen.Reviews(bs.product) },
                                    onQAClick = { buyerScreen = BuyerScreen.QA(bs.product) },
                                    onBackInStockClick = { buyerScreen = BuyerScreen.BackInStock(bs.product) },
                                    onSimilarProductClick = { rec ->
                                        buyerScreen = BuyerScreen.ProductDetail(recommendedToProduct(rec))
                                    }
                                )
                            }

                            is BuyerScreen.Reviews -> {
                                ReviewsScreen(
                                    productId = bs.product.id,
                                    productName = bs.product.name,
                                    onBack = { buyerScreen = BuyerScreen.ProductDetail(bs.product) }
                                )
                            }

                            is BuyerScreen.QA -> {
                                ProductQAScreen(
                                    productId = bs.product.id,
                                    productName = bs.product.name,
                                    onBack = { buyerScreen = BuyerScreen.ProductDetail(bs.product) }
                                )
                            }

                            is BuyerScreen.BackInStock -> {
                                BackInStockScreen(
                                    productId = bs.product.id,
                                    productName = bs.product.name,
                                    farmName = bs.product.farmName,
                                    expectedBackLabel = "Expected back soon",
                                    imageUrl = bs.product.imageUrl,
                                    onBack = { buyerScreen = BuyerScreen.ProductDetail(bs.product) },
                                    bottomBar = {}
                                )
                            }

                            BuyerScreen.SeasonalCalendar -> {
                                SeasonalCalendarScreen(
                                    bottomBar = {
                                        SmartAgriMarketBottomNav(
                                            selectedTab = selectedTab,
                                            onTabSelected = { newTab ->
                                                if (isGuest && newTab == BuyerTab.ACCOUNT) {
                                                    screen = AppScreen.SignIn
                                                } else {
                                                    selectedTab = newTab
                                                    buyerScreen = BuyerScreen.Tabs
                                                }
                                            },
                                            cartItemCount = cart.totalItems,
                                            isGuest = isGuest
                                        )
                                    }
                                )
                            }

                            is BuyerScreen.DeliverySlotPick -> {
                                DeliverySlotScreen(
                                    orderId = bs.order.id,
                                    onBack = { buyerScreen = BuyerScreen.Tabs },
                                    onConfirmed = { buyerScreen = BuyerScreen.Tabs }
                                )
                            }

                            is BuyerScreen.Tracking -> {
                                OrderTrackingScreen(
                                    order = bs.order,
                                    onBack = { buyerScreen = BuyerScreen.Tabs },
                                    onNeedHelp = {
                                        accountSubScreen = AccountSubScreen.HelpCenter
                                        buyerScreen = BuyerScreen.Tabs
                                        selectedTab = BuyerTab.ACCOUNT
                                    },
                                    bottomBar = {
                                        SmartAgriMarketBottomNav(
                                            selectedTab = selectedTab,
                                            onTabSelected = { newTab ->
                                                selectedTab = newTab
                                                buyerScreen = BuyerScreen.Tabs
                                            },
                                            cartItemCount = cart.totalItems
                                        )
                                    }
                                )
                            }

                            BuyerScreen.Tabs -> {
                                val bottomBar: @Composable () -> Unit = {
                                    SmartAgriMarketBottomNav(
                                        selectedTab = selectedTab,
                                        onTabSelected = { newTab ->
                                            if (isGuest && newTab == BuyerTab.ACCOUNT) {
                                                screen = AppScreen.SignIn
                                            } else if (newTab.name == "WEATHER") {
                                                buyerScreen = BuyerScreen.SeasonalCalendar
                                            } else {
                                                selectedTab = newTab
                                            }
                                        },
                                        cartItemCount = cart.totalItems,
                                        isGuest = isGuest
                                    )
                                }

                                when (selectedTab) {
                                    BuyerTab.ACCOUNT -> {
                                        when (accountSubScreen) {
                                            AccountSubScreen.RequestProduct -> {
                                                RequestProductScreen(
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    bottomBar = bottomBar
                                                )
                                            }
                                            AccountSubScreen.PrivacyPolicy -> {
                                                PrivacyPolicyScreen(onBack = { accountSubScreen = AccountSubScreen.Main })
                                            }
                                            AccountSubScreen.DownloadData -> {
                                                DownloadMyDataScreen(onBack = { accountSubScreen = AccountSubScreen.Main })
                                            }
                                            AccountSubScreen.EditProfile -> {
                                                var isSaving by remember { mutableStateOf(false) }
                                                var editError by remember { mutableStateOf<String?>(null) }
                                                EditProfileScreen(
                                                    user = TokenStore.currentUser,
                                                    isSaving = isSaving,
                                                    errorMessage = editError,
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    onSave = { fullName, phone ->
                                                        editError = null
                                                        scope.launch {
                                                            isSaving = true
                                                            val token = TokenStore.token
                                                            if (token == null) {
                                                                editError = "You're not signed in."
                                                            } else {
                                                                when (val result = AuthRepository.updateProfile("Bearer $token", fullName, phone)) {
                                                                    is ApiResult.Success -> {
                                                                        TokenStore.currentUser = result.data
                                                                        accountSubScreen = AccountSubScreen.Main
                                                                    }
                                                                    is ApiResult.Error -> editError = result.message
                                                                }
                                                            }
                                                            isSaving = false
                                                        }
                                                    }
                                                )
                                            }
                                            AccountSubScreen.ChangePassword -> {
                                                var isSaving by remember { mutableStateOf(false) }
                                                var pwError by remember { mutableStateOf<String?>(null) }
                                                var pwSuccess by remember { mutableStateOf<String?>(null) }
                                                ChangePasswordScreen(
                                                    isSaving = isSaving,
                                                    errorMessage = pwError,
                                                    successMessage = pwSuccess,
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    onSubmit = { currentPw, newPw ->
                                                        pwError = null
                                                        pwSuccess = null
                                                        scope.launch {
                                                            isSaving = true
                                                            val token = TokenStore.token
                                                            if (token == null) {
                                                                pwError = "You're not signed in."
                                                            } else {
                                                                when (val result = AuthRepository.changePassword("Bearer $token", currentPw, newPw)) {
                                                                    is ApiResult.Success -> pwSuccess = "Password updated."
                                                                    is ApiResult.Error -> pwError = result.message
                                                                }
                                                            }
                                                            isSaving = false
                                                        }
                                                    }
                                                )
                                            }
                                            AccountSubScreen.AddressOrFarm -> {
                                                if (showAddressPicker) {
                                                    com.smartagrimarket.components.LocationPickerScreen(
                                                        title = "Pin your delivery address",
                                                        subtitle = "Drag the map so the pin sits where you want deliveries.",
                                                        initialLat = pickedAddressCoords?.first
                                                            ?: deliveryAddress?.latitude,
                                                        initialLng = pickedAddressCoords?.second
                                                            ?: deliveryAddress?.longitude,
                                                        onConfirm = { picked ->
                                                            pickedAddressCoords = picked.lat to picked.lng
                                                            showAddressPicker = false
                                                        },
                                                        onBack = { showAddressPicker = false }
                                                    )
                                                } else {
                                                    var address by remember { mutableStateOf<AddressDto?>(null) }
                                                    var isAddrLoading by remember { mutableStateOf(true) }
                                                    var isAddrSaving by remember { mutableStateOf(false) }
                                                    var addrError by remember { mutableStateOf<String?>(null) }

                                                    fun leaveAddressScreen() {
                                                        accountSubScreen = AccountSubScreen.Main
                                                        if (returnToCheckoutAfterAddress) {
                                                            returnToCheckoutAfterAddress = false
                                                            selectedTab = BuyerTab.CART
                                                            buyerScreen = BuyerScreen.Checkout
                                                        }
                                                    }

                                                    LaunchedEffect(Unit) {
                                                        val token = TokenStore.token
                                                        if (token != null) {
                                                            when (val result = AuthRepository.getAddress("Bearer $token")) {
                                                                is ApiResult.Success -> address = result.data
                                                                is ApiResult.Error -> addrError = result.message
                                                            }
                                                        }
                                                        isAddrLoading = false
                                                    }

                                                    val displayAddress = address?.let { base ->
                                                        pickedAddressCoords?.let { (lat, lng) ->
                                                            base.copy(latitude = lat, longitude = lng)
                                                        } ?: base
                                                    }

                                                    DeliveryAddressScreen(
                                                        address = displayAddress,
                                                        isLoading = isAddrLoading,
                                                        isSaving = isAddrSaving,
                                                        errorMessage = addrError,
                                                        onBack = { leaveAddressScreen() },
                                                        onPickOnMap = { showAddressPicker = true },
                                                        onSave = { req ->
                                                            addrError = null
                                                            scope.launch {
                                                                isAddrSaving = true
                                                                val token = TokenStore.token
                                                                if (token == null) {
                                                                    addrError = "You're not signed in."
                                                                } else {
                                                                    when (val result = AuthRepository.updateAddress("Bearer $token", req)) {
                                                                        is ApiResult.Success -> {
                                                                            deliveryAddress = result.data
                                                                            pickedAddressCoords = null
                                                                            leaveAddressScreen()
                                                                        }
                                                                        is ApiResult.Error -> addrError = result.message
                                                                    }
                                                                }
                                                                isAddrSaving = false
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            AccountSubScreen.NotificationSettings -> {
                                                var settings by remember { mutableStateOf<NotificationSettingsDto?>(null) }
                                                var isNotifLoading by remember { mutableStateOf(true) }
                                                var isNotifSaving by remember { mutableStateOf(false) }
                                                var notifError by remember { mutableStateOf<String?>(null) }

                                                LaunchedEffect(Unit) {
                                                    val token = TokenStore.token
                                                    if (token != null) {
                                                        when (val result = AuthRepository.getNotificationSettings("Bearer $token")) {
                                                            is ApiResult.Success -> settings = result.data
                                                            is ApiResult.Error -> notifError = result.message
                                                        }
                                                    }
                                                    isNotifLoading = false
                                                }

                                                NotificationSettingsScreen(
                                                    settings = settings,
                                                    isLoading = isNotifLoading,
                                                    isSaving = isNotifSaving,
                                                    errorMessage = notifError,
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    onSave = { updated: NotificationSettingsDto ->
                                                        notifError = null
                                                        scope.launch {
                                                            isNotifSaving = true
                                                            val token = TokenStore.token
                                                            if (token == null) {
                                                                notifError = "You're not signed in."
                                                            } else {
                                                                when (val result = AuthRepository.updateNotificationSettings("Bearer $token", updated)) {
                                                                    is ApiResult.Success -> settings = result.data
                                                                    is ApiResult.Error -> notifError = result.message
                                                                }
                                                            }
                                                            isNotifSaving = false
                                                        }
                                                    }
                                                )
                                            }
                                            AccountSubScreen.OrderHistory -> {
                                                var orders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
                                                var isOrdersLoading by remember { mutableStateOf(true) }
                                                var ordersError by remember { mutableStateOf<String?>(null) }
                                                var reloadKey by remember { mutableStateOf(0) }

                                                LaunchedEffect(reloadKey) {
                                                    isOrdersLoading = true
                                                    val token = TokenStore.token
                                                    if (token != null) {
                                                        when (val result = AuthRepository.getOrders("Bearer $token")) {
                                                            is ApiResult.Success -> orders = result.data
                                                            is ApiResult.Error -> ordersError = result.message
                                                        }
                                                    }
                                                    isOrdersLoading = false
                                                }

                                                OrderHistoryScreen(
                                                    orders = orders,
                                                    isLoading = isOrdersLoading,
                                                    errorMessage = ordersError,
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    onOrderClick = { order ->
                                                        buyerScreen = BuyerScreen.Tracking(order)
                                                    },
                                                    onPickDeliverySlotClick = { order ->
                                                        buyerScreen = BuyerScreen.DeliverySlotPick(order)
                                                    }
                                                )
                                            }
                                            AccountSubScreen.AboutSupport -> {
                                                AboutSupportScreen(
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    onTermsClick = { openUrl(context, "$BASE_URL/terms") },
                                                    onPrivacyClick = { accountSubScreen = AccountSubScreen.PrivacyPolicy },
                                                    onReturnsPolicyClick = { openUrl(context, "$BASE_URL/returns") },
                                                    onFarmerTermsClick = { openUrl(context, "$BASE_URL/farmer-terms") },
                                                    onFarmerTrainingPackagingClick = { openUrl(context, "$BASE_URL/help/packaging") },
                                                    onFarmerTrainingPricingClick = { openUrl(context, "$BASE_URL/help/pricing") },
                                                    onApplyVerifiedBadgeClick = {
                                                        accountSubScreen = AccountSubScreen.HelpCenter
                                                    }
                                                )
                                            }
                                            AccountSubScreen.HelpCenter -> {
                                                HelpCenterScreen(
                                                    onBack = { accountSubScreen = AccountSubScreen.Main },
                                                    bottomBar = bottomBar
                                                )
                                            }
                                            AccountSubScreen.Wallet -> {
                                                accountSubScreen = AccountSubScreen.Main
                                            }
                                            AccountSubScreen.Main -> {
                                                AccountSettingsScreen(
                                                    user = TokenStore.currentUser,
                                                    role = UserRole.BUYER,
                                                    isDeactivatingAccount = isDeletingAccount,
                                                    deactivateAccountError = deleteAccountError,
                                                    onBack = { selectedTab = BuyerTab.HOME },
                                                    onEditProfile = { accountSubScreen = AccountSubScreen.EditProfile },
                                                    onChangePassword = { accountSubScreen = AccountSubScreen.ChangePassword },
                                                    onOrderHistoryClick = { accountSubScreen = AccountSubScreen.OrderHistory },
                                                    onDownloadMyData = { accountSubScreen = AccountSubScreen.DownloadData },
                                                    onPrivacyPolicyClick = { accountSubScreen = AccountSubScreen.PrivacyPolicy },
                                                    onNotificationSettingsClick = { accountSubScreen = AccountSubScreen.NotificationSettings },
                                                    onAboutSupportClick = { accountSubScreen = AccountSubScreen.AboutSupport },
                                                    onHelpCenterClick = { accountSubScreen = AccountSubScreen.HelpCenter },
                                                    onRequestProductClick = { accountSubScreen = AccountSubScreen.RequestProduct },
                                                    onWalletClick = { },
                                                    onLogout = {
                                                        TokenStore.clear()
                                                        screen = AppScreen.Home(UserRole.BUYER, isGuest = true)
                                                    },
                                                    onDeactivateAccountConfirmed = {
                                                        deleteAccountError = null
                                                        scope.launch {
                                                            isDeletingAccount = true
                                                            val token = TokenStore.token
                                                            if (token == null) {
                                                                deleteAccountError = "You're not signed in."
                                                            } else {
                                                                when (val result = AuthRepository.deactivateAccount("Bearer $token")) {
                                                                    is ApiResult.Success -> {
                                                                        TokenStore.clear()
                                                                        screen = AppScreen.Home(UserRole.BUYER, isGuest = true)
                                                                    }
                                                                    is ApiResult.Error -> deleteAccountError = result.message
                                                                }
                                                            }
                                                            isDeletingAccount = false
                                                        }
                                                    },
                                                    onDismissDeactivateError = { deleteAccountError = null },
                                                    bottomBar = bottomBar
                                                )
                                            }
                                        }
                                    }

                                    BuyerTab.CART -> {
                                        CartScreen(
                                            cart = cart,
                                            onCheckoutClick = {
                                                if (isGuest) {
                                                    screen = AppScreen.SignIn
                                                } else {
                                                    buyerScreen = BuyerScreen.Checkout
                                                }
                                            },
                                            bottomBar = bottomBar
                                        )
                                    }

                                    else -> {
                                        HomeScreen(
                                            isLoggedIn = !isGuest,
                                            buyerFirstName = if (isGuest) null
                                            else TokenStore.currentUser?.fullName?.split(" ")?.firstOrNull(),
                                            products = products,
                                            cartItemCount = cart.totalItems,
                                            unreadNotificationCount = unreadNotificationCount,
                                            onProductClick = { product ->
                                                buyerScreen = BuyerScreen.ProductDetail(product)
                                            },
                                            onAddToCart = { product -> tryAddToCart(product) },
                                            onRemoveFromCart = { product -> cart.decrement(product.id) },
                                            cartQuantityFor = { productId -> cart.quantityOf(productId) },
                                            onCartClick = { selectedTab = BuyerTab.CART },
                                            onNotificationsClick = {
                                                if (isGuest) screen = AppScreen.SignIn
                                                else buyerScreen = BuyerScreen.Notifications
                                            },
                                            onRecommendationClick = { rec ->
                                                buyerScreen = BuyerScreen.ProductDetail(recommendedToProduct(rec))
                                            },
                                            bottomBar = bottomBar
                                        )
                                    }
                                }
                            }
                        }

                        stockLimitMessage?.let { msg ->
                            AlertDialog(
                                onDismissRequest = { stockLimitMessage = null },
                                title = {
                                    Text(
                                        "Stock limit reached",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                },
                                text = {
                                    Text(
                                        msg,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        stockLimitMessage = null
                                        selectedTab = BuyerTab.CART
                                        buyerScreen = BuyerScreen.Tabs
                                    }) {
                                        Text(
                                            "View cart",
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { stockLimitMessage = null }) {
                                        Text("OK")
                                    }
                                }
                            )
                        }
                    }

                    UserRole.FARMER -> {
                        var selectedTab by remember { mutableStateOf(FarmerTab.DASHBOARD) }
                        var farmerScreen by remember { mutableStateOf<FarmerScreen>(FarmerScreen.Tabs) }
                        var accountSubScreen by remember { mutableStateOf<AccountSubScreen>(AccountSubScreen.Main) }
                        var farmerNotificationsOpen by remember { mutableStateOf(false) }
                        var farmerQuestionsOpen by remember { mutableStateOf(false) }
                        var farmerRequestsOpen by remember { mutableStateOf(false) }
                        var unreadRefreshKey by remember { mutableStateOf(0) }

                        var verificationStatus by remember { mutableStateOf<String?>(null) }
                        var showVerificationScreen by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            when (val r = VerificationRepository.fetchStatus()) {
                                is ApiResult.Success -> verificationStatus = r.data.status
                                is ApiResult.Error -> verificationStatus = null
                            }
                        }

                        val farmerCanGoBack = showVerificationScreen ||
                                farmerRequestsOpen ||
                                farmerQuestionsOpen ||
                                farmerNotificationsOpen ||
                                farmerScreen !is FarmerScreen.Tabs ||
                                (selectedTab == FarmerTab.ACCOUNT && accountSubScreen != AccountSubScreen.Main) ||
                                selectedTab != FarmerTab.DASHBOARD

                        BackHandler(enabled = farmerCanGoBack) {
                            when {
                                showVerificationScreen -> showVerificationScreen = false
                                farmerRequestsOpen -> {
                                    farmerRequestsOpen = false
                                    unreadRefreshKey++
                                }
                                farmerQuestionsOpen -> {
                                    farmerQuestionsOpen = false
                                    unreadRefreshKey++
                                }
                                farmerNotificationsOpen -> {
                                    farmerNotificationsOpen = false
                                    unreadRefreshKey++
                                }
                                farmerScreen !is FarmerScreen.Tabs -> farmerScreen = FarmerScreen.Tabs
                                selectedTab == FarmerTab.ACCOUNT && accountSubScreen != AccountSubScreen.Main -> {
                                    accountSubScreen = AccountSubScreen.Main
                                }
                                selectedTab != FarmerTab.DASHBOARD -> selectedTab = FarmerTab.DASHBOARD
                            }
                        }

                        val bottomBar: @Composable () -> Unit = {
                            FarmerBottomNav(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }

                        when {
                            showVerificationScreen -> {
                                FarmerVerificationScreen(
                                    onBack = { showVerificationScreen = false },
                                    onSubmitted = {
                                        showVerificationScreen = false
                                        verificationStatus = "pending"
                                    }
                                )
                            }

                            farmerRequestsOpen -> {
                                FarmerRequestsScreen(
                                    onBack = {
                                        farmerRequestsOpen = false
                                        unreadRefreshKey++
                                    },
                                    bottomBar = bottomBar
                                )
                            }

                            farmerQuestionsOpen -> {
                                FarmerQuestionsScreen(
                                    onBack = {
                                        farmerQuestionsOpen = false
                                        unreadRefreshKey++
                                    },
                                    bottomBar = bottomBar
                                )
                            }

                            farmerNotificationsOpen -> {
                                NotificationsScreen(
                                    onBack = {
                                        farmerNotificationsOpen = false
                                        unreadRefreshKey++
                                    },
                                    onNotificationClick = { notif ->
                                        when (notif.type) {
                                            "question" -> {
                                                farmerNotificationsOpen = false
                                                farmerQuestionsOpen = true
                                            }
                                            "request" -> {
                                                farmerNotificationsOpen = false
                                                farmerRequestsOpen = true
                                            }
                                        }
                                    }
                                )
                            }

                            else -> when (val fs = farmerScreen) {
                                is FarmerScreen.OrderDetail -> {
                                    FarmerOrderDetailScreen(
                                        order = fs.order,
                                        onBack = { farmerScreen = FarmerScreen.Tabs },
                                        onStatusUpdated = { farmerScreen = FarmerScreen.Tabs }
                                    )
                                }

                                FarmerScreen.UploadProduct -> {
                                    UploadProductScreen(
                                        onBack = { farmerScreen = FarmerScreen.Tabs },
                                        isUploading = isLoading,
                                        errorMessage = errorMessage,
                                        onUpload = { name, price, stock, imageUrl ->
                                            errorMessage = null
                                            scope.launch {
                                                isLoading = true
                                                when (val result = ProductsRepository.createProduct(name, price, stock, imageUrl)) {
                                                    is ApiResult.Success -> {
                                                        farmerScreen = FarmerScreen.MyProducts
                                                    }
                                                    is ApiResult.Error -> errorMessage = result.message
                                                }
                                                isLoading = false
                                            }
                                        }
                                    )
                                }

                                FarmerScreen.MyProducts -> {
                                    FarmerMyProductsScreen(
                                        onBack = { farmerScreen = FarmerScreen.Tabs },
                                        bottomBar = bottomBar
                                    )
                                }

                                FarmerScreen.Tabs -> {
                                    when (selectedTab) {
                                        FarmerTab.DASHBOARD -> {
                                            key(unreadRefreshKey) {
                                                FarmerDashboardScreen(
                                                    onNotificationsClick = { farmerNotificationsOpen = true },
                                                    onOrderClick = { order -> farmerScreen = FarmerScreen.OrderDetail(order) },
                                                    onUploadProductClick = {
                                                        if (verificationStatus != "approved") {
                                                            showVerificationScreen = true
                                                        } else {
                                                            farmerScreen = FarmerScreen.UploadProduct
                                                        }
                                                    },
                                                    onMyProductsClick = { farmerScreen = FarmerScreen.MyProducts },
                                                    onSeeAllOrdersClick = { selectedTab = FarmerTab.ORDERS },
                                                    onSeeAllRequestsClick = { farmerRequestsOpen = true },
                                                    bottomBar = bottomBar
                                                )
                                            }
                                        }

                                        FarmerTab.ORDERS -> {
                                            FarmerOrdersScreen(
                                                onOrderClick = { order -> farmerScreen = FarmerScreen.OrderDetail(order) },
                                                bottomBar = bottomBar
                                            )
                                        }

                                        FarmerTab.WEATHER -> {
                                            FarmerWeatherScreen(bottomBar = bottomBar)
                                        }

                                        FarmerTab.STATS -> {
                                            FarmerStatsScreen(bottomBar = bottomBar)
                                        }

                                        FarmerTab.ACCOUNT -> {
                                            when (accountSubScreen) {
                                                AccountSubScreen.RequestProduct -> {
                                                    accountSubScreen = AccountSubScreen.Main
                                                }
                                                AccountSubScreen.PrivacyPolicy -> {
                                                    PrivacyPolicyScreen(onBack = { accountSubScreen = AccountSubScreen.Main })
                                                }
                                                AccountSubScreen.DownloadData -> {
                                                    DownloadMyDataScreen(onBack = { accountSubScreen = AccountSubScreen.Main })
                                                }
                                                AccountSubScreen.EditProfile -> {
                                                    var isSaving by remember { mutableStateOf(false) }
                                                    var editError by remember { mutableStateOf<String?>(null) }
                                                    EditProfileScreen(
                                                        user = TokenStore.currentUser,
                                                        isSaving = isSaving,
                                                        errorMessage = editError,
                                                        onBack = { accountSubScreen = AccountSubScreen.Main },
                                                        onSave = { fullName, phone ->
                                                            editError = null
                                                            scope.launch {
                                                                isSaving = true
                                                                val token = TokenStore.token
                                                                if (token == null) {
                                                                    editError = "You're not signed in."
                                                                } else {
                                                                    when (val result = AuthRepository.updateProfile("Bearer $token", fullName, phone)) {
                                                                        is ApiResult.Success -> {
                                                                            TokenStore.currentUser = result.data
                                                                            accountSubScreen = AccountSubScreen.Main
                                                                        }
                                                                        is ApiResult.Error -> editError = result.message
                                                                    }
                                                                }
                                                                isSaving = false
                                                            }
                                                        }
                                                    )
                                                }
                                                AccountSubScreen.ChangePassword -> {
                                                    var isSaving by remember { mutableStateOf(false) }
                                                    var pwError by remember { mutableStateOf<String?>(null) }
                                                    var pwSuccess by remember { mutableStateOf<String?>(null) }
                                                    ChangePasswordScreen(
                                                        isSaving = isSaving,
                                                        errorMessage = pwError,
                                                        successMessage = pwSuccess,
                                                        onBack = { accountSubScreen = AccountSubScreen.Main },
                                                        onSubmit = { currentPw, newPw ->
                                                            pwError = null
                                                            pwSuccess = null
                                                            scope.launch {
                                                                isSaving = true
                                                                val token = TokenStore.token
                                                                if (token == null) {
                                                                    pwError = "You're not signed in."
                                                                } else {
                                                                    when (val result = AuthRepository.changePassword("Bearer $token", currentPw, newPw)) {
                                                                        is ApiResult.Success -> pwSuccess = "Password updated."
                                                                        is ApiResult.Error -> pwError = result.message
                                                                    }
                                                                }
                                                                isSaving = false
                                                            }
                                                        }
                                                    )
                                                }
                                                AccountSubScreen.AddressOrFarm -> {
                                                    accountSubScreen = AccountSubScreen.Main
                                                }
                                                AccountSubScreen.NotificationSettings -> {
                                                    var settings by remember { mutableStateOf<NotificationSettingsDto?>(null) }
                                                    var isNotifLoading by remember { mutableStateOf(true) }
                                                    var isNotifSaving by remember { mutableStateOf(false) }
                                                    var notifError by remember { mutableStateOf<String?>(null) }

                                                    LaunchedEffect(Unit) {
                                                        val token = TokenStore.token
                                                        if (token != null) {
                                                            when (val result = AuthRepository.getNotificationSettings("Bearer $token")) {
                                                                is ApiResult.Success -> settings = result.data
                                                                is ApiResult.Error -> notifError = result.message
                                                            }
                                                        }
                                                        isNotifLoading = false
                                                    }

                                                    NotificationSettingsScreen(
                                                        settings = settings,
                                                        isLoading = isNotifLoading,
                                                        isSaving = isNotifSaving,
                                                        errorMessage = notifError,
                                                        onBack = { accountSubScreen = AccountSubScreen.Main },
                                                        onSave = { updated: NotificationSettingsDto ->
                                                            notifError = null
                                                            scope.launch {
                                                                isNotifSaving = true
                                                                val token = TokenStore.token
                                                                if (token == null) {
                                                                    notifError = "You're not signed in."
                                                                } else {
                                                                    when (val result = AuthRepository.updateNotificationSettings("Bearer $token", updated)) {
                                                                        is ApiResult.Success -> settings = result.data
                                                                        is ApiResult.Error -> notifError = result.message
                                                                    }
                                                                }
                                                                isNotifSaving = false
                                                            }
                                                        }
                                                    )
                                                }
                                                AccountSubScreen.OrderHistory -> {
                                                    ComingSoonScreen("Order history", onBack = { accountSubScreen = AccountSubScreen.Main })
                                                }
                                                AccountSubScreen.AboutSupport -> {
                                                    AboutSupportScreen(
                                                        onBack = { accountSubScreen = AccountSubScreen.Main },
                                                        onTermsClick = { openUrl(context, "$BASE_URL/terms") },
                                                        onPrivacyClick = { accountSubScreen = AccountSubScreen.PrivacyPolicy },
                                                        onReturnsPolicyClick = { openUrl(context, "$BASE_URL/returns") },
                                                        onFarmerTermsClick = { openUrl(context, "$BASE_URL/farmer-terms") },
                                                        onFarmerTrainingPackagingClick = { openUrl(context, "$BASE_URL/help/packaging") },
                                                        onFarmerTrainingPricingClick = { openUrl(context, "$BASE_URL/help/pricing") },
                                                        onApplyVerifiedBadgeClick = {
                                                            accountSubScreen = AccountSubScreen.Main
                                                            showVerificationScreen = true
                                                        }
                                                    )
                                                }
                                                AccountSubScreen.HelpCenter -> {
                                                    HelpCenterScreen(
                                                        onBack = { accountSubScreen = AccountSubScreen.Main },
                                                        bottomBar = bottomBar
                                                    )
                                                }
                                                AccountSubScreen.Wallet -> {
                                                    FarmerWalletScreen(
                                                        onBack = { accountSubScreen = AccountSubScreen.Main },
                                                        bottomBar = bottomBar
                                                    )
                                                }
                                                AccountSubScreen.Main -> {
                                                    AccountSettingsScreen(
                                                        user = TokenStore.currentUser,
                                                        role = UserRole.FARMER,
                                                        isDeactivatingAccount = isDeletingAccount,
                                                        deactivateAccountError = deleteAccountError,
                                                        onBack = { selectedTab = FarmerTab.DASHBOARD },
                                                        onEditProfile = { accountSubScreen = AccountSubScreen.EditProfile },
                                                        onChangePassword = { accountSubScreen = AccountSubScreen.ChangePassword },
                                                        onOrderHistoryClick = { accountSubScreen = AccountSubScreen.OrderHistory },
                                                        onDownloadMyData = { accountSubScreen = AccountSubScreen.DownloadData },
                                                        onPrivacyPolicyClick = { accountSubScreen = AccountSubScreen.PrivacyPolicy },
                                                        onNotificationSettingsClick = { accountSubScreen = AccountSubScreen.NotificationSettings },
                                                        onAboutSupportClick = { accountSubScreen = AccountSubScreen.AboutSupport },
                                                        onHelpCenterClick = { accountSubScreen = AccountSubScreen.HelpCenter },
                                                        onRequestProductClick = { },
                                                        onWalletClick = { accountSubScreen = AccountSubScreen.Wallet },
                                                        onLogout = {
                                                            TokenStore.clear()
                                                            screen = AppScreen.Home(UserRole.BUYER, isGuest = true)
                                                        },
                                                        onDeactivateAccountConfirmed = {
                                                            deleteAccountError = null
                                                            scope.launch {
                                                                isDeletingAccount = true
                                                                val token = TokenStore.token
                                                                if (token == null) {
                                                                    deleteAccountError = "You're not signed in."
                                                                } else {
                                                                    when (val result = AuthRepository.deactivateAccount("Bearer $token")) {
                                                                        is ApiResult.Success -> {
                                                                            TokenStore.clear()
                                                                            screen = AppScreen.Home(UserRole.BUYER, isGuest = true)
                                                                        }
                                                                        is ApiResult.Error -> deleteAccountError = result.message
                                                                    }
                                                                }
                                                                isDeletingAccount = false
                                                            }
                                                        },
                                                        onDismissDeactivateError = { deleteAccountError = null },
                                                        bottomBar = bottomBar
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    UserRole.DRIVER -> {
                        var selectedTab by remember { mutableStateOf(DriverTab.HOME) }
                        var driverScreen by remember { mutableStateOf<DriverScreen>(DriverScreen.Tabs) }
                        var activeJobCount by remember { mutableStateOf(0) }

                        val driverCanGoBack = driverScreen !is DriverScreen.Tabs ||
                                selectedTab != DriverTab.HOME

                        BackHandler(enabled = driverCanGoBack) {
                            when {
                                driverScreen is DriverScreen.AccountEditProfile -> driverScreen = DriverScreen.Tabs
                                driverScreen is DriverScreen.AccountChangePassword -> driverScreen = DriverScreen.Tabs
                                driverScreen is DriverScreen.AccountDownloadData -> driverScreen = DriverScreen.Tabs
                                driverScreen !is DriverScreen.Tabs -> driverScreen = DriverScreen.Tabs
                                selectedTab != DriverTab.HOME -> selectedTab = DriverTab.HOME
                            }
                        }

                        val bottomBar: @Composable () -> Unit = {
                            DriverBottomNav(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                activeJobCount = activeJobCount
                            )
                        }

                        LaunchedEffect(selectedTab, driverScreen) {
                            when (val r = DriverRepository.fetchStats()) {
                                is ApiResult.Success -> activeJobCount = r.data.activeCount
                                is ApiResult.Error -> { }
                            }
                        }

                        when (val ds = driverScreen) {
                            DriverScreen.Verification -> {
                                DriverVerificationScreen(
                                    onBack = { driverScreen = DriverScreen.Tabs },
                                    onSubmitted = {
                                        driverScreen = DriverScreen.Tabs
                                    }
                                )
                            }

                            DriverScreen.Wallet -> {
                                DriverWalletScreen(
                                    onBack = { driverScreen = DriverScreen.Tabs }
                                )
                            }

                            DriverScreen.AccountEditProfile -> {
                                var isSaving by remember { mutableStateOf(false) }
                                var editError by remember { mutableStateOf<String?>(null) }
                                EditProfileScreen(
                                    user = TokenStore.currentUser,
                                    isSaving = isSaving,
                                    errorMessage = editError,
                                    onBack = { driverScreen = DriverScreen.Tabs },
                                    onSave = { fullName, phone ->
                                        editError = null
                                        scope.launch {
                                            isSaving = true
                                            val token = TokenStore.token
                                            if (token == null) {
                                                editError = "You're not signed in."
                                            } else {
                                                when (val result = AuthRepository.updateProfile("Bearer $token", fullName, phone)) {
                                                    is ApiResult.Success -> {
                                                        TokenStore.currentUser = result.data
                                                        driverScreen = DriverScreen.Tabs
                                                    }
                                                    is ApiResult.Error -> editError = result.message
                                                }
                                            }
                                            isSaving = false
                                        }
                                    }
                                )
                            }

                            DriverScreen.AccountChangePassword -> {
                                var isSaving by remember { mutableStateOf(false) }
                                var pwError by remember { mutableStateOf<String?>(null) }
                                var pwSuccess by remember { mutableStateOf<String?>(null) }
                                ChangePasswordScreen(
                                    isSaving = isSaving,
                                    errorMessage = pwError,
                                    successMessage = pwSuccess,
                                    onBack = { driverScreen = DriverScreen.Tabs },
                                    onSubmit = { currentPw, newPw ->
                                        pwError = null
                                        pwSuccess = null
                                        scope.launch {
                                            isSaving = true
                                            val token = TokenStore.token
                                            if (token == null) {
                                                pwError = "You're not signed in."
                                            } else {
                                                when (val result = AuthRepository.changePassword("Bearer $token", currentPw, newPw)) {
                                                    is ApiResult.Success -> pwSuccess = "Password updated."
                                                    is ApiResult.Error -> pwError = result.message
                                                }
                                            }
                                            isSaving = false
                                        }
                                    }
                                )
                            }

                            DriverScreen.AccountDownloadData -> {
                                DownloadMyDataScreen(
                                    onBack = { driverScreen = DriverScreen.Tabs }
                                )
                            }

                            DriverScreen.Tabs -> {
                                when (selectedTab) {
                                    DriverTab.HOME -> {
                                        DriverHomeScreen(
                                            onBrowseJobs = { selectedTab = DriverTab.JOBS },
                                            onOpenVerification = {
                                                driverScreen = DriverScreen.Verification
                                            },
                                            onOpenWallet = {
                                                driverScreen = DriverScreen.Wallet
                                            },
                                            bottomBar = bottomBar
                                        )
                                    }
                                    DriverTab.JOBS -> {
                                        DriverJobsScreen(
                                            onJobClick = { /* batch queue rows are self-contained */ },
                                            bottomBar = bottomBar
                                        )
                                    }
                                    DriverTab.ACCOUNT -> {
                                        AccountSettingsScreen(
                                            user = TokenStore.currentUser,
                                            role = UserRole.DRIVER,
                                            isDeactivatingAccount = isDeletingAccount,
                                            deactivateAccountError = deleteAccountError,
                                            onBack = { selectedTab = DriverTab.HOME },
                                            onEditProfile = { driverScreen = DriverScreen.AccountEditProfile },
                                            onChangePassword = { driverScreen = DriverScreen.AccountChangePassword },
                                            onOrderHistoryClick = { },
                                            onDownloadMyData = { driverScreen = DriverScreen.AccountDownloadData },
                                            onPrivacyPolicyClick = { openUrl(context, "$BASE_URL/terms") },
                                            onNotificationSettingsClick = { },
                                            onAboutSupportClick = { openUrl(context, "$BASE_URL/terms") },
                                            onHelpCenterClick = { openUrl(context, "$BASE_URL/help/packaging") },
                                            onRequestProductClick = { },
                                            onWalletClick = { driverScreen = DriverScreen.Wallet },
                                            onLogout = {
                                                TokenStore.clear()
                                                screen = AppScreen.Home(UserRole.BUYER, isGuest = true)
                                            },
                                            onDeactivateAccountConfirmed = {
                                                deleteAccountError = null
                                                scope.launch {
                                                    isDeletingAccount = true
                                                    val token = TokenStore.token
                                                    if (token == null) {
                                                        deleteAccountError = "You're not signed in."
                                                    } else {
                                                        when (val result = AuthRepository.deactivateAccount("Bearer $token")) {
                                                            is ApiResult.Success -> {
                                                                TokenStore.clear()
                                                                screen = AppScreen.Home(UserRole.BUYER, isGuest = true)
                                                            }
                                                            is ApiResult.Error -> deleteAccountError = result.message
                                                        }
                                                    }
                                                    isDeletingAccount = false
                                                }
                                            },
                                            onDismissDeactivateError = { deleteAccountError = null },
                                            bottomBar = bottomBar
                                        )
                                    }
                                }
                            }
                        }
                    }

                    UserRole.ADMIN -> {
                        // No-op — admins always go through the picker dialog
                    }
                }
            }
        }

        if (showAdminRolePicker) {
            BackHandler {
                showAdminRolePicker = false
                screen = AppScreen.Home(UserRole.BUYER, isGuest = false)
            }
            AdminRolePickerDialog(
                adminName = adminName,
                onOpenAdminPanel = {
                    showAdminRolePicker = false
                    val url = "$BASE_URL/admin.html"
                    openUrl(context, url)
                },
                onContinueAs = { roleStr ->
                    showAdminRolePicker = false
                    val role = when (roleStr) {
                        "FARMER" -> UserRole.FARMER
                        "DRIVER" -> UserRole.DRIVER
                        else -> UserRole.BUYER
                    }
                    screen = AppScreen.Home(role, isGuest = false)
                },
                onDismiss = {
                    showAdminRolePicker = false
                    screen = AppScreen.Home(UserRole.BUYER, isGuest = false)
                }
            )
        }

        errorMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(4000)
                errorMessage = null
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFF6DEDE), shape = MaterialTheme.shapes.medium)
                    .padding(14.dp)
            ) {
                Text(text = msg, color = Color(0xFFA13232))
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}