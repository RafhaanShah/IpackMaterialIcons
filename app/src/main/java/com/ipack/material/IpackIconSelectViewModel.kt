package com.ipack.material

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
    val isSelectAction: Boolean = false,
    val selectedIconForPopup: IpackIcon? = null
)

sealed class IpackIconSelectEvent {
    data class FinishWithResult(val resultCode: Int, val data: Intent) : IpackIconSelectEvent()
    data class NavigateToExport(val icon: IpackIcon) : IpackIconSelectEvent()
    data class ShowToast(val message: String) : IpackIconSelectEvent()
}

@HiltViewModel
@OptIn(FlowPreview::class)
class IpackIconSelectViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tag = IpackIconSelectViewModel::class.simpleName

    private val _searchQuery = MutableStateFlow("")
    private val _baseUiState = MutableStateFlow(
        IpackIconUiState(
            isSelectAction = savedStateHandle.toRoute<SelectDestination>().intentAction == IpackKeys.Actions.ICON_SELECT
        )
    )
    private val _isLoading = MutableStateFlow(true)

    private val _events = MutableSharedFlow<IpackIconSelectEvent>()
    val events: SharedFlow<IpackIconSelectEvent> = _events.asSharedFlow()

    // Filtering flow
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

    init {
        viewModelScope.launch {
            _isLoading.value = true

            val gridBackColour: Int = savedStateHandle[IpackKeys.Extras.GRID_BACK_COLOUR]
                ?: IpackContent.DEFAULT_GRID_BACK_COLOUR
            val cellSize: Int =
                savedStateHandle[IpackKeys.Extras.CELL_SIZE] ?: IpackContent.DEFAULT_CELL_SIZE
            val iconSize: Int = savedStateHandle[IpackKeys.Extras.ICON_DISPLAY_SIZE]
                ?: IpackContent.DEFAULT_ICON_SIZE

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

    fun onSearchQueryUpdate(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            _isLoading.value = true
        }
    }

    fun onIconSelected(icon: IpackIcon) {
        Log.i(tag, "onIconSelected: $icon")
        val state = uiState.value
        if (state.isSelectAction) {
            val dataString = getTaskerDataString(icon)
            viewModelScope.launch {
                _events.emit(IpackIconSelectEvent.ShowToast(dataString))
                _events.emit(
                    IpackIconSelectEvent.FinishWithResult(
                        Activity.RESULT_OK,
                        getIntent(dataString, icon)
                    )
                )
            }
        } else {
            _baseUiState.update { it.copy(selectedIconForPopup = icon) }
        }
    }

    private fun getIntent(
        dataString: String,
        icon: IpackIcon
    ): Intent =
        Intent().apply {
            data = dataString.toUri()
            putExtra(IpackKeys.Extras.ICON_LABEL, icon.name)
            putExtra(IpackKeys.Extras.ICON_NAME, icon.resourceName)
            putExtra(IpackKeys.Extras.ICON_ID, icon.id)
        }

    fun onDismissPopup() {
        _baseUiState.update { it.copy(selectedIconForPopup = null) }
    }

    fun onCopyAction(icon: IpackIcon) {
        val dataString = getTaskerDataString(icon)
        setClipboard(dataString)
        _baseUiState.update { it.copy(selectedIconForPopup = null) }
        viewModelScope.launch {
            _events.emit(IpackIconSelectEvent.ShowToast(dataString))
        }
    }

    fun onExportAction(icon: IpackIcon) {
        _baseUiState.update { it.copy(selectedIconForPopup = null) }
        viewModelScope.launch {
            _events.emit(IpackIconSelectEvent.NavigateToExport(icon))
        }
    }

    private fun getTaskerDataString(icon: IpackIcon): String {
        return "${IpackKeys.ANDROID_RESOURCE_PREFIX}${context.packageName}/${icon.resourceName}"
    }

    private fun setClipboard(content: String) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Tasker Icon Resource", content)
        clipboard.setPrimaryClip(clip)
    }

    private fun getIconDimensions(iconsList: List<IpackIcon>): Point? {
        val res = context.resources
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
