package com.experion.cameraxlibrary.adapter

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import com.experion.cameraxlibrary.constant.Constant
import kotlinx.android.synthetic.main.image_list.view.*
import android.app.Activity
import com.bumptech.glide.Glide
import com.experion.cameraxlibrary.R
import java.io.File


class GalleryImageAdapter(val items: ArrayList<String>, val context: Context) :
    RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.image_list, parent, false)
        return ViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position] == (null)) {
        } else {
            val imageUri = Uri.parse(items[position])
            Glide.with(context)
                .load(File(imageUri.getPath()))
                .placeholder(R.drawable.ic_photo_library_black_24dp)
                .into(holder?.galleryImage)
            holder.linearLayout.setOnClickListener {

                val broadcastImagePathIntent = Intent(Constant.ACTION_IMAGE_PATH)
                broadcastImagePathIntent.putExtra(Constant.IMAGE_ABSOLUTE_PATH, imageUri.toString())
                context.sendBroadcast(broadcastImagePathIntent)
                (context as Activity).finish()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val galleryImage = view.image_view_gallery
        val linearLayout = view.linear_layout_container

    }
}