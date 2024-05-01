package com.sam.imagespeak

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.sam.imagespeak.ui.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun ClickCamera(viewModel: AppViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val previewView = remember { PreviewView(context) }

    val imageCapture = ImageCapture.Builder().setTargetResolution(Size(320, 160)).build()
    speak(context, "Manual Click mode. Double tap to take a photo. Long press to view History.")
    ClickCameraScreen(previewView, viewModel, context, coroutineScope, imageCapture)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClickCameraScreen(
    previewView: PreviewView,
    viewModel: AppViewModel,
    context: Context,
    coroutineScope: CoroutineScope,
    imageCapture: ImageCapture
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val preview = Preview.Builder().build()
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    var caption by remember { mutableStateOf("") }
    val vibrator = getSystemService(context, Vibrator::class.java) as Vibrator
    val captureBrush = Brush.sweepGradient(listOf(Color(0xFF2E89AD), Color.Black, Color(0xFF2E89AD)))

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
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
    
    if (!viewModel.clickButton) {
        Dialog(
                onDismissRequest = {
                    viewModel.clickButton = true
                    textToSpeech?.stop()
                    stopSpeechRecognition()
                }
        ) {
            Card(
                    shape = RoundedCornerShape(10),
                    modifier = Modifier
                        .heightIn(400.dp, 400.dp)
                        .width(300.dp)
                        .border(4.dp, Color(0xFF2E89AD), RoundedCornerShape(10)),
                    colors = CardDefaults.cardColors().copy(containerColor = Color.Black),
                    onClick = { startSpeechRecognition(
                            context,
                            { query -> viewModel.addHistory(query); imageQuery(coroutineScope, query) { viewModel.addHistory(it); speak(context, it) } },
                            { speak(context, "No text recognized") },
                    ) }
            ) {
                Column (
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (caption.isNotEmpty()) {
                        if ("blurry" in caption) {
                            caption = "Blurry Image"
                            val vibe = VibrationEffect.createOneShot(500, 100)
                            vibrator.vibrate(vibe)
                        }
                        speak(context, caption)
                        Text(
                                text = caption,
                                modifier = Modifier
                                    .padding(50.dp)
                                    .verticalScroll(rememberScrollState())
                        )
                        Button(onClick = { startSpeechRecognition(
                                context,
                                { query -> viewModel.addHistory(query); imageQuery(coroutineScope, query) { viewModel.addHistory(it); speak(context, it) } },
                                { speak(context, "No text recognized") },
                        ) }) {
                            Icon(
                                    Icons.Filled.KeyboardVoice,
                                    "Ask a question"
                            )
                        }
                        Text(text = voiceRecognition)
                    } else {
                        speak(context, "Analyzing the image")
                        CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(50.dp),
                                color = Color(0xFF2E89AD)
                        )
                    }
                }
            }
        }
    }

    if (viewModel.showHistory) {
        var done by remember { mutableStateOf(false) }
        Dialog(
                onDismissRequest = { viewModel.showHistory = false; textToSpeech?.stop() }
        ) {
            LaunchedEffect(Unit) {
                speak(context, "Viewing History. Swipe horizontally to navigate")
                delay(3000)
                done = true
            }
            Card(
                    shape = RoundedCornerShape(10),
                    modifier = Modifier
                        .heightIn(300.dp, 300.dp)
                        .width(300.dp)
                        .border(4.dp, Color(0xFF2E89AD), RoundedCornerShape(10)),
                    colors = CardDefaults.cardColors().copy(containerColor = Color.Black)
            ) {
                val pagerState = rememberPagerState {uiState.history.size}
                if (uiState.history.isNotEmpty()) {
                    HorizontalPager(state = pagerState) {page ->
                        val text = uiState.history[page]
                        if (done) { textToSpeech?.stop() }
                        speak(context, text)
                        Text(text = text, modifier = Modifier.padding(50.dp).verticalScroll(rememberScrollState()).fillMaxSize().align(Alignment.CenterHorizontally))
                    }
                } else {
                    speak(context, "No history yet, click some images first")
                }
            }
        }
    }
    
    Box {
        AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                            viewModel.clickButton,
                            onDoubleClick = {
                                viewModel.clickButton = false
                                caption = ""
                                takePhoto(viewModel, context, coroutineScope, imageCapture) {
                                    caption = it
                                }
                            },
                            onLongClick = {
                                viewModel.showHistory = true
                            }
                    ) {}
        )
        Card(
                enabled = viewModel.clickButton,
                onClick = {
                    viewModel.clickButton = false
                    caption = ""
                    takePhoto(viewModel, context, coroutineScope, imageCapture) {
                        caption = it
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(210.dp)
                    .align(Alignment.BottomCenter)
                    .padding(70.dp),
                colors = CardDefaults.cardColors().copy(containerColor = Color.White),
                border = BorderStroke(4.dp, captureBrush)
        ) {}
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

var textToSpeech: TextToSpeech? = null
var speechRecognizer: SpeechRecognizer? = null
var voiceRecognition: String = ""

private fun takePhoto(viewModel: AppViewModel, context: Context, coroutineScope: CoroutineScope, imageCapture: ImageCapture, callback: (String) -> Unit) {
    Log.d("response", "Taking photo")
    val photoFile = File(context.filesDir, "photo.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
                    callback("Some error occurred")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("response", "Photo saved")
                    val photoUri = outputFileResults.savedUri
                    Log.d("response", photoUri.toString())
                    if (photoUri != null) {
                        val inputStream = context.contentResolver.openInputStream(photoUri)
                        if (inputStream != null) {
                            val byteArray = inputStream.readBytes()
                            inputStream.close()
                            Log.d("response", "byteArray read")
                            val b64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                            Log.d("response", "Response sent")
                            sendRequest(coroutineScope, b64) { answer ->
                                if (answer != "Server did not send a response") {
                                    viewModel.addHistory(answer)
                                }
                                callback(answer)
                                Log.d("response", answer)
                            }
                        } else Toast.makeText(context, "Failed to capture", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Failed to capture", Toast.LENGTH_SHORT).show()
                }
            }
    )
}

fun speak(context: Context, answer: String) {
    textToSpeech = TextToSpeech(context) {
        if (it == TextToSpeech.SUCCESS) textToSpeech?.let { txtToSpeech ->
            txtToSpeech.language = Locale.ENGLISH
            txtToSpeech.setSpeechRate(1.5f)
            txtToSpeech.speak(
                    answer,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    null
            )
        }
    }
}

fun startSpeechRecognition(context: Context, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask me Anything")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {speak(context, "Ask your query")}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                onError.invoke("Speech recognition error: $error")
            }

            override fun onResults(results: Bundle?) {
                val result =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                if (result != null) {
                    onResult.invoke(result)
                } else {
                    onError.invoke("No speech recognized")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val result =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                if (result != null) {
                    voiceRecognition = result
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(recognitionIntent)
}

fun stopSpeechRecognition() {
    speechRecognizer?.stopListening()
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({ continuation.resume(cameraProvider.get()) },
            ContextCompat.getMainExecutor(this))
        }
    }