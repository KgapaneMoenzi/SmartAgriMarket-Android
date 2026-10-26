package com.smartagrimarket.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.network.ProductImage
import com.smartagrimarket.ui.theme.AgriTextPrimary
import kotlinx.coroutines.launch

// ── Smart Basket palette (local to this screen only) ──
private val SbBackdrop = Color(0xFF3D4A52)
private val SbHeaderTeal = Color(0xFF399AA1)
private val SbPanel = Color(0xFF40626B)
private val SbGlow = Color(0xFF25A35A)
private val SbTagStart = Color(0xFF11BC49)
private val SbTagEnd = Color(0xFF62CA8D)
private val SbChipBorder = Color(0xFF499EA3)
private val SbChipText = Color(0xFF1F6F76)
private val SbInk = Color(0xFF151C24)
private val SbInkSecondary = Color(0xFF44525C)
private val SbAddGreen = Color(0xFF2FCB62)
private val SbOnGreen = Color(0xFF06301A)
private val SbUndoBg = Color(0xFF4F5F68)
private val SbMint = Color(0xFFDDF1EF)
private val SbSelect = Color(0xFF0F7A3A)
private val SbSkip = Color(0xFFB3261E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBasketScreen(
    products: List<Product>,
    onAddToCart: (Product, Int) -> Unit, // Changed to accept Quantity
    onClose: () -> Unit
) {
    var currentCards by remember { mutableStateOf(products) }
    var currentIndex by remember { mutableIntStateOf(0) }   // which card is on top
    var returning by remember { mutableStateOf(false) }     // true when a passed card is coming back
    var showTutorial by remember { mutableStateOf(true) }

    // Everything added during this Smart Basket session: product + quantity
    val basket = remember { mutableStateListOf<Pair<Product, Int>>() }
    val basketCount = basket.sumOf { it.second }
    val basketTotal = basket.fold(0.0) { acc, line -> acc + line.first.price.toDouble() * line.second }

    // State for the Quantity Modal
    var selectedProductForModal by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        containerColor = SbBackdrop,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SMART BASKET",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SbHeaderTeal)
            )
        },
        bottomBar = {
            Column {
                if (basketCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SbPanel)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total so far · $basketCount ${if (basketCount == 1) "item" else "items"}",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "R${"%.2f".format(basketTotal)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SbBackdrop)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showTutorial = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("How does this work?", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { /* Undo logic */ },
                        colors = ButtonDefaults.buttonColors(containerColor = SbUndoBg, contentColor = Color.White),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Undo", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Undo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SbPanel),
            contentAlignment = Alignment.Center
        ) {
            if (currentIndex >= currentCards.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Basket complete! 🎉", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$basketCount ${if (basketCount == 1) "item" else "items"} · R${"%.2f".format(basketTotal)}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentCards.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                returning = false
                                currentIndex = 0
                            },
                            border = BorderStroke(2.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("See passed items again", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = SbAddGreen, contentColor = SbOnGreen)
                    ) {
                        Text("Go to Cart", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val stack = currentCards.drop(currentIndex).take(3).reversed()
                stack.forEachIndexed { index, product ->
                    val isTopCard = index == stack.size - 1
                    key(product.id) {
                        SwipeableCard(
                            product = product,
                            isTopCard = isTopCard,
                            canGoBack = currentIndex > 0,
                            enterFromRight = returning && isTopCard,
                            onSwipeDown = {
                                // Instead of adding directly, open the modal
                                selectedProductForModal = product
                            },
                            onSwipeUp = {
                                // Remove this item from the deck
                                returning = false
                                currentCards = currentCards.filter { it.id != product.id }
                            },
                            onSwipeRight = {
                                // Pass: next item. It stays in the deck so you can come back to it
                                returning = false
                                currentIndex++
                            },
                            onSwipeLeft = {
                                // Bring back the item you just passed
                                returning = true
                                currentIndex--
                            }
                        )
                    }
                }
            }

            // ── TUTORIAL OVERLAY ──
            if (showTutorial) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showTutorial = false },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwipeTip(SwipeDir.UP, "SWIPE UP", "TO REMOVE")
                        Spacer(Modifier.height(16.dp))
                        SwipeTip(SwipeDir.SIDE, "SWIPE LEFT OR RIGHT", "TO BROWSE")
                        Spacer(Modifier.height(16.dp))
                        SwipeTip(SwipeDir.DOWN, "SWIPE DOWN", "TO ADD TO BASKET")
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = { showTutorial = false },
                            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SbHeaderTeal, contentColor = Color.White)
                        ) {
                            Text("Got It, Thanks", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }

    // ── QUANTITY MODAL POPUP ──
    if (selectedProductForModal != null) {
        AddToCartModal(
            product = selectedProductForModal!!,
            onDismiss = { selectedProductForModal = null },
            onConfirm = { quantity ->
                val added = selectedProductForModal!!
                onAddToCart(added, quantity)
                // Keep a running total for the bar at the bottom
                val existing = basket.indexOfFirst { it.first.id == added.id }
                if (existing >= 0) {
                    basket[existing] = added to (basket[existing].second + quantity)
                } else {
                    basket.add(added to quantity)
                }
                // Remove this card from the deck
                returning = false
                currentCards = currentCards.filter { it.id != added.id }
                selectedProductForModal = null
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// THE ADD TO CART MODAL
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCartModal(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = SbChipBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProductImage(
                imageUrl = product.imageUrl,
                productName = product.name,
                modifier = Modifier.size(120.dp),
                fallbackFontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                product.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                "R${"%.2f".format(product.price)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SbChipText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quantity Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier
                        .size(44.dp)
                        .background(SbMint, CircleShape)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = AgriTextPrimary)
                }

                Text(
                    quantity.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                IconButton(
                    onClick = {
                        // Check stock limit here if needed
                        quantity++
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(SbMint, CircleShape)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase", tint = AgriTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onConfirm(quantity) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SbAddGreen, contentColor = SbOnGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Add $quantity to Basket", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SwipeableCard(
    product: Product,
    isTopCard: Boolean,
    canGoBack: Boolean,
    enterFromRight: Boolean,
    onSwipeDown: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }

    // A card that was passed starts off-screen on the right and slides back in
    val offsetX = remember { Animatable(if (enterFromRight) screenWidthPx else 0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (enterFromRight) {
            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }
    }

    val swipeThresholdY = screenHeightPx / 5
    val swipeThresholdX = screenWidthPx / 3

    val scale = if (isTopCard) 1f else 0.95f
    val peekOffsetY = if (isTopCard) 0f else 20f

    val addOpacity = (offsetY.value / swipeThresholdY).coerceIn(0f, 1f)
    val skipOpacity = (-offsetY.value / swipeThresholdY).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .fillMaxHeight(0.65f)
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value + peekOffsetY
                rotationZ = rotation.value
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isTopCard) 24.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = SbGlow,
                spotColor = SbGlow
            )
            .then(if (isTopCard) Modifier.border(3.dp, SbTagEnd, RoundedCornerShape(24.dp)) else Modifier)
            .pointerInput(isTopCard) {
                if (!isTopCard) return@pointerInput
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)

                            val tiltX = offsetX.value / 60
                            val tiltY = offsetY.value / 100
                            rotation.snapTo((tiltX + tiltY).coerceIn(-15f, 15f))
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetY.value > swipeThresholdY) {
                                // User swiped DOWN -> Trigger Modal
                                // We snap back to center because we are opening a modal
                                launch { offsetX.animateTo(0f, spring()) }
                                launch { offsetY.animateTo(0f, spring()) }
                                launch { rotation.animateTo(0f, spring()) }
                                onSwipeDown()
                            }
                            else if (offsetY.value < -swipeThresholdY) {
                                // Swipe Up to skip (fly off screen)
                                offsetY.animateTo(-screenHeightPx * 1.5f, tween(300))
                                onSwipeUp()
                            }
                            else if (offsetX.value > swipeThresholdX) {
                                // Swipe Right: pass this item (fly off to the right)
                                offsetX.animateTo(screenWidthPx * 1.5f, tween(300))
                                onSwipeRight()
                            } else if (offsetX.value < -swipeThresholdX && canGoBack) {
                                // Swipe Left: this card settles back and the passed item returns on top
                                launch { offsetX.animateTo(0f, spring()) }
                                launch { offsetY.animateTo(0f, spring()) }
                                launch { rotation.animateTo(0f, spring()) }
                                onSwipeLeft()
                            }
                            else {
                                // Didn't pass threshold, snap back
                                launch { offsetX.animateTo(0f, spring()) }
                                launch { offsetY.animateTo(0f, spring()) }
                                launch { rotation.animateTo(0f, spring()) }
                            }
                        }
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .background(Brush.horizontalGradient(listOf(SbTagStart, SbTagEnd)))
                ) {
                    Text(
                        "📈 TRENDING NOW",
                        color = SbOnGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                ProductImage(
                    imageUrl = product.imageUrl,
                    productName = product.name,
                    modifier = Modifier.size(160.dp),
                    fallbackFontSize = 64.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "R${"%.2f".format(product.price)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SbInk
                )
                Text(
                    product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SbInkSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, SbChipBorder),
                    color = Color.Transparent
                ) {
                    Text(
                        "📍 Nelspruit Hub",
                        color = SbChipText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (addOpacity > 0.1f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = addOpacity * 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SELECT",
                        color = SbSelect.copy(alpha = addOpacity),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            if (skipOpacity > 0.1f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = skipOpacity * 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SKIP",
                        color = SbSkip.copy(alpha = skipOpacity),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TUTORIAL HANDS (outlined, see-through, drawn in code)
// ─────────────────────────────────────────────────────────────
private enum class SwipeDir { UP, SIDE, DOWN }

@Composable
private fun SwipeTip(direction: SwipeDir, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedSwipeHand(direction)
        Spacer(Modifier.height(4.dp))
        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
        Text(subtitle, fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
private fun AnimatedSwipeHand(direction: SwipeDir) {
    val transition = rememberInfiniteTransition(label = "swipeHand")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeHandProgress"
    )
    val travelPx = with(LocalDensity.current) { 14.dp.toPx() }
    val offset = (progress * 2f - 1f) * travelPx

    Box(
        modifier = Modifier.size(width = 84.dp, height = 104.dp),
        contentAlignment = Alignment.Center
    ) {
        SwipeHand(
            Modifier
                .size(width = 66.dp, height = 80.dp)
                .graphicsLayer {
                    when (direction) {
                        SwipeDir.UP -> translationY = -offset
                        SwipeDir.DOWN -> translationY = offset
                        SwipeDir.SIDE -> translationX = offset
                    }
                }
        )
    }
}

@Composable
private fun SwipeHand(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sx = size.width / 100f
        val sy = size.height / 120f

        fun block(l: Float, t: Float, r: Float, b: Float, rad: Float): Path = Path().apply {
            addRoundRect(
                RoundRect(l * sx, t * sy, r * sx, b * sy, CornerRadius(rad * sx, rad * sx))
            )
        }

        // index finger + palm + three folded fingers + thumb, merged into one outline
        var hand = block(34f, 6f, 54f, 66f, 10f)
        listOf(
            block(24f, 52f, 88f, 112f, 20f),
            block(54f, 44f, 67f, 72f, 7f),
            block(66f, 48f, 78f, 76f, 7f),
            block(77f, 54f, 90f, 80f, 7f),
            block(10f, 62f, 34f, 88f, 10f)
        ).forEach { hand = Path.combine(PathOperation.Union, hand, it) }

        drawPath(hand, Color.White.copy(alpha = 0.14f))
        drawPath(hand, Color.White, style = Stroke(width = 3.5.dp.toPx(), join = StrokeJoin.Round))

        // touch point on the fingertip
        val tip = Offset(44f * sx, 8f * sy)
        drawCircle(Color(0xFF8FB4FF).copy(alpha = 0.8f), radius = 14f * sx, center = tip)
        drawCircle(Color.White.copy(alpha = 0.9f), radius = 14f * sx, center = tip, style = Stroke(width = 2.dp.toPx()))
    }
}