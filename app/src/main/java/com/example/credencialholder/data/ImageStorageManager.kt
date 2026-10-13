package com.example.credencialholder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    private val credentialsDir: File
        get() {
            val dir = File(context.filesDir, "credentials")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    val cameraCacheDir: File
        get() {
            val dir = File(context.cacheDir, "camera")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun createTempCameraFile(): File {
        return File(cameraCacheDir, "temp_capture_${UUID.randomUUID()}.jpg")
    }

    /**
     * Copies and optimizes an image from a content Uri (camera or gallery) into app internal storage.
     */
    fun saveImageFromUri(uri: Uri, credentialId: String, isBack: Boolean): String {
        val fileName = "${credentialId}_${if (isBack) "back" else "front"}.jpg"
        val destFile = File(credentialsDir, fileName)

        val tempFile = File.createTempFile("temp_in_", ".jpg", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val orientation = getExifOrientation(tempFile.absolutePath)
            val bitmap = decodeSampledBitmap(tempFile.absolutePath, 1400, 900)
            val rotatedBitmap = if (orientation != 0 && bitmap != null) {
                rotateBitmap(bitmap, orientation)
            } else {
                bitmap
            }

            if (rotatedBitmap != null) {
                FileOutputStream(destFile).use { out ->
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                if (rotatedBitmap != bitmap) {
                    rotatedBitmap.recycle()
                }
                bitmap?.recycle()
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }

        return destFile.absolutePath
    }

    /**
     * Decodes a bitmap scaled for widget or display.
     */
    fun loadBitmap(path: String?, reqWidth: Int = 800, reqHeight: Int = 500): Bitmap? {
        if (path.isNullOrEmpty()) return null
        val file = File(path)
        if (!file.exists()) return null

        return decodeSampledBitmap(path, reqWidth, reqHeight)
    }

    fun deleteCredentialImages(frontPath: String?, backPath: String?) {
        if (!frontPath.isNullOrEmpty()) {
            val f = File(frontPath)
            if (f.exists()) f.delete()
        }
        if (!backPath.isNullOrEmpty()) {
            val b = File(backPath)
            if (b.exists()) b.delete()
        }
    }

    private fun getExifOrientation(filePath: String): Int {
        return try {
            val exif = ExifInterface(filePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Generates a sample realistic credential card bitmap and saves to internal storage.
     */
    fun createSampleCardImage(
        credentialId: String,
        isBack: Boolean,
        title: String,
        name: String,
        number: String,
        primaryHex: Int,
        secondaryHex: Int
    ): String {
        val width = 1000
        val height = 630 // ISO 7810 aspect ratio ~ 1.586
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Card base with subtle gradient
        val bgGradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            AndroidColor.rgb(245, 247, 250), AndroidColor.rgb(226, 232, 240),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgGradient
        val cardRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(cardRect, 28f, 28f, paint)
        paint.shader = null

        // Header ribbon
        paint.color = primaryHex
        val ribbonRect = RectF(0f, 0f, width.toFloat(), 130f)
        canvas.drawRoundRect(ribbonRect, 28f, 28f, paint)
        canvas.drawRect(0f, 80f, width.toFloat(), 130f, paint)

        // Accent strip
        paint.color = secondaryHex
        canvas.drawRect(0f, 130f, width.toFloat(), 145f, paint)

        // Header Title
        paint.color = AndroidColor.WHITE
        paint.textSize = 38f
        paint.isFakeBoldText = true
        canvas.drawText(title.uppercase(), 40f, 75f, paint)

        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("CREDENCIAL OFICIAL Y VIGENTE", 40f, 110f, paint)

        if (!isBack) {
            // FRONT DESIGN
            // Photo placeholder box
            val photoRect = RectF(50f, 180f, 290f, 500f)
            paint.color = AndroidColor.rgb(203, 213, 225)
            canvas.drawRoundRect(photoRect, 16f, 16f, paint)

            // Photo avatar silhouette
            paint.color = AndroidColor.rgb(148, 163, 184)
            canvas.drawCircle(170f, 290f, 55f, paint)
            val bodyRect = RectF(95f, 370f, 245f, 500f)
            canvas.drawRoundRect(bodyRect, 40f, 40f, paint)

            // Text info
            paint.color = AndroidColor.rgb(100, 116, 139)
            paint.textSize = 20f
            paint.isFakeBoldText = false
            canvas.drawText("NOMBRE DEL TITULAR:", 330f, 220f, paint)

            paint.color = AndroidColor.rgb(15, 23, 42)
            paint.textSize = 34f
            paint.isFakeBoldText = true
            canvas.drawText(name, 330f, 265f, paint)

            paint.color = AndroidColor.rgb(100, 116, 139)
            paint.textSize = 20f
            paint.isFakeBoldText = false
            canvas.drawText("FOLIO / IDENTIFICADOR:", 330f, 325f, paint)

            paint.color = AndroidColor.rgb(30, 41, 59)
            paint.textSize = 30f
            paint.isFakeBoldText = true
            canvas.drawText(number, 330f, 365f, paint)

            // Security Chip simulation
            paint.color = AndroidColor.rgb(234, 179, 8) // Gold
            val chipRect = RectF(330f, 410f, 430f, 490f)
            canvas.drawRoundRect(chipRect, 12f, 12f, paint)
            paint.color = AndroidColor.rgb(161, 98, 7)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawRoundRect(chipRect, 12f, 12f, paint)
            canvas.drawLine(330f, 450f, 430f, 450f, paint)
            canvas.drawLine(380f, 410f, 380f, 490f, paint)
            paint.style = Paint.Style.FILL

            // Barcode simulation at bottom right
            paint.color = AndroidColor.rgb(15, 23, 42)
            var startX = 600f
            val barWidths = listOf(4f, 8f, 14f, 6f, 10f, 5f, 12f, 4f, 16f, 8f, 6f, 12f, 4f, 10f, 6f, 14f, 5f, 8f)
            for (bw in barWidths) {
                canvas.drawRect(startX, 420f, startX + bw, 490f, paint)
                startX += bw + 7f
            }
        } else {
            // BACK DESIGN
            // Magnetic stripe
            paint.color = AndroidColor.rgb(30, 41, 59)
            canvas.drawRect(0f, 170f, width.toFloat(), 270f, paint)

            // Signature strip
            paint.color = AndroidColor.rgb(255, 255, 255)
            val sigRect = RectF(50f, 310f, 750f, 390f)
            canvas.drawRect(sigRect, paint)
            paint.color = AndroidColor.rgb(148, 163, 184)
            paint.textSize = 20f
            canvas.drawText("FIRMA AUTORIZADA DEL TITULAR", 60f, 355f, paint)

            // Legal text lines
            paint.color = AndroidColor.rgb(100, 116, 139)
            paint.textSize = 17f
            paint.isFakeBoldText = false
            canvas.drawText("Esta credencial es personal e intransferible. En caso de extravío,", 50f, 440f, paint)
            canvas.drawText("favor de reportar de inmediato a las autoridades correspondientes.", 50f, 470f, paint)
            canvas.drawText("Válida únicamente con identificación oficial con fotografía vigente.", 50f, 500f, paint)
            canvas.drawText("EMISIÓN DIGITAL CERTIFICADA - NÚMERO DE CONTROL: $number", 50f, 550f, paint)
        }

        // Save to file
        val fileName = "${credentialId}_${if (isBack) "back" else "front"}.jpg"
        val destFile = File(credentialsDir, fileName)
        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        bitmap.recycle()
        return destFile.absolutePath
    }
}
