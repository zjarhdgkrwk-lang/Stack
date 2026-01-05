package com.stack.feature.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stack.feature.library.LibraryContract.LibraryTab

/**
 * Empty state shown when no content is available.
 */
@Composable
fun EmptyLibraryState(
    currentTab: LibraryTab,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, title, subtitle) = when (currentTab) {
        LibraryTab.TRACKS -> Triple(
            Icons.Outlined.MusicNote,
            "No tracks found",
            "Add music folders to scan your library"
        )
        LibraryTab.ALBUMS -> Triple(
            Icons.Outlined.Album,
            "No albums found",
            "Albums will appear once tracks are scanned"
        )
        LibraryTab.ARTISTS -> Triple(
            Icons.Outlined.Person,
            "No artists found",
            "Artists will appear once tracks are scanned"
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Library")
            }
        }
    }
}
