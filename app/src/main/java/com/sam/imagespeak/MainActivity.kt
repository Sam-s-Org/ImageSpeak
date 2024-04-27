package com.sam.imagespeak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sam.imagespeak.ui.theme.ImageSpeakTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://40225bb0466c3f04cb5d0f746ad16ed3.serveo.net"

val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

@Serializable
data class Response(
    @SerialName("answer")
    val answer: String?
)

interface AppApiService {
    @POST("caps")
    suspend fun postRequest(@Body request: String): Response
}

object AppApi {
    val retrofitService: AppApiService by lazy {
        retrofit.create(AppApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {}
            else -> cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }
        setContent {
            ImageSpeakTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val paddingValues = innerPadding
                    CameraPreviewScreen()
//                    App(Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun setCameraPreview() {
        setContent {
            ImageSpeakTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    CameraPreviewScreen()
                }
            }
        }
    }
}

var response by mutableStateOf("")

@Composable
fun App(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var t1 by remember { mutableStateOf("") }
    Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Image(
                painterResource(R.drawable.ic_launcher_foreground),
                "Logo"
        )
        Text(text = "ImageSpeak")
        OutlinedTextField(
                value = t1,
                onValueChange = { t1 = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter something") },
                placeholder = { Text("Enter something") }
        )
        Button(onClick = { sendRequest(coroutineScope, t1) {} }) {
            Text(text = "Send Request")
        }
        Text(text = response)
    }
}

fun sendRequest(coroutineScope: CoroutineScope, t1: String = "", callback: (String) -> Unit) {
    coroutineScope.launch {
        response = try {
            AppApi.retrofitService.postRequest(t1).answer?: ""
        } catch ( e: Exception) {
            "Server did not send a response"
        }
        callback(response)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImageSpeakTheme {
        App()
    }
}