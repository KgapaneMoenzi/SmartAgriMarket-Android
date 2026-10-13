package com.smartagrimarket.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.DataExportRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DownloadMyDataScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                Text("Download my data", style = MaterialTheme.typography.headlineSmall)
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val emoji = when {
                    successMessage != null -> "📩"
                    errorMessage != null -> "⚠️"
                    else -> "📄"
                }
                Text(emoji, fontSize = 40.sp)
                Spacer(Modifier.height(16.dp))

                val title = when {
                    successMessage != null -> "Request sent!"
                    errorMessage != null -> "Something went wrong"
                    else -> "Request a copy of your data"
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                val body = successMessage ?: errorMessage
                ?: "We'll email you a PDF summary plus a machine-readable JSON file, as required by POPIA."
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (errorMessage != null) AgriRedAlert else AgriTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        when (val r = DataExportRepository.requestExport()) {
                            is ApiResult.Success -> successMessage = r.data.message
                            is ApiResult.Error -> errorMessage = r.message
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriYellow,
                    contentColor = AgriTextPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AgriTextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sending…", fontWeight = FontWeight.Bold)
                } else {
                    Text("Request my data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}