package com.github.catomon.denpaplayer

import DenpaScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import playerContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = this
            playerContext = { context }
            DenpaScreen(Modifier.fillMaxSize())
        }
    }

    override fun onResume() {
        super.onResume()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    DenpaScreen()
}