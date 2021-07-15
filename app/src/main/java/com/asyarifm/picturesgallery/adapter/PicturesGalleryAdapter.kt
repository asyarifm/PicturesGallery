package com.asyarifm.picturesgallery.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asyarifm.picturesgallery.Constant.Companion.SPAN_COUNT_ONE
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.Utils
import com.asyarifm.picturesgallery.model.ItemPicture
import kotlin.concurrent.thread

// adapter to hold recycleView data
class PicturesGalleryAdapter(layoutManager: GridLayoutManager) :
    RecyclerView.Adapter<PicturesGalleryAdapter.ItemHolder>() {

    private var itemList : ArrayList<ItemPicture>? = null
    private var layoutManager : GridLayoutManager? = null
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private val VIEW_TYPE_LIST = 1
    private val VIEW_TYPE_GRID = 2

    private var numOfImageLoaded : Int? = null

    init {
        this.itemList = ArrayList()
        this.layoutManager = layoutManager
        this.numOfImageLoaded = 0
    }

    //on click listener for each item
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    //update item list
    fun updateList(searchlist: ArrayList<ItemPicture>) {
        numOfImageLoaded = 0
        this.itemList = searchlist
        notifyDataSetChanged()
    }

    //item view holder for each item
    class ItemHolder(itemView: View, listener : PicturesGalleryAdapter.OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var usernameTextView: TextView? = null
        var descriptionTextView: TextView? = null
        var imageView: ImageView? = null

        init {
            // find all View in view holder
            usernameTextView = itemView.findViewById(R.id.usernameTextView)
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView)
            imageView = itemView.findViewById(R.id.pictureImageView)

            //set on click listener for each item
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

        // if span is 1 view type returns VIEW_TYPE_LIST otherwise returns VIEW_TYPE_GRID
        return if (spanCount == SPAN_COUNT_ONE) {
            VIEW_TYPE_LIST
        } else {
            VIEW_TYPE_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        var v: View? = null
        if (viewType == VIEW_TYPE_LIST) {
            // if viewType is VIEW_TYPE_LIST inflate list layout
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_card_picture_list, parent, false)
        } else {
            // otherwise inflate grid layout
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_card_picture_grid, parent, false)
        }

        return ItemHolder(v, listener!!)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentItem : ItemPicture = itemList!!.get(position)

        holder.usernameTextView!!.setText(currentItem.user.username)

        //if decription is empty, display alt_description
        var desc : String? = currentItem.description
        if (desc.isNullOrEmpty()) {
            desc = currentItem.alt_description
        }
        holder.descriptionTextView!!.setText(desc)

        // create a thread for each item to download image bitmap from url
        val uiHandler = Handler(Looper.getMainLooper())
        thread(start = true) {
            // from grid and list view, use thumbnail url to save memory
            val bitmap = currentItem.urls.thumb?.let { Utils.downloadBitmap(it) }
            uiHandler.post {
                // display image in imageview
                holder.imageView!!.setImageBitmap(bitmap)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }

    fun getItemList() : ArrayList<ItemPicture> {
        return itemList!!
    }
}