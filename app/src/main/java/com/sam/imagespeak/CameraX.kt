package com.sam.imagespeak

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreviewScreen() {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        AndroidView(factory = { previewView }, modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 190.dp, top = 80.dp))
        Card(
                onClick = { takePhoto(context, coroutineScope, imageCapture) },
                shape = CircleShape,
                modifier = Modifier
                    .size(210.dp)
                    .align(Alignment.BottomCenter)
                    .padding(70.dp),
                colors = CardDefaults.cardColors().copy(containerColor = Color.White),
                border = BorderStroke(4.dp, Color.Gray)
        ) {}
        IconButton(onClick = { switchCamera() }, modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(120.dp, 200.dp)
            .padding(40.dp, 80.dp)) {
            Card (
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors().copy(containerColor = Color.Gray),
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

var textToSpeech: TextToSpeech? = null

private fun takePhoto(context: Context, coroutineScope: CoroutineScope, imageCapture: ImageCapture) {
    val photoFile = File(context.filesDir, "photo.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
                }
                @RequiresApi(Build.VERSION_CODES.R)
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val photoUri = outputFileResults.savedUri
                    Log.d("response", photoUri.toString())
                    if (photoUri != null) {
                        val inputStream = context.contentResolver.openInputStream(photoUri)
                        if (inputStream != null) {
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream.close()
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val b64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                            sendRequest(coroutineScope, b64) { answer ->
                                textToSpeech = TextToSpeech(context) {
                                    if (it == TextToSpeech.SUCCESS) textToSpeech?.let { txtToSpeech ->
                                        txtToSpeech.language = Locale.ENGLISH
                                        txtToSpeech.setSpeechRate(1.0f)
                                        txtToSpeech.speak(
                                                answer,
                                                TextToSpeech.QUEUE_ADD,
                                                null,
                                                null
                                        )
                                    }
                                }
                                Log.d("response", answer)
                                Toast.makeText(context, answer, Toast.LENGTH_SHORT).show()
                            }
                        } else Toast.makeText(context, "Failed to capture", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Failed to capture", Toast.LENGTH_SHORT).show()
                }
            }
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                                           continuation.resume(cameraProvider.get())
                                       }, ContextCompat.getMainExecutor(this))
        }
    }