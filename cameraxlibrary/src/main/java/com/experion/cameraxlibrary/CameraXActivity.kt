package com.experion.cameraxlibrary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.util.Rational
import android.util.Size
import android.view.*
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.experion.cameraxlibrary.constant.Constant
import kotlinx.android.synthetic.main.activity_camera_x.*
import org.jetbrains.anko.toast
import java.io.File
import android.view.MotionEvent
import android.view.View.OnTouchListener



class CameraXActivity : AppCompatActivity(), LifecycleOwner {
    var galleryImageUrls: ArrayList<String>? = null
    var galleryUp: Boolean = true;

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUEST_CODE_GALLERY = 2
    }

    // This is an array of all the permission specified in the manifest
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_x)

        //gallery button operatin
        //click to open gallery and select the image from gallery
        image_button_gallery.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY)
        }

    }

    override fun onResume() {
        super.onResume()
        CameraX.unbindAll()
        openCamera()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (data == null) {
                toast(Constant.CANCELLED)
            } else {

                val selectedImage = data?.getData()
                val filePath = arrayOf<String>(MediaStore.Images.Media.DATA)
                val cursor = getContentResolver().query(selectedImage, filePath, null, null, null)
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePath[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                val broadcastImagePathIntent = Intent(Constant.ACTION_IMAGE_PATH)
                broadcastImagePathIntent.putExtra(Constant.IMAGE_ABSOLUTE_PATH, picturePath)
                sendBroadcast(broadcastImagePathIntent)
                closeActivity()
            }
        }
    }

    private fun fetchGalleryImages(context: Activity): ArrayList<String> {

        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)//get all columns of type images
        val orderBy = MediaStore.Images.Media.DATE_TAKEN//order data by date
        val imagecursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
            null, null, "$orderBy DESC"
        )//get all data in Cursor by sorting in DESC order

        galleryImageUrls = ArrayList()

        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA)//get column index
            galleryImageUrls!!.add(imagecursor.getString(dataColumnIndex))

        }

        return galleryImageUrls as ArrayList<String>
    }


    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                texture_view_camera.post {
                    startCamera(this)
                    fetchGalleryImages(this)
                    SettingImageAdapter()
                }
            } else {
                Toast.makeText(this, Constant.PERMISSION_NOT_GRANDED_BY_USER, Toast.LENGTH_SHORT).show()
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
            setTargetResolution(Size(R.dimen.six_hundred, R.dimen.six_hundred))

        }.build()
        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            2

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = texture_view_camera.parent as ViewGroup
            parent.removeView(texture_view_camera)
            parent.addView(texture_view_camera, 0)

            texture_view_camera.surfaceTexture = it.surfaceTexture
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
                "${System.currentTimeMillis()}" + Constant.JPG
            )

            imageCapture.takePicture(file,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        error: ImageCapture.UseCaseError,
                        message: String, exc: Throwable?
                    ) {
                        val msg = Constant.PHOTO_CAPTURE_FAILED + message
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        exc?.printStackTrace()
                    }

                    override fun onImageSaved(file: File) {

                        val broadcastImagePathIntent = Intent(Constant.ACTION_IMAGE_PATH)
                        broadcastImagePathIntent.putExtra(Constant.IMAGE_ABSOLUTE_PATH, file.absolutePath)
                        context.sendBroadcast(broadcastImagePathIntent)

                        closeActivity()

                    }
                })
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.


        CameraX.bindToLifecycle(this, preview, imageCapture)


    }


    private fun updateTransform(): Int {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = texture_view_camera.width / 2f
        val centerY = texture_view_camera.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (texture_view_camera.display.rotation) {

            Surface.ROTATION_90 -> 180
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 180
            else -> return 90
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        texture_view_camera.setTransform(matrix)
        return 0
    }

    private fun closeActivity() {

        finish()
    }

    private fun slideUp(view: View) {
        view.setVisibility(View.VISIBLE)
        val animate: TranslateAnimation = TranslateAnimation(0f, 0f, view.getHeight().toFloat() * 3, 0f)
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
    }

    private fun slideDown(view: View) {
        view.setVisibility(View.GONE)
        val animate: TranslateAnimation = TranslateAnimation(0f, 0f, 0f, view.getHeight().toFloat() * 3)
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);


    }

    private fun openCamera() {
        if (allPermissionsGranted()) {
            texture_view_camera.post {
                CameraX.unbindAll()
                startCamera(this)
                fetchGalleryImages(this)
                SettingImageAdapter()
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Every time the provided texture view changes, recompute layout
        texture_view_camera.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun SettingImageAdapter() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view_gallery.setLayoutManager(layoutManager);

        recycler_view_gallery.adapter = galleryImageUrls?.let { GalleryImageAdapter(it, this) }


        image_button_slide.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
        image_button_slide.setOnClickListener {
            if (galleryUp) {
                slideDown(recycler_view_gallery);
                image_button_slide.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
            } else {
                slideUp(recycler_view_gallery);
                image_button_slide.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
            }
            galleryUp = !galleryUp;
        }
    }


}
