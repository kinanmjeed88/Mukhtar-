package com.kinan.mukhtar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** بيانات الإعداد الأولي للمنطقة */
@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "governorate") val governorate: String = "",
    @ColumnInfo(name = "district") val district: String = "",
    @ColumnInfo(name = "region") val region: String = "",
    @ColumnInfo(name = "isSetupComplete") val isSetupComplete: Boolean = false
) {
    val headerLine: String
        get() = listOf(governorate, district, region).filter { it.isNotBlank() }.joinToString(" - ")
}

/** الأفراد وأرباب العوائل */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String = "",
    val birthDate: Long = 0L,
    val isMarried: Boolean = false,
    val spouseName: String? = null,
    val spouseBirthDate: Long? = null,
    val job: String = "",
    val spouseJob: String? = null,
    val notes: String? = null
)

object StaticData {
    val iraqiGovernorates = listOf(
        "بغداد", "بابل", "البصرة", "نينوى", "أربيل", "النجف الأشرف",
        "كربلاء المقدسة", "كركوك", "الأنبار", "ديالى", "ذي قار", "ميسان",
        "المثنى", "صلاح الدين", "السليمانية", "واسط", "دهوك", "حلبجة"
    )
}
