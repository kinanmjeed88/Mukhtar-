package com.kinan.mukhtar.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/** يحفظ أي انهيار في ملف نصي لعرضه عند التشغيل التالي */
object CrashLogger {

    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                File(appContext.filesDir, FILE_NAME).writeText(
                    "Thread: ${thread.name}\n\n$sw"
                )
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun read(context: Context): String? {
        val f = File(context.applicationContext.filesDir, FILE_NAME)
        return if (f.exists()) f.readText() else null
    }

    fun clear(context: Context) {
        runCatching { File(context.applicationContext.filesDir, FILE_NAME).delete() }
    }
}
