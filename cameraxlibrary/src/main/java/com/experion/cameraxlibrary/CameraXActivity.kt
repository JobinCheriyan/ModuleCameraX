package com.experion.cameraxlibrary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.experion.cameraxlibrary.constant.Constant
import kotlinx.android.synthetic.main.activity_camera_x.*
import org.jetbrains.anko.toast
import java.io.File

class CameraXActivity : AppCompatActivity(), LifecycleOwner {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    var constant = Constant()


    // This is an array of all the permission specified in the manifest
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_x)


        textureViewCamera = findViewById(R.id.texture_view_camera)

        // Request camera permissions
        if (allPermissionsGranted()) {
            textureViewCamera.post {
                startCamera(this)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Every time the provided texture view changes, recompute layout
        textureViewCamera.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

    }

    private lateinit var textureViewCamera: TextureView


    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                textureViewCamera.post { startCamera(this) }
            } else {
                Toast.makeText(this, constant.PERMISSION_NOT_GRANDED_BY_USER, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun startCamera(context: Context) {

        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))

        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            2

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = textureViewCamera.parent as ViewGroup
            parent.removeView(textureViewCamera)
            parent.addView(textureViewCamera, 0)

            textureViewCamera.surfaceTexture = it.surfaceTexture
            updateTransform()
        }
        // Add this before CameraX.bindToLifecycle

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(Rational(1, 1))
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()


        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)
        capture_button.setOnClickListener {
            val file = File(
                externalMediaDirs.first(),
                "${System.currentTimeMillis()}" + constant.JPG
            )
            imageCapture.takePicture(file,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        error: ImageCapture.UseCaseError,
                        message: String, exc: Throwable?
                    ) {
                        val msg = constant.PHOTO_CAPTURE_FAILED + message
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        exc?.printStackTrace()
                    }

                    override fun onImageSaved(file: File) {
                        var imageAbsolutePath: String = file.absolutePath
                        val broadcastImagePathIntent = Intent(constant.ACTION_IMAGE_PATH)
                        broadcastImagePathIntent.putExtra(constant.IMAGE_ABSOLUTE_PATH, imageAbsolutePath)
                        context.sendBroadcast(broadcastImagePathIntent)


                    }
                })
        }


        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = textureViewCamera.width / 2f
        val centerY = textureViewCamera.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (textureViewCamera.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)


        // Finally, apply transformations to our TextureView
        textureViewCamera.setTransform(matrix)
    }


}
