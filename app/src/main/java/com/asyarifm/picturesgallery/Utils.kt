package com.asyarifm.picturesgallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.util.Log
import com.asyarifm.picturesgallery.viewmodel.PicturesGalleryViewModel
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class Utils {
    companion object {
        val TAG = Utils::class.simpleName

        // download Bitmap from url
        fun downloadBitmap(imgUrl: String): Bitmap? {
            if (imgUrl.isNullOrEmpty()) {
                return null
            }

            // try and catch mode of unexpected error such as connection
            try {
                val conn : URLConnection = URL(imgUrl).openConnection()
                conn.connect()
                val inputStream : InputStream = conn.getInputStream()
                val bitmap : Bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                return bitmap
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.toString())
                return null
            }
        }

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                   return true
                }
            }
            return false
        }
    }
}