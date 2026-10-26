package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.BulkQuoteDto
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerQuotesScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var quotes by remember { mutableStateOf<List<BulkQuoteDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedQuote by remember { mutableStateOf<BulkQuoteDto?>(null) }
    val scope = rememberCoroutineScope()

    fun loadQuotes() {
        scope.launch {
            isLoading = true
            when (val res = FarmerRepository.fetchQuotes()) {
                is ApiResult.Success -> quotes = res.data
                is ApiResult.Error -> { }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadQuotes() }

    Scaffold(
        containerColor = AgriCreamBg,
        topBar = {
            TopAppBar(
                title = { Text("Quote Requests", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding), 
                contentAlignment = Alignment.Center
            ) { 
                CircularProgressIndicator(color = AgriGreen) 
            }
        } else if (quotes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding), 
                contentAlignment = Alignment.Center
            ) { 
                Text("No pending quotes.", color = AgriTextSecondary) 
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quotes) { quote ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(), 
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(quote.buyerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(quote.product.name, fontWeight = FontWeight.SemiBold, color = AgriGreen)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Wants: ${quote.quantity} ${quote.product.unit}", fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { selectedQuote = quote },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreen)
                            ) { 
                                Text("Reply") 
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedQuote != null) {
        val quote = selectedQuote!!
        var price by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var isSending by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSending) selectedQuote = null },
            title = { Text("Reply to ${quote.buyerName}") },
            text = {
                Column {
                    Text("Requested: ${quote.quantity} ${quote.product.unit} of ${quote.product.name}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Final Price (R)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Delivery Date (e.g., 25 Oct)") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note to buyer (optional)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = price.toDoubleOrNull()
                        if (priceVal != null && date.isNotBlank()) {
                            isSending = true
                            scope.launch {
                                val res = FarmerRepository.replyToQuote(quote.id, priceVal, date, note.ifBlank { null })
                                if (res is ApiResult.Success) {
                                    selectedQuote = null
                                    loadQuotes()
                                }
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending
                ) { 
                    Text(if (isSending) "Sending..." else "Send Reply") 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedQuote = null }, 
                    enabled = !isSending
                ) { 
                    Text("Cancel") 
                }
            }
        )
    }
}
