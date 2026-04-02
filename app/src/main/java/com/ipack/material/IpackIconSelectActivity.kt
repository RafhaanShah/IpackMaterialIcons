package com.ipack.material

import android.R.attr.tag
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.net.toUri
import com.ipack.material.ui.theme.IpackMaterialIconsTheme

class IpackIconSelectActivity : ComponentActivity() {

    private val tag = IpackIconSelectActivity::class.simpleName
    private val viewModel: IpackIconSelectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
        
        // Initial result is canceled
        setResult(RESULT_CANCELED)
        
        // Start loading icons based on intent extras
        viewModel.loadIcons(intent)

        setContent {
            IpackMaterialIconsTheme {
                IpackIconSelectScreen(
                    viewModel = viewModel,
                    onIconSelected = { icon ->
                        handleIconSelection(icon)
                    }
                )
            }
        }
    }

    private fun handleIconSelection(icon: IpackIcon) {
        Log.i(tag, "handleIconSelection: $icon")
        val dataString = "${IpackKeys.ANDROID_RESOURCE_PREFIX}$packageName/${icon.resourceName}"
        setClipboard(dataString)
        val result = Intent().apply {
            data = dataString.toUri()
            putExtra(IpackKeys.Extras.ICON_LABEL, icon.name)
            putExtra(IpackKeys.Extras.ICON_NAME, icon.resourceName)
            putExtra(IpackKeys.Extras.ICON_ID, icon.id)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun setClipboard(string: String) {

    }
}
