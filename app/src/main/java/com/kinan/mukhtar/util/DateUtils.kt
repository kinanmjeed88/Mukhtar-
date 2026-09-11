package com.kinan.mukhtar.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val arLocale = Locale("ar")

    /** صيغة يوم/شهر/سنة */
    fun format(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return "غير محدد"
        val fmt = SimpleDateFormat("dd/MM/yyyy", arLocale)
        return fmt.format(Date(timestamp))
    }

    /** حساب العمر ديناميكياً بالسنوات */
    fun calculateAge(birthDate: Long?): Int? {
        if (birthDate == null || birthDate <= 0L) return null
        val birth = Calendar.getInstance().apply { timeInMillis = birthDate }
        val now = Calendar.getInstance()
        if (birth.after(now)) return 0
        var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }

    fun ageText(birthDate: Long?): String {
        val age = calculateAge(birthDate) ?: return "غير محدد"
        return "$age سنة"
    }

    fun todayFormatted(): String = format(System.currentTimeMillis())
}
