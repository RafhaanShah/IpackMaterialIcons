package com.ipack.material

import android.graphics.Point
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipack.material.ui.theme.IpackMaterialIconsTheme

@Composable
fun IpackIconSelectScreen(
    viewModel: IpackIconSelectViewModel,
    onIconSelected: (IpackIcon) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    IpackIconSelectContent(uiState, onIconSelected)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpackIconSelectContent(
    uiState: IpackIconUiState,
    onIconSelected: (IpackIcon) -> Unit
) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (uiState.gridBackColour != IpackContent.DEFAULT_GRID_BACK_COLOUR) Color(uiState.gridBackColour)
                        else MaterialTheme.colorScheme.surface
                    )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    IpackIconGrid(
                        icons = uiState.icons,
                        cellSize = uiState.cellSize,
                        iconSize = uiState.iconSize,
                        onIconSelected = onIconSelected
                    )
                }
            }
        }
    }
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
                append(uiState.icons.size.toString())
                uiState.iconDimensions?.let {
                    append(" (${it.x}x${it.y} dp)")
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
    onIconSelected: (IpackIcon) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = cellSize.dp),
        contentPadding = PaddingValues(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(icons) { icon ->
            IpackIconItem(
                icon = icon,
                cellSize = cellSize,
                iconSize = iconSize,
                onClick = { onIconSelected(icon) }
            )
        }
    }
}

@Composable
fun IpackIconItem(
    icon: IpackIcon,
    cellSize: Int,
    iconSize: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(cellSize.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon.id),
            contentDescription = icon.name,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IpackIconHeaderPreview() {
    IpackMaterialIconsTheme {
        IpackIconHeader(
            uiState = IpackIconUiState(
                icons = List(100) { IpackIcon(0, "icon", "icon") },
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
            icon = IpackIcon(android.R.drawable.ic_menu_search, "search", "search"),
            cellSize = IpackContent.DEFAULT_CELL_SIZE,
            iconSize = IpackContent.DEFAULT_ICON_SIZE,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IpackIconSelectContentPreview() {
    IpackMaterialIconsTheme {
        IpackIconSelectContent(
            uiState = IpackIconUiState(
                icons = List(100) { IpackIcon(android.R.drawable.ic_menu_gallery, "icon $it", "icon_$it") },
                isLoading = false,
                gridBackColour = IpackContent.DEFAULT_GRID_BACK_COLOUR,
                attribution = IpackContent.ATTRIBUTION
            ),
            onIconSelected = {}
        )
    }
}
