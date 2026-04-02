package com.ipack.material

import android.app.Application
import android.content.Intent
import android.graphics.Point
import android.util.Log
import androidx.core.os.bundleOf
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
    val cellSize: Int = IpackContent.DEFAULT_CELL_SIZE,
    val iconSize: Int = IpackContent.DEFAULT_ICON_SIZE,
    val gridBackColour: Int = IpackContent.DEFAULT_GRID_BACK_COLOUR,
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

            val extras = intent.extras ?: bundleOf()
            val gridBackColour = extras.getInt(
                IpackKeys.Extras.GRID_BACK_COLOUR,
                IpackContent.DEFAULT_GRID_BACK_COLOUR
            )
            val cellSize = extras.getInt(IpackKeys.Extras.CELL_SIZE, IpackContent.DEFAULT_CELL_SIZE)
            val iconSize =
                extras.getInt(IpackKeys.Extras.ICON_DISPLAY_SIZE, IpackContent.DEFAULT_ICON_SIZE)

            withContext(Dispatchers.Default) {
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
    }

    private fun getIconDimensions(iconsList: List<IpackIcon>): Point? {
        val res = getApplication<Application>().resources
        try {
            val id = iconsList.firstOrNull()?.id ?: return null
            val drawable = res.getDrawable(id, null)
            val density = res.displayMetrics.density
            return Point(
                (drawable.intrinsicWidth / density).toInt(),
                (drawable.intrinsicHeight / density).toInt()
            )
        } catch (e: Exception) {
            Log.e(tag, "getIconDimensions", e)
        }
        return null
    }
}
