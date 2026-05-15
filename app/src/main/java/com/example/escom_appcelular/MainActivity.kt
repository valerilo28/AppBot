package com.example.escom_appcelular

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== Canales de notificación =====
        NotificationHelper.createChannels(this)

        // ===== Drawer / Hamburguesa =====
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val btnHamburger = findViewById<ImageButton>(R.id.btnHamburger)
        val menuRecordatorios = findViewById<LinearLayout>(R.id.menuRecordatorios)
        val menuAcercaDe = findViewById<LinearLayout>(R.id.menuAcercaDe)

        btnHamburger.setOnClickListener {
            if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            } else {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
            }
        }

        menuRecordatorios.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            startActivity(Intent(this, RecordatoriosActivity::class.java))
        }

        menuAcercaDe.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            AlertDialog.Builder(this)
                .setTitle("Acerca de ESCOMobile")
                .setMessage("ESCOMobile v1.0\n\nAplicación informativa de la Escuela Superior de Cómputo del IPN.\n\nDesarrollada para facilitar el acceso a información académica, becas, servicio social y más.")
                .setPositiveButton("Cerrar", null)
                .show()
        }

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