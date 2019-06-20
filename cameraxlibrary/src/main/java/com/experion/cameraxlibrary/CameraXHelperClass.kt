package com.experion.cameraxlibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import com.experion.cameraxlibrary.constant.Constant

class CameraXHelperClass(context: Context, savedImagePathInterface: SavedImagePathInterface) {
    var constant = Constant()

    private val savedImagePathInterface = savedImagePathInterface
    private val context = context


    fun openCamera() {
        val intent = Intent(context, CameraXActivity::class.java)
        context.startActivities(arrayOf(intent))
        var intentFilter: IntentFilter = IntentFilter(constant.ACTION_IMAGE_PATH)
        context.registerReceiver(broadcastReceiver, intentFilter)



    }


    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent!!.action.equals(constant.ACTION_IMAGE_PATH)) {
                val imageAbsolutePath = intent.getStringExtra(constant.IMAGE_ABSOLUTE_PATH)
                savedImagePathInterface.imageAbsolutePath(imageAbsolutePath)

            }

        }


    }


}