package com.experion.cameraxlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

//implement SaveImagePathInterface to get the image path
class TestActivity : AppCompatActivity(),SavedImagePathInterface {


    override fun imageAbsolutePath(imagePath: String) {
        //get the image uri here
        Toast.makeText(this,imagePath,Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        //create an object of CameraXHelperClass by passing context
        val helperClass = CameraXHelperClass(this,this)
        //call the function openCamera to get the job done
        helperClass.openCamera()

    }
}
