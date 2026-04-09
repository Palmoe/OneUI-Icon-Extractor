package com.palmoe.oneuiiconextractor

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.palmoe.oneuiiconextractor.model.ExportResult
import com.palmoe.oneuiiconextractor.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

data class MainUiState(
    val apps: List<InstalledApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val showThirdPartyApps: Boolean = true,
    val showSystemApps: Boolean = true,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val samsungIconApiAvailable: Boolean = false,
    val preferredIconSourceMode: IconSourceMode = IconSourceMode.Samsung,
    val effectiveIconSourceMode: IconSourceMode = IconSourceMode.Android,
    val exportSizePx: Int = AppSettingsStore.DEFAULT_EXPORT_SIZE_PX,
    val message: String? = null
) {
    fun filteredApps(): List<InstalledApp> {
        val normalizedQuery = searchQuery.trim()
        return apps.filter { app ->
            ((app.isSystemApp && showSystemApps) || (!app.isSystemApp && showThirdPartyApps)) &&
                (normalizedQuery.isBlank() ||
                    app.label.contains(normalizedQuery, ignoreCase = true) ||
                    app.packageName.contains(normalizedQuery, ignoreCase = true))
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager = application.packageManager
    private val launcherApps = application.getSystemService(LauncherApps::class.java)
    private val settingsStore = AppSettingsStore.get(application)
    private val samsungIconApiAvailable = SamsungIconResolver.isSamsungIconApiAvailable(packageManager)
    private val initialSettings = settingsStore.settings.value

    private val _uiState = MutableStateFlow(
        MainUiState(
            samsungIconApiAvailable = samsungIconApiAvailable,
            preferredIconSourceMode = initialSettings.preferredIconSourceMode,
            effectiveIconSourceMode = resolveEffectiveIconSourceMode(
                preferredIconSourceMode = initialSettings.preferredIconSourceMode,
                samsungIconApiAvailable = samsungIconApiAvailable
            ),
            exportSizePx = initialSettings.exportSizePx
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        loadApps()
    }

    fun toggleThirdPartyApps() {
        _uiState.update { state ->
            if (!state.showThirdPartyApps && !state.showSystemApps) {
                state.copy(showThirdPartyApps = true)
            } else if (state.showThirdPartyApps && !state.showSystemApps) {
                state
            } else {
                state.copy(showThirdPartyApps = !state.showThirdPartyApps)
            }
        }
    }

    fun toggleSystemApps() {
        _uiState.update { state ->
            if (!state.showThirdPartyApps && !state.showSystemApps) {
                state.copy(showSystemApps = true)
            } else if (!state.showThirdPartyApps && state.showSystemApps) {
                state
            } else {
                state.copy(showSystemApps = !state.showSystemApps)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state -> state.copy(searchQuery = query) }
    }

    fun toggleSelection(packageName: String) {
        _uiState.update { state ->
            val nextSelection = state.selectedPackages.toMutableSet()
            if (!nextSelection.add(packageName)) {
                nextSelection.remove(packageName)
            }
            state.copy(selectedPackages = nextSelection)
        }
    }

    fun selectAllVisible() {
        _uiState.update { state ->
            state.copy(
                selectedPackages = state.selectedPackages + state.filteredApps().map { it.packageName }.toSet()
            )
        }
    }

    fun toggleVisibleSelection(visiblePackageNames: Set<String>) {
        if (visiblePackageNames.isEmpty()) {
            return
        }

        _uiState.update { state ->
            val allVisibleSelected = visiblePackageNames.all { it in state.selectedPackages }
            state.copy(
                selectedPackages = if (allVisibleSelected) {
                    emptySet()
                } else {
                    state.selectedPackages + visiblePackageNames
                }
            )
        }
    }

    fun clearSelection() {
        _uiState.update { state -> state.copy(selectedPackages = emptySet()) }
    }

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    fun onExportPickerDismissed() {
        if (_uiState.value.selectedPackages.isNotEmpty()) {
            _uiState.update {
                it.copy(message = getApplication<Application>().getString(R.string.export_cancelled))
            }
        }
    }

    fun exportSelectedIcons(treeUri: Uri) {
        val selectedApps = _uiState.value.apps.filter { it.packageName in _uiState.value.selectedPackages }
        if (selectedApps.isEmpty()) {
            _uiState.update {
                it.copy(message = getApplication<Application>().getString(R.string.select_apps_first))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            val exportFolderName = buildExportFolderName()
            val result = withContext(Dispatchers.IO) {
                IconExportManager.exportSelectedApps(
                    context = getApplication(),
                    treeUri = treeUri,
                    apps = selectedApps,
                    exportFolderName = exportFolderName,
                    exportSizePx = _uiState.value.exportSizePx,
                    iconSourceMode = _uiState.value.effectiveIconSourceMode
                )
            }
            _uiState.update { current ->
                current.copy(
                    isExporting = false,
                    message = result.toMessage(getApplication()),
                    selectedPackages = if (result.exportedCount > 0) emptySet() else current.selectedPackages
                )
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.settings.collectLatest { settings ->
                _uiState.update { state ->
                    state.copy(
                        preferredIconSourceMode = settings.preferredIconSourceMode,
                        effectiveIconSourceMode = resolveEffectiveIconSourceMode(
                            preferredIconSourceMode = settings.preferredIconSourceMode,
                            samsungIconApiAvailable = state.samsungIconApiAvailable
                        ),
                        exportSizePx = settings.exportSizePx
                    )
                }
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                queryLauncherApps()
            }
            _uiState.update { state ->
                state.copy(
                    apps = apps,
                    isLoading = false
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun queryLauncherApps(): List<InstalledApp> {
        val launchableApps = LinkedHashMap<String, InstalledApp>()
        queryLauncherAppsService().forEach { app ->
            launchableApps[app.packageName] = app
        }
        queryLauncherAppsFromPackageManager().forEach { app ->
            launchableApps.putIfAbsent(app.packageName, app)
        }

        val installedApps = LinkedHashMap<String, InstalledApp>()
        queryInstalledApplications(launchableApps).forEach { app ->
            installedApps[app.packageName] = app
        }
        launchableApps.values.forEach { app ->
            installedApps.putIfAbsent(app.packageName, app)
        }

        return installedApps.values
            .sortedWith(
                compareBy<InstalledApp>(
                    { it.label.lowercase(Locale.getDefault()) },
                    { it.packageName.lowercase(Locale.getDefault()) }
                )
            )
    }

    @Suppress("DEPRECATION")
    private fun queryInstalledApplications(
        launchableApps: Map<String, InstalledApp>
    ): List<InstalledApp> {
        // val selfPackageName = getApplication<Application>().packageName
        val flags = PackageManager.MATCH_DISABLED_COMPONENTS or
            PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS

        return packageManager.getInstalledApplications(flags)
            .asSequence()
            // .filterNot { applicationInfo -> applicationInfo.packageName == selfPackageName }
            .map { applicationInfo ->
                val packageName = applicationInfo.packageName
                val launchableApp = launchableApps[packageName]

                InstalledApp(
                    packageName = packageName,
                    label = packageManager.getApplicationLabel(applicationInfo)
                        ?.toString()
                        ?.ifBlank { packageName }
                        ?: packageName,
                    componentName = launchableApp?.componentName,
                    isSystemApp = isSystemApp(applicationInfo)
                )
            }
            .toList()
    }

    private fun queryLauncherAppsService(): List<InstalledApp> {
        val service = launcherApps ?: return emptyList()
        // val selfPackageName = getApplication<Application>().packageName

        return runCatching {
            service.getActivityList(null, Process.myUserHandle())
        }.getOrDefault(emptyList())
            .groupBy { it.applicationInfo.packageName }
            .mapNotNull { (packageName, activities) ->
                // if (packageName == selfPackageName) {
                //     return@mapNotNull null
                // }

                val chosenActivity = pickLauncherActivity(
                    packageName = packageName,
                    activities = activities,
                    componentSelector = { it.componentName },
                    labelSelector = { it.label?.toString() ?: packageName }
                )

                chosenActivity?.toInstalledApp()
            }
    }

    private fun queryLauncherAppsFromPackageManager(): List<InstalledApp> {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        // val selfPackageName = getApplication<Application>().packageName

        return packageManager.queryIntentActivities(launchIntent, 0)
            .groupBy { it.activityInfo.packageName }
            .mapNotNull { (packageName, activities) ->
                // if (packageName == selfPackageName) {
                //     return@mapNotNull null
                // }

                val chosenActivity = pickLauncherActivity(
                    packageName = packageName,
                    activities = activities,
                    componentSelector = { ComponentName(it.activityInfo.packageName, it.activityInfo.name) },
                    labelSelector = {
                        it.loadLabel(packageManager)?.toString()?.ifBlank { packageName } ?: packageName
                    }
                )

                chosenActivity?.let { resolveInfo ->
                    val activityInfo = resolveInfo.activityInfo
                    val applicationInfo = activityInfo.applicationInfo

                    InstalledApp(
                        packageName = packageName,
                        label = resolveInfo.loadLabel(packageManager)?.toString()?.ifBlank { packageName } ?: packageName,
                        componentName = ComponentName(activityInfo.packageName, activityInfo.name),
                        isSystemApp = isSystemApp(applicationInfo)
                    )
                }
            }
    }

    private fun <T> pickLauncherActivity(
        packageName: String,
        activities: List<T>,
        componentSelector: (T) -> ComponentName,
        labelSelector: (T) -> String
    ): T? {
        val preferredComponent = packageManager.getLaunchIntentForPackage(packageName)?.component
        return preferredComponent?.let { component ->
            activities.firstOrNull { componentSelector(it) == component }
        } ?: activities.minByOrNull { entry ->
            labelSelector(entry).lowercase(Locale.getDefault())
        }
    }

    private fun buildExportFolderName(): String {
        val timestamp = DateFormat.format("yyyyMMdd-HHmmss", Date()).toString()
        return "oneui-icon-export-$timestamp"
    }
}

private fun LauncherActivityInfo.toInstalledApp(): InstalledApp {
    val applicationInfo = applicationInfo

    return InstalledApp(
        packageName = applicationInfo.packageName,
        label = label?.toString()?.ifBlank { applicationInfo.packageName } ?: applicationInfo.packageName,
        componentName = componentName,
        isSystemApp = isSystemApp(applicationInfo)
    )
}

private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
    return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 ||
        applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
}

private fun resolveEffectiveIconSourceMode(
    preferredIconSourceMode: IconSourceMode,
    samsungIconApiAvailable: Boolean
): IconSourceMode {
    return if (preferredIconSourceMode == IconSourceMode.Samsung && samsungIconApiAvailable) {
        IconSourceMode.Samsung
    } else {
        IconSourceMode.Android
    }
}

private fun ExportResult.toMessage(application: Application): String {
    return when {
        exportedCount > 0 && failedCount == 0 -> {
            application.getString(R.string.export_success, exportedCount, folderName)
        }

        exportedCount > 0 -> {
            application.getString(R.string.export_partial_success, exportedCount, failedCount, folderName)
        }

        else -> {
            failureReason ?: application.getString(R.string.export_failed_generic)
        }
    }
}
