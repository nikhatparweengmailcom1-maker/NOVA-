package com.nova.assistant.presentation.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nova.assistant.presentation.ui.theme.*
import timber.log.Timber
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboard = LocalClipboardManager.current

    var hasPermission by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) hasPermission = true
        else permLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission && scannedResult == null && isScanning) {
            // Camera preview
            val barcodeScanner = remember { BarcodeScanning.getClient() }
            val executor = remember { Executors.newSingleThreadExecutor() }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(executor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null && isScanning) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        barcodeScanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                barcodes.firstOrNull()?.let { barcode ->
                                                    val value = barcode.rawValue ?: return@let
                                                    isScanning = false
                                                    scannedResult = value
                                                    Timber.d("QR scanned: $value")
                                                }
                                            }
                                            .addOnCompleteListener { imageProxy.close() }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "QRScanner: camera bind failed")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scan overlay
            Box(modifier = Modifier.fillMaxSize()) {
                // Dark overlay with cutout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(0.5f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(0.5f))
                )

                // Scan frame
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(NovaCyan, NovaPurple)),
                            RoundedCornerShape(12.dp)
                        )
                )

                // Scan line animation
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val scanY by infiniteTransition.animateFloat(
                    0f, 240f,
                    animationSpec = infiniteRepeatable(
                        androidx.compose.animation.core.tween(1500),
                        androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "scanLine"
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (scanY - 120).dp)
                        .width(240.dp)
                        .height(2.dp)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, NovaCyan, Color.Transparent)))
                )

                Text(
                    "Point at a QR code or barcode",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 180.dp)
                )
            }
        }

        // No permission
        if (!hasPermission) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = TextDisabled, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Camera permission required", color = TextTertiary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan.copy(0.2f))) {
                        Text("Grant Permission", color = NovaCyan)
                    }
                }
            }
        }

        // Result card
        scannedResult?.let { result ->
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, NovaCyan.copy(0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.QrCode, null, tint = NovaCyan, modifier = Modifier.size(40.dp))
                        Text("Scanned", style = MaterialTheme.typography.titleMedium, color = NovaCyan)
                        Text(result, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(result))
                                },
                                border = BorderStroke(1.dp, NovaCyan),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Copy", color = NovaCyan) }
                            Button(
                                onClick = { scannedResult = null; isScanning = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan.copy(0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Scan Again", color = NovaCyan) }
                        }
                    }
                }
            }
        }

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
        }
    }
}
