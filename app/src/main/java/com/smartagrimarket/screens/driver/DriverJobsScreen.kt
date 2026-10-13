package com.smartagrimarket.screens.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.BatchStopDto
import com.smartagrimarket.model.network.DriverRepository
import com.smartagrimarket.model.network.HubDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

private enum class JobTab(val label: String) {
    BATCH("Batch"),
    PAST("Past")
}

@Composable
fun DriverJobsScreen(
    onJobClick: (BatchStopDto) -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(JobTab.BATCH) }
    var batchState by remember { mutableStateOf("idle") }
    var queue by remember { mutableStateOf<List<BatchStopDto>>(emptyList()) }
    var nextStop by remember { mutableStateOf<BatchStopDto?>(null) }
    var past by remember { mutableStateOf<List<BatchStopDto>>(emptyList()) }
    var hub by remember { mutableStateOf<HubDto?>(null) }
    var totalDistanceKm by remember { mutableStateOf(0.0) }
    var estimatedMinutes by remember { mutableStateOf(0) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    fun openNavigation(lat: Double?, lng: Double?, fallback: String?, label: String) {
        val uri = when {
            lat != null && lng != null ->
                Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
            !fallback.isNullOrBlank() ->
                Uri.parse("geo:0,0?q=${Uri.encode(fallback)}")
            else -> return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        when (val r = DriverRepository.fetchJobs()) {
            is ApiResult.Success -> {
                batchState = r.data.batchState
                queue = r.data.batch.queue
                nextStop = r.data.batch.nextStop
                past = r.data.past
                hub = r.data.hub
                totalDistanceKm = r.data.batch.totalDistanceKm
                estimatedMinutes = r.data.batch.estimatedMinutes
            }
            is ApiResult.Error -> errorMessage = r.message
        }
        isLoading = false
    }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "My batch",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    if (batchState == "collected" && queue.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${queue.size} stop${if (queue.size == 1) "" else "s"} · ${"%.1f".format(totalDistanceKm)} km · ~${estimatedMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable { reloadKey++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔄", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Tabs ──
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JobTab.values().forEach { t ->
                    val selected = t == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) AgriGreen else AgriCardBg)
                            .clickable { tab = t }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = when (t) {
                                JobTab.BATCH -> "${t.label} (${queue.size})"
                                JobTab.PAST -> t.label
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else AgriTextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreen)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(errorMessage!!, color = AgriRedAlert, fontSize = 13.sp)
                    }
                }
                tab == JobTab.PAST -> {
                    if (past.isEmpty()) {
                        EmptyBatchCard(emoji = "📦", title = "No past deliveries",
                            body = "Completed deliveries appear here.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(past, key = { it.deliveryId }) { stop ->
                                PastStopRow(stop)
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                }
                else -> {
                    // BATCH tab
                    when (batchState) {
                        "idle" -> {
                            if (queue.isEmpty()) {
                                EmptyBatchCard(
                                    emoji = "📭",
                                    title = "No batch assigned",
                                    body = "Dispatch will assign your next batch."
                                )
                            } else {
                                StartBatchCard(
                                    count = queue.size,
                                    isWorking = working,
                                    onStart = {
                                        working = true
                                        scope.launch {
                                            when (val r = DriverRepository.startBatch()) {
                                                is ApiResult.Success -> {
                                                    batchState = r.data.batchState
                                                    queue = r.data.batch.queue
                                                    nextStop = r.data.batch.nextStop
                                                }
                                                is ApiResult.Error -> errorMessage = r.message
                                            }
                                            working = false
                                        }
                                    }
                                )
                            }
                        }
                        "at_hub" -> {
                            CollectCard(
                                hub = hub,
                                count = queue.size,
                                isWorking = working,
                                onNavigate = {
                                    openNavigation(
                                        lat = hub?.latitude,
                                        lng = hub?.longitude,
                                        fallback = hub?.address,
                                        label = hub?.name ?: "Hub"
                                    )
                                },
                                onCollect = {
                                    working = true
                                    scope.launch {
                                        when (val r = DriverRepository.collectBatch()) {
                                            is ApiResult.Success -> {
                                                batchState = r.data.batchState
                                                queue = r.data.batch.queue
                                                nextStop = r.data.batch.nextStop
                                                totalDistanceKm = r.data.batch.totalDistanceKm
                                                estimatedMinutes = r.data.batch.estimatedMinutes
                                                // Auto-navigate to first stop
                                                nextStop?.let { s ->
                                                    openNavigation(
                                                        lat = s.deliveryLatitude,
                                                        lng = s.deliveryLongitude,
                                                        fallback = null,
                                                        label = s.buyerName
                                                    )
                                                }
                                            }
                                            is ApiResult.Error -> errorMessage = r.message
                                        }
                                        working = false
                                    }
                                }
                            )
                        }
                        "collected" -> {
                            if (queue.isEmpty()) {
                                EmptyBatchCard(
                                    emoji = "🎉",
                                    title = "Batch complete",
                                    body = "Nice work. New batches will appear here."
                                )
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Next stop card
                                    nextStop?.let { ns ->
                                        item {
                                            NextStopCard(
                                                stop = ns,
                                                onNavigate = {
                                                    openNavigation(
                                                        lat = ns.deliveryLatitude,
                                                        lng = ns.deliveryLongitude,
                                                        fallback = null,
                                                        label = ns.buyerName
                                                    )
                                                },
                                                onDeliver = {
                                                    working = true
                                                    scope.launch {
                                                        when (val r = DriverRepository.deliverStop(ns.deliveryId)) {
                                                            is ApiResult.Success -> {
                                                                batchState = r.data.batchState
                                                                queue = r.data.batch.queue
                                                                nextStop = r.data.batch.nextStop
                                                                totalDistanceKm = r.data.batch.totalDistanceKm
                                                                estimatedMinutes = r.data.batch.estimatedMinutes
                                                                // Auto-navigate to next
                                                                nextStop?.let { next ->
                                                                    openNavigation(
                                                                        lat = next.deliveryLatitude,
                                                                        lng = next.deliveryLongitude,
                                                                        fallback = null,
                                                                        label = next.buyerName
                                                                    )
                                                                }
                                                            }
                                                            is ApiResult.Error -> errorMessage = r.message
                                                        }
                                                        working = false
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // Remaining queue
                                    val remaining = queue.drop(1)
                                    if (remaining.isNotEmpty()) {
                                        item {
                                            Text(
                                                "Remaining (${remaining.size})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AgriTextSecondary,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                        items(remaining, key = { it.deliveryId }) { stop ->
                                            QueuedStopRow(
                                                stop = stop,
                                                onNavigate = {
                                                    openNavigation(
                                                        lat = stop.deliveryLatitude,
                                                        lng = stop.deliveryLongitude,
                                                        fallback = null,
                                                        label = stop.buyerName
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    item { Spacer(Modifier.height(24.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartBatchCard(count: Int, isWorking: Boolean, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriMintLight),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "$count deliver${if (count == 1) "y" else "ies"} ready",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AgriGreenDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Start the batch when you're ready to head to the hub.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriGreenDark.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onStart,
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreen,
                    contentColor = Color.White
                )
            ) {
                if (isWorking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Starting…", fontWeight = FontWeight.Bold)
                } else {
                    Text("🚚  Start batch", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CollectCard(
    hub: HubDto?,
    count: Int,
    isWorking: Boolean,
    onNavigate: () -> Unit,
    onCollect: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AgriOrange.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏬", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Pick up from hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AgriOrange
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    hub?.name ?: "AgriMarket Central Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AgriTextPrimary
                )
                hub?.address?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onNavigate,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriOrange),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriOrange.copy(alpha = 0.5f))
                ) {
                    Text("🗺️  Navigate to hub", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = onCollect,
            enabled = !isWorking,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgriGreen,
                contentColor = Color.White
            )
        ) {
            if (isWorking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Collecting…", fontWeight = FontWeight.Bold)
            } else {
                Text("✅  Collected all $count", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun NextStopCard(
    stop: BatchStopDto,
    onNavigate: () -> Unit,
    onDeliver: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriGreen),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "Next stop" +
                        (stop.distanceFromHubKm?.let { " · ${"%.1f".format(it)} km" } ?: ""),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stop.buyerName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${stop.itemCount} item${if (stop.itemCount == 1) "" else "s"} · #${stop.trackingId}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text("🗺️  Navigate", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onDeliver,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AgriGreenDark
                    )
                ) {
                    Text("✅  Delivered", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QueuedStopRow(stop: BatchStopDto, onNavigate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AgriMintSoft),
                contentAlignment = Alignment.Center
            ) {
                Text("🏠", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stop.buyerName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${stop.distanceFromHubKm?.let { "${"%.1f".format(it)} km · " } ?: ""}#${stop.trackingId}",
                    fontSize = 11.sp,
                    color = AgriTextSecondary
                )
            }
            IconButton(onClick = onNavigate) {
                Text("🗺️", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun PastStopRow(stop: BatchStopDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AgriMintLight),
                contentAlignment = Alignment.Center
            ) {
                Text("✅", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stop.buyerName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "#${stop.trackingId} · ${stop.itemCount} item${if (stop.itemCount == 1) "" else "s"}",
                    fontSize = 11.sp,
                    color = AgriTextSecondary
                )
            }
            Text(
                "Delivered",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AgriGreenDark
            )
        }
    }
}

@Composable
private fun EmptyBatchCard(emoji: String, title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AgriMintLight),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 36.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}