package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.ui.theme.*

@Composable
fun OrderConfirmationScreen(onContinueShopping: () -> Unit) {
    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AgriMintLight),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 60.sp, color = AgriGreen, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Order placed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "We've let the farmer know. You'll get updates as your order is packed and delivered.",
                style = MaterialTheme.typography.bodyMedium,
                color = AgriTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Continue shopping",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}