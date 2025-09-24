package com.tesis.appmovil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * ChatActivity: WebView-based bot screen (fallback).
 * - Seguro: guarda la instancia WebView y evita class cast.
 * - No asigna null a propiedades que Kotlin marca non-null.
 * - Puedes pasar URL por Intent: ChatActivity.start(context, url)
 */
class ChatActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_URL = "extra_bot_url"
        private const val DEFAULT_BOT_URL =
            "https://page.botpenguin.com/68bf90565500a0e58efff9db/68bf8dfa78b3c6392ed581fc"

        fun start(context: Context, url: String? = null) {
            val i = Intent(context, ChatActivity::class.java)
            if (!url.isNullOrBlank()) i.putExtra(EXTRA_URL, url)
            context.startActivity(i)
        }
    }

    private lateinit var webView: WebView
    private var loadedUrl: String = DEFAULT_BOT_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadedUrl = intent?.getStringExtra(EXTRA_URL) ?: DEFAULT_BOT_URL

        webView = WebView(this)
        setContentView(webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        try {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } catch (_: Throwable) { }

        // Usa instancias "vacías" para evitar asignar null
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.loadUrl(loadedUrl)
    }

    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::webView.isInitialized) {
            try { webView.onResume() } catch (_: Throwable) {}
        }
    }

    override fun onPause() {
        if (this::webView.isInitialized) {
            try { webView.onPause() } catch (_: Throwable) {}
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (this::webView.isInitialized) {
            try {
                // limpieza segura: no asignamos null, usamos clientes por defecto o new instances
                webView.loadUrl("about:blank")
                webView.stopLoading()

                // reemplazamos por clientes por defecto (evita asignar null)
                webView.webChromeClient = WebChromeClient()
                webView.webViewClient = WebViewClient()

                webView.removeAllViews()
                webView.destroy()
            } catch (_: Throwable) {}
        }
        super.onDestroy()
    }
}
