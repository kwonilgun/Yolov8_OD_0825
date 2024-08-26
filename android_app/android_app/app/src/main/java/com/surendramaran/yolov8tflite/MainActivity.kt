package com.surendramaran.yolov8tflite

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.Constants.LABELS_PATH
import com.surendramaran.yolov8tflite.Constants.MODEL_PATH
import com.surendramaran.yolov8tflite.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/*
 2024년 8월 25일 추가 한다.
 */

class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: Detector

    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraButton: Button // Assuming you have a Button named cameraButton

    // Add this variable to hold boundingBoxes
    private var detectedBoundingBoxes: List<BoundingBox>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraButton = binding.cameraButton // Assuming your Button is bound this way
        cameraButton.setOnClickListener {

            if(cameraButton.isEnabled){
                detectedBoundingBoxes?.let { it1 -> takePhoto(it1) }
            } else{
                Log.d("debug4", "camera is disabled")
            }

        }

        cameraButton.isEnabled = false
//        onEnableButton(false)

        detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        detector.setup()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        /*
        cameraExecutor = Executors.newSingleThreadExecutor()은 Android 애플리케이션에서 사용되는 코드로, 이 줄은 cameraExecutor라는 변수에 단일 스레드를 갖는 ExecutorService를 할당하는 역할을 합니다. 이를 이해하기 위해서는 ExecutorService와 스레드 풀(Thread Pool)의 개념을 간략하게 설명할 필요가 있습니다.

        ExecutorService와 스레드 풀
        ExecutorService: Java에서 ExecutorService는 작업을 비동기적으로 실행할 수 있는 서비스입니다. 이를 통해 직접 스레드를 생성하고 관리하는 대신, 스레드 풀을 사용하여 작업을 쉽게 관리할 수 있습니다.

        스레드 풀(Thread Pool): 스레드 풀은 미리 생성된 스레드 집합을 유지하고, 작업이 제출될 때 스레드 풀에서 사용 가능한 스레드가 작업을 처리하도록 합니다. 작업이 완료되면 스레드는 반환되어 다음 작업에 재사용될 수 있습니다.
        Executors.newSingleThreadExecutor()
        Executors.newSingleThreadExecutor()는 단일 스레드로 구성된 스레드 풀을 생성하는 정적 메서드입니다.
        이 메서드는 하나의 스레드만을 사용하는 ExecutorService를 반환합니다. 즉, 이 ExecutorService는 주어진 작업들을 하나의 스레드에서 순차적으로 처리하게 됩니다.
        작업이 여러 개 제출되면, 이 스레드는 하나씩 차례로 작업을 처리합니다. 모든 작업이 완료될 때까지 새로운 작업은 대기 상태에 놓입니다.
        왜 단일 스레드 풀을 사용할까?
        이 코드에서 cameraExecutor가 newSingleThreadExecutor()로 생성된 이유는 카메라와 관련된 작업(예: 이미지 분석)이 순차적으로 처리되기를 원하기 때문입니다. 카메라 이미지 분석은 동시에 여러 작업이 처리되면 안 되는 경우가 많습니다. 예를 들어, 이미지 프레임 분석이 동시에 여러 개 실행되면 예상치 못한 문제가 발생할 수 있기 때문에, 단일 스레드에서 순서대로 처리하도록 한 것입니다.

        요약
        cameraExecutor = Executors.newSingleThreadExecutor()는 카메라 이미지 분석과 같은 작업을 하나의 스레드에서 순차적으로 실행하기 위해 단일 스레드 풀을 생성합니다.
        이를 통해 카메라 프레임이 안전하게 처리되며, 여러 스레드가 동시에 동일한 작업을 실행하여 발생할 수 있는 잠재적인 문제를 방지할 수 있습니다.

         */

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview =  Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

//            Log.d("debug 2", " image detector ..")
            detector.detect(rotatedBitmap)
        }

        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        cameraProvider.unbindAll()

        Log.d("debug 1", "camera unbindAll")

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun takePhoto(boundingBoxes: List<BoundingBox>) {

        Log.d("debug5", "takePhoto")
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        // MediaStore 콘텐츠 값을 만든다.
        // MediaStore의 표시 이름이 고유하도록 현재 시간을 기준으로 타임스탬프를 사용한다.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA)
            .format(System.currentTimeMillis())

        // ContentValues를 사용하여 이미지에 대한 메타데이터 설정
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            // Android Q 이상에서는 RELATIVE_PATH를 사용하여 저장 경로 지정
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        // OutputFileOptions 객체를 생성.
        // 이미지 저장 옵션 설정. MediaStore를 통해 이미지 저장 위치와 메타데이터 지정
        // 이 객체에서 원하는 출력 방법을 지정할 수 있다.
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        // takePicture()를 호출한다. 이미지 캡처 및 저장
        // outputOptions, 실행자, 이미지가 저장될 때 콜백을 전달
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                // 캡처에 실패하지 않으면 사진을 저장하고 완료되었다는 토스트 메시지를 표시한다.
                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val savedUri = output.savedUri ?: return
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

//                    stopCameraAndShowImage(savedUri)



                    // Start the DisplayPhotoActivity and pass the photo URI
                    val intent = Intent(this@MainActivity, DisplayPhotoActivity::class.java).apply {
                        putExtra("photo_uri", savedUri)
//                        putParcelableArrayListExtra("bounding_boxes", ArrayList(boundingBoxes ?: emptyList()))

                        putParcelableArrayListExtra("bounding_boxes", boundingBoxes.toParcelableArrayList())

                    }
                    startActivity(intent)
                }
            }
        )
    }

    private fun stopCameraAndShowImage(imageUri: Uri) {
        // Unbind all camera use cases
        cameraProvider?.unbindAll()

        // Hide the viewFinder (where the camera preview is shown)
        binding.viewFinder.visibility = View.GONE

//        // Show the captured image
//        binding.capturedImageView.visibility = View.VISIBLE
//        binding.capturedImageView.setImageURI(imageUri)
    }

    private fun onEnableButton(enable: Boolean) {
        if (!cameraButton.isEnabled) {
            cameraButton.isEnabled = enable

            // 버튼의 배경색을 활성화/비활성화에 따라 변경
            val backgroundColor = if (enable) {
                ContextCompat.getColor(this, R.color.button_color_enabled)
            } else {
                ContextCompat.getColor(this, R.color.button_color_disabled)
            }
            cameraButton.setBackgroundColor(backgroundColor)

            // 버튼의 텍스트 색상도 활성화/비활성화에 따라 변경
            val textColor = if (enable) {
                ContextCompat.getColor(this, R.color.button_text_color_enabled)
            } else {
                ContextCompat.getColor(this, R.color.button_text_color_disabled)
            }
            cameraButton.setTextColor(textColor)
        }
    }



    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long, enable: Boolean) {


        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                Log.d("onDetect bounding box",  "$boundingBoxes")
                setResults(boundingBoxes)
                invalidate()
            }

            // Save boundingBoxes to a class-level variable
            detectedBoundingBoxes = boundingBoxes

            // Schedule a photo to be taken 500ms after detection
            if(!cameraButton.isEnabled){
                Handler(Looper.getMainLooper()).postDelayed({
//                    takePhoto()
                    cameraButton.isEnabled = enable
//                    onEnableButton(enable)
                }, 10)
            }

        }
    }

    override fun onEnableCameraButton(enable: Boolean) {

        cameraButton.isEnabled = enable


    }
}
