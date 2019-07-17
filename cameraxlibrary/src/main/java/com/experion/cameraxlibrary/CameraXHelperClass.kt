package com.experion.cameraxlibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.experion.cameraxlibrary.constant.Constant

class CameraXHelperClass(context: Context, imagePath: ImagePath) {

    private val imagePath = imagePath
    private val context = context

    fun openCamera() {
        val intent = Intent(context, CameraXActivity::class.java)
        context.startActivities(arrayOf(intent))
        var intentFilter = IntentFilter(Constant.ACTION_IMAGE_PATH)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun unRegisterReceiver() {
        context.unregisterReceiver(broadcastReceiver)

    }

    val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent!!.action.equals(Constant.ACTION_IMAGE_PATH)) {
                val imageAbsolutePath = intent.getStringExtra(Constant.IMAGE_ABSOLUTE_PATH)
                imagePath.onCapture(imageAbsolutePath)
                unRegisterReceiver()
            }
        }
    }
}