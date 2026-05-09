package com.example.escom_appcelular

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView



class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val listaPoligonos = mutableListOf<Polygon>()
    private val markersByCodigo = mutableMapOf<String, Marker>() // marcadores por polígono/salón
    private var marcadorActual: Marker? = null
    private var nivelActual = 1
    private var tipoActual: String? = null   // filtro de tipo (Salon, Departamento, etc.)
    private var profesorSeleccionado: String? = null

    // lista estática original de tipos (ahora con "Profesor" agregado)
    private val tiposOriginales = listOf(
        "Todos",
        "Salon",
        "Departamento",
        "Area Comun",
        "Area Administrativa",
        "Cubiculo",
        "Laboratorio",
        "Club",
        "Profesor"   // <-- agregado
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa)

        // -------------------------------------
        // BOTONES DE PISOS
        // -------------------------------------
        val btnPiso1 = findViewById<Button>(R.id.btnPiso1)
        val btnPiso2 = findViewById<Button>(R.id.btnPiso2)
        val btnPiso3 = findViewById<Button>(R.id.btnPiso3)

        btnPiso1.setOnClickListener {
            nivelActual = 1
            actualizarBotonesUI(nivelActual) // Actualizamos color del botón
            cargarPoligonos()
        }
        btnPiso2.setOnClickListener {
            nivelActual = 2
            actualizarBotonesUI(nivelActual)
            cargarPoligonos()
        }
        btnPiso3.setOnClickListener {
            nivelActual = 3
            actualizarBotonesUI(nivelActual)
            cargarPoligonos()
        }

        // Inicializar estado visual de los botones (por defecto Nivel 1 o el que tengas)
        actualizarBotonesUI(nivelActual)

        // -------------------------------------
        // SPINNER (CONFIGURACIÓN Y LÓGICA DE REINICIO)
        // -------------------------------------
        val spinnerTipo = findViewById<Spinner>(R.id.spinnerMarcador)
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposOriginales)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adaptador
        spinnerTipo.setSelection(0)

        spinnerTipo.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val seleccionado = tiposOriginales[position]

                if (seleccionado == "Profesor") {
                    // MODO PROFESOR
                    // Reseteamos el spinner visualmente a 0 para que no se quede trabado en "Profesor"
                    // y permita volver a seleccionarlo si el usuario quiere buscar otro.
                    spinnerTipo.setSelection(0, false)

                    // Limpiamos selección previa antes de abrir el diálogo
                    profesorSeleccionado = null
                    tipoActual = null

                    mostrarDialogoProfesores()

                } else {
                    // MODO NORMAL (Cualquier otra opción)

                    // 1. IMPORTANTE: Reiniciar variable para salir del modo "buscar profesor"
                    profesorSeleccionado = null

                    // 2. Limpiar marcadores de horarios anteriores (verdes/rojos)
                    markersByCodigo.values.forEach { it.remove() }
                    markersByCodigo.clear()

                    // 3. Definir el filtro de tipo
                    tipoActual = if (seleccionado == "Todos") null else seleccionado

                    // 4. Recargar el mapa limpio con el filtro seleccionado
                    cargarPoligonos()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // -------------------------------------
        // CARGAR MAPA
        // -------------------------------------
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val escomBounds = LatLngBounds(
            LatLng(19.5041, -99.1478),
            LatLng(19.5058, -99.1458)
        )
        map.setLatLngBoundsForCameraTarget(escomBounds)
        map.setMinZoomPreference(19.2f)
        map.setMaxZoomPreference(21f)
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(escomBounds, 40))
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? {
                val view = layoutInflater.inflate(R.layout.custom_info_window, null)
                val title = view.findViewById<TextView>(R.id.title)
                val snippet = view.findViewById<TextView>(R.id.snippet)
                title.text = marker.title
                snippet.text = marker.snippet
                return view
            }
            override fun getInfoWindow(marker: Marker): View? = null
        })
        try {
            val success = map.setMapStyle(
                com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_sin_poi)
            )
            if (!success) Log.e("MapStyle", "Error al aplicar el estilo del mapa")
        } catch (e: Exception) {
            Log.e("MapStyle", "Error cargando estilo del mapa", e)
        }

        map.setOnPolygonClickListener { polygon -> onPolygonClicked(polygon) }

        cargarPoligonos()
    }

    private fun mostrarDialogoProfesores() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api-escomapp.onrender.com/profesores?nivel=$nivelActual"
        val spinner = findViewById<Spinner>(R.id.spinnerMarcador)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val array = response.optJSONArray("profesores") ?: JSONArray()
                val listaOriginal = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    listaOriginal.add(array.getString(i))
                }

                if (listaOriginal.isEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Profesores")
                        .setMessage("No se encontraron profesores para este nivel.")
                        .setPositiveButton("OK", null)
                        .show()
                    spinner.setSelection(0)
                    return@JsonObjectRequest
                }

                // Layout personalizado con EditText y ListView
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.dialog_buscar_lista, null)
                val editTextBuscar = view.findViewById<android.widget.EditText>(R.id.editTextBuscar)
                val listView = view.findViewById<android.widget.ListView>(R.id.listViewProfesores)

                val listaCopia = listaOriginal.toMutableList()
                val adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaCopia)
                listView.adapter = adaptador

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Selecciona un profesor")
                    .setView(view)
                    .setNegativeButton("Cancelar") { d, _ ->
                        d.dismiss()
                        spinner.setSelection(0)
                    }
                    .create()

                // Filtrado en tiempo real
                editTextBuscar.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adaptador.filter.filter(s)
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })

                listView.setOnItemClickListener { _, _, position, _ ->
                    val seleccionado = adaptador.getItem(position)
                    profesorSeleccionado = seleccionado
                    cargarPoligonos()
                    dialog.dismiss()
                }

                dialog.show()
            },
            { error ->
                Log.e("API", "Error cargando profesores: ${error.message}")
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No se pudieron cargar profesores.")
                    .setPositiveButton("OK", null)
                    .show()
                spinner.setSelection(0)
            }
        )
        queue.add(request)
    }

    private fun cargarPoligonos() {
        listaPoligonos.forEach { it.remove() }
        listaPoligonos.clear()
        markersByCodigo.values.forEach { it.remove() }
        markersByCodigo.clear()
        marcadorActual?.remove()
        marcadorActual = null

        val base = "https://api-escomapp.onrender.com/Nivel$nivelActual"
        val url = if (tipoActual != null)
            "$base?tipo=${URLEncoder.encode(tipoActual, "utf-8")}"
        else
            base

        Log.d("API", "Consultando: $url")

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response -> procesarFeatureCollection(response) },
            { error -> Log.e("API", "Error: ${error.message}") }
        )

        queue.add(request)
    }

    private fun procesarFeatureCollection(fc: JSONObject) {
        if (!fc.has("features")) return
        val features = fc.getJSONArray("features")

        val boundsBuilder = LatLngBounds.Builder()
        var anyAdded = false

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val props = feature.optJSONObject("properties") ?: JSONObject()
            val codigo = props.optString("codigo", "")
            val tipo = props.optString("tipo", "")
            val geometry = feature.getJSONObject("geometry")
            val gtype = geometry.getString("type")

            if (gtype == "Polygon") {
                val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                val polygon = crearPoligonoDesdeCoords(coords, tipo, codigo, boundsBuilder)
                if (polygon != null) { listaPoligonos.add(polygon); anyAdded = true }
            } else if (gtype == "MultiPolygon") {
                val multipoly = geometry.getJSONArray("coordinates")
                for (p in 0 until multipoly.length()) {
                    val coords = multipoly.getJSONArray(p).getJSONArray(0)
                    val polygon = crearPoligonoDesdeCoords(coords, tipo, codigo, boundsBuilder)
                    if (polygon != null) { listaPoligonos.add(polygon); anyAdded = true }
                }
            }
        }

        try {
            if (anyAdded) map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
        } catch (_: Exception) {}

        if (profesorSeleccionado != null) {
            marcarProfesorEnTodosLosPoligonos()
        }
    }

    private fun crearPoligonoDesdeCoords(coords: JSONArray, tipo: String, codigo: String, boundsBuilder: LatLngBounds.Builder): Polygon? {
        if (coords.length() == 0) return null

        val color = getColorByTipo(tipo)

        val opciones = PolygonOptions()
            .strokeWidth(3f)
            .strokeColor(color)
            .fillColor(adjustAlpha(color, 0.28f))
            .clickable(true)

        val puntos = mutableListOf<LatLng>()

        for (j in 0 until coords.length()) {
            val punto = coords.getJSONArray(j)
            val lng = punto.getDouble(0)
            val lat = punto.getDouble(1)
            val latLng = LatLng(lat, lng)
            opciones.add(latLng)
            puntos.add(latLng)
            boundsBuilder.include(latLng)
        }

        val polygon = map.addPolygon(opciones)

        val tagObj = JSONObject()
        tagObj.put("codigo", codigo)
        val centro = promedioLatLng(puntos)
        tagObj.put("lat", centro.latitude)
        tagObj.put("lng", centro.longitude)
        tagObj.put("tipo", tipo)
        val puntosArray = JSONArray()
        puntos.forEach { p ->
            val o = JSONObject()
            o.put("lat", p.latitude)
            o.put("lng", p.longitude)
            puntosArray.put(o)
        }
        tagObj.put("puntos", puntosArray)

        polygon.tag = tagObj

        return polygon
    }

    private fun onPolygonClicked(polygon: Polygon) {
        marcadorActual?.remove()
        marcadorActual = null

        val tag = polygon.tag
        if (tag is JSONObject) {
            val codigo = tag.optString("codigo", "sin código")
            val lat = tag.optDouble("lat", Double.NaN)
            val lng = tag.optDouble("lng", Double.NaN)

            if (!lat.isNaN() && !lng.isNaN()) {
                val pos = LatLng(lat, lng)
                marcadorActual = map.addMarker(MarkerOptions().position(pos).title("Código: $codigo"))
                marcadorActual?.showInfoWindow()
            }
        }
    }
    private fun actualizarBotonesUI(nivel: Int) {
        val btnPiso1 = findViewById<Button>(R.id.btnPiso1)
        val btnPiso2 = findViewById<Button>(R.id.btnPiso2)
        val btnPiso3 = findViewById<Button>(R.id.btnPiso3)

        // Colores sugeridos: Gris para inactivo, Azul (o tu color primario) para activo
        val colorInactivo = Color.GRAY
        val colorActivo = Color.BLUE

        btnPiso1.setBackgroundColor(if (nivel == 1) colorActivo else colorInactivo)
        btnPiso2.setBackgroundColor(if (nivel == 2) colorActivo else colorInactivo)
        btnPiso3.setBackgroundColor(if (nivel == 3) colorActivo else colorInactivo)
    }
    private fun marcarProfesorEnTodosLosPoligonos() {
        if (profesorSeleccionado == null) return

        val queue = Volley.newRequestQueue(this)
        val urlUltimo = "https://api-escomapp.onrender.com/ultimo_salon_profesor?profesor=${URLEncoder.encode(profesorSeleccionado!!, "utf-8")}"

        val requestUltimo = JsonObjectRequest(Request.Method.GET, urlUltimo, null,
            { response ->
                val salonUltimo = response.optString("salon", "")
                // La API devuelve -1, así que ignoraremos este valor para la navegación
                // y confiaremos en buscar el salón físicamente en el mapa.
                val nivelProfesorAPI = response.optInt("nivel", -1)

                // Si la API SÍ manda un nivel explícito y diferente, obedecemos a la API.
                if (nivelProfesorAPI != -1 && nivelProfesorAPI != nivelActual) {
                    nivelActual = nivelProfesorAPI
                    actualizarBotonesUI(nivelActual)
                    cargarPoligonos()
                    return@JsonObjectRequest
                }

                if (salonUltimo.isEmpty()) return@JsonObjectRequest

                // --- BUSCAR SALÓN EN EL PISO ACTUAL ---
                var encontrado = false
                var boundsBuilder: LatLngBounds.Builder? = null

                listaPoligonos.forEach { polygon ->
                    val tag = polygon.tag as? JSONObject ?: return@forEach
                    val codigo = tag.optString("codigo", "")

                    if (codigo == salonUltimo) {
                        polygon.isVisible = true
                        encontrado = true

                        // Calcular centro y bordes
                        val puntosArray = tag.optJSONArray("puntos") ?: JSONArray()
                        val puntos = mutableListOf<LatLng>()
                        for (i in 0 until puntosArray.length()) {
                            val p = puntosArray.getJSONObject(i)
                            puntos.add(LatLng(p.optDouble("lat"), p.optDouble("lng")))
                        }
                        boundsBuilder = LatLngBounds.Builder()
                        puntos.forEach { boundsBuilder?.include(it) }

                        val lat = tag.optDouble("lat", Double.NaN)
                        val lng = tag.optDouble("lng", Double.NaN)

                        if (!lat.isNaN() && !lng.isNaN()) {
                            val pos = LatLng(lat, lng)
                            consultarHorarioYMarcar(salonUltimo, codigo, pos)
                        }
                    } else {
                        polygon.isVisible = false
                    }
                }

                // --- LÓGICA DE BÚSQUEDA AUTOMÁTICA EN OTROS PISOS ---
                if (!encontrado) {
                    Log.d("BUSQUEDA", "Salón $salonUltimo NO encontrado en Nivel $nivelActual.")

                    // Definimos el orden de búsqueda para no ciclar infinitamente.
                    // Si estoy en 3, voy al 1. Si estoy en 1, voy al 2. Si estoy en 2, termino.
                    var siguienteNivel = -1

                    if (nivelActual == 3) {
                        siguienteNivel = 1 // De piso 3 baja a buscar al 1
                    } else if (nivelActual == 1) {
                        siguienteNivel = 2 // De piso 1 sube a buscar al 2
                    }
                    // Si estamos en el 2 y no lo encontró, ya no hay más donde buscar (asumiendo 3 pisos)

                    if (siguienteNivel != -1) {
                        Log.d("BUSQUEDA", "Intentando buscar en Nivel $siguienteNivel...")

                        nivelActual = siguienteNivel
                        actualizarBotonesUI(nivelActual) // Actualiza los botones visualmente
                        cargarPoligonos() // Recarga el mapa y vuelve a ejecutar esta función
                        return@JsonObjectRequest // Detenemos aquí para esperar la recarga
                    } else {
                        // Ya buscamos en 3, 1 y 2 y no apareció.
                        Log.e("BUSQUEDA", "Error: El salón $salonUltimo no aparece en ningún nivel.")
                        AlertDialog.Builder(this)
                            .setTitle("No encontrado")
                            .setMessage("El profesor está en el salón $salonUltimo, pero no se encuentra en los mapas.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                } else {
                    // Si SÍ lo encontró, movemos la cámara
                    boundsBuilder?.let {
                        try {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(it.build(), 100))
                        } catch (e: Exception) { Log.e("MAP", "Error camara: $e") }
                    }
                }
            },
            { error -> Log.e("API", "Error al buscar profesor: ${error.message}") }
        )
        queue.add(requestUltimo)
    }

    // Asegúrate de tener esta función auxiliar en tu clase también para pintar el horario
    private fun consultarHorarioYMarcar(salonUltimo: String, codigo: String, pos: LatLng) {
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDia = SimpleDateFormat("EEEE", Locale("es", "MX"))
        val horaActual = sdfHora.format(Date())
        val diaActual = sdfDia.format(Date())

        val urlHorario = "https://api-escomapp.onrender.com/horario?" +
                "profesor=${URLEncoder.encode(profesorSeleccionado!!, "utf-8")}" +
                "&salon=${URLEncoder.encode(salonUltimo, "utf-8")}" +
                "&dia=${URLEncoder.encode(diaActual, "utf-8")}" +
                "&hora=${URLEncoder.encode(horaActual, "utf-8")}"

        val request = JsonObjectRequest(Request.Method.GET, urlHorario, null,
            { horarioResp ->
                runOnUiThread {
                    markersByCodigo[codigo]?.remove()
                    markersByCodigo.remove(codigo)

                    val disponible = horarioResp.optBoolean("disponible", false)
                    val snippet = if (disponible) {
                        "Código: $codigo\n${horarioResp.optString("materia")}\n${horarioResp.optString("entrada")} - ${horarioResp.optString("salida")}"
                    } else {
                        "Código: $codigo\nNo está en este salón ahora"
                    }
                    val color = if (disponible) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED

                    val marker = map.addMarker(MarkerOptions()
                        .position(pos)
                        .title(profesorSeleccionado)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(color)))

                    marker?.let {
                        markersByCodigo[codigo] = it
                        it.showInfoWindow()
                    }
                }
            },
            { Log.e("API", "Error horario") }
        )
        Volley.newRequestQueue(this).add(request)
    }



    // ===============================================
    // FUNCIONES AUXILIARES
    // ===============================================
    private fun promedioLatLng(puntos: List<LatLng>): LatLng {
        val lat = puntos.sumOf { it.latitude } / puntos.size
        val lng = puntos.sumOf { it.longitude } / puntos.size
        return LatLng(lat, lng)
    }

    private fun getColorByTipo(tipo: String): Int {
        return when (tipo) {
            "Salon" -> Color.BLUE
            "Departamento" -> Color.MAGENTA
            "Area Comun" -> Color.CYAN
            "Area Administrativa" -> Color.YELLOW
            "Cubiculo" -> Color.GRAY
            "Laboratorio" -> Color.GREEN
            "Club" -> Color.DKGRAY
            "Profesor" -> Color.RED
            else -> Color.LTGRAY
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
