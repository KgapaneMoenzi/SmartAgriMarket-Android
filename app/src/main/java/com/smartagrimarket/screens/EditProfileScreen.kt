package com.smartagrimarket.screens.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import coil.compose.AsyncImage
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.model.network.UploadRepository
import com.smartagrimarket.model.network.UserDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

private const val API_BASE = "https://smartagrimarket-backend.onrender.com"

/**
 * Edit name, phone, and photo. [user] seeds the initial field values.
 * [isSaving]/[errorMessage] reflect a real in-flight PATCH request —
 * this is not a fire-and-forget form.
 *
 * Photo uploads are handled inside this screen so the avatar always
 * reflects what the user just picked, regardless of how the parent
 * wires up [onChangePhotoClick] (kept for backward compatibility).
 */
@Composable
fun EditProfileScreen(
    user: UserDto?,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onChangePhotoClick: (() -> Unit)? = null,
    onSave: (fullName: String, phone: String?) -> Unit = { _, _ -> }
) {
    var fullName by remember(user?.email) { mutableStateOf(user?.fullName.orEmpty()) }
    var phone by remember(user?.email) { mutableStateOf(user?.phone.orEmpty()) }
    var showValidationError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Freshest URL we know about — seeded from the user, overwritten on upload.
    var localPhotoUrl by remember(user?.email) { mutableStateOf(user?.photoUrl) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

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
                        photoError = "Upload succeeded but server returned no URL."
                    } else {
                        // Cache-bust so Coil re-fetches even if the backend
                        // reuses the same filename.
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

    val launchPicker: () -> Unit = {
        if (!isUploadingPhoto) {
            if (onChangePhotoClick != null) onChangePhotoClick()
            else pickPhotoLauncher.launch("image/*")
        }
    }

    val isFormValid = fullName.isNotBlank()
    val effectivePhotoUrl = localPhotoUrl ?: normalizePhotoUrl(user?.photoUrl)

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onBack)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Back", color = AgriTextSecondary)
            }

            Spacer(Modifier.height(12.dp))

            Text("Edit profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(20.dp))

            // ── Avatar: shows the actual photo, tap to change ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(AgriYellow)
                        .clickable(enabled = !isUploadingPhoto, onClick = launchPicker),
                    contentAlignment = Alignment.Center
                ) {
                    if (!effectivePhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = effectivePhotoUrl,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Change photo",
                            tint = AgriTextPrimary,
                            modifier = Modifier.size(32.dp)
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
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isUploadingPhoto) "Uploading photo…" else "Change photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    modifier = Modifier.clickable(enabled = !isUploadingPhoto, onClick = launchPicker)
                )

                photoError?.let { msg ->
                    Spacer(Modifier.height(6.dp))
                    Text(msg, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Email intentionally not editable here — changing it should
            // go through a re-verification flow, out of scope for this screen.
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Email: ${user?.email ?: "—"} (contact support to change)",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            if (showValidationError && !isFormValid) {
                Spacer(Modifier.height(8.dp))
                Text("Full name can't be empty.", color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isFormValid) onSave(fullName.trim(), phone.trim().ifBlank { null })
                    else showValidationError = true
                },
                enabled = !isSaving && !isUploadingPhoto,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriYellow, contentColor = AgriTextPrimary),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AgriTextPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Saving…", fontWeight = FontWeight.Bold)
                } else {
                    Text("Save changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Turns a relative path from the backend into a fully-qualified URL. */
private fun normalizePhotoUrl(url: String?): String? {
    val trimmed = url?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.startsWith("//")) return "https:$trimmed"
    return if (trimmed.startsWith("/")) "$API_BASE$trimmed" else "$API_BASE/$trimmed"
}

/** Appends `?t=…` so Coil re-fetches when the backend reuses a filename. */
private fun appendCacheBuster(url: String): String {
    val sep = if (url.contains("?")) "&" else "?"
    return "$url${sep}t=${System.currentTimeMillis()}"
}