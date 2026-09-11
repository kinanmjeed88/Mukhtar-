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
    @ColumnInfo(name = "mukhtarName", defaultValue = "") val mukhtarName: String = "",
    @ColumnInfo(name = "isSetupComplete") val isSetupComplete: Boolean = false
) {
    /** سطر العنوان: اسم المختار - المحافظة - القضاء - المنطقة */
    val headerLine: String
        get() = listOf(mukhtarName, governorate, district, region)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
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
    @ColumnInfo(name = "gender", defaultValue = "ذكر") val gender: String = Gender.MALE,
    @ColumnInfo(name = "phoneNumber") val phoneNumber: String? = null,
    val spouseJob: String? = null,
    val notes: String? = null
)

/** قيم الجنس المعتمدة في قاعدة البيانات */
object Gender {
    const val MALE = "ذكر"
    const val FEMALE = "أنثى"
    val all = listOf(MALE, FEMALE)
}

object StaticData {
    val iraqiGovernorates = listOf(
        "بغداد", "بابل", "البصرة", "نينوى", "أربيل", "النجف الأشرف",
        "كربلاء المقدسة", "كركوك", "الأنبار", "ديالى", "ذي قار", "ميسان",
        "المثنى", "صلاح الدين", "السليمانية", "واسط", "دهوك", "حلبجة"
    )
}
