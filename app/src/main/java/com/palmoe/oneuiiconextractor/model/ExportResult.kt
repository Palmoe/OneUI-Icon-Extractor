package com.palmoe.oneuiiconextractor.model

data class ExportResult(
    val exportedCount: Int,
    val failedCount: Int,
    val folderName: String,
    val failureReason: String? = null
)
