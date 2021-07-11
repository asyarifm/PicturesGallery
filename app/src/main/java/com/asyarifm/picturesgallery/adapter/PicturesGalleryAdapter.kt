package com.asyarifm.picturesgallery.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.model.ItemPicture
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import kotlin.concurrent.thread

class PicturesGalleryAdapter(itemList: ArrayList<ItemPicture>, layoutManager: GridLayoutManager) :
    RecyclerView.Adapter<PicturesGalleryAdapter.ItemHolder>() {

    companion object {
        val TAG = PicturesGalleryAdapter::class.simpleName
        const val SPAN_COUNT_ONE = 1
        const val SPAN_COUNT_THREE = 3
    }

    private var itemList : ArrayList<ItemPicture>? = null
    private var layoutManager : GridLayoutManager? = null
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private val VIEW_TYPE_LIST = 1
    private val VIEW_TYPE_GRID = 2

    private var numOfImageLoaded : Int? = null
    private var finishLoadPictures : MutableLiveData<Boolean>? = null


    init {
        this.itemList = itemList
        this.layoutManager = layoutManager
        this.numOfImageLoaded = 0
        this.finishLoadPictures = MutableLiveData()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateList(searchlist: ArrayList<ItemPicture>) {
        numOfImageLoaded = 0
        this.itemList = searchlist
        notifyDataSetChanged()
    }

    class ItemHolder(itemView: View, listener : PicturesGalleryAdapter.OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var usernameTextView: TextView? = null
        var descriptionTextView: TextView? = null
        var imageView: ImageView? = null

        init {
            usernameTextView = itemView.findViewById(R.id.usernameTextView)
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView)
            imageView = itemView.findViewById(R.id.pictureImageView)

            itemView.setOnClickListener { view: View? ->
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val spanCount: Int = layoutManager!!.getSpanCount()
        return if (spanCount == SPAN_COUNT_ONE) {
            VIEW_TYPE_LIST
        } else {
            VIEW_TYPE_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {

        var v: View? = null
        if (viewType == VIEW_TYPE_LIST) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_card_picture_list, parent, false)
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_card_picture_grid, parent, false)
        }

        return ItemHolder(v, listener!!)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentItem : ItemPicture = itemList!!.get(position)

        holder.usernameTextView!!.setText(currentItem.user.username)

        var desc : String? = currentItem.description
        if (desc.isNullOrEmpty()) {
            desc = currentItem.alt_description
        }
        holder.descriptionTextView!!.setText(desc)

        val uiHandler = Handler(Looper.getMainLooper())
        thread(start = true) {
            val bitmap = currentItem.urls.thumb?.let { downloadBitmap(it) }
            uiHandler.post {
                holder.imageView!!.setImageBitmap(bitmap)

                numOfImageLoaded = numOfImageLoaded!! + 1
                if (numOfImageLoaded!! >= itemList!!.size) {
                    finishLoadPictures!!.postValue(true)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }

    fun getItemList() : ArrayList<ItemPicture> {
        return itemList!!
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
            finishLoadPictures!!.postValue(false)
            return null
        }
    }

    fun getFinishLoadPictures(): LiveData<Boolean> {
        return finishLoadPictures!!
    }
}