package com.bn.easymediapicker.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bn.easymediapicker.compose.MediaPickerState
import com.bn.easymediapicker.compose.rememberMediaPickerState
import com.bn.easymediapicker.core.MediaPickerConfig
import com.bn.easymediapicker.core.MediaResult
import com.bn.easymediapicker.core.MediaType

@Composable
fun DashboardScreen() {
    val pickerState = rememberMediaPickerState()
    var selectedMedia by remember { mutableStateOf<List<MediaResult>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pickerState.error) {
        pickerState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "An unknown error occurred",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD), // Light Blue
                            Color(0xFFF3E5F5)  // Light Purple
                        )
                    )
                )
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                item {
                    HeaderSection()
                }

                item {
                    MediaSectionCard(
                        title = "Images",
                        subtitle = "Pick or capture photos",
                        icon = Icons.Outlined.Image,
                        iconColor = Color(0xFF536DFE)
                    ) {
                        ActionButton(
                            text = "Pick Single Image",
                            icon = Icons.Default.Image,
                            backgroundColor = Color(0xFF536DFE),
                            onClick = {
                                pickerState.pickImage { result ->
                                    result?.let { selectedMedia = listOf(it) }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(
                            text = "Pick Multiple Images",
                            icon = Icons.Default.PhotoLibrary,
                            isOutlined = true,
                            borderColor = Color(0xFF536DFE),
                            textColor = Color(0xFF536DFE),
                            onClick = {
                                pickerState.pickImages(maxSelection = 10) { results ->
                                    selectedMedia = results
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(
                            text = "Capture with Camera",
                            icon = Icons.Default.CameraAlt,
                            isOutlined = true,
                            borderColor = Color(0xFF536DFE),
                            textColor = Color(0xFF536DFE),
                            onClick = {
                                pickerState.captureImage { result ->
                                    result?.let { selectedMedia = listOf(it) }
                                }
                            }
                        )
                    }
                }

                item {
                    MediaSectionCard(
                        title = "Videos",
                        subtitle = "Pick or record videos",
                        icon = Icons.Outlined.Movie,
                        iconColor = Color(0xFFF50057)
                    ) {
                        ActionButton(
                            text = "Pick Single Video",
                            icon = Icons.Default.PlayCircle,
                            backgroundColor = Color(0xFFF50057),
                            onClick = {
                                pickerState.pickVideo { result ->
                                    result?.let { selectedMedia = listOf(it) }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(
                            text = "Pick Multiple Videos",
                            icon = Icons.Default.VideoLibrary,
                            isOutlined = true,
                            borderColor = Color(0xFFF50057),
                            textColor = Color(0xFFF50057),
                            onClick = {
                                pickerState.pickVideos(maxSelection = 5) { results ->
                                    selectedMedia = results
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                         ActionButton(
                            text = "Record Video",
                            icon = Icons.Default.Videocam,
                            isOutlined = true,
                            borderColor = Color(0xFFF50057),
                            textColor = Color(0xFFF50057),
                            onClick = {
                                pickerState.captureVideo { result ->
                                     result?.let { selectedMedia = listOf(it) }
                                }
                            }
                        )
                    }
                }

                item {
                    MediaSectionCard(
                        title = "Files & Documents",
                        subtitle = "Pick any file type",
                        icon = Icons.Outlined.Description,
                        iconColor = Color(0xFF00BFA5)
                    ) {
                        ActionButton(
                            text = "Pick Single File",
                            icon = Icons.Outlined.Description,
                            backgroundColor = Color(0xFF00BFA5),
                            onClick = {
                                pickerState.pickFile { result ->
                                    result?.let { selectedMedia = listOf(it) }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(
                            text = "Pick Multiple Files",
                            icon = Icons.Default.FolderOpen,
                            isOutlined = true,
                            borderColor = Color(0xFF00BFA5),
                            textColor = Color(0xFF00BFA5),
                            onClick = {
                                pickerState.pickFiles(maxSelection = 5) { results ->
                                    selectedMedia = results
                                }
                            }
                        )
                    }
                }
                
                item {
                    MediaSectionCard(
                        title = "Mixed Media",
                        subtitle = "Images and videos together",
                        icon = Icons.Outlined.PermMedia,
                        iconColor = Color(0xFFFF6D00)
                    ) {
                        ActionButton(
                            text = "Pick Any Media",
                            icon = Icons.Outlined.PermMedia,
                            backgroundColor = Color(0xFFFF6D00),
                            onClick = {
                                // For mixed: usually just fallback to pickImage or verify if backend supports mixed
                                // Assuming pickMedia exists or fallback
                                pickerState.pickMedia { result ->
                                    result?.let { selectedMedia = listOf(it) }
                                }
                            }
                        )
                         Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(
                            text = "Pick Multiple Media",
                            icon = Icons.Default.Collections,
                            isOutlined = true,
                            borderColor = Color(0xFFFF6D00),
                            textColor = Color(0xFFFF6D00),
                            onClick = {
                                pickerState.pickMultipleMedia(maxSelection = 10) { results ->
                                    selectedMedia = results
                                }
                            }
                        )
                    }
                }

                if (selectedMedia.isNotEmpty()) {
                    item {
                        Text(
                            text = "Selected Media",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(selectedMedia) { media ->
                                SelectedMediaItem(media)
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }


@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C4DFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Easy Media Picker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF263238) // Dark Slate
        )
        Text(
            text = "Compose Multiplatform Demo",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF7C4DFF) // Purple Accent
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick images, videos & files across\nAndroid, iOS, and Desktop",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun MediaSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    textColor: Color = Color.White,
    isOutlined: Boolean = false,
    onClick: () -> Unit
) {
    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = textColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SelectedMediaItem(media: MediaResult) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (media.type) {
                MediaType.IMAGE -> {
                    AsyncImage(
                        model = media.uri, // Used Uri directly
                        contentDescription = media.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                MediaType.VIDEO -> {
                    // Placeholder for video thumbnail since we don't have a thumbnail generator here
                    // Ideally we'd use coil-video or a custom loader
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.InsertDriveFile,
                            contentDescription = "File",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
