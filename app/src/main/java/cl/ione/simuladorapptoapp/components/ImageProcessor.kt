package cl.ione.simuladorapptoapp.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageProcessor {
    private const val TAG = "ImageProcessor"
    private const val MAX_WIDTH = 384  // Ancho máximo para impresora térmica
    private const val QUALITY = 80     // Calidad de compresión

    /**
     * Optimiza imagen para impresión térmica
     */
    fun optimizarParaImpresion(original: Bitmap): Bitmap {
        return try {
            // 1. Redimensionar
            val scale = MAX_WIDTH.toFloat() / original.width.toFloat()
            val newHeight = (original.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(original, MAX_WIDTH, newHeight, true)

            // 2. Convertir a escala de grises
            val grayBitmap = Bitmap.createBitmap(MAX_WIDTH, newHeight, Bitmap.Config.RGB_565)
            val canvas = Canvas(grayBitmap)
            canvas.drawColor(Color.WHITE)

            val paint = Paint().apply {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            }
            canvas.drawBitmap(scaled, 0f, 0f, paint)

            // 3. Binarizar (blanco y negro puro)
            for (x in 0 until MAX_WIDTH) {
                for (y in 0 until newHeight) {
                    val pixel = grayBitmap.getPixel(x, y)
                    val gray = (0.299 * Color.red(pixel) +
                            0.587 * Color.green(pixel) +
                            0.114 * Color.blue(pixel)).toInt()
                    grayBitmap.setPixel(x, y, if (gray > 128) Color.WHITE else Color.BLACK)
                }
            }

            scaled.recycle()
            grayBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizando imagen: ${e.message}")
            original
        }
    }

    /**
     * Convierte Bitmap a Base64
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        return try {
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, stream)
                val bytes = stream.toByteArray()
                Log.d(TAG, "Tamaño imagen: ${bytes.size} bytes")
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo a Base64: ${e.message}")
            ""
        }
    }

    /**
     * Genera imagen de texto simple (fallback)
     */
    fun generarImagenTexto(texto: String = "GETNET"): Bitmap {
        val width = 384
        val height = 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        Paint().apply {
            color = Color.BLACK
            textSize = 40f
            isAntiAlias = false
            typeface = android.graphics.Typeface.DEFAULT
            textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText(texto, width / 2f, 70f, this)
        }

        return bitmap
    }

    /**
     * Procesa imagen desde recurso
     */
    fun procesarImagenDesdeRecurso(bitmap: Bitmap?): String {
        return try {
            if (bitmap == null) return ""

            val optimizada = optimizarParaImpresion(bitmap)
            val base64 = bitmapToBase64(optimizada)

            optimizada.recycle()
            base64
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando imagen: ${e.message}")
            ""
        }
    }

    /**
     * Obtiene imagen por defecto del simulador
     */
    fun getImagenSimuladorBase64(): String {
        // Imagen PNG pequeña de "SIMULADOR" en Base64 (1x1 pixel transparente como fallback)
        return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
    }
}