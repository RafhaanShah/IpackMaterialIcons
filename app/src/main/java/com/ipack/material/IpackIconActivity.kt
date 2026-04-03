package com.ipack.material

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ipack.material.ui.theme.IpackMaterialIconsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IpackIconActivity : ComponentActivity() {

    private val tag = IpackIconActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val intentAction = intent.action
        Log.d(tag, "onCreate $intentAction")
        setContent {
            IpackMaterialIconsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    IpackAppNavigation(intentAction = intentAction)
                }
            }
        }
    }
}
