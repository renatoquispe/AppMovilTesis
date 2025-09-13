package com.tesis.appmovil

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {

    private val botUrl = "https://page.botpenguin.com/68bf90565500a0e58efff9db/68bf8dfa78b3c6392ed581fc" // <-- Pega tu URL aquí

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Versión programática (sin XML), igual a lo que te dio BotPenguin ---
        val myWebView = WebView(this)           // "activityContext" en su ejemplo = this
        setContentView(myWebView)

        with(myWebView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        // Para que los enlaces se abran dentro del WebView
        myWebView.webViewClient = WebViewClient()
        myWebView.webChromeClient = WebChromeClient()

        // Cargar el bot (¡con comillas!)
        myWebView.loadUrl(botUrl)
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(android.R.id.content).rootView as WebView
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
