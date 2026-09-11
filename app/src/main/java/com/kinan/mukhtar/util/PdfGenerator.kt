package com.kinan.mukhtar.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
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
 * يدعم حجم خط متغير، أنماط إطار متعددة، وخلفية صورة من المعرض.
 * الرسم من اليمين إلى اليسار (Paint.Align.RIGHT) لدعم العربية.
 */
object PdfGenerator {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f

    fun buildDocumentText(
        governorate: String,
        district: String,
        region: String,
        mukhtarName: String,
        personName: String,
        spouseName: String?,
        job: String,
        notes: String?
    ): String = buildString {
        appendLine("جمهورية العراق")
        appendLine("محافظة $governorate")
        appendLine("قضاء / ناحية $district")
        appendLine("منطقة / حي $region")
        appendLine()
        appendLine("مضبطة تأييد سكن")
        appendLine()
        appendLine("نحن مختار واختيارية منطقة $region في $district / محافظة $governorate،")
        appendLine("نشهد ونؤيد بأن المواطن ($personName) من سكنة منطقتنا،")
        if (!spouseName.isNullOrBlank()) {
            appendLine("ويسكن مع زوجته ($spouseName) وأفراد عائلته في المنطقة أعلاه.")
        } else {
            appendLine("ويسكن في المنطقة أعلاه.")
        }
        if (job.isNotBlank()) appendLine("ومهنته: $job.")
        appendLine()
        appendLine("وقد جرى تأييدنا هذا بناءً على طلبه لتقديمه إلى الجهات المختصة،")
        appendLine("دون أدنى مسؤولية على المختار تجاه الحقوق الشرعية والقانونية للغير.")
        if (!notes.isNullOrBlank()) {
            appendLine()
            appendLine("ملاحظات: $notes")
        }
        appendLine()
        appendLine("التاريخ: ${DateUtils.todayFormatted()}")
        appendLine()
        appendLine("مختار منطقة $region")
        if (mukhtarName.isNotBlank()) appendLine("الاسم: $mukhtarName")
        appendLine("التوقيع والختم: ..............................")
    }

    /** يرسم النص على صفحة A4 وفق النمط المحدد */
    fun render(
        context: Context,
        text: String,
        style: DocumentStyle,
        title: String = "مضبطة تأييد سكن"
    ): PdfDocument {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        // 1) خلفية الصورة تُرسم أولاً خلف كل شيء
        if (style.border == BorderStyle.IMAGE && style.backgroundUri != null) {
            loadBitmap(context, style.backgroundUri)?.let { bmp ->
                canvas.drawBitmap(
                    bmp,
                    null,
                    Rect(0, 0, PAGE_WIDTH, PAGE_HEIGHT),
                    Paint(Paint.FILTER_BITMAP_FLAG)
                )
                bmp.recycle()
            }
        } else {
            drawBorder(canvas, style.border)
        }

        // 2) النص
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = style.fontSize + 6f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = style.fontSize
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        canvas.drawText(title, PAGE_WIDTH / 2f, MARGIN + style.fontSize + 14f, titlePaint)

        val lineHeight = style.fontSize * 1.7f
        var y = MARGIN + (style.fontSize * 3f)
        val right = PAGE_WIDTH - MARGIN
        val maxWidth = PAGE_WIDTH - (MARGIN * 2)

        text.split("\n").forEach { line ->
            wrap(line, bodyPaint, maxWidth).forEach { part ->
                if (y <= PAGE_HEIGHT - MARGIN) {
                    canvas.drawText(part, right, y, bodyPaint)
                    y += lineHeight
                }
            }
        }

        doc.finishPage(page)
        return doc
    }

    private fun drawBorder(canvas: android.graphics.Canvas, border: BorderStyle) {
        if (border == BorderStyle.NONE || border == BorderStyle.IMAGE) return

        val stroke = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = 2f
            color = Color.rgb(15, 81, 50)
        }

        val outer = 22f
        when (border) {
            BorderStyle.SINGLE ->
                canvas.drawRect(outer, outer, PAGE_WIDTH - outer, PAGE_HEIGHT - outer, stroke)

            BorderStyle.DOUBLE -> {
                canvas.drawRect(outer, outer, PAGE_WIDTH - outer, PAGE_HEIGHT - outer, stroke)
                val inner = outer + 7f
                canvas.drawRect(inner, inner, PAGE_WIDTH - inner, PAGE_HEIGHT - inner, stroke)
            }

            BorderStyle.SOLID_DASHED -> {
                canvas.drawRect(outer, outer, PAGE_WIDTH - outer, PAGE_HEIGHT - outer, stroke)
                val dashed = Paint(stroke).apply {
                    strokeWidth = 1.5f
                    pathEffect = DashPathEffect(floatArrayOf(10f, 6f), 0f)
                }
                val inner = outer + 8f
                canvas.drawRect(inner, inner, PAGE_WIDTH - inner, PAGE_HEIGHT - inner, dashed)
            }

            else -> Unit
        }
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

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
    fun saveToDownloads(
        context: Context,
        text: String,
        style: DocumentStyle,
        fileName: String
    ): Uri? {
        val doc = render(context, text, style)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return null
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
}
