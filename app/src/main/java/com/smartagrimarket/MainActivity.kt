package com.smartagrimarket

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.smartagrimarket.model.network.NotificationChannels
import com.smartagrimarket.model.network.NotificationHelper
import com.smartagrimarket.model.network.NotificationPollingScheduler
import com.smartagrimarket.model.network.RetrofitClient
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.screens.BrandedSplashScreen
import com.smartagrimarket.screens.OnboardingScreen
import com.smartagrimarket.ui.theme.SmartAgriMarketTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "agrimarket_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val TAG = "AppEntry"

/**
 * Top-level deep-link state. When the app is launched (or resumed) by
 * tapping a system notification, we store the payload here. AppRoot can
 * observe this and route to the right screen.
 *
 * Reset to null after handling.
 */
object NotificationDeepLink {
    var pendingLinkType by mutableStateOf<String?>(null)
    var pendingLinkId by mutableStateOf<String?>(null)

    fun consume(): Pair<String?, String?>? {
        val t = pendingLinkType
        val i = pendingLinkId
        if (t == null && i == null) return null
        pendingLinkType = null
        pendingLinkId = null
        return t to i
    }
}

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "POST_NOTIFICATIONS granted=$granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStore.init(applicationContext)

        // ── Notifications setup ──
        // 1. Create the channels (no-op on Android 7 and below)
        NotificationChannels.ensureChannels(this)

        // 2. Request POST_NOTIFICATIONS on Android 13+
        ensureNotificationPermission()

        // 3. Schedule the polling worker if the user is signed in
        if (TokenStore.token != null) {
            NotificationPollingScheduler.schedule(this)
        }

        // 4. Read deep-link extras if the app was launched from a notification tap
        readNotificationIntent(intent)

        // 🔥 Warm-up ping — wakes the Render service if it's cold.
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.api.healthCheck()
                } catch (_: Exception) {
                    // Silent — this is just a warm-up.
                }
            }
        }

        setContent {
            SmartAgriMarketTheme {
                AppEntry()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // App was already alive and the user tapped a notification.
        setIntent(intent)
        readNotificationIntent(intent)
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Below Android 13, no runtime permission required.
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun readNotificationIntent(intent: Intent?) {
        val linkType = intent?.getStringExtra(NotificationHelper.EXTRA_LINK_TYPE)
        val linkId = intent?.getStringExtra(NotificationHelper.EXTRA_LINK_ID)
        if (linkType != null || linkId != null) {
            Log.d(TAG, "Deep-link from notification: type=$linkType id=$linkId")
            NotificationDeepLink.pendingLinkType = linkType
            NotificationDeepLink.pendingLinkId = linkId
        }
    }
}

/**
 * Three-stage entry flow:
 *   1. Splash (branded, ~1.5s)
 *   2. Onboarding (only on first install)
 *   3. AppRoot (the actual app)
 */
@Composable
private fun AppEntry() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Force a clean recomposition whenever we change stages
    var navigationTrigger by remember { mutableStateOf(0) }
    var stage by remember { mutableStateOf("splash") }

    Log.d(TAG, "AppEntry composition — stage=$stage, trigger=$navigationTrigger")

    key(navigationTrigger) {
        when (stage) {
            "splash" -> BrandedSplashScreen(
                onFinished = {
                    Log.d(TAG, "Splash finished — deciding next stage")
                    val done = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
                    stage = if (done) "app" else "onboarding"
                    navigationTrigger++
                }
            )

            "onboarding" -> OnboardingScreen(
                onFinished = {
                    Log.d(TAG, "Onboarding finished — moving to AppRoot")
                    prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                    stage = "app"
                    navigationTrigger++
                }
            )

            else -> {
                Log.d(TAG, "Rendering AppRoot")
                AppRoot()
            }
        }
    }
}