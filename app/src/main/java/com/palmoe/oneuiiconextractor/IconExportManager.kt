package com.palmoe.oneuiiconextractor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.palmoe.oneuiiconextractor.model.ExportResult
import com.palmoe.oneuiiconextractor.model.InstalledApp
import java.io.IOException
import java.util.Locale

object IconExportManager {
    fun exportSelectedApps(
        context: Context,
        treeUri: Uri,
        apps: List<InstalledApp>,
        exportFolderName: String,
        exportSizePx: Int,
        iconSourceMode: IconSourceMode
    ): ExportResult {
        val rootDirectory = DocumentFile.fromTreeUri(context, treeUri)
            ?: return ExportResult(
                exportedCount = 0,
                failedCount = apps.size,
                folderName = exportFolderName,
                failureReason = context.getString(R.string.export_directory_unavailable)
            )

        val targetDirectory = rootDirectory.createDirectory(exportFolderName) ?: rootDirectory
        var exported = 0
        var failed = 0

        apps.forEachIndexed { index, app ->
            val bitmap = SamsungIconResolver.loadBitmap(
                context = context,
                app = app,
                sizePx = exportSizePx,
                useCache = false,
                iconSourceMode = iconSourceMode
            )

            if (bitmap == null) {
                failed += 1
                return@forEachIndexed
            }

            val baseFileName = buildFileName(index = index, app = app)
            val file = targetDirectory.createFile("image/png", baseFileName)
            if (file == null) {
                failed += 1
                return@forEachIndexed
            }

            if (writeBitmap(context, file.uri, bitmap)) {
                exported += 1
            } else {
                failed += 1
            }
        }

        return ExportResult(
            exportedCount = exported,
            failedCount = failed,
            folderName = exportFolderName
        )
    }

    private fun writeBitmap(context: Context, uri: Uri, bitmap: Bitmap): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            } == true
        } catch (_: IOException) {
            false
        }
    }

    private fun buildFileName(index: Int, app: InstalledApp): String {
        val prefix = (index + 1).toString().padStart(3, '0')
        val safeLabel = sanitize(app.label)
        val safePackage = sanitize(app.packageName)
        return "$prefix-$safeLabel-$safePackage"
    }

    private fun sanitize(value: String): String {
        val normalized = value
            .replace(invalidFileNameCharacters, "-")
            .replace(whitespacePattern, "_")
            .trim('_', '-', '.')
        return normalized.ifBlank { "app" }.lowercase(Locale.US)
    }

    private val invalidFileNameCharacters = Regex("[\\\\/:*?\"<>|]")
    private val whitespacePattern = Regex("\\s+")
}
