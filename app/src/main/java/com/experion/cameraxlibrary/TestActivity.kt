package com.experion.cameraxlibrary

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.File

//implement SaveImagePathInterface to get the image path
class TestActivity : AppCompatActivity(), OnImageCaptureListener {

    override fun onCapture(imagePath: String) {
        //get the image uri here
        Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show()
        val imageUri = Uri.parse(imagePath)
        Glide.with(this)
            .load( File(imageUri.getPath()))
            .into(image_view_demo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        button_open_camera.setOnClickListener {
            //create an object of CameraXHelperClass by passing context
            val helperClass = CameraXHelperClass(this, this)
            //call the function openCamera to get the job done
            helperClass.openCamera()
        }



    }
}
