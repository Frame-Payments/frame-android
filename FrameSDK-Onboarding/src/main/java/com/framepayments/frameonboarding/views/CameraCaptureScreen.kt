package com.framepayments.frameonboarding.views

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.framepayments.frameonboarding.classes.PhotoType
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CameraCaptureScreen(
    photoType: PhotoType,
    onClose: () -> Unit,
    onPhotoCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scope = rememberCoroutineScope()

    val title = when (photoType) {
        PhotoType.FRONT -> "Front Photo"
        PhotoType.BACK -> "Back Photo"
        PhotoType.SELFIE -> "Take a Selfie"
    }

    val instruction = when (photoType) {
        PhotoType.FRONT -> "Place the front of your ID within the frame. Make sure all details are clear and readable."
        PhotoType.BACK -> "Place the back of your ID within the frame. Make sure all details are clear and readable."
        PhotoType.SELFIE -> "Position your face within the frame and look straight at the camera."
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showPermissionRationale = true
        }
    }

    // Check permission on first launch
    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.CAMERA
        } else {
            Manifest.permission.CAMERA
        }
        
        val hasPermissionAlready = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (hasPermissionAlready) {
            hasPermission = true
        } else {
            permissionLauncher.launch(permission)
        }
    }

    // Capture photo function
    val capturePhoto: () -> Unit = {
        val capture = imageCapture ?: return
        scope.launch {
            try {
                // Create file for photo
                val photoFile = createImageFile(context, photoType)
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "com.framepayments.frameonboarding.fileprovider",
                    photoFile
                )

                // Create output file options
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                // Capture photo
                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onPhotoCaptured(photoUri)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                            // Handle error - could show a snackbar or error message
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Text("X")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                showPermissionRationale -> {
                    // Permission denied - show rationale
                    PermissionDeniedView(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onClose = onClose
                    )
                }
                hasPermission -> {
                    // Camera preview
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        cameraProviderFuture = cameraProviderFuture,
                        lifecycleOwner = lifecycleOwner,
                        photoType = photoType,
                        onImageCaptureReady = { capture ->
                            imageCapture = capture
                        }
                    )

                    // Instruction text overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(24.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Overlay frame
                    when (photoType) {
                    PhotoType.FRONT, PhotoType.BACK -> {
                        // Rectangle frame for ID
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val frameWidth = size.width * 0.85f
                            val frameHeight = size.height * 0.4f
                            val left = (size.width - frameWidth) / 2
                            val top = (size.height - frameHeight) / 2

                            // Draw semi-transparent overlay with cutout for frame
                            val overlayPath = androidx.compose.ui.graphics.Path().apply {
                                // Outer rectangle (full screen)
                                addRect(android.graphics.RectF(0f, 0f, size.width, size.height))
                                // Inner rectangle (frame area) - subtract this
                                addRect(android.graphics.RectF(left, top, left + frameWidth, top + frameHeight))
                                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                            }
                            drawPath(
                                path = overlayPath,
                                color = Color.Black.copy(alpha = 0.5f)
                            )

                            // Draw white border for frame
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(left, top),
                                size = Size(frameWidth, frameHeight),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                            )
                        }
                    }
                        PhotoType.SELFIE -> {
                            // Circular frame for selfie
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val radius = size.minDimension * 0.35f
                                val centerX = size.width / 2
                                val centerY = size.height / 2

                                // Draw semi-transparent overlay with circular cutout
                                val overlayPath = androidx.compose.ui.graphics.Path().apply {
                                    // Outer rectangle (full screen)
                                    addRect(android.graphics.RectF(0f, 0f, size.width, size.height))
                                    // Inner circle (frame area) - subtract this
                                    addCircle(centerX, centerY, radius)
                                    fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                                }
                                drawPath(
                                    path = overlayPath,
                                    color = Color.Black.copy(alpha = 0.5f)
                                )

                                // Draw circular frame border
                                drawCircle(
                                    color = Color.White,
                                    radius = radius,
                                    center = Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                                )
                            }
                        }
                    }

                    // Capture button
                    FloatingActionButton(
                        onClick = capturePhoto,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        containerColor = FramePrimaryColor,
                        contentColor = FrameOnPrimaryColor
                    ) {
                        // Camera icon - using text as placeholder, replace with actual icon
                        Text("📷", style = MaterialTheme.typography.titleLarge)
                    }
                }
                else -> {
                    // Loading or checking permission
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedView(
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "We need camera permission to take photos for identity verification.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onClose) {
            Text("Cancel")
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraProviderFuture: androidx.concurrent.futures.ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    photoType: PhotoType,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Build preview
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    // Build image capture
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    
                    imageCapture = capture
                    onImageCaptureReady(capture)

                    // Select camera (front for selfie, back for ID photos)
                    val cameraSelector = when (photoType) {
                        PhotoType.SELFIE -> CameraSelector.DEFAULT_FRONT_CAMERA
                        PhotoType.FRONT, PhotoType.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to lifecycle
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        capture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)

            previewView
        },
        modifier = modifier,
        update = { view ->
            // Update if needed
        }
    )
}

private fun createImageFile(context: Context, photoType: PhotoType): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = when (photoType) {
        PhotoType.FRONT -> "ID_FRONT_$timeStamp"
        PhotoType.BACK -> "ID_BACK_$timeStamp"
        PhotoType.SELFIE -> "SELFIE_$timeStamp"
    }
    
    val storageDir = File(context.cacheDir, "onboarding_photos")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    
    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
}
