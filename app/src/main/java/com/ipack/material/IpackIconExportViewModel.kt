package com.ipack.material

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class IpackIconExportUiState(
    val icon: IpackIcon,
    val selectedColor: Color = Color.White,
    val hexCode: String = "FFFFFFFF",
    val fileName: String = icon.name,
    val iconSizeInput: String = "128",
    val showDialog: Boolean = false,
    val isLoading: Boolean = false,
)

sealed class ExportEvent {
    data class Success(val message: String) : ExportEvent()
    data class Error(val message: String) : ExportEvent()
}

@HiltViewModel
class IpackIconExportViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val tag = IpackIconExportViewModel::class.simpleName

    private val _uiState = MutableStateFlow(
        IpackIconExportUiState(
            icon = IpackContent.getIcon(
                context,
                savedStateHandle.toRoute<ExportDestination>().iconId
            ),
        )
    )
    val uiState: StateFlow<IpackIconExportUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ExportEvent>()
    val events: SharedFlow<ExportEvent> = _events.asSharedFlow()


    fun updateColor(color: Color) {
        _uiState.update {
            it.copy(
                selectedColor = color,
                hexCode = String.format(Locale.US, "%08X", color.toArgb())
            )
        }
    }

    fun updateHexCode(hex: String) {
        _uiState.update { it.copy(hexCode = hex) }
        try {
            val sanitizedHex = if (hex.startsWith("#")) hex else "#$hex"
            val colorInt = sanitizedHex.toColorInt()
            val color = Color(colorInt)
            _uiState.update { it.copy(selectedColor = color) }
        } catch (_: Exception) {
            // Invalid hex, keep as is
        }
    }

    fun updateFileName(name: String) {
        _uiState.update { it.copy(fileName = name) }
    }

    fun updateIconSize(size: String) {
        _uiState.update { it.copy(iconSizeInput = size) }
    }

    fun setShowDialog(show: Boolean) {
        _uiState.update { it.copy(showDialog = show) }
    }

    fun exportIcon(context: Context, uri: Uri) {
        val state = _uiState.value
        val icon = state.icon
        val size = state.iconSizeInput.toIntOrNull() ?: 128
        val color = state.selectedColor

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bitmap = createBitmap(size, size)
                val canvas = Canvas(bitmap)
                val drawable = ContextCompat.getDrawable(context, icon.id)
                drawable?.let { d ->
                    val wrapped = DrawableCompat.wrap(d).mutate()
                    DrawableCompat.setTint(wrapped, color.toArgb())
                    wrapped.setBounds(0, 0, size, size)
                    wrapped.draw(canvas)
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                _events.emit(ExportEvent.Success("Icon exported successfully"))
            } catch (e: Exception) {
                Log.e(tag, "exportIcon", e)
                _events.emit(ExportEvent.Error("Failed to export icon: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
