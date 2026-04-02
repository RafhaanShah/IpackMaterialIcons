package com.ipack.material

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import com.ipack.material.ui.theme.IpackMaterialIconsTheme

class IpackIconSelectActivity : ComponentActivity() {

    private val tag = IpackIconSelectActivity::class.simpleName
    private val viewModel: IpackIconSelectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate ${intent.action}")

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
        setResult(icon, dataString)
    }

    private fun setResult(icon: IpackIcon, dataString: String) {
        if (intent.action == IpackKeys.Actions.ICON_SELECT) {
            val result = Intent().apply {
                data = dataString.toUri()
                putExtra(IpackKeys.Extras.ICON_LABEL, icon.name)
                putExtra(IpackKeys.Extras.ICON_NAME, icon.resourceName)
                putExtra(IpackKeys.Extras.ICON_ID, icon.id)
            }
            Log.i(tag, "handleIconSelection: ICON_SELECT")
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private fun setClipboard(string: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ipack Icon Resource", string)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}
