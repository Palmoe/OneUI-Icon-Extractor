package com.palmoe.oneuiiconextractor.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.palmoe.oneuiiconextractor.MainUiState
import com.palmoe.oneuiiconextractor.IconSourceMode
import com.palmoe.oneuiiconextractor.R
import com.palmoe.oneuiiconextractor.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(
    uiState: MainUiState,
    previewIconLoader: suspend (InstalledApp, Int) -> Bitmap?,
    onToggleThirdPartyApps: () -> Unit,
    onToggleSystemApps: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    onToggleVisibleSelection: (Set<String>) -> Unit,
    onOpenSettings: () -> Unit,
    onExportClick: () -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val visibleApps = remember(
        uiState.apps,
        uiState.showThirdPartyApps,
        uiState.showSystemApps,
        uiState.searchQuery
    ) {
        uiState.filteredApps()
    }
    val visiblePackageNames = remember(visibleApps) {
        visibleApps.map { it.packageName }.toSet()
    }
    val allVisibleSelected = visibleApps.isNotEmpty() &&
        visibleApps.all { it.packageName in uiState.selectedPackages }
    val selectionToggleState = when {
        allVisibleSelected -> ToggleableState.On
        uiState.selectedPackages.isNotEmpty() -> ToggleableState.Indeterminate
        else -> ToggleableState.Off
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActionBar(
                selectedCount = uiState.selectedPackages.size,
                visibleCount = visibleApps.size,
                selectionToggleState = selectionToggleState,
                isExporting = uiState.isExporting,
                onToggleVisibleSelection = { onToggleVisibleSelection(visiblePackageNames) },
                onExportClick = onExportClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_label)) },
                trailingIcon = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_settings),
                            contentDescription = stringResource(R.string.open_settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilterRow(
                showThirdPartyApps = uiState.showThirdPartyApps,
                showSystemApps = uiState.showSystemApps,
                onToggleThirdPartyApps = onToggleThirdPartyApps,
                onToggleSystemApps = onToggleSystemApps
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> LoadingState()
                visibleApps.isEmpty() -> EmptyState()
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 3.dp, bottom = 8.dp)
                    ) {
                        items(
                            items = visibleApps,
                            key = { app -> app.packageName }
                        ) { app ->
                            AppRow(
                                app = app,
                                isSelected = app.packageName in uiState.selectedPackages,
                                previewIconSource = uiState.effectiveIconSourceMode,
                                previewIconLoader = previewIconLoader,
                                onToggle = { onToggleApp(app.packageName) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(96.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    showThirdPartyApps: Boolean,
    showSystemApps: Boolean,
    onToggleThirdPartyApps: () -> Unit,
    onToggleSystemApps: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.third_party_apps),
            selected = showThirdPartyApps,
            onClick = onToggleThirdPartyApps
        )
        FilterCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.system_apps),
            selected = showSystemApps,
            onClick = onToggleSystemApps
        )
    }
}

@Composable
private fun FilterCard(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val cardColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
            .copy(alpha = 0.88f)
            .opaqueOn(MaterialTheme.colorScheme.surface)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = 0.5f)
            .opaqueOn(MaterialTheme.colorScheme.surface)
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter_selected),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    isSelected: Boolean,
    previewIconSource: IconSourceMode,
    previewIconLoader: suspend (InstalledApp, Int) -> Bitmap?,
    onToggle: () -> Unit
) {
    val density = LocalDensity.current
    val sizePx = remember(density) { with(density) { 56.dp.roundToPx() } }
    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        app.packageName,
        app.componentName?.className,
        sizePx,
        previewIconSource
    ) {
        value = withContext(Dispatchers.IO) {
            previewIconLoader(app, sizePx)
        }
    }
    val cardColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
            .copy(alpha = 0.72f)
            .opaqueOn(MaterialTheme.colorScheme.surface)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconPreview(
                label = app.label,
                bitmap = bitmap,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (app.isSystemApp) {
                        SystemDot()
                    }
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun IconPreview(
    label: String,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(0.dp),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (bitmap == null) {
                Text(
                    text = label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun Color.opaqueOn(background: Color): Color = compositeOver(background)

@Composable
private fun SystemDot() {
    Box(
        modifier = Modifier
            .size(5.5.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(999.dp)
            )
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.loading_apps),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.empty_state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BottomActionBar(
    selectedCount: Int,
    visibleCount: Int,
    selectionToggleState: ToggleableState,
    isExporting: Boolean,
    onToggleVisibleSelection: () -> Unit,
    onExportClick: () -> Unit
) {
    Surface(
        shadowElevation = 10.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.selection_summary, selectedCount, visibleCount),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TriStateCheckbox(
                    state = selectionToggleState,
                    onClick = onToggleVisibleSelection,
                    enabled = visibleCount > 0 && !isExporting
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCount > 0 && !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = stringResource(
                        if (isExporting) {
                            R.string.export_in_progress
                        } else {
                            R.string.export_selected
                        }
                    )
                )
            }
        }
    }
}
