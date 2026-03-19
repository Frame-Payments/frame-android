package com.framepayments.frameonboarding.views

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.framepayments.frameonboarding.classes.PhotoType
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var captureError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(captureError) {
        val error = captureError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        captureError = null
    }

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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
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

    val capturePhoto: () -> Unit = {
        imageCapture?.let { capture ->
            scope.launch {
                try {
                    val photoFile = createImageFile(context, photoType)
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.frameonboarding.fileprovider",
                        photoFile
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onPhotoCaptured(photoUri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                captureError = "Failed to capture photo. Please try again."
                            }
                        }
                    )
                } catch (e: Exception) {
                    captureError = "Failed to capture photo. Please try again."
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close"
                        )
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
                    PermissionDeniedView(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onClose = onClose
                    )
                }
                hasPermission -> {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        cameraProviderFuture = cameraProviderFuture,
                        lifecycleOwner = lifecycleOwner,
                        photoType = photoType,
                        onImageCaptureReady = { capture ->
                            imageCapture = capture
                        },
                        onError = { message ->
                            captureError = message
                        }
                    )

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

                    when (photoType) {
                        PhotoType.FRONT, PhotoType.BACK -> {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val frameWidth = size.width * 0.85f
                                val frameHeight = size.height * 0.4f
                                val left = (size.width - frameWidth) / 2
                                val top = (size.height - frameHeight) / 2
                                val overlayPath = Path().apply {
                                    addRect(Rect(0f, 0f, size.width, size.height))
                                    addRect(Rect(left, top, left + frameWidth, top + frameHeight))
                                    fillType = PathFillType.EvenOdd
                                }
                                drawPath(path = overlayPath, color = Color.Black.copy(alpha = 0.5f))
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(left, top),
                                    size = Size(frameWidth, frameHeight),
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                        }
                        PhotoType.SELFIE -> {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension * 0.35f
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                val overlayPath = Path().apply {
                                    addRect(Rect(0f, 0f, size.width, size.height))
                                    addOval(Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius))
                                    fillType = PathFillType.EvenOdd
                                }
                                drawPath(path = overlayPath, color = Color.Black.copy(alpha = 0.5f))
                                drawCircle(
                                    color = Color.White,
                                    radius = radius,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = capturePhoto,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        containerColor = FramePrimaryColor,
                        contentColor = FrameOnPrimaryColor
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take photo"
                        )
                    }
                }
                else -> {
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
            text = "We need access to your camera to take photos of your identity documents.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
        TextButton(onClick = onClose) {
            Text("Cancel")
        }
    }
}

private fun createImageFile(context: Context, photoType: PhotoType): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "FRAME_${photoType.name}_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    photoType: PhotoType,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onError: (String) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                onImageCaptureReady(imageCapture)
                val cameraSelector = if (photoType == PhotoType.SELFIE) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    onError("Failed to start camera. Please try again.")
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}
