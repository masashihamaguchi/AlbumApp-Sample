package com.masashi.externalfilessample

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

/**
 * Created by Masashi Hamaguchi on 2021/08/22.
 */

class ImageAdapter(
    private val context: Context,
    private val imageList: JSONArray?

) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun getItemCount(): Int = imageList?.length() ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image: JSONObject = imageList?.getJSONObject(position) ?: return
        val bmp = loadImage(image.getString("filepath"), context)
        Log.d("bmp", bmp.toString())
        holder.imageView.setImageBitmap(bmp)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

    }

    private fun loadImage(fileName: String, context: Context): Bitmap? {
        try {
            val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
            val file = File(directory, fileName)
            return BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e:IOException){
            e.printStackTrace()
            return  null
        }
    }
}
