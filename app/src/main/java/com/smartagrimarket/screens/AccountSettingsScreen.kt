package com.smartagrimarket.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.smartagrimarket.model.UserRole
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.model.network.UploadRepository
import com.smartagrimarket.model.network.UserDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

private const val API_BASE = "https://smartagrimarket-backend.onrender.com"

@Composable
fun AccountSettingsScreen(
    user: UserDto? = null,
    role: UserRole = UserRole.BUYER,
    isDeactivatingAccount: Boolean = false,
    deactivateAccountError: String? = null,
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onOrderHistoryClick: () -> Unit = {},
    onDownloadMyData: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    onAboutSupportClick: () -> Unit = {},
    onHelpCenterClick: () -> Unit = {},
    onRequestProductClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDeactivateAccountConfirmed: () -> Unit = {},
    onDismissDeactivateError: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var showDeactivateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var localPhotoUrl by remember(user?.email) { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    val effectivePhotoUrl: String? = localPhotoUrl ?: normalizePhotoUrl(user?.photoUrl)

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isUploadingPhoto = true
            photoError = null
            val bearer = TokenStore.token?.let { "Bearer $it" }
            if (bearer.isNullOrBlank()) {
                photoError = "You're not signed in."
                isUploadingPhoto = false
                return@launch
            }
            when (val result = UploadRepository.uploadProfilePhoto(context, uri, bearer)) {
                is ApiResult.Success -> {
                    val raw = result.data?.trim().orEmpty()
                    if (raw.isBlank()) {
                        photoError = "Upload succeeded but the server returned no URL."
                    } else {
                        val busted = appendCacheBuster(normalizePhotoUrl(raw)!!)
                        localPhotoUrl = busted
                        TokenStore.currentUser = TokenStore.currentUser?.copy(photoUrl = busted)
                    }
                }
                is ApiResult.Error -> photoError = result.message
            }
            isUploadingPhoto = false
        }
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(16.dp))

            ProfileHeader(
                user = user,
                photoUrl = effectivePhotoUrl,
                isUploadingPhoto = isUploadingPhoto,
                onPhotoClick = { if (!isUploadingPhoto) pickPhotoLauncher.launch("image/*") },
                onHeaderClick = onEditProfile
            )

            photoError?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Profile")
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsRow(label = "Edit name & photo", onClick = onEditProfile)
                SettingsDivider()
                SettingsRow(label = "Change password", onClick = onChangePassword)
                // Order history is hidden for drivers — they have no buyer-style order list.
                if (role != UserRole.DRIVER) {
                    SettingsDivider()
                    SettingsRow(label = "Order history", onClick = onOrderHistoryClick)
                }
            }

            if (role == UserRole.FARMER) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Earnings")
                Spacer(Modifier.height(10.dp))
                SettingsGroup {
                    SettingsRow(label = "My Wallet & Withdrawals", onClick = onWalletClick)
                }
            }

            if (role == UserRole.BUYER) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Shopping")
                Spacer(Modifier.height(10.dp))
                SettingsGroup {
                    SettingsRow(label = "Request a product", onClick = onRequestProductClick)
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Privacy")
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsRow(label = "Download my data", onClick = onDownloadMyData)
                SettingsDivider()
                SettingsRow(label = "Privacy Policy", onClick = onPrivacyPolicyClick)
                // Notification settings is hidden for drivers.
                if (role != UserRole.DRIVER) {
                    SettingsDivider()
                    SettingsRow(label = "Notification settings", onClick = onNotificationSettingsClick)
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("More")
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsRow(label = "Help center", onClick = onHelpCenterClick)
                SettingsDivider()
                SettingsRow(label = "About & support", onClick = onAboutSupportClick)
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Session")
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsRow(
                    label = "Log out",
                    icon = Icons.Filled.Logout,
                    onClick = onLogout
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Account status", color = AgriOrange)
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AgriOrange.copy(alpha = 0.08f))
                    .padding(16.dp)
            ) {
                Text(
                    "Deactivate my account",
                    fontWeight = FontWeight.Bold,
                    color = AgriOrange
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your profile and listings will be hidden from the marketplace and you'll be signed out. " +
                            "Your data stays safe — sign in again any time to reactivate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
                if (deactivateAccountError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        deactivateAccountError,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriRedAlert,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showDeactivateDialog = true },
                    enabled = !isDeactivatingAccount,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriOrange.copy(alpha = 0.18f),
                        contentColor = AgriOrange
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (isDeactivatingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AgriOrange,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Deactivating…", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Deactivate account", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeactivatingAccount) {
                    showDeactivateDialog = false
                    onDismissDeactivateError()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = !isDeactivatingAccount,
                dismissOnClickOutside = !isDeactivatingAccount
            ),
            title = { Text("Deactivate your account?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your profile and product listings will be hidden from the marketplace, and you'll be signed out on this device.\n\n" +
                            "Your orders, reviews, and account data stay safe. Sign in again any time to reactivate."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeactivatingAccount,
                    onClick = { onDeactivateAccountConfirmed() }
                ) {
                    Text("Deactivate", color = AgriOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeactivatingAccount,
                    onClick = {
                        showDeactivateDialog = false
                        onDismissDeactivateError()
                    }
                ) { Text("Cancel") }
            }
        )
    }
}

private fun normalizePhotoUrl(url: String?): String? {
    val trimmed = url?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.startsWith("//")) return "https:$trimmed"
    return if (trimmed.startsWith("/")) "$API_BASE$trimmed" else "$API_BASE/$trimmed"
}

private fun appendCacheBuster(url: String): String {
    val sep = if (url.contains("?")) "&" else "?"
    return "$url${sep}t=${System.currentTimeMillis()}"
}

@Composable
private fun ProfileHeader(
    user: UserDto?,
    photoUrl: String?,
    isUploadingPhoto: Boolean,
    onPhotoClick: () -> Unit,
    onHeaderClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AgriCardBg)
            .clickable(onClick = onHeaderClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(AgriMintLight)
                .clickable(enabled = !isUploadingPhoto, onClick = onPhotoClick),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = initialsFor(user?.fullName),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AgriGreenDark
                )
            }
            if (isUploadingPhoto) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                user?.fullName ?: "Not signed in",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AgriTextPrimary
            )
            Text(
                user?.email ?: "Sign in to manage your account",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isUploadingPhoto) "Uploading photo…"
                else "Tap to edit profile",
                style = MaterialTheme.typography.labelSmall,
                color = AgriTextTertiary
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AgriTextTertiary)
    }
}

private fun initialsFor(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "?"
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> "?"
    }
}

@Composable
private fun SectionTitle(text: String, color: Color = AgriTextPrimary) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun SettingsRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = AgriTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AgriTextTertiary)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = AgriBorderLight, thickness = 1.dp)
}