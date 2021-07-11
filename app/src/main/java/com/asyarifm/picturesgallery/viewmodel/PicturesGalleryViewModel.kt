package com.asyarifm.picturesgallery.viewmodel


import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.model.*
import com.google.gson.Gson
import org.json.JSONObject
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.text.FieldPosition
import kotlin.concurrent.thread


class PicturesGalleryViewModel(ctx: Context){

    companion object {
        val TAG = PicturesGalleryViewModel::class.simpleName
        const val BASE_URL = "https://api.unsplash.com/"
        const val ACCESS_KEY = "BKy7B9c2NVlGdSXfqzWumcH4-CaShaDQ1R9u9OxPOjo"
        const val SECRET_KEY = "VAlkiC_pxv6CSpSUZO3BmKlAZ2WFPKoqZgPoDUPdvt4"
    }

    private var updatedPictures : MutableLiveData<ArrayList<ItemPicture>> = MutableLiveData()
    private var loadedFullImage : MutableLiveData<Boolean> = MutableLiveData()

    private var ctx: Context? = null

    init {
        this.ctx = ctx
    }

    fun loadPictures(queue: RequestQueue, page:Int) {
        val fullUrl : String = BASE_URL + "photos" + "?client_id="+ ACCESS_KEY + "&page=" + page + "&per_page=12"
        Volley.newRequestQueue(ctx)

        var pictures : ArrayList<ItemPicture> = ArrayList()

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, fullUrl, null,
                { response ->
                    for (i in 0 until response.length()) {
                        val item: JSONObject = response.get(i) as JSONObject
                        val gson = Gson()
                        val itemPicture: ItemPicture = gson.fromJson(item.toString(), ItemPicture::class.java)

                        pictures.add(itemPicture)
                    }

                    updatedPictures.postValue(pictures)
                },
                { error ->
                    Log.e(TAG, "ERROR: " + error.toString())
                    updatedPictures.postValue(pictures)
                }
        )

        queue.add(jsonArrayRequest)
    }

    fun searchPictures(queue: RequestQueue, query: String, page:Int)  {
        val fullUrl : String = BASE_URL + "search/photos" + "?client_id="+ ACCESS_KEY + "&query=" + query + "&page=" + page + "&per_page=12"

        var pictures : ArrayList<ItemPicture> = ArrayList()

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, fullUrl, null,
                { response ->
                    val jsonArray = response.getJSONArray("results")
                    for (i in 0 until jsonArray.length()) {
                        val item: JSONObject = jsonArray.get(i) as JSONObject
                        val gson = Gson()
                        val itemPicture: ItemPicture = gson.fromJson(item.toString(), ItemPicture::class.java)

                        pictures.add(itemPicture)
                    }

                    updatedPictures.postValue(pictures)
                },
                { error ->
                    Log.e(TAG, "ERROR: " + error.toString())
                    updatedPictures.postValue(pictures)
                }
        )

        queue.add(jsonObjectRequest)
    }

    fun dispalyFullImage(item: ItemPicture, ctx: Context) {
        val fullImageView = View.inflate(ctx, R.layout.layout_full_image_view, null)
        val imageView : ImageView = fullImageView.findViewById(R.id.fullImageView)

        val fullImageDialog = AlertDialog.Builder(ctx)
        fullImageDialog.setView(fullImageView)

        val uiHandler = Handler(Looper.getMainLooper())
        thread(start = true) {
            val bitmap = item.urls.full?.let { downloadBitmap(it) }
            uiHandler.post {
                imageView!!.setImageBitmap(bitmap)
                loadedFullImage.postValue(true)
                fullImageDialog.show()
            }
        }
    }

    private fun downloadBitmap(imgUrl: String): Bitmap? {
        try {
            val conn : URLConnection = URL(imgUrl).openConnection()
            conn.connect()
            val inputStream : InputStream = conn.getInputStream()
            val bitmap : Bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.toString())
            loadedFullImage.postValue(false)
            return null
        }
    }

    fun getUpdatedPictures(): LiveData<ArrayList<ItemPicture>> {
        return updatedPictures
    }

    fun getLoadedFullImage(): LiveData<Boolean> {
        return loadedFullImage
    }
}