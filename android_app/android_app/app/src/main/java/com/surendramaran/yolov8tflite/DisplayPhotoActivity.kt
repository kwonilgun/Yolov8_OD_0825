package com.surendramaran.yolov8tflite

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.surendramaran.yolov8tflite.databinding.ActivityDisplayPhotoBinding

class DisplayPhotoActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDisplayPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisplayPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_host_fragment_content_display_photo)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
// Get the photo URI from the intent

        val photoUri: Uri? = intent.getParcelableExtra("photo_uri")
        val boundingBoxes: ArrayList<BoundingBox>? =
            intent.getParcelableArrayListExtra<Bundle>("bounding_boxes")?.toBoundingBoxList() as ArrayList<BoundingBox>?



        // Display the photo in the ImageView
//        photoUri?.let { ... }는 photoUri가 null이 아닌지 확인하고, null이 아니면 let 블록 안의 코드를 실행합니다. 이때 photoUri는 it으로 참조됩니다.
//        주어진 코드에서는 photoUri가 null이 아닌 경우에만 URI가 ImageView에 전달되어 이미지를 표시하도록 합니다.
        photoUri?.let {
            binding.photoImageView.setImageURI(it)
        }

        // Close the activity when the ImageView is clicked, returning to the camera
        binding.photoImageView.setOnClickListener {
            finish()
        }

        runOnUiThread {

//            binding.overlay.apply {
//                Log.d("overlay boundingBox",  "$boundingBoxes")
//                boundingBoxes?.let { setResults(it) }
//                invalidate()
//            }
            binding.overlayCarving.apply{
                Log.d("overlay carving bouningBox",  "$boundingBoxes")

                // Obtain or create the Bitmap. This example assumes you're creating a bitmap from the ImageView.
                val bitmap = binding.photoImageView.drawable.toBitmap() // Convert the ImageView's drawable to Bitmap

                boundingBoxes?.let { setBoundingBoxes(it, bitmap) } // Pass the bitmap along with boundingBoxes
            }

//            binding.overlay_carving.apply {
//                Log.d("onDetect bounding box",  "$boundingBoxes")
//                boundingBoxes?.let { setResults(it) }
//                invalidate()
//            }
        }



//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_display_photo)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}