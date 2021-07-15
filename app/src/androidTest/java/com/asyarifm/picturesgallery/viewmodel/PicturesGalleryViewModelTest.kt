package com.asyarifm.picturesgallery.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.asyarifm.picturesgallery.Constant
import com.asyarifm.picturesgallery.Utils
import com.asyarifm.picturesgallery.model.ItemPicture
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test

class PicturesGalleryViewModelTest {

    val ctx = ApplicationProvider.getApplicationContext<Context>()
    var picturesGalleryViewModel : PicturesGalleryViewModel? = null
    var volleyQueue : RequestQueue? = null

    var picturesList = ArrayList<ItemPicture>()
    var currentPage = 1

    @Before
    fun setUp() {
        picturesGalleryViewModel = PicturesGalleryViewModel(ctx)
        volleyQueue = Volley.newRequestQueue(ctx)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun loadPicturesTest() {
        currentPage = 1
        // if success result will be current page + 1
        val result = picturesGalleryViewModel!!.loadPictures(volleyQueue, currentPage)
        assertEquals(result, currentPage + 1)

        //wait for observer update
        Thread.sleep(3000)

        assertEquals(picturesGalleryViewModel!!.getOnUpdatePictures().value!!.size, 12 * currentPage)
        picturesList = picturesGalleryViewModel!!.getOnUpdatePictures().value!!

        currentPage = result
    }

    @Test
    fun searchPictureTest() {
        currentPage = 1
        // if success result will be current page + 1
        val result = picturesGalleryViewModel!!.searchPictures(volleyQueue, "car", currentPage)
        assertEquals(result, currentPage + 1)

        //wait for observer update
        Thread.sleep(3000)
        assertEquals(picturesGalleryViewModel!!.getOnUpdatePictures().value!!.size, 12 * currentPage)
        picturesList = picturesGalleryViewModel!!.getOnUpdatePictures().value!!

        currentPage = result
    }

    @Test
    fun loadFullImageTest() {
        // must run loadPictureTest() or searchPictureTest()
        loadPicturesTest()
        val result = picturesGalleryViewModel!!.loadFullImage(picturesList.get(2))
        assertEquals(result, Constant.SUCCESS)

        //wait for observer update
        Thread.sleep(3000)
        assertNotNull(picturesGalleryViewModel!!.getOnLoadFullImage().value!!)
    }

    @Test
    fun loadExtraPageTest() {
        // must run loadPictureTest() or searchPictureTest()
        loadPicturesTest()

        // if success result will be current page + 1
        val result = picturesGalleryViewModel!!.loadExtraPage(currentPage, "", volleyQueue!!)
        assertEquals(result, currentPage + 1)

        //wait for observer update
        Thread.sleep(3000)
        assertEquals(picturesGalleryViewModel!!.getOnUpdatePictures().value!!.size, 12 * currentPage)
    }

    @Test
    fun downloadImageBitmapTest() {
        // must run loadPictureTest() or searchPictureTest()
        loadPicturesTest()

        var result = picturesList.get(3).urls.full?.let { Utils.downloadBitmap(it) }
        assertNotNull(result)
    }

    @Test
    fun loadPicturesFailTest() {
        currentPage = 0
        // if success result will be current page + 1, otherwise return the currentPage
        var result = picturesGalleryViewModel!!.loadPictures(volleyQueue, currentPage)
        assertEquals(result, currentPage)

        result = picturesGalleryViewModel!!.loadPictures(null, currentPage)
        assertEquals(result, currentPage)
    }

    @Test
    fun searchPictureFailTest() {
        currentPage = 0
        // if success result will be current page + 1
        var result = picturesGalleryViewModel!!.searchPictures(volleyQueue, "car", currentPage)
        assertEquals(result, currentPage)

        result = picturesGalleryViewModel!!.searchPictures(null, "car", currentPage)
        assertEquals(result, currentPage)

        result = picturesGalleryViewModel!!.searchPictures(volleyQueue, "", currentPage)
        assertEquals(result, currentPage)
    }

    @Test
    fun loadFullImageFailTest() {
        val result = picturesGalleryViewModel!!.loadFullImage(null)
        assertEquals(result, Constant.ERROR_INVALID_INPUT_NULL)
    }

    @Test
    fun loadExtraPageFailTest() {
        currentPage = 0
        // if success result will be current page + 1, otherwise return currentpage
        var result = picturesGalleryViewModel!!.loadExtraPage(currentPage, "", volleyQueue!!)
        assertEquals(result, currentPage)

        result = picturesGalleryViewModel!!.loadExtraPage(currentPage, "", null)
        assertEquals(result, currentPage)
    }

    @Test
    fun downloadImageBitmapFailTest() {
        var result = Utils.downloadBitmap("")
        assertEquals(result, null)
    }
}