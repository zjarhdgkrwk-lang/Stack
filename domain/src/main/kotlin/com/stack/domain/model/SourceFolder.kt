package com.stack.domain.model

/**
 * Domain model representing a SAF source folder (SSOT 3.1, 4.1).
 */
data class SourceFolder(
    val id: Long,
    val treeUri: String,          // SAF Tree URI (persisted)
    val displayName: String,      // User-friendly name
    val displayPath: String,      // UI display path
    val trackCount: Int,
    val lastScanned: Long?,
    val addedAt: Long,
    val isActive: Boolean         // Permission still valid
)
