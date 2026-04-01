package com.ipack.material

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.net.toUri
import com.ipack.material.ui.theme.IpackMaterialIconsTheme

class IpackIconSelectActivity : ComponentActivity() {

    private val viewModel: IpackIconSelectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        val result = Intent().apply {
            data = "${IpackKeys.ANDROID_RESOURCE_PREFIX}$packageName/${icon.resourceName}".toUri()
            putExtra(IpackKeys.Extras.ICON_LABEL, icon.name)
            putExtra(IpackKeys.Extras.ICON_NAME, icon.resourceName)
            putExtra(IpackKeys.Extras.ICON_ID, icon.id)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}
