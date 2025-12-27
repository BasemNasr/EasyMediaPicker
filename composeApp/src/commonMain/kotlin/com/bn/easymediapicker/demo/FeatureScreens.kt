package com.bn.easymediapicker.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bn.easymediapicker.compose.rememberMediaPickerState
import com.bn.easymediapicker.core.MediaResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickVideosScreen(onBack: () -> Unit) {
    val pickerState = rememberMediaPickerState()
    var selectedVideos by remember { mutableStateOf<List<MediaResult>>(emptyList()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Videos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    pickerState.pickVideos { videos -> selectedVideos = videos }
                },
                icon = { Icon(Icons.Default.VideoLibrary, "Pick") },
                text = { Text("Pick Videos") }
            )
        }
    ) { padding ->
        MediaList(selectedVideos, padding, Icons.Default.VideoFile)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickFilesScreen(onBack: () -> Unit) {
    val pickerState = rememberMediaPickerState()
    var selectedFiles by remember { mutableStateOf<List<MediaResult>>(emptyList()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    pickerState.pickFiles { files -> selectedFiles = files }
                },
                icon = { Icon(Icons.Default.AttachFile, "Pick") },
                text = { Text("Pick Any File") }
            )
        }
    ) { padding ->
        MediaList(selectedFiles, padding, Icons.Default.InsertDriveFile)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(onBack: () -> Unit) {
    val pickerState = rememberMediaPickerState()
    var capturedMedia by remember { mutableStateOf<MediaResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Capture") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                pickerState.captureImage { result -> capturedMedia = result }
            }) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Capture Photo")
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(onClick = {
                pickerState.captureVideo { result -> capturedMedia = result }
            }) {
                Icon(Icons.Default.Videocam, null)
                Spacer(Modifier.width(8.dp))
                Text("Capture Video")
            }
            
            Spacer(Modifier.height(32.dp))
            
            capturedMedia?.let { media ->
                Card(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Captured:", style = MaterialTheme.typography.titleMedium)
                        Text("Name: ${media.name}")
                        Text("Path: ${media.uri}")
                    }
                }
            }
        }
    }
}

@Composable
fun MediaList(
    items: List<MediaResult>, 
    padding: PaddingValues,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
     if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("No items selected")
        }
    } else {
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                ListItem(
                    headlineContent = { Text(item.name ?: "Unknown") },
                    supportingContent = { Text("Size: ${item.size} bytes") },
                    leadingContent = { Icon(icon, null) }
                )
                Divider()
            }
        }
    }
}
