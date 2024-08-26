package com.surendramaran.yolov8tflite

import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
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