package com.ipack.material

import android.content.Intent
import android.graphics.Point
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ipack.icons.R
import com.ipack.material.ui.theme.IpackMaterialIconsTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Serializable
data class SelectDestination(val intentAction: String? = null)

@Composable
fun IpackIconSelectRoute(
    onNavigateToExport: (IpackIcon) -> Unit,
    onResult: (Int, Intent?) -> Unit = { _, _ -> },
    viewModel: IpackIconSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is IpackIconSelectEvent.NavigateToExport -> {
                    onNavigateToExport(event.icon)
                }

                is IpackIconSelectEvent.FinishWithResult -> {
                    onResult(event.resultCode, event.data)
                }

                is IpackIconSelectEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    IpackIconSelectScreen(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryUpdate,
        onIconSelected = viewModel::onIconSelected,
        onDismissPopup = viewModel::onDismissPopup,
        onVariantSelected = viewModel::onVariantSelected,
        onExportAction = viewModel::onExportAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpackIconSelectScreen(
    uiState: IpackIconUiState,
    onSearchQueryChanged: (String) -> Unit = {},
    onIconSelected: (IpackIcon) -> Unit = {},
    onDismissPopup: () -> Unit = {},
    onVariantSelected: (IpackIcon, Boolean) -> Unit = { _, _ -> },
    onExportAction: (IpackIcon) -> Unit = {},
) {
    BackHandler(enabled = uiState.searchQuery.isNotEmpty()) {
        onSearchQueryChanged("")
    }

    val backgroundColor = if (uiState.gridBackColour != IpackContent.DEFAULT_GRID_BACK_COLOUR) {
        Color(uiState.gridBackColour)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (uiState.gridBackColour != IpackContent.DEFAULT_GRID_BACK_COLOUR) {
        if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    if (uiState.selectedIconForPopup != null) {
        IpackIconSelectionDialog(
            icon = uiState.selectedIconForPopup,
            isSelectAction = uiState.isSelectAction,
            onDismissRequest = onDismissPopup,
            onVariantSelected = onVariantSelected,
            onExportAction = onExportAction
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.label) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            IpackIconHeader(uiState)
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "ContentTransition"
                ) { loading ->
                    if (loading) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    } else {
                        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
                        IpackIconGrid(
                            icons = uiState.icons,
                            cellSize = uiState.cellSize,
                            iconSize = uiState.iconSize,
                            contentColor = contentColor,
                            onIconSelected = onIconSelected,
                            bottomPadding = navBarPadding.calculateBottomPadding()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IpackIconSelectionDialog(
    icon: IpackIcon,
    isSelectAction: Boolean,
    onDismissRequest: () -> Unit,
    onVariantSelected: (IpackIcon, Boolean) -> Unit,
    onExportAction: (IpackIcon) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .heightIn(min = 200.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = icon.lightResId),
                    contentDescription = icon.name,
                    modifier = Modifier.size(64.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = icon.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (!isSelectAction) {
                    Button(
                        onClick = { onExportAction(icon) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export icon as a PNG")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Copy Resource URI for Tasker",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onVariantSelected(icon, false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Light Icon")
                    }
                    Button(
                        onClick = { onVariantSelected(icon, true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dark Icon")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = { Text("Search icons...") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.search_web_light),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.close_light),
                        contentDescription = "Clear search",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun IpackIconHeader(uiState: IpackIconUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append("Icons: ")
                if (uiState.isLoading) {
                    append("loading...")
                } else {
                    append(uiState.icons.size.toString())
                    uiState.iconDimensions?.let {
                        append(" (${it.x}x${it.y} dp)")
                    }
                }
                append("\nSource: ")
            }
            withLink(
                LinkAnnotation.Url(
                    url = uiState.attribution,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            fontStyle = FontStyle.Italic
                        )
                    )
                )
            ) {
                append(uiState.attribution)
            }
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun IpackIconGrid(
    icons: List<IpackIcon>,
    cellSize: Int,
    iconSize: Int,
    contentColor: Color,
    onIconSelected: (IpackIcon) -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val state = rememberLazyGridState()

    LazyVerticalGridScrollbar(
        state = state,
        settings = ScrollbarSettings.Default.copy(
            thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            thumbSelectedColor = MaterialTheme.colorScheme.primary,
            alwaysShowScrollbar = true
        ),
        indicatorContent = { index, isThumbSelected ->
            if (isThumbSelected) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = icons.getOrNull(index)?.name?.take(1)?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    ) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(minSize = cellSize.dp),
            contentPadding = PaddingValues(
                start = 10.dp,
                top = 10.dp,
                end = 10.dp,
                bottom = 10.dp + bottomPadding
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = icons,
                key = { it.name }
            ) { icon ->
                IpackIconItem(
                    icon = icon,
                    cellSize = cellSize,
                    iconSize = iconSize,
                    contentColor = contentColor,
                    onClick = { onIconSelected(icon) }
                )
            }
        }
    }
}

@Composable
fun IpackIconItem(
    icon: IpackIcon,
    cellSize: Int,
    iconSize: Int,
    contentColor: Color,
    onClick: () -> Unit
) {
    val wrappedName = icon.name.replace("_", "_\u200B")

    Column(
        modifier = Modifier
            .width(cellSize.dp)
            .heightIn(min = cellSize.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = icon.lightResId),
            contentDescription = icon.name,
            modifier = Modifier.size(iconSize.dp),
            colorFilter = ColorFilter.tint(contentColor)
        )
        Text(
            text = wrappedName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp
            ),
            color = contentColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IpackIconHeaderPreview() {
    IpackMaterialIconsTheme {
        IpackIconHeader(
            uiState = IpackIconUiState(
                icons = List(100) { IpackIcon(name = "icon $it", darkResId = 0, lightResId = 0) },
                isLoading = false,
                label = IpackContent.LABEL,
                attribution = IpackContent.ATTRIBUTION,
                iconDimensions = Point(24, 24)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IpackIconItemPreview() {
    IpackMaterialIconsTheme {
        IpackIconItem(
            icon = IpackIcon(
                name = "search_icon_with_a_very_long_name",
                darkResId = android.R.drawable.ic_menu_search,
                lightResId = android.R.drawable.ic_menu_search,
            ),
            cellSize = IpackContent.DEFAULT_CELL_SIZE,
            iconSize = IpackContent.DEFAULT_ICON_SIZE,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IpackIconSelectContentPreview() {
    IpackMaterialIconsTheme {
        IpackIconSelectScreen(
            uiState = IpackIconUiState(
                icons = List(100) {
                    IpackIcon(
                        name = "icon $it",
                        darkResId = android.R.drawable.ic_menu_gallery,
                        lightResId = android.R.drawable.ic_menu_gallery,
                    )
                },
                isLoading = false,
                gridBackColour = IpackContent.DEFAULT_GRID_BACK_COLOUR,
                attribution = IpackContent.ATTRIBUTION
            ),
        )
    }
}
