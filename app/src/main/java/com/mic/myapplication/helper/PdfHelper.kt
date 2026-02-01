package com.mic.myapplication.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.graphics.createBitmap

object PdfHelper {

    fun getPdfList(file: File?, context: Context, zoom: Float = 0.25f): List<Bitmap> {
        val list: MutableList<Bitmap> = mutableListOf()
        val resources = context.resources

        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)?.let {
            val renderer = PdfRenderer(it)
            for (pageIndex in 0 until renderer.pageCount) {
                val page = renderer.openPage(pageIndex)
                val bitmap = createBitmap(
                    (resources.displayMetrics.densityDpi * page.width / 72 * zoom).toInt(),
                    (resources.displayMetrics.densityDpi * page.height / 72 * zoom).toInt()
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                list.add(bitmap)
                page.close()
            }
            renderer.close()
        }
        return list
    }

    suspend fun getPdfFromUrl(link: String?, file: File): File = suspendCoroutine { continuation ->
        var inputStream: InputStream? = null
        try {
            val url = URL(link)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection
            if (urlConnection.responseCode == 200) {
                inputStream = BufferedInputStream(urlConnection.inputStream)
            }
            val output = FileOutputStream(file)
            inputStream?.copyTo(output)
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        continuation.resume(file)
    }

}