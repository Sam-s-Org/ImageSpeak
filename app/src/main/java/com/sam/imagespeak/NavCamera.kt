package com.sam.imagespeak

import android.content.Context
import android.graphics.Bitmap
import android.os.Vibrator
import android.util.Base64
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun NavCamera() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val previewView = remember { PreviewView(context) }

    speak(context, "Auto Click Mode")
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { imageAnalysis ->
            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalyzer(coroutineScope) {
                caption = it
                if ("Server did not send a response" !in it) {
                    speak(context, it)
                }
            } )
        }

    NavCameraScreen(context, previewView, imageAnalyzer)

}

var caption by mutableStateOf("")

@Composable
fun NavCameraScreen(
    context: Context,
    previewView: PreviewView,
    imageAnalyzer: ImageAnalysis
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = Preview.Builder().build()

    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalyzer)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    fun switchCamera() {
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            lensFacing = CameraSelector.LENS_FACING_BACK
            speak(context, "Switching to back camera")
        } else {
            lensFacing = CameraSelector.LENS_FACING_FRONT
            speak(context, "Switching to front camera")
        }
    }

    Box {
        AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
        )
        if (caption.isNotEmpty()) {
            Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .widthIn(0.dp, 300.dp)
                        .animateContentSize()
                        .align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors().copy(containerColor = Color(0xFF000000)),
                    border = BorderStroke(2.dp, Color(0xFFCECECE))
            ) {
                Text(text = caption, modifier = Modifier.padding(10.dp))
            }
        }
        IconButton(
                onClick = { switchCamera() }, modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(120.dp, 200.dp)
            .padding(40.dp, 80.dp)
        ) {
            Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors().copy(containerColor = Color(0xFF000000)),
            ) {
                Icon(
                        Icons.Filled.Cached,
                        "Switch Camera",
                        modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


private class ImageAnalyzer(private val coroutineScope: CoroutineScope, private var callback: (String) -> Unit) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimeMillis: Long = 0
    private val analysisIntervalMillis = 5000

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastAnalyzedTimeMillis >= analysisIntervalMillis) {
            val outputStream = ByteArrayOutputStream()
            val bitmap = image.toBitmap()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
            val data = outputStream.toByteArray()
            val b64 = Base64.encodeToString(data, Base64.DEFAULT)
            Log.d("response", "${image.format}")
            sendRequest(coroutineScope, b64) {
                callback(it)
                Log.d("response", it)
            }
            lastAnalyzedTimeMillis = currentTimeMillis
        }
        image.close()
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({ continuation.resume(cameraProvider.get()) },
            ContextCompat.getMainExecutor(this))
        }
    }