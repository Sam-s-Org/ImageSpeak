package com.sam.imagespeak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sam.imagespeak.ui.AppViewModel
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

private const val BASE_URL = "https://ec87ab2955d2f02624a85622a5dd878c.serveo.net"

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

    @POST("query")
    suspend fun queryRequest(@Body query: String): Response
}

object AppApi {
    val retrofitService: AppApiService by lazy {
        retrofit.create(AppApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {}
            else -> permissionRequest.launch(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            permissionRequest.launch(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
        }
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        setContent {
            val viewModel: AppViewModel = viewModel()
            ImageSpeakTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    ClickCamera(viewModel)
                    App(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

var response by mutableStateOf("")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun App(modifier: Modifier = Modifier, viewModel: AppViewModel = viewModel()) {
    val pagerState = rememberPagerState {2}
    
    HorizontalPager(state = pagerState) {page ->
        when (page) {
            0 -> ClickCamera(viewModel)
            1 -> NavCamera()
            else -> ClickCamera(viewModel)
        }
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

fun imageQuery(coroutineScope: CoroutineScope, query: String, callback: (String) -> Unit) {
    coroutineScope.launch {
        response = try {
            AppApi.retrofitService.queryRequest(query).answer ?: ""
        } catch (e: Exception) {
            "Server did not send a response. Error : $e"
        }
        callback(response)
    }
}