package com.example.kioskt

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.kioskt.ui.theme.KioskTTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.menu.RecyclerViewAdapter
import com.example.menu.ItemData
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.kioskt.databinding.ActivityMainBinding
import com.example.kioskt.ml.FaceModel
import com.example.kioskt.viewmodel.DetectedResult
import com.example.kioskt.viewmodel.DetectedResultViewModel
import com.example.menu.ViewPager2Adapter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.widget.FrameLayout

private const val LOG_TAG = "Kiosk++"
typealias DetectedResultListener = (detectedResult: DetectedResult) -> Unit

@ExperimentalGetImage
class MainActivity : AppCompatActivity() {
    lateinit var adapter: RecyclerViewAdapter
    lateinit var itemList: ArrayList<ItemData>
    private lateinit var binding: ActivityMainBinding

    // Cameras
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Result data storage
    private val detectedResultViewModel: DetectedResultViewModel by viewModels()

    // Permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayout = findViewById<LinearLayout>(R.id.my_linear_layout)



        CoroutineScope(Dispatchers.IO).launch {
            try {
                val age = async {
                    detectedResultViewModel.getValue().value?.toString() ?: "24"
                }
                val response = sendAge(this@MainActivity, age.await())
//                withContext(Dispatchers.Main) {
//                    linearLayout.removeAllViews() // Remove all existing views
//                    for (i in 0 until response.size) {
//                        val cardView = CardView(this@MainActivity).apply {
//                            layoutParams = LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.WRAP_CONTENT, // 변경된 너비
//                                LinearLayout.LayoutParams.WRAP_CONTENT  // 변경된 높이
//                            ).apply {
//                                setMargins(
//                                    dpToPx(this@MainActivity, 10),
//                                    dpToPx(this@MainActivity, 10),
//                                    dpToPx(this@MainActivity, 10),
//                                    dpToPx(this@MainActivity, 10)
//                                )
//                            }
//                            radius = 30f
//                        }
//
//                        val imageView = ImageView(this@MainActivity).apply {
//                            val imageName = response[i].removeSuffix(".png").toLowerCase() // Remove the file extension
//                            val imageResource = getDrawableIdentifier(this@MainActivity, imageName)
//                            if (imageResource != 0) {
//                                val imageSize = dpToPx(this@MainActivity, 80) // Set the size of the image to 100dp
//                                layoutParams = LinearLayout.LayoutParams(
//                                    imageSize, // Set width to imageSize
//                                    imageSize  // Set height to imageSize
//                                ).apply {
//                                    gravity = Gravity.CENTER // Set gravity to center
//                                }
//                                scaleType = ImageView.ScaleType.CENTER_INSIDE // Set scaleType to centerInside
//                                setImageResource(imageResource) // Replace with your image resource
//                            } else {
//                                val noImageText = TextView(this@MainActivity).apply {
//                                    text = "No image"
//                                    gravity = Gravity.CENTER
//                                }
//                                cardView.addView(noImageText)
//                            }
//                        }
//
//                        val textView = TextView(this@MainActivity).apply {
//                            id = View.generateViewId()
//                            layoutParams = LinearLayout.LayoutParams(
//                                dpToPx(this@MainActivity, 120),
//                                dpToPx(this@MainActivity, 160)
//                            )
//                            this.text = if (i < response.size) response[i] else "No data"
//                            gravity = Gravity.CENTER
//                        }
//
//                        cardView.addView(imageView)
//                        cardView.addView(textView)
//                        linearLayout.addView(cardView)
//                    }
//                }
                withContext(Dispatchers.Main) {
                    linearLayout.removeAllViews() // Remove all existing views
                    for (i in 0 until response.size) {
                        val cardView = CardView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(this@MainActivity, 150),
                                dpToPx(this@MainActivity, 150)
                            ).apply {
                                setMargins(
                                    dpToPx(this@MainActivity, 10),
                                    dpToPx(this@MainActivity, 10),
                                    dpToPx(this@MainActivity, 10),
                                    dpToPx(this@MainActivity, 10)
                                )
                            }
                            radius = 30f
                        }

                        val linearLayout_2 = LinearLayout(this@MainActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                        val imageView = ImageView(this@MainActivity).apply {
                            val imageName = response[i].toLowerCase().replace(" ", "_") // Remove the file extension
                            val imageResource = getDrawableIdentifier(this@MainActivity, imageName)
                            if (imageResource != 0) {
                                val imageSize = dpToPx(this@MainActivity, 80) // Set the size of the image to 100dp
                                layoutParams = LinearLayout.LayoutParams(
                                    imageSize, // Set width to imageSize
                                    imageSize  // Set height to imageSize
                                ).apply {
                                    gravity = Gravity.CENTER // Set gravity to center
                                }
                                scaleType = ImageView.ScaleType.CENTER_INSIDE // Set scaleType to centerInside
                                setImageResource(imageResource) // Replace with your image resource
                            } else {
                                val noImageText = TextView(this@MainActivity).apply {
                                    text = "No image"
                                    gravity = Gravity.CENTER
                                }
                                linearLayout_2.addView(noImageText)
                            }
                            linearLayout_2.addView(this)
                        }

                        val textView = TextView(this@MainActivity).apply {
                            id = View.generateViewId()
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(this@MainActivity, 120),
                                dpToPx(this@MainActivity, 30)
                            ).apply {
                                gravity = Gravity.CENTER // Set gravity to center
                            }
                            this.text = if (i < response.size) response[i] else "No data"
                            gravity = Gravity.CENTER
                        }
                        linearLayout_2.addView(textView)

                        cardView.addView(linearLayout_2)
                        linearLayout.addView(cardView)
                    }
                }
            }   catch (e: Exception) {
                // Handle the exception here, e.g., show a toast message
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "error",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }


        // Set up tabs
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // When a tab is selected
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // When a tab is unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // When a tab is reselected
            }
        })

        // Connect adapter to viewpager
        binding.viewPager2.adapter = ViewPager2Adapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Pizza"
                1 -> tab.text = "Chicken"
                2 -> tab.text = "Burger"
                3 -> tab.text = "Snacks"
            }
        }.attach()

        itemList = ArrayList()
        adapter = RecyclerViewAdapter(itemList, ::updateTotalPrice)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this)
    }

    // Acquire permission func
    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun updateTotalPrice() {
        var totalPrice = 0
        for (item in itemList) {
            totalPrice += item.ItemPriceText.toInt() * item.Itemcnt
        }
        binding.totalPrice.text = totalPrice.toString()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageAnalyzer = ImageAnalysis.Builder()
//                .setTargetResolution(Size(224, 224))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysisUseCase: ImageAnalysis ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(this) {
                            detectedResult -> detectedResultViewModel.updateData(detectedResult)
                    })
                }

            val cameraSelector =
                if(cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA))
                    CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(LOG_TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    @ExperimentalGetImage
    private class ImageAnalyzer(ctx: Context, private val listener: DetectedResultListener) : ImageAnalysis.Analyzer {

        lateinit var item: DetectedResult

        val faceModel = FaceModel.newInstance(ctx)
        override fun analyze(image: ImageProxy) {
            val cameraImage = image.image
            if (cameraImage != null) {
                val gottenImage =
                    InputImage.fromMediaImage(cameraImage, image.imageInfo.rotationDegrees)
                FaceDetection.getClient().process(gottenImage)
                    .addOnFailureListener { throw it }
                    .addOnSuccessListener { faces ->

                        var maxFaceArea = 0
                        var largestFace: Face? = null
                        for (face in faces) {
                            val bound = face.boundingBox
                            val faceArea = bound.width() * bound.height()
                            if (faceArea > maxFaceArea) {
                                largestFace = face
                                maxFaceArea = faceArea
                            }
                        }
                        if (maxFaceArea != 0) {
                            val targetFace = cropImage(
                                image, largestFace!!.boundingBox.top, largestFace.boundingBox.left,
                                largestFace.boundingBox.width(), largestFace.boundingBox.height()
                            )
                            runModel(targetFace)
                        }
                        image.close()
                    }
            }
        }

        private fun cropImage(
            image: ImageProxy,
            upper: Int,
            left: Int,
            width: Int,
            height: Int
        ): Bitmap {
            var bitmap = image.toBitmap()

            var cropX = left
            if (cropX < 0) cropX = 0
            if (cropX > bitmap.width) cropX = bitmap.width

            var cropY = upper
            if (cropY < 0) cropY = 0
            if (cropY > bitmap.height) cropY = bitmap.height

            var cropWidth = width
            if (bitmap.width <= cropX + width) {
                cropWidth = bitmap.width - cropX
                if (cropWidth <= 0) {
                    cropWidth = 1
                    cropX -= 1
                }
            }

            var cropHeight = height
            if (bitmap.height <= cropY + height) {
                cropHeight = bitmap.height - cropY
                if (cropHeight <= 0) {
                    cropHeight = 1
                    cropY -= 1
                }
            }

            bitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            return bitmap
        }

        private fun runModel(targetFace: Bitmap) {
            val tfImage = TensorImage.fromBitmap(targetFace)

            val floatImage = ImageProcessor.Builder()
                .add(CastOp(DataType.FLOAT32))
                .build()
                .process(tfImage)

            val outputs = faceModel.process(floatImage.tensorBuffer)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
            val outputFeature1 = outputs.outputFeature1AsTensorBuffer.floatArray

            var predictedAge = 0
            var largestProb = 0.0f
            for (i in 0 until 101) {
                if (largestProb < outputFeature1[i]) {
                    largestProb = outputFeature1[i]
                    predictedAge = i
                }
            }

            var predictedGender = "F"
            if (outputFeature0[0] <= 0.5f) predictedGender = "M"

            Log.e(LOG_TAG, "$predictedGender $predictedAge")

            item = DetectedResult(predictedGender, predictedAge + 0.0f)
            listener(item)
        }
    }
}

fun dpToPx(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}
suspend fun sendAge(context: Context, age: String): List<String> = suspendCancellableCoroutine { continuation ->
    try {
        val queue: RequestQueue = Volley.newRequestQueue(context)
        val url = "https://kiosk-yfnhk.run.goorm.site/menu_choice/"

        val ageJson = JSONObject()
        ageJson.put("age", age.toInt())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, ageJson,
            Response.Listener<JSONObject> { response ->
                if (continuation.isActive) {
                    val array = response.getJSONArray("menu") // Replace 'yourArrayKey' with the actual key in the response
                    val list = mutableListOf<String>()
                    for (i in 0 until array.length()) {
                        list.add(array.getString(i))
                    }
                    continuation.resume(list)
                }
            },
            Response.ErrorListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        )

        continuation.invokeOnCancellation {
            jsonObjectRequest.cancel()
        }

        queue.add(jsonObjectRequest)
    } catch (e: Exception) {
        if (continuation.isActive) {
            continuation.resumeWithException(e)
        }
    }
}fun getDrawableIdentifier(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}