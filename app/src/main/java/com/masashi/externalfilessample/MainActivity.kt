package com.masashi.externalfilessample

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by Masashi Hamaguchi on 2021/08/22.
 */

class MainActivity : AppCompatActivity() {

    private val RESULT_PICK_IMAGEFILE = 1000

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ImageAdapter
    private var imageData: JSONArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Google Photo もどき"

        sharedPreferences = getSharedPreferences("ImageData", Context.MODE_PRIVATE)
        loadJsonData()
        Log.d("imageData-onCreate", imageData.toString(4))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        adapter = ImageAdapter(this, imageData)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter


        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, RESULT_PICK_IMAGEFILE)
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            deleteAll()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                try {
                    val bmp = getBitmapFromUri(uri)
                    saveImage(bmp, applicationContext)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "取得に失敗しました。", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImage(bmp: Bitmap, context: Context) {
        // json
        val filename = createFileName()
        val filepath = "${filename}.jpg"
        val obj = JSONObject();
        obj.put("filename", filename)
        obj.put("filepath", filepath)

        // bitmap
        try {
            val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
            val file = File(directory, filepath)

            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.use {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
            return
        }

        imageData.put(obj)
        adapter.notifyDataSetChanged()

        saveJsonData()
    }

    private fun deleteImage(position: Int) {
        // image data
        val image = imageData.getJSONObject(position)

        // bitmap
        val directory = ContextWrapper(applicationContext).getDir("image", Context.MODE_PRIVATE)
        val file = File(directory, image.getString("filepath"))
        file.delete()
        // json
        imageData.remove(position)
        adapter.notifyDataSetChanged()

        saveJsonData()
    }

    private fun createFileName(): String {
        val now = LocalDateTime.now()
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
        return now.format(dtf)
    }

    private fun deleteAll() {
        val len = imageData.length() - 1
        for (i in 0..len) {
            deleteImage(0)
        }
    }

    private fun loadJsonData() {
        val str = sharedPreferences.getString("ImageArray", null)
        for (i in 0..imageData.length()) {
            imageData.remove(i)
        }
        if (str != null) {
            val newArr = JSONArray(str)
            for (i in 0..newArr.length() - 1) {
                imageData.put(newArr.getJSONObject(i))
            }
        }
        Log.d("imageData", imageData.toString(4))
    }

    private fun saveJsonData() {
        val str = imageData.toString()
        sharedPreferences.edit {
            this.putString("ImageArray", str)
//            this.apply()
        }
        Log.d("imageData", imageData.toString(4))
    }


    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri?): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

}