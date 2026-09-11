package com.kinan.mukhtar.util

/**
 * منطق مطابقة الأسماء العربية لربط الأبناء برب العائلة.
 *
 * الاسم الرباعي يتكون من: الاسم الشخصي + اسم الأب + اسم الجد + اسم الجد الأكبر [+ اللقب]
 * ابن الشخص "كنان مجيد حميد محمد الصائغ" يكون اسمه: "<اسمه> كنان مجيد حميد ..."
 * أي أن سلسلة نسب الأب (اسمه + أبوه + جده) تظهر في اسم الابن بدءاً من الموضع الثاني.
 */
object NameMatcher {

    /** حروف التشكيل والمدّ التي يجب تجاهلها عند المقارنة */
    private val DIACRITICS = Regex("[\\u064B-\\u0652\\u0640]")

    /** توحيد شكل النص العربي: إزالة التشكيل وتوحيد الألف والياء والهاء */
    fun normalize(raw: String): String =
        raw.replace(DIACRITICS, "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ى', 'ي').replace('ة', 'ه')
            .replace(Regex("\\s+"), " ")
            .trim()

    fun tokens(raw: String): List<String> =
        normalize(raw).split(' ').filter { it.isNotBlank() }

    /**
     * سلسلة نسب الأب التي يجب أن يرثها الابن.
     * من "كنان مجيد حميد محمد الصائغ" تُستخرج "كنان مجيد حميد".
     * تُهمل الكلمة الأخيرة إن كانت لقباً (اسم من 5 كلمات فأكثر).
     */
    fun lineageOf(fatherFullName: String, depth: Int = 3): List<String> {
        val t = tokens(fatherFullName)
        if (t.size < 2) return emptyList()
        val withoutSurname = if (t.size >= 5) t.dropLast(1) else t
        return withoutSurname.take(depth)
    }

    /**
     * هل [candidateFullName] ابن لـ [fatherFullName]؟
     * الشرط: أن تبدأ بقية اسم المرشح (بعد اسمه الشخصي) بسلسلة نسب الأب.
     */
    fun isChildOf(candidateFullName: String, fatherFullName: String): Boolean {
        val lineage = lineageOf(fatherFullName)
        if (lineage.size < 2) return false

        val child = tokens(candidateFullName)
        // الابن يحتاج اسمه الشخصي + سلسلة نسب الأب على الأقل
        if (child.size <= lineage.size) return false

        // لا يكون الشخص ابن نفسه
        if (normalize(candidateFullName) == normalize(fatherFullName)) return false

        val afterOwnName = child.drop(1)
        if (afterOwnName.size < lineage.size) return false

        return afterOwnName.take(lineage.size) == lineage
    }
}
