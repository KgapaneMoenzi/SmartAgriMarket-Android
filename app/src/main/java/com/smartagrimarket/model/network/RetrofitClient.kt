package com.smartagrimarket.model.network

import android.util.Log
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://smartagrimarket-backend.onrender.com/"
    private const val TAG = "RetrofitDiag"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Logs every socket-level event so we can see exactly where it fails.
    private val diagnosticListener = object : EventListener() {
        override fun callStart(call: okhttp3.Call) {
            Log.d(TAG, "→ START ${call.request().method} ${call.request().url}")
        }
        override fun callFailed(call: okhttp3.Call, ioe: IOException) {
            Log.e(TAG, "✗ FAILED ${call.request().url} — ${ioe.javaClass.simpleName}: ${ioe.message}", ioe)
        }
        override fun connectStart(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
            Log.d(TAG, "  connectStart → ${inetSocketAddress.hostName}:${inetSocketAddress.port}")
        }
        override fun connectFailed(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy, protocol: okhttp3.Protocol?, ioe: IOException) {
            Log.e(TAG, "  connectFailed → ${inetSocketAddress.hostName}:${inetSocketAddress.port} — ${ioe.javaClass.simpleName}: ${ioe.message}")
        }
        override fun secureConnectStart(call: okhttp3.Call) {
            Log.d(TAG, "  secureConnectStart (TLS handshake)")
        }
    }

    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var lastException: IOException? = null
        val backoffMs = longArrayOf(2000L, 4000L, 8000L)

        for (attempt in 0..backoffMs.size) {
            try {
                return@Interceptor chain.proceed(request)
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "  attempt ${attempt + 1} failed: ${e.javaClass.simpleName} — ${e.message}")
                val shouldRetry = e is SocketTimeoutException
                        || e is ConnectException
                        || e is UnknownHostException
                        || e.message?.contains("timeout", ignoreCase = true) == true

                if (!shouldRetry || attempt == backoffMs.size) throw e
                Thread.sleep(backoffMs[attempt])
            }
        }
        throw lastException ?: IOException("Unknown network error")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(retryInterceptor)
        .addInterceptor(loggingInterceptor)
        .eventListener(diagnosticListener)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        // 120s — large multipart uploads (verification docs) can take a while
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        // ── Force HTTP/1.1 ──
        // Render's edge (behind Cloudflare) resets HTTP/2 streams when
        // multipart bodies exceed ~10 MB. That surfaces as:
        //   "stream was reset: PROTOCOL_ERROR"
        // Cloudflare serves HTTP/1.1 uploads up to 100 MB without issue,
        // so falling back to 1.1 sidesteps the problem entirely.
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}