package com.stack.feature.gate

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.stack.core.ui.theme.StackTheme
import com.stack.feature.gate.GateContract.*

/**
 * Gate screen - Entry point enforcing Permission → Folders → Scan flow.
 */
@Composable
fun GateScreen(
    onGateReady: () -> Unit,
    viewModel: GateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val isPermanentlyDenied = !granted &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                context as android.app.Activity,
                getRequiredPermission()
            )
        viewModel.onIntent(Intent.OnPermissionResult(granted, isPermanentlyDenied))
    }

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            val displayName = uri.lastPathSegment?.substringAfterLast(':') ?: "Unknown"
            viewModel.onIntent(Intent.OnFolderSelected(uri.toString(), displayName))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToLibrary -> onGateReady()
                is Effect.LaunchPermissionRequest -> {
                    permissionLauncher.launch(getRequiredPermission())
                }
                is Effect.LaunchSafPicker -> safLauncher.launch(null)
                is Effect.OpenSystemSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
                is Effect.ShowError -> { /* Shown via snackbar below */ }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            val message = when (error) {
                is GateError.PermissionRequired -> "Storage permission required"
                is GateError.NoFoldersSelected -> "Select at least one folder"
                is GateError.ScanFailed -> "Scan failed: ${error.message}"
                is GateError.Unknown -> "An error occurred"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(Intent.DismissError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "Stack",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your Local Music Archive",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            when (state.currentStep) {
                GateStep.PERMISSION -> PermissionStep(
                    permissionStatus = state.permissionStatus,
                    onRequestPermission = { viewModel.onIntent(Intent.RequestPermission) },
                    onRetryPermission = { viewModel.onIntent(Intent.RetryPermission) }
                )
                GateStep.FOLDER_SELECTION -> FolderSelectionStep(
                    selectedFolders = state.selectedFolders,
                    isLoading = state.isLoading,
                    onOpenSafPicker = { viewModel.onIntent(Intent.OpenSafPicker) },
                    onRemoveFolder = { viewModel.onIntent(Intent.RemoveFolder(it)) },
                    onStartScan = { viewModel.onIntent(Intent.StartInitialScan) },
                    canStartScan = state.canStartScan
                )
                GateStep.SCANNING -> ScanProgressStep(
                    progress = state.scanProgress,
                    currentFile = state.scanningFile,
                    scannedCount = state.scannedCount,
                    totalCount = state.totalCount
                )
                GateStep.READY -> {
                    Text("Complete! Loading library...")
                }
            }
        }
    }
}

@Composable
private fun PermissionStep(
    permissionStatus: PermissionStatus,
    onRequestPermission: () -> Unit,
    onRetryPermission: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Access Your Music",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Stack needs permission to access your audio files.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        when (permissionStatus) {
            PermissionStatus.NOT_REQUESTED,
            PermissionStatus.DENIED -> {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(if (permissionStatus == PermissionStatus.DENIED) "Try Again" else "Grant Permission")
                }
            }
            PermissionStatus.PERMANENTLY_DENIED -> {
                OutlinedButton(
                    onClick = onRetryPermission,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Open Settings")
                }
            }
            PermissionStatus.GRANTED -> {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun FolderSelectionStep(
    selectedFolders: List<com.stack.domain.model.SourceFolder>,
    isLoading: Boolean,
    onOpenSafPicker: () -> Unit,
    onRemoveFolder: (com.stack.domain.model.SourceFolder) -> Unit,
    onStartScan: () -> Unit,
    canStartScan: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Music Folders",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onOpenSafPicker,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Folder")
        }

        Spacer(Modifier.height(16.dp))

        selectedFolders.forEach { folder ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = folder.displayName,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onRemoveFolder(folder) }) {
                        Text("Remove")
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStartScan,
            enabled = canStartScan && !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Scan Library")
            }
        }
    }
}

@Composable
private fun ScanProgressStep(
    progress: Float,
    currentFile: String,
    scannedCount: Int,
    totalCount: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(120.dp),
            strokeWidth = 8.dp
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Scanning...",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        if (totalCount > 0) {
            Text(
                text = "$scannedCount / $totalCount tracks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (currentFile.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = currentFile,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getRequiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
