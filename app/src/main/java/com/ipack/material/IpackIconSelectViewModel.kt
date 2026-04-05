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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class IpackIconUiState(
    val icons: List<IpackIcon> = emptyList(),
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

    private val baseUiState = MutableStateFlow(
        IpackIconUiState(
            isSelectAction = savedStateHandle.toRoute<SelectDestination>().intentAction == IpackKeys.Actions.ICON_SELECT,
        )
    )
    private val searchQuery = MutableStateFlow("")
    private val allIcons = MutableStateFlow<List<IpackIcon>>(emptyList())
    private val isLoadingIcons = MutableStateFlow(false)
    private val isFilteringIcons = MutableStateFlow(false)

    private val _events = MutableSharedFlow<IpackIconSelectEvent>()
    val events: SharedFlow<IpackIconSelectEvent> = _events.asSharedFlow()

    private val filteredIcons = combine(
        searchQuery.debounce(300L).distinctUntilChanged(),
        allIcons
    ) { query, icons -> query to icons }
        .scan(Pair("", emptyList<IpackIcon>())) { accumulator, current ->
            val (prevQuery, prevList) = accumulator
            val (newQuery, allIcons) = current

            val result = when {
                // Case 1: Search cleared
                newQuery.isBlank() -> allIcons

                // Case 2: Refinement (e.g., "a" -> "ab")
                newQuery.startsWith(prevQuery) && prevQuery.isNotEmpty() -> {
                    prevList.filter { it.name.contains(newQuery) }
                }

                // Case 3: Deletion (e.g., "ab" -> "a") or new search
                else -> allIcons.filter { it.name.contains(newQuery) }
            }

            newQuery to result
        }
        .map { it.second }
        .flowOn(Dispatchers.Default)
        .onEach { isFilteringIcons.value = false }

    val uiState: StateFlow<IpackIconUiState> = combine(
        baseUiState,
        searchQuery,
        filteredIcons,
        isLoadingIcons,
        isFilteringIcons
    ) { base, query, filtered, isGettingIcons, isFiltering ->
        base.copy(
            icons = filtered,
            searchQuery = query,
            isLoading = isGettingIcons || isFiltering
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = baseUiState.value
    )

    init {
        viewModelScope.launch {

            val gridBackColour: Int = savedStateHandle[IpackKeys.Extras.GRID_BACK_COLOUR]
                ?: IpackContent.DEFAULT_GRID_BACK_COLOUR
            val cellSize: Int =
                savedStateHandle[IpackKeys.Extras.CELL_SIZE] ?: IpackContent.DEFAULT_CELL_SIZE
            val iconSize: Int = savedStateHandle[IpackKeys.Extras.ICON_DISPLAY_SIZE]
                ?: IpackContent.DEFAULT_ICON_SIZE

            withContext(Dispatchers.Default) {
                isLoadingIcons.value = true
                val iconsList = IpackContent.getIcons(context)
                val iconDimensions = getIconDimensions(iconsList)

                allIcons.value = iconsList
                baseUiState.update {
                    it.copy(
                        cellSize = cellSize,
                        iconSize = iconSize,
                        gridBackColour = gridBackColour,
                        iconDimensions = iconDimensions,
                    )
                }
                isLoadingIcons.value = false
            }
        }
    }

    fun onSearchQueryUpdate(query: String) {
        val sanitizedQuery = query.trim().replace(" ", "_").lowercase()
        if (searchQuery.value != sanitizedQuery) {
            // set filtering true so we immediately show the loading icon
            isFilteringIcons.value = true
            searchQuery.value = sanitizedQuery
        }
    }

    fun onIconSelected(icon: IpackIcon) {
        Log.i(tag, "onIconSelected: $icon")
        baseUiState.update { it.copy(selectedIconForPopup = icon) }
    }

    private fun getIntent(
        dataString: String,
        icon: IpackIcon,
        isDark: Boolean
    ): Intent =
        Intent().apply {
            data = dataString.toUri()
            putExtra(IpackKeys.Extras.ICON_LABEL, icon.name)
            putExtra(IpackKeys.Extras.ICON_NAME, icon.getResName(isDark))
            putExtra(IpackKeys.Extras.ICON_ID, icon.getResId(isDark))
        }

    fun onDismissPopup() {
        baseUiState.update { it.copy(selectedIconForPopup = null) }
    }

    fun onVariantSelected(icon: IpackIcon, isDark: Boolean) {
        val dataString = getTaskerDataString(icon, isDark)
        if (uiState.value.isSelectAction) {
            viewModelScope.launch {
                _events.emit(IpackIconSelectEvent.ShowToast(dataString))
                _events.emit(
                    IpackIconSelectEvent.FinishWithResult(
                        Activity.RESULT_OK,
                        getIntent(dataString, icon, isDark)
                    )
                )
            }
        } else {
            setClipboard(dataString)
            baseUiState.update { it.copy(selectedIconForPopup = null) }
            viewModelScope.launch {
                _events.emit(IpackIconSelectEvent.ShowToast(dataString))
            }
        }
    }

    fun onExportAction(icon: IpackIcon) {
        baseUiState.update { it.copy(selectedIconForPopup = null) }
        viewModelScope.launch {
            _events.emit(IpackIconSelectEvent.NavigateToExport(icon))
        }
    }

    private fun getTaskerDataString(icon: IpackIcon, isDark: Boolean): String {
        val resName = icon.getResName(isDark)
        return "${IpackKeys.ANDROID_RESOURCE_PREFIX}${context.packageName}/$resName"
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
            val id = iconsList.first().darkResId
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
