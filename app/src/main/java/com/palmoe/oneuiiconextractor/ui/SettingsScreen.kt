package com.palmoe.oneuiiconextractor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.palmoe.oneuiiconextractor.AppSettings
import com.palmoe.oneuiiconextractor.AppSettingsStore
import com.palmoe.oneuiiconextractor.IconSourceMode
import com.palmoe.oneuiiconextractor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    samsungIconApiAvailable: Boolean,
    onBack: () -> Unit,
    onSelectIconSourceMode: (IconSourceMode) -> Unit,
    onSelectExportSize: (Int) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (samsungIconApiAvailable) {
                SettingsSection(
                    title = stringResource(R.string.settings_source_title),
                    subtitle = stringResource(R.string.settings_source_subtitle)
                ) {
                    SettingsOptionCard(
                        title = stringResource(R.string.settings_source_samsung),
                        subtitle = stringResource(R.string.settings_source_samsung_description),
                        selected = settings.preferredIconSourceMode == IconSourceMode.Samsung,
                        onClick = { onSelectIconSourceMode(IconSourceMode.Samsung) }
                    )
                    SettingsOptionCard(
                        title = stringResource(R.string.settings_source_android),
                        subtitle = stringResource(R.string.settings_source_android_description),
                        selected = settings.preferredIconSourceMode == IconSourceMode.Android,
                        onClick = { onSelectIconSourceMode(IconSourceMode.Android) }
                    )
                }
            }

            SettingsSection(
                title = stringResource(R.string.settings_resolution_title),
                subtitle = stringResource(R.string.settings_resolution_subtitle)
            ) {
                AppSettingsStore.supportedExportSizes.forEach { sizePx ->
                    val titleRes = when (sizePx) {
                        512 -> R.string.settings_resolution_512_title
                        1024 -> R.string.settings_resolution_1024_title
                        else -> R.string.settings_resolution_2048_title
                    }
                    val descriptionRes = when (sizePx) {
                        512 -> R.string.settings_resolution_512_description
                        1024 -> R.string.settings_resolution_1024_description
                        else -> R.string.settings_resolution_2048_description
                    }

                    SettingsOptionCard(
                        title = stringResource(titleRes),
                        subtitle = stringResource(descriptionRes),
                        selected = settings.exportSizePx == sizePx,
                        onClick = { onSelectExportSize(sizePx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun SettingsOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
            .copy(alpha = 0.76f)
            .opaqueOn(MaterialTheme.colorScheme.surface)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Color.opaqueOn(background: Color): Color = compositeOver(background)
