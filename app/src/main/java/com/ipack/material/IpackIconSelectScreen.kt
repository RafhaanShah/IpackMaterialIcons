package com.ipack.material

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image

@Composable
fun IpackIconSelectScreen(
    viewModel: IpackIconSelectViewModel,
    onIconSelected: (IpackIcon) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            IpackIconHeader(uiState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(uiState.gridBackColour))
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

@Composable
fun IpackIconHeader(uiState: IpackIconUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val infoText = buildString {
            append("#${uiState.icons.size}")
            uiState.iconDimensions?.let {
                append(" ${it.x}x${it.y}")
            }
        }
        Text(
            text = infoText,
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodyMedium
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
