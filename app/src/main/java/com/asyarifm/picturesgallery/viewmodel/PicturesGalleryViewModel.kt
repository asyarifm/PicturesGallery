package com.asyarifm.picturesgallery.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.asyarifm.picturesgallery.Constant
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.Utils
import com.asyarifm.picturesgallery.model.*
import com.google.gson.Gson
import org.json.JSONObject
import kotlin.concurrent.thread


class PicturesGalleryViewModel(ctx : Context) : ViewModel() {

    private var ctx : Context? = null

    private var onUpdatePictures : MutableLiveData<ArrayList<ItemPicture>> = MutableLiveData()
    private var onDisplayLoadingDialog : MutableLiveData<Boolean> = MutableLiveData()
    private var onDisplayNotification : MutableLiveData<String> = MutableLiveData()
    private var onLoadFullImage : MutableLiveData<Bitmap> = MutableLiveData()

    private var currentPictureList : ArrayList<ItemPicture> = ArrayList()

    // unsplash api detail
    private val BASE_URL = "https://api.unsplash.com/"
    private val ACCESS_KEY = "BKy7B9c2NVlGdSXfqzWumcH4-CaShaDQ1R9u9OxPOjo"
    private val SECRET_KEY = "VAlkiC_pxv6CSpSUZO3BmKlAZ2WFPKoqZgPoDUPdvt4"

    private val TAG = PicturesGalleryViewModel::class.simpleName

    init {
        this.ctx = ctx
    }

    // function to initially load images, return page + 1 if success otherwise return page
    fun loadPictures(queue: RequestQueue?, page:Int) : Int {
        //connectionCheck
        if (ctx?.let { Utils.isOnline(it) } == false) {
            onDisplayNotification.postValue(ctx!!.getString(R.string.error_connection))
            return page
        }

        //params checker
        if (queue == null || page == null) {
            return page
        }
        if (page < 1 || page > Constant.MAX_PAGES) {
            return page
        }

        // show loading dialog
        onDisplayLoadingDialog.postValue(true)

        //page will start with 1, 12 images per page
        val fullUrl : String = BASE_URL + "photos" + "?client_id="+ ACCESS_KEY + "&page=" + page + "&per_page=12"

        var pictures : ArrayList<ItemPicture> = ArrayList()
        // get response as json array
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, fullUrl, null,
                { response ->
                    for (i in 0 until response.length()) {
                        // loop for each item and put the value into specific parameter using Gson Serialization
                        val item: JSONObject = response.get(i) as JSONObject
                        val gson = Gson()
                        val itemPicture: ItemPicture = gson.fromJson(item.toString(), ItemPicture::class.java)

                        // add item into list
                        pictures.add(itemPicture)
                    }

                    if (page > 1 && !currentPictureList.isEmpty()) {
                        currentPictureList.addAll(pictures)
                    } else {
                        currentPictureList = pictures
                    }

                    // send the picturelist to MainActivity
                    onUpdatePictures.postValue(currentPictureList)

                    // hide loading dialog
                    onDisplayLoadingDialog.postValue(false)
                },
                { error ->
                    Log.e(TAG, "ERROR: " + error.toString())
                    // hide loading dialog
                    onDisplayLoadingDialog.postValue(false)

                    // display notification to user
                    onDisplayNotification.postValue(ctx!!.getString(R.string.error_connection))
                }
        )

        // add the request to volley queue
        queue.add(jsonArrayRequest)

        return page + 1
    }

    // function to search images based on query, return page + 1 if success otherwise return page
    fun searchPictures(queue: RequestQueue?, query: String, page:Int) : Int {
        //connectionCheck
        if (ctx?.let { Utils.isOnline(it) } == false) {
            onDisplayNotification.postValue(ctx!!.getString(R.string.error_connection))
            return page
        }

        //params checker
        if (queue == null || page == null || query.isNullOrEmpty()) {
            return page
        }

        if (page < 1 || page > Constant.MAX_PAGES) {
            return page
        }

        // show loading dialog
        onDisplayLoadingDialog.postValue(true)

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

                    if (page > 1 && !currentPictureList.isEmpty()) {
                        currentPictureList.addAll(pictures)
                    } else {
                        currentPictureList = pictures
                    }

                    // send the picturelist to MainActivity
                    onUpdatePictures.postValue(currentPictureList)

                    // hide loading dialog
                    onDisplayLoadingDialog.postValue(false)
                },
                { error ->
                    Log.e(TAG, "ERROR: " + error.toString())

                    // hide loading dialog
                    onDisplayLoadingDialog.postValue(false)

                    // display notification to user
                    onDisplayNotification.postValue(ctx!!.getString(R.string.error_connection))
                }
        )

        queue.add(jsonObjectRequest)

        return page + 1
    }


    // function to display full image
    fun loadFullImage(item: ItemPicture?) : Int {
        //connectionCheck
        if (ctx?.let { Utils.isOnline(it) } == false) {
            onDisplayNotification.postValue(ctx!!.getString(R.string.error_connection))
            return Constant.ERROR_NO_INTERNET_CONECTION
        }

        //params checker
        if (item == null) {
            return Constant.ERROR_INVALID_INPUT_NULL
        }

        // show loading dialog
        onDisplayLoadingDialog.postValue(true)

        val uiHandler = Handler(Looper.getMainLooper())
        thread(start = true) {
            val bitmap = item.urls.full?.let { Utils.downloadBitmap(it) }
            uiHandler.post {
                // hide loading dialog
                onDisplayLoadingDialog.postValue(false)

                if (bitmap == null) {
                    // display notification to user
                    onDisplayNotification.postValue(ctx!!.getString(R.string.error_connection))
                } else {
                    onLoadFullImage.postValue(bitmap)
                }
            }
        }

        return Constant.SUCCESS
    }

    // function to load an extra page (12 Pictures), return number of page loaded, if fail return current number of page
    fun loadExtraPage(currentPage: Int, searchQuery: String, queue: RequestQueue?) : Int {
        if (currentPage == null || searchQuery == null || queue == null) {
            return currentPage
        }

        var curPage = currentPage

        if (curPage <= Constant.MAX_PAGES && curPage > 1) {
            if (searchQuery.isNullOrEmpty()) {
                if (loadPictures(queue!!, currentPage) != (curPage + 1)) {
                    return curPage
                }
            } else {
                if (searchPictures(queue!!, searchQuery, currentPage) != (curPage + 1)) {
                    return curPage
                }
            }

            curPage += 1
        }

        return curPage
    }

    //live data for updated picture list
    fun getOnLoadFullImage(): LiveData<Bitmap> {
        return onLoadFullImage
    }

    //live data for updated picture list
    fun getOnUpdatePictures(): LiveData<ArrayList<ItemPicture>> {
        return onUpdatePictures
    }

    //live data for dsiplay loading dialog
    fun getOnDisplayLoadingDialog(): LiveData<Boolean> {
        return onDisplayLoadingDialog
    }

    //live data for dsiplay notification
    fun getOnDisplayNotification(): LiveData<String> {
        return onDisplayNotification
    }
}