package com.kinan.mukhtar.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

/**
 * توليد مستند A4 (595 x 842 نقطة) لتأييد السكن.
 * يتم الرسم من اليمين إلى اليسار (Paint.Align.RIGHT) لدعم العربية.
 */
object PdfGenerator {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f

    fun buildDocumentText(
        governorate: String,
        district: String,
        region: String,
        personName: String,
        spouseName: String?,
        job: String,
        notes: String?
    ): String {
        val sb = StringBuilder()
        sb.appendLine("جمهورية العراق")
        sb.appendLine("محافظة $governorate")
        sb.appendLine("قضاء / ناحية $district")
        sb.appendLine("منطقة / حي $region")
        sb.appendLine()
        sb.appendLine("مضبطة تأييد سكن")
        sb.appendLine()
        sb.appendLine("نحن مختار واختيارية منطقة $region في $district / محافظة $governorate،")
        sb.appendLine("نشهد ونؤيد بأن المواطن ($personName) من سكنة منطقتنا،")
        if (!spouseName.isNullOrBlank()) {
            sb.appendLine("ويسكن مع زوجته ($spouseName) وأفراد عائلته في المنطقة أعلاه.")
        } else {
            sb.appendLine("ويسكن في المنطقة أعلاه.")
        }
        if (job.isNotBlank()) sb.appendLine("ومهنته: $job.")
        sb.appendLine()
        sb.appendLine("وقد جرى تأييدنا هذا بناءً على طلبه لتقديمه إلى الجهات المختصة،")
        sb.appendLine("دون أدنى مسؤولية على المختار تجاه الحقوق الشرعية والقانونية للغير.")
        if (!notes.isNullOrBlank()) {
            sb.appendLine()
            sb.appendLine("ملاحظات: $notes")
        }
        sb.appendLine()
        sb.appendLine("التاريخ: ${DateUtils.todayFormatted()}")
        sb.appendLine()
        sb.appendLine("مختار منطقة $region")
        sb.appendLine("التوقيع والختم: ..............................")
        return sb.toString()
    }

    /** يرسم النص على صفحة A4 ويعيد المستند */
    fun render(text: String, title: String = "مضبطة تأييد سكن"): PdfDocument {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val border = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.rgb(15, 81, 50)
        }
        canvas.drawRect(24f, 24f, PAGE_WIDTH - 24f, PAGE_HEIGHT - 24f, border)

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        canvas.drawText(title, PAGE_WIDTH / 2f, 70f, titlePaint)

        var y = 110f
        val right = PAGE_WIDTH - MARGIN
        val maxWidth = PAGE_WIDTH - (MARGIN * 2)
        text.split("\n").forEach { line ->
            wrap(line, bodyPaint, maxWidth).forEach { part ->
                if (y > PAGE_HEIGHT - MARGIN) return@forEach
                canvas.drawText(part, right, y, bodyPaint)
                y += 24f
            }
        }
        doc.finishPage(page)
        return doc
    }

    private fun wrap(line: String, paint: Paint, maxWidth: Float): List<String> {
        if (line.isBlank()) return listOf(" ")
        val words = line.split(" ")
        val out = mutableListOf<String>()
        var current = StringBuilder()
        words.forEach { w ->
            val candidate = if (current.isEmpty()) w else "$current $w"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) out += current.toString()
                current = StringBuilder(w)
            }
        }
        if (current.isNotEmpty()) out += current.toString()
        return out
    }

    /** حفظ الملف في مجلد التنزيلات عبر MediaStore */
    fun saveToDownloads(context: Context, text: String, fileName: String): Uri? {
        val doc = render(text)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
                resolver.openOutputStream(uri)?.use { doc.writeTo(it) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(dir, fileName)
                FileOutputStream(file).use { doc.writeTo(it) }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            null
        } finally {
            doc.close()
        }
    }

    /** كتابة المستند إلى Uri مختار عبر SAF */
    fun writeToUri(context: Context, uri: Uri, text: String): Boolean = try {
        val doc = render(text)
        context.contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) }
        doc.close()
        true
    } catch (e: Exception) { false }
}
