package com.example.escom_appcelular

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.json.JSONArray

object AreaRepository {

    fun getAll(context: Context): List<Area> {
        return try {
            val json = context.assets.open("areas.json")
                .bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            val list = mutableListOf<Area>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Area(
                        area        = obj.getString("area"),
                        responsable = obj.getString("responsable"),
                        cargo       = obj.getString("cargo"),
                        correo      = obj.getString("correo"),
                        extension   = obj.getString("extension"),
                        ubicacion   = obj.getString("ubicacion"),
                        foto        = obj.getString("foto")
                    )
                )
            }
            list
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("repository", "AreaRepository")
                recordException(e)
            }
            emptyList()
        }
    }
}
