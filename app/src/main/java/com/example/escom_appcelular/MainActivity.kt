package com.example.escom_appcelular

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webFacebook: WebView
    private lateinit var webInstagram: WebView

    private val pdfUrl    = "https://www.ipn.mx/assets/files/website/docs/inicio/calendarioipn-escolarizada.pdf"
    private val pdfViewer = "https://docs.google.com/viewer?url=$pdfUrl"

    private val executor    = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createChannels(this)

        // ===== Drawer =====
        val drawerLayout      = findViewById<DrawerLayout>(R.id.drawerLayout)
        val btnHamburger      = findViewById<ImageButton>(R.id.btnHamburger)
        val menuRecordatorios = findViewById<LinearLayout>(R.id.menuRecordatorios)
        val menuAcercaDe      = findViewById<LinearLayout>(R.id.menuAcercaDe)

        btnHamburger.setOnClickListener {
            if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START))
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            else
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
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

        // ===== Preview PDF con PdfRenderer =====
        val imgPreview  = findViewById<ImageView>(R.id.imgCalendarioPreview)
        val placeholder = findViewById<android.view.View>(R.id.layoutCalendarioPlaceholder)
        cargarPreviewPdf(imgPreview, placeholder)

        // ===== Card y botón "Ver completo" abren el mismo dialog =====
        findViewById<CardView>(R.id.cardCalendario).setOnClickListener { mostrarCalendarioDialog() }
        findViewById<LinearLayout>(R.id.fab).setOnClickListener { mostrarCalendarioDialog() }

        // ===== WebView Facebook =====
        val placeholderFacebook = findViewById<android.view.View>(R.id.placeholderFacebook)
        webFacebook = findViewById(R.id.webFacebook)
        setupWebView(webFacebook)
        webFacebook.webChromeClient = WebChromeClient()
        webFacebook.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                try { placeholderFacebook.visibility = android.view.View.GONE } catch (_: Exception) {}
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                view?.loadDataWithBaseURL(null, sinConexionHtml("Facebook"), "text/html", "UTF-8", null)
                try { placeholderFacebook.visibility = android.view.View.GONE } catch (_: Exception) {}
            }
        }
        webFacebook.loadDataWithBaseURL(
            "https://www.facebook.com",
            """
            <html>
              <head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
              <body style="margin:0;padding:0;">
                <div id="fb-root"></div>
                <script async defer crossorigin="anonymous"
                  src="https://connect.facebook.net/es_LA/sdk.js#xfbml=1&version=v18.0"></script>
                <div class="fb-page"
                     data-href="https://www.facebook.com/escomipnmx"
                     data-tabs="timeline" data-width="500" data-height="500"
                     data-small-header="false" data-adapt-container-width="true"
                     data-hide-cover="false" data-show-facepile="true"></div>
              </body>
            </html>
            """.trimIndent(),
            "text/html", "UTF-8", null
        )

        // ===== WebView Instagram =====
        val placeholderInstagram = findViewById<android.view.View>(R.id.placeholderInstagram)
        webInstagram = findViewById(R.id.webInstagram)
        setupWebView(webInstagram)
        webInstagram.webChromeClient = WebChromeClient()
        webInstagram.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                try { placeholderInstagram.visibility = android.view.View.GONE } catch (_: Exception) {}
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                view?.loadDataWithBaseURL(null, sinConexionHtml("Instagram"), "text/html", "UTF-8", null)
                try { placeholderInstagram.visibility = android.view.View.GONE } catch (_: Exception) {}
            }
        }
        webInstagram.loadUrl("https://www.instagram.com/reels/DOAeV9bjwou/")

        // ===== Botones redes sociales =====
        findViewById<LinearLayout>(R.id.btnFacebook).setOnClickListener { abrirLink("https://www.facebook.com/groups/164168577040524") }
        findViewById<LinearLayout>(R.id.btnWhats).setOnClickListener    { abrirLink("https://chat.whatsapp.com/CDuJz9jibVb1sIvOSVeEQb") }
        findViewById<LinearLayout>(R.id.btnWeb).setOnClickListener      { abrirLink("https://www.escom.ipn.mx/") }

        // ===== FAB Chatbot =====
        findViewById<FloatingActionButton>(R.id.fabChat).setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        // ===== Bottom nav =====
        findViewById<Button>(R.id.btnInicio).setOnClickListener {
            findViewById<android.widget.ScrollView>(R.id.scrollMain).smoothScrollTo(0, 0)
        }
        findViewById<Button>(R.id.btnConsultar).setOnClickListener {
            startActivity(Intent(this, ConsultarActivity::class.java))
        }
        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            startActivity(Intent(this, Mapa::class.java))
        }

        // ===== Botón atrás =====
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    webFacebook.canGoBack()  -> webFacebook.goBack()
                    webInstagram.canGoBack() -> webInstagram.goBack()
                    else -> { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
                }
            }
        })
    }

    // ===== Descarga el PDF y renderiza la página 1 como preview =====
    private fun cargarPreviewPdf(imageView: ImageView, placeholder: android.view.View) {
        executor.execute {
            try {
                val cacheFile = File(cacheDir, "cal_preview.pdf")

                if (!cacheFile.exists() || cacheFile.length() < 1000L) {
                    val conn = URL(pdfUrl).openConnection() as HttpURLConnection
                    conn.connectTimeout = 15_000
                    conn.readTimeout    = 30_000
                    conn.instanceFollowRedirects = true
                    conn.connect()
                    if (conn.responseCode != HttpURLConnection.HTTP_OK) return@execute
                    conn.inputStream.use { input ->
                        FileOutputStream(cacheFile).use { out -> input.copyTo(out) }
                    }
                    conn.disconnect()
                }

                val pfd      = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val page     = renderer.openPage(0)

                val maxW  = 800
                val ratio = page.height.toFloat() / page.width.toFloat()
                val bmpW  = maxW
                val bmpH  = (maxW * ratio).toInt().coerceAtMost(1100)

                val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                mainHandler.post {
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility  = android.view.View.VISIBLE
                    placeholder.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post { placeholder.visibility = android.view.View.GONE }
            }
        }
    }

    // ===== Abre el PDF en pantalla completa dentro de la app =====
    @SuppressLint("SetJavaScriptEnabled")
    private fun mostrarCalendarioDialog() {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF003972.toInt())
        }

        // Barra superior
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            android.view.Gravity.CENTER_VERTICAL.also { gravity = it }
            setPadding(16.dp, 8.dp, 16.dp, 8.dp)
        }
        val titulo = android.widget.TextView(this).apply {
            text = "Calendario Escolar IPN"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btnCerrar = Button(this).apply {
            text = "✕ Cerrar"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(0x33FFFFFF.toInt())
            textSize = 13f
            setOnClickListener { dialog.dismiss() }
        }
        topBar.addView(titulo)
        topBar.addView(btnCerrar)

        // WebView sin WebViewClient personalizado para no interferir con Google Docs Viewer
        val webCalendario = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            settings.apply {
                javaScriptEnabled    = true
                domStorageEnabled    = true
                setSupportZoom(true)
                builtInZoomControls  = true
                displayZoomControls  = false
                loadWithOverviewMode = true
                useWideViewPort      = true
            }
            webChromeClient = WebChromeClient()
            loadUrl(pdfViewer)
        }

        // Barra inferior
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(16.dp, 8.dp, 16.dp, 8.dp)
            setBackgroundColor(0xFF002255.toInt())
        }
        val btnExterno = Button(this).apply {
            text = "🔗 Abrir en navegador"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(0xFF2196F3.toInt())
            setOnClickListener { abrirLink(pdfUrl) }
        }
        bottomBar.addView(btnExterno)

        root.addView(topBar)
        root.addView(webCalendario)
        root.addView(bottomBar)
        dialog.setContentView(root)

        // Botón atrás navega dentro del WebView si puede
        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && webCalendario.canGoBack()) {
                webCalendario.goBack(); true
            } else false
        }
        dialog.show()
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    // ===== Configuración común de WebView (para redes sociales) =====
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled        = true
            domStorageEnabled        = true
            setSupportZoom(true)
            builtInZoomControls      = true
            displayZoomControls      = false
            loadWithOverviewMode     = true
            useWideViewPort          = true
            allowFileAccess          = true
            allowContentAccess       = true
            loadsImagesAutomatically = true
            @Suppress("DEPRECATION")
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }

    private fun sinConexionHtml(red: String) = """
        <!DOCTYPE html><html>
        <head><meta name="viewport" content="width=device-width,initial-scale=1">
        <style>
          body{margin:0;display:flex;flex-direction:column;align-items:center;
               justify-content:center;height:100vh;background:#f8f9fa;
               font-family:sans-serif;text-align:center;padding:24px;box-sizing:border-box}
          .icon{font-size:56px;margin-bottom:16px}
          h2{color:#333;font-size:18px;margin:0 0 8px}
          p{color:#888;font-size:13px;margin:0 0 24px}
          button{background:#003972;color:#fff;border:none;border-radius:24px;
                 padding:12px 28px;font-size:14px;cursor:pointer}
        </style></head>
        <body>
          <div class="icon">📡</div>
          <h2>Sin conexión</h2>
          <p>No se pudo cargar $red.<br>Revisa tu conexión a internet.</p>
          <button onclick="location.reload()">Reintentar</button>
        </body></html>
    """.trimIndent()

    private fun abrirLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}