package com.smartagrimarket.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Loads PayFast's hosted payment page inside the app via a self-submitting
 * HTML form (PayFast requires a POST, not a plain URL). Watches every page
 * load for the app's own return_url/cancel_url — those never actually need
 * to render, they just signal that PayFast finished and redirected back.
 */
@Composable
fun PayFastWebViewScreen(
    paymentUrl: String,
    fields: Map<String, String>,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val formHtml = buildAutoSubmitFormHtml(paymentUrl, fields)

    Scaffold { padding ->
        AndroidWebView(
            html = formHtml,
            onUrlChanged = { url ->
                when {
                    url.contains("/api/payments/return") -> onSuccess()
                    url.contains("/api/payments/cancel") -> onCancel()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AndroidWebView(
    html: String,
    onUrlChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true // required for the auto-submit form below
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { onUrlChanged(it) }
                    }
                }
                loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            }
        }
    )
}

private fun buildAutoSubmitFormHtml(paymentUrl: String, fields: Map<String, String>): String {
    val inputs = fields.entries.joinToString("\n") { (key, value) ->
        """<input type="hidden" name="${escapeHtml(key)}" value="${escapeHtml(value)}">"""
    }
    return """
        <html>
        <body onload="document.forms[0].submit()">
          <form method="post" action="${escapeHtml(paymentUrl)}">
            $inputs
          </form>
          <p style="font-family: sans-serif; text-align: center; margin-top: 40px;">Redirecting to PayFast...</p>
        </body>
        </html>
    """.trimIndent()
}

private fun escapeHtml(text: String): String {
    return text.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
}