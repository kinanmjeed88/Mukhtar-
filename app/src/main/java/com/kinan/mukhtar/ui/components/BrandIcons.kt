package com.kinan.mukhtar.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** أيقونات العلامات التجارية الرسمية كمسارات متجهة */
object BrandIcons {

    val Telegram: ImageVector by lazy {
        ImageVector.Builder(
            name = "Telegram",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9.78f, 18.65f)
                lineToRelative(0.28f, -4.23f)
                lineToRelative(7.68f, -6.92f)
                curveToRelative(0.34f, -0.31f, -0.07f, -0.46f, -0.52f, -0.19f)
                lineTo(7.74f, 13.3f)
                lineTo(3.64f, 12f)
                curveToRelative(-0.88f, -0.25f, -0.89f, -0.86f, 0.2f, -1.3f)
                lineToRelative(15.97f, -6.16f)
                curveToRelative(0.73f, -0.33f, 1.43f, 0.18f, 1.15f, 1.3f)
                lineToRelative(-2.72f, 12.81f)
                curveToRelative(-0.19f, 0.91f, -0.74f, 1.13f, -1.5f, 0.71f)
                lineTo(12.6f, 16.3f)
                lineToRelative(-1.99f, 1.93f)
                curveToRelative(-0.23f, 0.23f, -0.42f, 0.42f, -0.83f, 0.42f)
                close()
            }
        }.build()
    }

    val YouTube: ImageVector by lazy {
        ImageVector.Builder(
            name = "YouTube",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(23.5f, 6.19f)
                arcToRelative(3.02f, 3.02f, 0f, false, false, -2.12f, -2.14f)
                curveTo(19.5f, 3.55f, 12f, 3.55f, 12f, 3.55f)
                reflectiveCurveToRelative(-7.5f, 0f, -9.38f, 0.5f)
                arcTo(3.02f, 3.02f, 0f, false, false, 0.5f, 6.19f)
                curveTo(0f, 8.08f, 0f, 12f, 0f, 12f)
                reflectiveCurveToRelative(0f, 3.92f, 0.5f, 5.81f)
                arcToRelative(3.02f, 3.02f, 0f, false, false, 2.12f, 2.14f)
                curveToRelative(1.88f, 0.5f, 9.38f, 0.5f, 9.38f, 0.5f)
                reflectiveCurveToRelative(7.5f, 0f, 9.38f, -0.5f)
                arcToRelative(3.02f, 3.02f, 0f, false, false, 2.12f, -2.14f)
                curveTo(24f, 15.92f, 24f, 12f, 24f, 12f)
                reflectiveCurveToRelative(0f, -3.92f, -0.5f, -5.81f)
                close()
                moveTo(9.55f, 15.57f)
                verticalLineTo(8.43f)
                lineTo(15.82f, 12f)
                close()
            }
        }.build()
    }
}
