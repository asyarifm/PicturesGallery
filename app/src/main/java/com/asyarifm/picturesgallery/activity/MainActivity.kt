package com.asyarifm.picturesgallery.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.adapter.PicturesGalleryAdapter
import com.asyarifm.picturesgallery.adapter.PicturesGalleryAdapter.Companion.SPAN_COUNT_ONE
import com.asyarifm.picturesgallery.adapter.PicturesGalleryAdapter.Companion.SPAN_COUNT_THREE
import com.asyarifm.picturesgallery.model.ItemPicture
import com.asyarifm.picturesgallery.viewmodel.PicturesGalleryViewModel

class MainActivity : AppCompatActivity() {

    private var picturesGalleryViewModel : PicturesGalleryViewModel? = null
    private var volleyQueue : RequestQueue? = null

    private var picturesRecyclerView : RecyclerView? = null
    private var picturesAdapter : PicturesGalleryAdapter? = null
    private var layoutManager : GridLayoutManager? = null

    private var loadingDialog : LoadingDialog? = null
    private var picturesList : ArrayList<ItemPicture>? = null

    private var searchQuery = ""
    private var currentPage = 1
    private var MAX_PAGES = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingDialog = LoadingDialog(this)
        volleyQueue = Volley.newRequestQueue(this)
        picturesGalleryViewModel = PicturesGalleryViewModel(this)

        //start loading dialog before load pictures
        loadingDialog!!.startLoading()

        //ensure start from first page
        currentPage = 1;
        picturesGalleryViewModel?.loadPictures(volleyQueue!!, currentPage)
        picturesGalleryViewModel!!.getUpdatedPictures()!!.observe(this, updatedPictureObserver)
        picturesGalleryViewModel!!.getLoadedFullImage()!!.observe(this, loadedFullImageView)

        layoutManager = GridLayoutManager(this, SPAN_COUNT_THREE)

        picturesList = ArrayList()
        picturesAdapter = PicturesGalleryAdapter(picturesList!!, layoutManager!!)
        picturesAdapter!!.setOnItemClickListener(object : PicturesGalleryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                loadingDialog!!.startLoading()
                picturesGalleryViewModel!!.dispalyFullImage(picturesList!!.get(position) , this@MainActivity)
            }
        })

        picturesAdapter!!.getFinishLoadPictures().observe(this, finishLoadPicturesObserver)

        picturesRecyclerView = findViewById(R.id.pictureRecyclerView)
        picturesRecyclerView?.setHasFixedSize(true)
        picturesRecyclerView?.setAdapter(picturesAdapter)
        picturesRecyclerView?.setLayoutManager(layoutManager)
        picturesRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView : RecyclerView, newState : Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && currentPage <= MAX_PAGES) {
                    if (searchQuery.isNullOrEmpty()) {
                        currentPage = currentPage + 1
                        picturesGalleryViewModel!!.loadPictures(volleyQueue!!, currentPage)
                    } else {
                        currentPage = currentPage + 1
                        picturesGalleryViewModel!!.searchPictures(volleyQueue!!, searchQuery, currentPage)
                    }
                }
            }
        });
    }

    private val updatedPictureObserver: Observer<ArrayList<ItemPicture>> = Observer {
        loadingDialog!!.isDismiss()
        if (it.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_connection), Toast.LENGTH_LONG).show()
        } else {
            if (currentPage > 1) {
                picturesList!!.addAll(it)
                picturesAdapter!!.updateList(picturesList!!)
            } else {
                picturesAdapter!!.updateList(it)
                picturesList = picturesAdapter!!.getItemList()
            }
        }
    }

    private val finishLoadPicturesObserver: Observer<Boolean> = Observer {

        if (!it) {
            Toast.makeText(this, getString(R.string.error_connection), Toast.LENGTH_LONG).show()
        }
    }

    private val loadedFullImageView: Observer<Boolean> = Observer {
        loadingDialog!!.isDismiss()
        if (!it) {
            Toast.makeText(this, getString(R.string.error_connection), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_switch_layout) {
            switchLayout()
            switchIcon(item)
            return true
        } else if (item.itemId == R.id.menu_search) {
            search(item)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun switchLayout() {
        if (layoutManager!!.getSpanCount() === SPAN_COUNT_ONE) {
            layoutManager!!.setSpanCount(SPAN_COUNT_THREE)
        } else {
            layoutManager!!.setSpanCount(SPAN_COUNT_ONE)
        }
        picturesAdapter?.let { picturesAdapter!!.notifyItemRangeChanged(0, it.getItemCount()) }
    }

    private fun switchIcon(item: MenuItem) {
        if (layoutManager!!.getSpanCount() === SPAN_COUNT_THREE) {
            item.icon = getDrawable(R.drawable.ic_baseline_view_list_24)
        } else {
            item.icon = getDrawable(R.drawable.ic_baseline_grid_on_24)
        }
    }

    private fun search(item: MenuItem) {
        val searchView = item.getActionView() as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //start loading dialog before load pictures
                loadingDialog!!.startLoading()
                searchQuery = query
                //ensure start from page 1
                currentPage = 1;
                picturesGalleryViewModel?.searchPictures(volleyQueue!!, query, currentPage)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

}