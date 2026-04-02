package com.ipack.material

import android.app.Application
import android.content.Intent
import android.graphics.Point
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class IpackIconUiState(
    val icons: List<IpackIcon> = emptyList(),
    val allIcons: List<IpackIcon> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val cellSize: Int = IpackContent.DEFAULT_CELL_SIZE,
    val iconSize: Int = IpackContent.DEFAULT_ICON_SIZE,
    val gridBackColour: Int = IpackContent.DEFAULT_GRID_BACK_COLOUR,
    val iconDimensions: Point? = null,
    val label: String = IpackContent.LABEL,
    val attribution: String = IpackContent.ATTRIBUTION,
)

@OptIn(FlowPreview::class)
class IpackIconSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = IpackIconSelectViewModel::class.simpleName

    private val _searchQuery = MutableStateFlow("")
    private val _baseUiState = MutableStateFlow(IpackIconUiState())
    private val _isLoading = MutableStateFlow(true)

    // Filtering flow: debounced and offloaded to Default dispatcher
    private val _filteredIcons = combine(
        _searchQuery.debounce(300L),
        _baseUiState.map { state -> state.allIcons }.distinctUntilChanged()
    ) { query, allIcons ->
        val filtered = when {
            query.isBlank() -> allIcons
            else -> withContext(Dispatchers.Default) {
                allIcons.filter { icon ->
                    icon.name.contains(query.lowercase())
                }
            }
        }
        _isLoading.value = false
        filtered
    }

    // Final UI state: combines metadata, immediate query, and debounced filtered results
    val uiState: StateFlow<IpackIconUiState> = combine(
        _baseUiState,
        _searchQuery,
        _filteredIcons,
        _isLoading
    ) { base, query, filtered, isLoading ->
        base.copy(
            icons = filtered,
            searchQuery = query,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IpackIconUiState()
    )

    fun onSearchQueryUpdate(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            _isLoading.value = true
        }
    }

    fun loadIcons(intent: Intent) {
        viewModelScope.launch {
            _isLoading.value = true

            val extras = intent.extras ?: bundleOf()
            val gridBackColour = extras.getInt(
                IpackKeys.Extras.GRID_BACK_COLOUR,
                IpackContent.DEFAULT_GRID_BACK_COLOUR
            )
            val cellSize = extras.getInt(IpackKeys.Extras.CELL_SIZE, IpackContent.DEFAULT_CELL_SIZE)
            val iconSize =
                extras.getInt(IpackKeys.Extras.ICON_DISPLAY_SIZE, IpackContent.DEFAULT_ICON_SIZE)

            withContext(Dispatchers.Default) {
                val iconsList = IpackContent.getIcons()
                val iconDimensions = getIconDimensions(iconsList)

                _baseUiState.update {
                    it.copy(
                        allIcons = iconsList,
                        cellSize = cellSize,
                        iconSize = iconSize,
                        gridBackColour = gridBackColour,
                        iconDimensions = iconDimensions,
                    )
                }
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
