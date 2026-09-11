package com.kinan.mukhtar.util

import android.net.Uri

/** أنماط إطار المستند */
enum class BorderStyle(val label: String) {
    NONE("بدون إطار"),
    SINGLE("خط مفرد"),
    DOUBLE("خطان مستمران"),
    SOLID_DASHED("مستمر ومتقطع"),
    IMAGE("إطار من المعرض")
}

/** إعدادات تنسيق مستند تأييد السكن */
data class DocumentStyle(
    val fontSize: Float = 14f,
    val border: BorderStyle = BorderStyle.SINGLE,
    val backgroundUri: Uri? = null
)
