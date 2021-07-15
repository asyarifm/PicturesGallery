package com.asyarifm.picturesgallery.activity

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.asyarifm.picturesgallery.Constant.Companion.SPAN_COUNT_ONE
import com.asyarifm.picturesgallery.Constant.Companion.SPAN_COUNT_THREE
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.adapter.PicturesGalleryAdapter
import com.asyarifm.picturesgallery.model.ItemPicture
import com.asyarifm.picturesgallery.viewmodel.PicturesGalleryViewModel

class MainActivity : AppCompatActivity() {

    private var picturesGalleryViewModel : PicturesGalleryViewModel? = null
    private var volleyQueue : RequestQueue? = null

    private var picturesRecyclerView : RecyclerView? = null
    private var picturesAdapter : PicturesGalleryAdapter? = null
    private var layoutManager : GridLayoutManager? = null

    private var loadingDialog : LoadingDialog? = null

    //initial value for seqrchQuery and currentPage
    private var searchQuery = ""
    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init loading dialog, volleyQueue and picturesGalleryViewModel
        loadingDialog = LoadingDialog(this)
        volleyQueue = Volley.newRequestQueue(this)
        picturesGalleryViewModel = PicturesGalleryViewModel(this)

        //ensure start from first page
        //the function will return currentPage + 1 if success, otherwise currentpage remains the same
        currentPage = 1;
        currentPage = picturesGalleryViewModel?.loadPictures(volleyQueue, currentPage)!!

        //observe few items from ViewModel Class
        //These observe functions will get notified once the app finish the background job and will update the UI accordingly
        picturesGalleryViewModel!!.getOnUpdatePictures()!!.observe(this, updatedPictureObserver)
        picturesGalleryViewModel!!.getOnDisplayLoadingDialog()!!.observe(this, displayLoadingDialogObserver)
        picturesGalleryViewModel!!.getOnDisplayNotification()!!.observe(this, displayNotificationObserver)
        picturesGalleryViewModel!!.getOnLoadFullImage()!!.observe(this, loadFullImageObserver)

        //default layout is Grid View with 3 columns
        layoutManager = GridLayoutManager(this, SPAN_COUNT_THREE)
        picturesAdapter = PicturesGalleryAdapter(layoutManager!!)

        //set on item click listener to display full image in dialog
        picturesAdapter!!.setOnItemClickListener(object : PicturesGalleryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                picturesGalleryViewModel!!.loadFullImage(picturesAdapter!!.getItemList()!!.get(position))
            }
        })

        picturesRecyclerView = findViewById(R.id.pictureRecyclerView)
        picturesRecyclerView?.setHasFixedSize(true)
        picturesRecyclerView?.setAdapter(picturesAdapter)
        picturesRecyclerView?.setLayoutManager(layoutManager)

        // set listener for end of scroll event
        picturesRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView : RecyclerView, newState : Int) {
                super.onScrollStateChanged(recyclerView, newState);

                // if reach end of scroll, will load next page of image (12 images)
                // if success returns currentPage + 1, otherwise currentpage will remain as it is
                if (!recyclerView.canScrollVertically(1)) {
                    currentPage = picturesGalleryViewModel!!.loadExtraPage(currentPage, searchQuery, volleyQueue)
                }
            }
        });

        // onback press handler
        onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // load initial image once back button pressed
                //ensure start from first page and clear search query
                searchQuery = ""
                currentPage = 1
                currentPage = picturesGalleryViewModel?.loadPictures(volleyQueue, currentPage)!!
            }
        })
    }

    // create Options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // detect which icon is pressed by user
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_switch_layout) {
            // swicthing layout from grid ot list, vice versa
            switchLayout()
            // also update the displayed icon,
            // if current view is grid, list icon is displayed, vice versa
            switchIcon(item)
            return true
        } else if (item.itemId == R.id.menu_search) {
            // display text box to input query for image search
            search(item)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // swicthing layout from grid ot list, vice versa
    private fun switchLayout() {
        if (layoutManager!!.getSpanCount() === SPAN_COUNT_ONE) {
            layoutManager!!.setSpanCount(SPAN_COUNT_THREE)
        } else {
            layoutManager!!.setSpanCount(SPAN_COUNT_ONE)
        }

        picturesAdapter?.let { picturesAdapter!!.notifyItemRangeChanged(0, it.getItemCount()) }
    }

    // change the icon based on current View Mode
    private fun switchIcon(item: MenuItem) {
        if (layoutManager!!.getSpanCount() === SPAN_COUNT_THREE) {
            item.icon = getDrawable(R.drawable.ic_baseline_view_list_24)
        } else {
            item.icon = getDrawable(R.drawable.ic_baseline_grid_on_24)
        }
    }

    // display text box to input query for image search
    private fun search(item: MenuItem) {
        val searchView = item.getActionView() as SearchView

        // once user submit the query, start image searching process
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchQuery = query
                //ensure start from page 1
                currentPage = 1;
                currentPage = picturesGalleryViewModel?.searchPictures(volleyQueue, query, currentPage)!!
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    // display full image dialog
    private fun displayFullImageDialog(bitmap : Bitmap) {
        val fullImageView = View.inflate(this, R.layout.layout_full_image_view, null)
        val imageView : ImageView = fullImageView.findViewById(R.id.fullImageView)
        val fullImageDialog = AlertDialog.Builder(this)
        fullImageDialog.setView(fullImageView)

        imageView!!.setImageBitmap(bitmap)
        fullImageDialog.show()
    }

    // load full image observer
    private val loadFullImageObserver: Observer<Bitmap> = Observer {
        displayFullImageDialog(it)
    }

    // update pictures list received from viewmodel
    private val updatedPictureObserver: Observer<ArrayList<ItemPicture>> = Observer {
        picturesAdapter!!.updateList(it)
    }

    // Observer for display loading dialog
    private val displayLoadingDialogObserver: Observer<Boolean> = Observer {
        if (it) {
            loadingDialog!!.startLoading()
        } else {
            loadingDialog!!.isDismiss()
        }
    }

    // Observer for display notification
    private val displayNotificationObserver: Observer<String> = Observer {
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
    }
}