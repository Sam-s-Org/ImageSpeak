package com.sam.imagespeak

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class Archive {

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun archive(modifier: Modifier) {
        var imageUri: Uri? by remember { mutableStateOf(null) }
        var imageBitmap: ImageBitmap? by remember { mutableStateOf(null) }
        val chooseFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            imageUri = it
            imageBitmap = null
        }
        val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            imageBitmap = it?.asImageBitmap()
            imageUri = null
        }
        Column (modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Button(
                    onClick = { chooseFileLauncher.launch("image/*") },
            ) {
                Text("Choose Picture")
            }
            Button(
                    onClick = { takePictureLauncher.launch() },
            ) {
                Text("Take Picture")
            }
            if (imageUri != null) {
                GlideImage(
                        model = imageUri,
                        contentDescription = "Captured Image",
                        modifier = modifier.fillMaxSize()
                )
            }
            if (imageBitmap != null) {
                Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Captured Image",
                        modifier = modifier.fillMaxSize()

                )
            }
        }
    }
}