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

        // ===== WebView =====
        val webView = findViewById<WebView>(R.id.webView)

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

        //  Zoom (sin botones)
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        //  Ajuste pantalla
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        //  Permisos internos
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true

        // ===== PDF =====
        val pdfUrl = "https://www.ipn.mx/assets/files/website/docs/inicio/calendarioipn-escolarizada.pdf"
        val viewer = "https://drive.google.com/viewerng/viewer?embedded=true&url=$pdfUrl"

        webView.loadUrl(viewer)

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

        val btnMapa = findViewById<Button>(R.id.btnMapa)

        btnMapa.setOnClickListener {
            startActivity(Intent(this, Mapa::class.java))
        }
    }

    private fun abrirLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // 🔙 Botón atrás dentro del WebView
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}