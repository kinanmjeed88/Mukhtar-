package com.kinan.mukhtar.util

import com.kinan.mukhtar.data.AppConfigEntity
import com.kinan.mukhtar.data.PersonEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(val config: AppConfigEntity?, val persons: List<PersonEntity>)

object BackupManager {

    fun toJson(config: AppConfigEntity?, persons: List<PersonEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        config?.let {
            root.put("config", JSONObject().apply {
                put("id", it.id)
                put("governorate", it.governorate)
                put("district", it.district)
                put("region", it.region)
                put("isSetupComplete", it.isSetupComplete)
            })
        }
        val arr = JSONArray()
        persons.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("fullName", p.fullName)
                put("birthDate", p.birthDate)
                put("isMarried", p.isMarried)
                put("spouseName", p.spouseName ?: JSONObject.NULL)
                put("spouseBirthDate", p.spouseBirthDate ?: JSONObject.NULL)
                put("job", p.job)
                put("spouseJob", p.spouseJob ?: JSONObject.NULL)
                put("notes", p.notes ?: JSONObject.NULL)
            })
        }
        root.put("persons", arr)
        return root.toString(2)
    }

    fun fromJson(json: String): BackupPayload {
        val root = JSONObject(json)
        val config = root.optJSONObject("config")?.let {
            AppConfigEntity(
                id = it.optInt("id", 1),
                governorate = it.optString("governorate", ""),
                district = it.optString("district", ""),
                region = it.optString("region", ""),
                isSetupComplete = it.optBoolean("isSetupComplete", true)
            )
        }
        val list = mutableListOf<PersonEntity>()
        val arr = root.optJSONArray("persons") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += PersonEntity(
                id = o.optLong("id", 0L),
                fullName = o.optString("fullName", ""),
                birthDate = o.optLong("birthDate", 0L),
                isMarried = o.optBoolean("isMarried", false),
                spouseName = o.optStringOrNull("spouseName"),
                spouseBirthDate = if (o.isNull("spouseBirthDate")) null else o.optLong("spouseBirthDate"),
                job = o.optString("job", ""),
                spouseJob = o.optStringOrNull("spouseJob"),
                notes = o.optStringOrNull("notes")
            )
        }
        return BackupPayload(config, list)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    /** تحليل ملف نصي/CSV: كل سطر اسم كامل */
    fun parseNames(raw: String): List<String> =
        raw.split("\n", "\r")
            .map { it.substringBefore(',').trim().trim('"') }
            .filter { it.isNotBlank() && it.length > 2 }
            .distinct()
}
