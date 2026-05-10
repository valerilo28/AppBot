package com.example.escom_appcelular

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val listaPoligonos       = mutableListOf<Polygon>()
    private val listaPoligonosNivel0 = mutableListOf<Polygon>()
    private val markersByCodigo      = mutableMapOf<String, Marker>()
    private var marcadorActual: Marker? = null
    private var nivelActual = 1

    private val tiposSeleccionados = mutableSetOf(
        "Salon", "Sala", "Area Comun", "Area Administrativa",
        "Cubiculo", "Laboratorio", "Club"
    )
    private var profesorSeleccionado: String? = null

    private var buscarProfesorAlTerminar = false

    // Token para invalidar respuestas obsoletas de búsquedas anteriores
    private var tokenBusqueda = 0

    private val tiposDisponibles = listOf(
        "Salon", "Sala", "Area Comun", "Area Administrativa",
        "Cubiculo", "Laboratorio", "Club"
    )
    // checkboxMap solo se usa para referencia; la fuente de verdad es tiposSeleccionados
    private val checkboxMap = mutableMapOf<String, CheckBox>()

    // ─────────────────────────────────────────────────────────────
    // onCreate
    // ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa)

        val btnPiso1 = findViewById<Button>(R.id.btnPiso1)
        val btnPiso2 = findViewById<Button>(R.id.btnPiso2)
        val btnPiso3 = findViewById<Button>(R.id.btnPiso3)

        btnPiso1.setOnClickListener { cambiarNivel(1) }
        btnPiso2.setOnClickListener { cambiarNivel(2) }
        btnPiso3.setOnClickListener { cambiarNivel(3) }

        actualizarBotonesUI(nivelActual)

        // ── Filtros con checkboxes ────────────────────────────────
        val btnFiltros       = findViewById<Button>(R.id.btnFiltros)
        val panelFiltros     = findViewById<ScrollView>(R.id.panelFiltros)
        val layoutCheckboxes = findViewById<LinearLayout>(R.id.layoutCheckboxes)

        // Reconstruir checkboxes cada vez que se abre el panel,
        // garantizando que reflejan el estado actual de tiposSeleccionados.
        btnFiltros.setOnClickListener {
            if (panelFiltros.visibility == View.VISIBLE) {
                panelFiltros.visibility = View.INVISIBLE
                btnFiltros.text = "Filtros ▾"
            } else {
                construirCheckboxes(layoutCheckboxes)   // siempre reconstruir
                panelFiltros.visibility = View.VISIBLE
                btnFiltros.text = "Filtros ▴"
            }
        }

        // ── Búsqueda de profesor ──────────────────────────────────
        val editTextProfesor    = findViewById<EditText>(R.id.editTextProfesor)
        val btnBuscarProfesor   = findViewById<ImageButton>(R.id.btnBuscarProfesor)
        val listViewSugerencias = findViewById<ListView>(R.id.listViewSugerencias)

        editTextProfesor.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.isNotEmpty()) cargarSugerencias(texto, listViewSugerencias, editTextProfesor)
                else listViewSugerencias.visibility = View.GONE
            }
        })

        btnBuscarProfesor.setOnClickListener {
            val texto = editTextProfesor.text.toString().trim()
            if (texto.isNotEmpty()) seleccionarProfesor(texto, editTextProfesor, listViewSugerencias)
            else cargarSugerencias("", listViewSugerencias, editTextProfesor)
        }

        editTextProfesor.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val texto = editTextProfesor.text.toString().trim()
                if (texto.isNotEmpty()) seleccionarProfesor(texto, editTextProfesor, listViewSugerencias)
                true
            } else false
        }

        // ── Navegación inferior ───────────────────────────────────
        findViewById<Button>(R.id.btnInicio).setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }

        // ── FAB ───────────────────────────────────────────────────
        findViewById<FloatingActionButton>(R.id.fabChat).setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        // ── Mapa ──────────────────────────────────────────────────
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // ─────────────────────────────────────────────────────────────
    // Cambiar nivel (botones Piso 1/2/3)
    // ─────────────────────────────────────────────────────────────

    private fun cambiarNivel(nivel: Int) {
        nivelActual = nivel
        actualizarBotonesUI(nivelActual)

        // Resetear profesor
        if (profesorSeleccionado != null) {
            profesorSeleccionado = null
            buscarProfesorAlTerminar = false
            tokenBusqueda++
            limpiarMarcadores()
            findViewById<EditText>(R.id.editTextProfesor).setText("")
        }

        // Seleccionar todos los tipos al cambiar de piso
        tiposSeleccionados.clear()
        tiposSeleccionados.addAll(tiposDisponibles)
        // Actualizar checkboxes si el panel está abierto
        checkboxMap.forEach { (tipo, cb) ->
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = true
            setCheckboxColor(cb, true, tipo)
            cb.setOnCheckedChangeListener { _, isChecked ->
                setCheckboxColor(cb, isChecked, tipo)
                if (isChecked) tiposSeleccionados.add(tipo) else tiposSeleccionados.remove(tipo)
                profesorSeleccionado = null
                buscarProfesorAlTerminar = false
                limpiarMarcadores()
                cargarPoligonos()
            }
        }

        cargarPoligonos()
    }

    // ─────────────────────────────────────────────────────────────
    // Checkboxes de tipo
    // FIX PRINCIPAL: el listener se asigna DESPUÉS de setChecked,
    // evitando que se dispare durante la construcción del panel.
    // tiposSeleccionados es siempre la fuente de verdad.
    // ─────────────────────────────────────────────────────────────

    private fun construirCheckboxes(container: LinearLayout) {
        container.removeAllViews()
        checkboxMap.clear()

        tiposDisponibles.forEach { tipo ->
            val cb = CheckBox(this)
            cb.text          = tipo
            cb.textSize      = 13f
            cb.setTypeface(null, Typeface.BOLD)
            cb.isSaveEnabled = false

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }
            cb.layoutParams = lp

            // ── 1. Aplicar color y estado visual SIN listener activo ──
            val estaSeleccionado = tiposSeleccionados.contains(tipo)
            setCheckboxColor(cb, estaSeleccionado, tipo)

            // Nullear el listener primero para que setChecked no dispare nada
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = estaSeleccionado

            // ── 2. Asignar el listener DESPUÉS de setChecked ──────────
            cb.setOnCheckedChangeListener { _, isChecked ->
                setCheckboxColor(cb, isChecked, tipo)
                if (isChecked) tiposSeleccionados.add(tipo) else tiposSeleccionados.remove(tipo)
                profesorSeleccionado = null
                buscarProfesorAlTerminar = false
                limpiarMarcadores()
                cargarPoligonos()
            }

            checkboxMap[tipo] = cb
            container.addView(cb)
        }
    }

    private fun setCheckboxColor(cb: CheckBox, checked: Boolean, tipo: String) {
        val colorActivo = getColorByTipo(tipo)
        val colorGris   = Color.parseColor("#9E9E9E")

        cb.setTextColor(if (checked) colorActivo else colorGris)

        val states = arrayOf(
            intArrayOf( android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(
            if (checked) colorActivo else colorGris,
            colorGris
        )
        cb.buttonTintList = android.content.res.ColorStateList(states, colors)
    }

    /**
     * Limpia tiposSeleccionados y desmarca checkboxes si el panel está abierto.
     * No llama a cargarPoligonos (responsabilidad del llamador).
     */
    private fun limpiarCheckboxesSilenciosamente() {
        tiposSeleccionados.clear()
        // Actualizar la UI de los checkboxes si ya están en pantalla
        checkboxMap.forEach { (tipo, cb) ->
            // Quitar listener temporalmente para no disparar lógica de negocio
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = false
            setCheckboxColor(cb, false, tipo)
            // Restaurar listener
            cb.setOnCheckedChangeListener { _, isChecked ->
                setCheckboxColor(cb, isChecked, tipo)
                if (isChecked) tiposSeleccionados.add(tipo) else tiposSeleccionados.remove(tipo)
                profesorSeleccionado = null
                buscarProfesorAlTerminar = false
                limpiarMarcadores()
                cargarPoligonos()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Seleccionar profesor
    // ─────────────────────────────────────────────────────────────

    private fun seleccionarProfesor(nombre: String, editText: EditText, listView: ListView) {
        limpiarMarcadores()
        limpiarCheckboxesSilenciosamente()

        profesorSeleccionado = nombre
        listView.visibility  = View.GONE

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)

        tokenBusqueda++
        val miToken = tokenBusqueda

        buscarSalonDelProfesor(nombre, miToken)
    }

    // ─────────────────────────────────────────────────────────────
    // FLUJO DE BÚSQUEDA DE PROFESOR
    // ─────────────────────────────────────────────────────────────

    private fun buscarSalonDelProfesor(profesor: String, token: Int) {
        val url = "https://api-escomapp.onrender.com/ultimo_salon_profesor?profesor=${
            URLEncoder.encode(profesor, "utf-8")
        }"
        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                if (token != tokenBusqueda) return@JsonObjectRequest

                val salon = response.optString("salon", "")
                if (salon.isEmpty()) {
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("No encontrado")
                            .setMessage("No se encontró salón para el profesor.")
                            .setPositiveButton("OK", null).show()
                    }
                    return@JsonObjectRequest
                }

                val nivelApi   = response.optInt("nivel", -1)
                val nivelSalon = if (nivelApi in 1..3) nivelApi else nivelDesdeSalon(salon)

                runOnUiThread {
                    if (nivelSalon != -1 && nivelSalon != nivelActual) {
                        Log.d("BUSQUEDA", "Salón $salon → Nivel $nivelSalon, cambiando desde $nivelActual")
                        nivelActual = nivelSalon
                        actualizarBotonesUI(nivelActual)
                    }
                    cargarPoligonosYBuscar(salon, token)
                }
            },
            { error ->
                Log.e("API", "Error al buscar profesor: ${error.message}")
            }
        )
        queue.add(request)
    }

    private fun cargarPoligonosYBuscar(salon: String, token: Int) {
        listaPoligonos.forEach { it.remove() }
        listaPoligonos.clear()
        limpiarMarcadores()

        val url = "https://api-escomapp.onrender.com/Nivel$nivelActual"
        Log.d("API", "cargarPoligonosYBuscar: $url  →  buscando $salon (token=$token)")
        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                if (token != tokenBusqueda) {
                    Log.d("API", "Respuesta obsoleta ignorada (token=$token, actual=$tokenBusqueda)")
                    return@JsonObjectRequest
                }

                val poligonosNuevos  = mutableListOf<Polygon>()
                val boundsBuilder    = LatLngBounds.Builder()
                var anyAdded         = false

                if (response.has("features")) {
                    val features = response.getJSONArray("features")
                    for (i in 0 until features.length()) {
                        val feature  = features.getJSONObject(i)
                        val props    = feature.optJSONObject("properties") ?: JSONObject()
                        val codigo   = props.optString("codigo", "")
                        val tipo     = props.optString("tipo", "")
                        val geometry = feature.getJSONObject("geometry")
                        val gtype    = geometry.getString("type")

                        if (gtype == "Polygon") {
                            val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                            val poly   = crearPoligonoDesdeCoords(coords, tipo, codigo, boundsBuilder)
                            if (poly != null) { poligonosNuevos.add(poly); anyAdded = true }
                        } else if (gtype == "MultiPolygon") {
                            val mp = geometry.getJSONArray("coordinates")
                            for (p in 0 until mp.length()) {
                                val coords = mp.getJSONArray(p).getJSONArray(0)
                                val poly   = crearPoligonoDesdeCoords(coords, tipo, codigo, boundsBuilder)
                                if (poly != null) { poligonosNuevos.add(poly); anyAdded = true }
                            }
                        }
                    }
                }

                runOnUiThread {
                    listaPoligonos.addAll(poligonosNuevos)
                    try {
                        if (anyAdded) map.moveCamera(
                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                        )
                    } catch (_: Exception) {}
                    buscarSalonEnPoligonos(salon)
                }
            },
            { error ->
                Log.e("API", "Error cargarPoligonosYBuscar: ${error.message}")
            }
        )
        queue.add(request)
    }

    // ─────────────────────────────────────────────────────────────
    // Sugerencias inline
    // ─────────────────────────────────────────────────────────────

    private fun cargarSugerencias(filtro: String, listView: ListView, editText: EditText) {
        val url   = "https://api-escomapp.onrender.com/profesores?nivel=$nivelActual"
        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val array = response.optJSONArray("profesores") ?: JSONArray()
                val lista = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val nombre = array.getString(i)
                    if (filtro.isEmpty() || nombre.contains(filtro, ignoreCase = true)) lista.add(nombre)
                }
                if (lista.isEmpty()) { listView.visibility = View.GONE; return@JsonObjectRequest }

                listView.adapter    = ArrayAdapter(this, R.layout.item_sugerencia, lista)
                listView.visibility = View.VISIBLE
                listView.setOnItemClickListener { _, _, position, _ ->
                    val nombre = lista[position]
                    editText.setText("")
                    editText.clearFocus()
                    seleccionarProfesor(nombre, editText, listView)
                }
            },
            { error ->
                Log.e("API", "Error sugerencias: ${error.message}")
                listView.visibility = View.GONE
            }
        )
        queue.add(request)
    }

    // ─────────────────────────────────────────────────────────────
    // Mapa listo
    // ─────────────────────────────────────────────────────────────

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val escomBounds = LatLngBounds(
            LatLng(19.5041, -99.1478),
            LatLng(19.5058, -99.1458)
        )
        map.setLatLngBoundsForCameraTarget(escomBounds)
        map.setMinZoomPreference(18f)
        map.setMaxZoomPreference(22f)
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(escomBounds, 40))

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? {
                val view    = layoutInflater.inflate(R.layout.custom_info_window, null)
                val title   = view.findViewById<TextView>(R.id.title)
                val snippet = view.findViewById<TextView>(R.id.snippet)
                title.text   = marker.title
                snippet.text = marker.snippet
                return view
            }
            override fun getInfoWindow(marker: Marker): View? = null
        })

        try {
            val success = map.setMapStyle(
                com.google.android.gms.maps.model.MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.map_style_sin_poi)
            )
            if (!success) Log.e("MapStyle", "Error al aplicar estilo")
        } catch (e: Exception) {
            Log.e("MapStyle", "Error cargando estilo", e)
        }

        map.setOnPolygonClickListener { polygon -> onPolygonClicked(polygon) }

        cargarNivel0()
        cargarPoligonos()
    }

    // ─────────────────────────────────────────────────────────────
    // Nivel 0 (fondo gris)
    // ─────────────────────────────────────────────────────────────

    private fun cargarNivel0() {
        listaPoligonosNivel0.forEach { it.remove() }
        listaPoligonosNivel0.clear()
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, "https://api-escomapp.onrender.com/Nivel0", null,
            { response -> procesarNivel0(response) },
            { error -> Log.e("API", "Error Nivel0: ${error.message}") }
        )
        queue.add(request)
    }

    private fun procesarNivel0(fc: JSONObject) {
        if (!fc.has("features")) return
        val features = fc.getJSONArray("features")
        for (i in 0 until features.length()) {
            val feature  = features.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val gtype    = geometry.getString("type")
            if (gtype == "Polygon") {
                crearPoligonoFondo(geometry.getJSONArray("coordinates").getJSONArray(0))
                    ?.let { listaPoligonosNivel0.add(it) }
            } else if (gtype == "MultiPolygon") {
                val mp = geometry.getJSONArray("coordinates")
                for (p in 0 until mp.length()) {
                    crearPoligonoFondo(mp.getJSONArray(p).getJSONArray(0))
                        ?.let { listaPoligonosNivel0.add(it) }
                }
            }
        }
    }

    private fun crearPoligonoFondo(coords: JSONArray): Polygon? {
        if (coords.length() == 0) return null
        val opciones = PolygonOptions()
            .strokeWidth(2f)
            .strokeColor(Color.parseColor("#888888"))
            .fillColor(Color.argb(60, 180, 180, 180))
            .clickable(false)
            .zIndex(0f)
        for (j in 0 until coords.length()) {
            val p = coords.getJSONArray(j)
            opciones.add(LatLng(p.getDouble(1), p.getDouble(0)))
        }
        return map.addPolygon(opciones)
    }

    // ─────────────────────────────────────────────────────────────
    // Carga normal de polígonos (filtros de tipo / cambio de piso)
    // ─────────────────────────────────────────────────────────────

    private fun cargarPoligonos() {
        listaPoligonos.forEach { it.remove() }
        listaPoligonos.clear()
        limpiarMarcadores()

        val base = "https://api-escomapp.onrender.com/Nivel$nivelActual"

        when {
            tiposSeleccionados.isEmpty() -> { /* sin filtros = no mostrar nada */ }
            else -> fetchMultiTipo(base, tiposSeleccionados.toList())
        }
    }

    private fun fetchYProcesar(url: String) {
        Log.d("API", "Consultando: $url")
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response -> procesarFeatureCollection(response) },
            { error -> Log.e("API", "Error: ${error.message}") }
        )
        queue.add(request)
    }

    private fun fetchMultiTipo(base: String, tipos: List<String>) {
        val boundsBuilder = LatLngBounds.Builder()
        var anyAdded      = false
        var pending       = tipos.size

        tipos.forEach { tipo ->
            val url   = "$base?tipo=${URLEncoder.encode(tipo, "utf-8")}"
            val queue = Volley.newRequestQueue(this)

            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    val locales = mutableListOf<Polygon>()
                    if (response.has("features")) {
                        val features = response.getJSONArray("features")
                        for (i in 0 until features.length()) {
                            val feature  = features.getJSONObject(i)
                            val props    = feature.optJSONObject("properties") ?: JSONObject()
                            val codigo   = props.optString("codigo", "")
                            val tipoF    = props.optString("tipo", "")
                            val geometry = feature.getJSONObject("geometry")
                            val gtype    = geometry.getString("type")
                            if (gtype == "Polygon") {
                                val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                                val poly   = crearPoligonoDesdeCoords(coords, tipoF, codigo, boundsBuilder)
                                if (poly != null) { locales.add(poly); anyAdded = true }
                            } else if (gtype == "MultiPolygon") {
                                val mp = geometry.getJSONArray("coordinates")
                                for (p in 0 until mp.length()) {
                                    val coords = mp.getJSONArray(p).getJSONArray(0)
                                    val poly   = crearPoligonoDesdeCoords(coords, tipoF, codigo, boundsBuilder)
                                    if (poly != null) { locales.add(poly); anyAdded = true }
                                }
                            }
                        }
                    }
                    // Modificar listaPoligonos solo desde el hilo principal
                    runOnUiThread {
                        listaPoligonos.addAll(locales)
                        pending--
                        if (pending == 0) {
                            try {
                                if (anyAdded) map.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                                )
                            } catch (_: Exception) {}
                        }
                    }
                },
                { error ->
                    Log.e("API", "Error multi-tipo: ${error.message}")
                    runOnUiThread { pending-- }
                }
            )
            queue.add(request)
        }
    }

    private fun procesarFeatureCollection(fc: JSONObject) {
        if (!fc.has("features")) return
        val features = fc.getJSONArray("features")

        val boundsBuilder = LatLngBounds.Builder()
        var anyAdded      = false

        for (i in 0 until features.length()) {
            val feature  = features.getJSONObject(i)
            val props    = feature.optJSONObject("properties") ?: JSONObject()
            val codigo   = props.optString("codigo", "")
            val tipo     = props.optString("tipo", "")
            val geometry = feature.getJSONObject("geometry")
            val gtype    = geometry.getString("type")

            if (gtype == "Polygon") {
                val coords  = geometry.getJSONArray("coordinates").getJSONArray(0)
                val polygon = crearPoligonoDesdeCoords(coords, tipo, codigo, boundsBuilder)
                if (polygon != null) { listaPoligonos.add(polygon); anyAdded = true }
            } else if (gtype == "MultiPolygon") {
                val mp = geometry.getJSONArray("coordinates")
                for (p in 0 until mp.length()) {
                    val coords  = mp.getJSONArray(p).getJSONArray(0)
                    val polygon = crearPoligonoDesdeCoords(coords, tipo, codigo, boundsBuilder)
                    if (polygon != null) { listaPoligonos.add(polygon); anyAdded = true }
                }
            }
        }

        try {
            if (anyAdded) map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
            )
        } catch (_: Exception) {}

        if (buscarProfesorAlTerminar && profesorSeleccionado != null) {
            buscarProfesorAlTerminar = false
            tokenBusqueda++
            buscarSalonDelProfesor(profesorSeleccionado!!, tokenBusqueda)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Buscar salón en los polígonos ya cargados
    // ─────────────────────────────────────────────────────────────

    private fun buscarSalonEnPoligonos(salonUltimo: String) {
        if (salonUltimo.isEmpty()) return

        var encontrado = false

        listaPoligonos.forEach { polygon ->
            val tag    = polygon.tag as? JSONObject ?: return@forEach
            val codigo = tag.optString("codigo", "")

            if (codigo == salonUltimo) {
                polygon.isVisible = true
                encontrado = true

                val puntosArray = tag.optJSONArray("puntos") ?: JSONArray()
                val bb          = LatLngBounds.Builder()
                var tienePuntos = false
                for (i in 0 until puntosArray.length()) {
                    val p = puntosArray.getJSONObject(i)
                    bb.include(LatLng(p.optDouble("lat"), p.optDouble("lng")))
                    tienePuntos = true
                }

                val lat = tag.optDouble("lat", Double.NaN)
                val lng = tag.optDouble("lng", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    val pos = LatLng(lat, lng)
                    try {
                        if (tienePuntos) map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bb.build(), 80)
                        ) else map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(pos, 20f)
                        )
                    } catch (_: Exception) {}
                    consultarHorarioYMarcar(salonUltimo, codigo, pos)
                }
            } else {
                polygon.isVisible = false
            }
        }

        if (!encontrado) {
            Log.e("BUSQUEDA", "Salón $salonUltimo NO encontrado en Nivel $nivelActual.")
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("No encontrado")
                    .setMessage("El profesor está en el salón $salonUltimo, pero no se encuentra en los mapas.")
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    private fun consultarHorarioYMarcar(salonUltimo: String, codigo: String, pos: LatLng) {
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDia  = SimpleDateFormat("EEEE", Locale("es", "MX"))
        val hora    = sdfHora.format(Date())
        val dia     = sdfDia.format(Date())

        val url = "https://api-escomapp.onrender.com/horario?" +
                "profesor=${URLEncoder.encode(profesorSeleccionado!!, "utf-8")}" +
                "&salon=${URLEncoder.encode(salonUltimo, "utf-8")}" +
                "&dia=${URLEncoder.encode(dia, "utf-8")}" +
                "&hora=${URLEncoder.encode(hora, "utf-8")}"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { resp ->
                runOnUiThread {
                    markersByCodigo[codigo]?.remove()
                    markersByCodigo.remove(codigo)

                    val disponible = resp.optBoolean("disponible", false)
                    val snippet    = if (disponible)
                        "Código: $codigo\n${resp.optString("materia")}\n${resp.optString("entrada")} - ${resp.optString("salida")}"
                    else
                        "Código: $codigo\nNo está en este salón ahora"

                    val color = if (disponible) BitmapDescriptorFactory.HUE_GREEN
                    else           BitmapDescriptorFactory.HUE_RED

                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(profesorSeleccionado)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                    )
                    marker?.let { markersByCodigo[codigo] = it; it.showInfoWindow() }
                }
            },
            { Log.e("API", "Error horario") }
        )
        Volley.newRequestQueue(this).add(request)
    }

    // ─────────────────────────────────────────────────────────────
    // Click en polígono
    // ─────────────────────────────────────────────────────────────

    private fun onPolygonClicked(polygon: Polygon) {
        marcadorActual?.remove()
        marcadorActual = null
        val tag    = polygon.tag as? JSONObject ?: return
        val codigo = tag.optString("codigo", "sin código")
        val lat    = tag.optDouble("lat", Double.NaN)
        val lng    = tag.optDouble("lng", Double.NaN)
        if (!lat.isNaN() && !lng.isNaN()) {
            marcadorActual = map.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title("Código: $codigo")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marcadorActual?.showInfoWindow()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────

    private fun limpiarMarcadores() {
        markersByCodigo.values.forEach { it.remove() }
        markersByCodigo.clear()
        marcadorActual?.remove()
        marcadorActual = null
    }

    private fun actualizarBotonesUI(nivel: Int) {
        val btnPiso1 = findViewById<Button>(R.id.btnPiso1)
        val btnPiso2 = findViewById<Button>(R.id.btnPiso2)
        val btnPiso3 = findViewById<Button>(R.id.btnPiso3)
        btnPiso1.setBackgroundColor(if (nivel == 1) Color.BLUE else Color.GRAY)
        btnPiso2.setBackgroundColor(if (nivel == 2) Color.BLUE else Color.GRAY)
        btnPiso3.setBackgroundColor(if (nivel == 3) Color.BLUE else Color.GRAY)
    }

    private fun nivelDesdeSalon(salon: String): Int = when {
        salon.startsWith("10") -> 1
        salon.startsWith("11") -> 2
        salon.startsWith("12") -> 3
        else                   -> -1
    }

    private fun crearPoligonoDesdeCoords(
        coords: JSONArray,
        tipo: String,
        codigo: String,
        boundsBuilder: LatLngBounds.Builder
    ): Polygon? {
        if (coords.length() == 0) return null
        val color    = getColorByTipo(tipo)
        val opciones = PolygonOptions()
            .strokeWidth(3f)
            .strokeColor(color)
            .fillColor(adjustAlpha(color, 0.28f))
            .clickable(true)
            .zIndex(1f)

        val puntos = mutableListOf<LatLng>()
        for (j in 0 until coords.length()) {
            val p      = coords.getJSONArray(j)
            val latLng = LatLng(p.getDouble(1), p.getDouble(0))
            opciones.add(latLng)
            puntos.add(latLng)
            boundsBuilder.include(latLng)
        }

        val polygon     = map.addPolygon(opciones)
        val centro      = promedioLatLng(puntos)
        val puntosArray = JSONArray()
        puntos.forEach { pt ->
            puntosArray.put(JSONObject().apply {
                put("lat", pt.latitude)
                put("lng", pt.longitude)
            })
        }
        polygon.tag = JSONObject().apply {
            put("codigo", codigo)
            put("lat", centro.latitude)
            put("lng", centro.longitude)
            put("tipo", tipo)
            put("puntos", puntosArray)
        }
        return polygon
    }

    private fun promedioLatLng(puntos: List<LatLng>): LatLng {
        val lat = puntos.sumOf { it.latitude }  / puntos.size
        val lng = puntos.sumOf { it.longitude } / puntos.size
        return LatLng(lat, lng)
    }

    private fun getColorByTipo(tipo: String): Int = when (tipo) {
        "Salon"                -> Color.parseColor("#1565C0")
        "Sala", "Departamento" -> Color.parseColor("#1E88E5")
        "Area Comun"           -> Color.parseColor("#42A5F5")
        "Area Administrativa"  -> Color.parseColor("#90CAF9")
        "Cubiculo"             -> Color.parseColor("#0D47A1")
        "Laboratorio"          -> Color.parseColor("#00B0FF")
        "Club"                 -> Color.parseColor("#0277BD")
        "Profesor"             -> Color.parseColor("#B3E5FC")
        else                   -> Color.parseColor("#64B5F6")
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}