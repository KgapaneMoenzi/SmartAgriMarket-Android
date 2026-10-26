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

        NotificationChannels.ensureChannels(this)
        ensureNotificationPermission()

        if (TokenStore.token != null) {
            NotificationPollingScheduler.schedule(this)
        }

        readNotificationIntent(intent)

        // 🔥 Warm-up ping — wakes the Render service if it's cold.
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.api.healthCheck()
                } catch (_: Exception) {}
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
        setIntent(intent)
        readNotificationIntent(intent)
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun readNotificationIntent(intent: Intent?) {
        val linkType = intent?.getStringExtra(NotificationHelper.EXTRA_LINK_TYPE)
        val linkId = intent?.getStringExtra(NotificationHelper.EXTRA_LINK_ID)
        if (linkType != null || linkId != null) {
            NotificationDeepLink.pendingLinkType = linkType
            NotificationDeepLink.pendingLinkId = linkId
        }
    }
}

@Composable
private fun AppEntry() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var navigationTrigger by remember { mutableStateOf(0) }
    var stage by remember { mutableStateOf("splash") }

    key(navigationTrigger) {
        when (stage) {
            "splash" -> BrandedSplashScreen(
                onFinished = {
                    val done = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
                    stage = if (done) "app" else "onboarding"
                    navigationTrigger++
                }
            )

            "onboarding" -> OnboardingScreen(
                onFinished = {
                    prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                    stage = "app"
                    navigationTrigger++
                }
            )

            else -> {
                AppRoot() // This is where your AppRoot lives!
            }
        }
    }
}