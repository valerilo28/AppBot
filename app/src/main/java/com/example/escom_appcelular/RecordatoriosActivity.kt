package com.example.escom_appcelular

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class RecordatoriosActivity : AppCompatActivity() {

    // ── Views ────────────────────────────────────────────────────────────────
    private lateinit var cbBecaGeneral: CheckBox
    private lateinit var detalleBecaGeneral: View
    private lateinit var cbServicioSocial: CheckBox
    private lateinit var layoutServicioSocial: View
    private lateinit var rgTipoSS: RadioGroup
    private lateinit var rbSSInterno: RadioButton
    private lateinit var spinnerPeriodoSS: Spinner
    private lateinit var tvSSFechaLimite: TextView
    private lateinit var cbEstancia: CheckBox
    private lateinit var detalleEstancia: View
    private lateinit var cbAvisos: CheckBox
    private lateinit var layoutAvisos: View
    private lateinit var rgFrecuenciaAvisos: RadioGroup
    private lateinit var rb3dias: RadioButton
    private lateinit var rb7dias: RadioButton
    private lateinit var rb15dias: RadioButton
    private lateinit var btnGuardar: Button

    // ── Prefs keys ───────────────────────────────────────────────────────────
    private val PREFS             = "reminder_prefs"
    private val KEY_BECA_GENERAL  = "pref_beca_general"
    private val KEY_SS_ACTIVO     = "pref_ss_activo"
    private val KEY_SS_INTERNO    = "pref_ss_interno"
    private val KEY_SS_PERIODO    = "pref_ss_periodo"
    private val KEY_ESTANCIA      = "pref_estancia"
    private val KEY_AVISOS        = "pref_avisos"
    private val KEY_AVISOS_DIAS   = "pref_avisos_dias"

    // ── Periodos filtrados (solo futuros) ────────────────────────────────────
    private var periodosActuales: List<ReminderData.ServicioSocialPeriodo> = emptyList()

    // ── Permiso notificaciones ───────────────────────────────────────────────
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) guardarYProgramar()
            else Toast.makeText(this, "Se necesita permiso para enviar notificaciones", Toast.LENGTH_LONG).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordatorios)
        bindViews()
        loadPrefs()
        setupListeners()
    }

    private fun bindViews() {
        cbBecaGeneral        = findViewById(R.id.cbBecaGeneral)
        detalleBecaGeneral   = findViewById(R.id.detalleBecaGeneral)
        cbServicioSocial     = findViewById(R.id.cbServicioSocial)
        layoutServicioSocial = findViewById(R.id.layoutServicioSocial)
        rgTipoSS             = findViewById(R.id.rgTipoSS)
        rbSSInterno          = findViewById(R.id.rbSSInterno)
        spinnerPeriodoSS     = findViewById(R.id.spinnerPeriodoSS)
        tvSSFechaLimite      = findViewById(R.id.tvSSFechaLimite)
        cbEstancia           = findViewById(R.id.cbEstancia)
        detalleEstancia      = findViewById(R.id.detalleEstancia)
        cbAvisos             = findViewById(R.id.cbAvisos)
        layoutAvisos         = findViewById(R.id.layoutAvisos)
        rgFrecuenciaAvisos   = findViewById(R.id.rgFrecuenciaAvisos)
        rb3dias              = findViewById(R.id.rb3dias)
        rb7dias              = findViewById(R.id.rb7dias)
        rb15dias             = findViewById(R.id.rb15dias)
        btnGuardar           = findViewById(R.id.btnGuardarRecordatorios)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        cbBecaGeneral.setOnCheckedChangeListener { _, checked ->
            detalleBecaGeneral.visibility = if (checked) View.VISIBLE else View.GONE
        }
        cbServicioSocial.setOnCheckedChangeListener { _, checked ->
            layoutServicioSocial.visibility = if (checked) View.VISIBLE else View.GONE
        }
        cbEstancia.setOnCheckedChangeListener { _, checked ->
            detalleEstancia.visibility = if (checked) View.VISIBLE else View.GONE
        }
        cbAvisos.setOnCheckedChangeListener { _, checked ->
            layoutAvisos.visibility = if (checked) View.VISIBLE else View.GONE
        }

        rgTipoSS.setOnCheckedChangeListener { _, _ -> actualizarSpinnerSS() }

        spinnerPeriodoSS.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos < periodosActuales.size) {
                    val periodo = periodosActuales[pos]
                    tvSSFechaLimite.text =
                        "📌 Fecha límite para entregar expediente (antes de las 2pm):\n" +
                        android.text.format.DateFormat.format("dd/MM/yyyy", periodo.fechaLimiteExpediente)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnGuardar.setOnClickListener { checkPermissionAndSave() }
    }

    private fun actualizarSpinnerSS() {
        val isInterno = rbSSInterno.isChecked
        val hoy = Calendar.getInstance()

        val todos = if (isInterno)
            ReminderData.servicioSocialInternosPeriodos
        else
            ReminderData.servicioSocialExternosPeriodos

        // Mostrar solo periodos cuya fecha límite de expediente no haya pasado
        periodosActuales = todos.filter { it.fechaLimiteExpediente.after(hoy) }
        if (periodosActuales.isEmpty()) periodosActuales = todos

        val labels = periodosActuales.map { it.label }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriodoSS.adapter = adapter

        val savedIdx = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SS_PERIODO, 0)
        if (savedIdx < periodosActuales.size) spinnerPeriodoSS.setSelection(savedIdx)
    }

    private fun loadPrefs() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)

        cbBecaGeneral.isChecked = prefs.getBoolean(KEY_BECA_GENERAL, false)
        detalleBecaGeneral.visibility = if (cbBecaGeneral.isChecked) View.VISIBLE else View.GONE

        cbServicioSocial.isChecked = prefs.getBoolean(KEY_SS_ACTIVO, false)
        layoutServicioSocial.visibility = if (cbServicioSocial.isChecked) View.VISIBLE else View.GONE

        rbSSInterno.isChecked = prefs.getBoolean(KEY_SS_INTERNO, true)
        actualizarSpinnerSS()

        cbEstancia.isChecked = prefs.getBoolean(KEY_ESTANCIA, false)
        detalleEstancia.visibility = if (cbEstancia.isChecked) View.VISIBLE else View.GONE

        cbAvisos.isChecked = prefs.getBoolean(KEY_AVISOS, false)
        layoutAvisos.visibility = if (cbAvisos.isChecked) View.VISIBLE else View.GONE

        when (prefs.getInt(KEY_AVISOS_DIAS, 7)) {
            3    -> rb3dias.isChecked  = true
            15   -> rb15dias.isChecked = true
            else -> rb7dias.isChecked  = true
        }
    }

    private fun checkPermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        guardarYProgramar()
    }

    private fun guardarYProgramar() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE).edit()

        // ── Beca General ─────────────────────────────────────────────────────
        val becaGeneralOn = cbBecaGeneral.isChecked
        prefs.putBoolean(KEY_BECA_GENERAL, becaGeneralOn)
        if (becaGeneralOn) {
            ReminderData.becaGeneralEvents.forEach { ReminderScheduler.scheduleEvent(this, it) }
        } else {
            ReminderScheduler.cancelCategory(this, ReminderCategory.BECA_GENERAL)
        }

        // ── Servicio Social ──────────────────────────────────────────────────
        val ssOn       = cbServicioSocial.isChecked
        val ssInterno  = rbSSInterno.isChecked
        val ssPeriodoIdx = spinnerPeriodoSS.selectedItemPosition
        prefs.putBoolean(KEY_SS_ACTIVO, ssOn)
        prefs.putBoolean(KEY_SS_INTERNO, ssInterno)
        prefs.putInt(KEY_SS_PERIODO, ssPeriodoIdx)

        ReminderScheduler.cancelCategory(this, ReminderCategory.SERVICIO_SOCIAL_INTERNO)
        ReminderScheduler.cancelCategory(this, ReminderCategory.SERVICIO_SOCIAL_EXTERNO)
        if (ssOn && ssPeriodoIdx >= 0 && ssPeriodoIdx < periodosActuales.size) {
            ReminderData.buildServicioSocialEvents(periodosActuales[ssPeriodoIdx], ssInterno)
                .forEach { ReminderScheduler.scheduleEvent(this, it) }
        }

        // ── Estancia Profesional ─────────────────────────────────────────────
        val estanciaOn = cbEstancia.isChecked
        prefs.putBoolean(KEY_ESTANCIA, estanciaOn)
        if (estanciaOn) {
            ReminderData.estanciaEvents.forEach { ReminderScheduler.scheduleEvent(this, it) }
        } else {
            ReminderScheduler.cancelCategory(this, ReminderCategory.ESTANCIA_PROFESIONAL)
        }

        // ── Avisos Importantes ───────────────────────────────────────────────
        val avisosOn  = cbAvisos.isChecked
        val avisosDias = when (rgFrecuenciaAvisos.checkedRadioButtonId) {
            R.id.rb3dias  -> 3
            R.id.rb15dias -> 15
            else          -> 7
        }
        prefs.putBoolean(KEY_AVISOS, avisosOn)
        prefs.putInt(KEY_AVISOS_DIAS, avisosDias)
        if (avisosOn) ReminderScheduler.schedulePeriodicAviso(this, avisosDias)
        else          ReminderScheduler.cancelPeriodicAviso(this)

        prefs.apply()
        Toast.makeText(this, "✅ Recordatorios guardados", Toast.LENGTH_SHORT).show()
        finish()
    }
}
