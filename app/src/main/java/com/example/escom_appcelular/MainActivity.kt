package com.example.escom_appcelular

import android.annotation.SuppressLint
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== PDF URL =====
        val pdfUrl = "https://www.ipn.mx/assets/files/website/docs/inicio/calendarioipn-escolarizada.pdf"

        // ===== WebView Facebook =====
        val webFacebook = findViewById<WebView>(R.id.webFacebook)
        setupWebView(webFacebook)
        webFacebook.loadUrl("https://www.facebook.com/ESCOMoficial")

        // ===== WebView Instagram =====
        val webInstagram = findViewById<WebView>(R.id.webInstagram)
        setupWebView(webInstagram)
        webInstagram.loadUrl("https://www.instagram.com/escom_ipn/")

        // ===== Botones redes =====
        val btnFacebook = findViewById<LinearLayout>(R.id.btnFacebook)
        val btnWhats = findViewById<LinearLayout>(R.id.btnWhats)
        val btnWeb = findViewById<LinearLayout>(R.id.btnWeb)

        btnFacebook.setOnClickListener {
            abrirLink("https://www.facebook.com/")
        }

        btnWhats.setOnClickListener {
            abrirLink("https://wa.me/")
        }

        btnWeb.setOnClickListener {
            abrirLink("https://www.ipn.mx")
        }

        // ===== FAB =====
        val fabChat = findViewById<FloatingActionButton>(R.id.fabChat)
        fabChat.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }
        val fabVerPDF = findViewById<LinearLayout>(R.id.fab)
        fabVerPDF.setOnClickListener { abrirLink(pdfUrl) }

    // El fab del PDF ahora es ExtendedFloatingActionButton

        val btnConsultar = findViewById<Button>(R.id.btnConsultar)
        btnConsultar.setOnClickListener {
            startActivity(Intent(this, ConsultarActivity::class.java))
        }

        val btnMapa = findViewById<Button>(R.id.btnMapa)
        btnMapa.setOnClickListener {
            startActivity(Intent(this, Mapa::class.java))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true
    }

    private fun abrirLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // 🔙 Botón atrás dentro del WebView
    override fun onBackPressed() {
        val webFacebook = findViewById<WebView>(R.id.webFacebook)
        val webInstagram = findViewById<WebView>(R.id.webInstagram)
        when {
            webFacebook.canGoBack() -> webFacebook.goBack()
            webInstagram.canGoBack() -> webInstagram.goBack()
            else -> super.onBackPressed()
        }
    }
}