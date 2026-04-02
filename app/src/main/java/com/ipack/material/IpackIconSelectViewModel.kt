package com.ipack.material

import android.app.Application
import android.content.Intent
import android.graphics.Point
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class IpackIconUiState(
    val icons: List<IpackIcon> = emptyList(),
    val isLoading: Boolean = true,
    val cellSize: Int = 90,
    val iconSize: Int = 60,
    val gridBackColour: Int = 0x33777777,
    val iconDimensions: Point? = null,
    val label: String = IpackContent.LABEL,
    val attribution: String = IpackContent.ATTRIBUTION,
)

class IpackIconSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = IpackIconSelectViewModel::class.simpleName
    private val _uiState = MutableStateFlow(IpackIconUiState())
    val uiState: StateFlow<IpackIconUiState> = _uiState

    fun loadIcons(intent: Intent) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val extras = intent.extras
            val gridBackColour = extras?.getInt(IpackKeys.Extras.GRID_BACK_COLOUR, 0x33777777) ?: 0x33777777
            val cellSize = extras?.getInt(IpackKeys.Extras.CELL_SIZE, 90) ?: 90
            val iconSize = extras?.getInt(IpackKeys.Extras.ICON_DISPLAY_SIZE, 60) ?: 60

            val iconsList = withContext(Dispatchers.Default) {
                IpackContent.getIcons()
            }

            val iconDimensions = getIconDimensions(iconsList)

            _uiState.value = IpackIconUiState(
                icons = iconsList,
                isLoading = false,
                cellSize = cellSize,
                iconSize = iconSize,
                gridBackColour = gridBackColour,
                iconDimensions = iconDimensions,
            )
        }
    }

    private suspend fun getIconDimensions(iconsList: List<IpackIcon>): Point? {
        withContext(Dispatchers.Default) {
            if (iconsList.isNotEmpty()) {
                val res = getApplication<Application>().resources
                try {
                    val drawable = res.getDrawable(iconsList[0].id, null)
                    val density = res.displayMetrics.density
                    return@withContext Point(
                        (drawable.intrinsicWidth / density).toInt(),
                        (drawable.intrinsicHeight / density).toInt()
                    )
                } catch (e: Exception) {
                    Log.e(tag, "getIconDimensions", e)
                }
            }
        }

        return null
    }
}
