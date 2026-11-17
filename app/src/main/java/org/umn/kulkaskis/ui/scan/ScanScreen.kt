package org.umn.kulkaskis.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import org.umn.kulkaskis.data.FoodInfo
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.data.lookupFood
import org.umn.kulkaskis.ui.theme.Border
import org.umn.kulkaskis.ui.theme.Primary
import org.umn.kulkaskis.ui.theme.PrimaryContainer
import java.time.LocalDate

private val ButtonShape = RoundedCornerShape(12.dp)

private data class Candidate(
    val label: String,
    val confidence: Float,
    val info: FoodInfo
)

@Composable
fun ScanScreen(
    onAddItem: (FridgeItem) -> Unit = {},
    onManualAdd: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val labeler = remember {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder().setConfidenceThreshold(0.5f).build()
        )
    }
    DisposableEffect(Unit) { onDispose { labeler.close() } }

    var scanning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Candidate>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var added by remember { mutableStateOf(setOf<String>()) }

    DisposableEffect(hasPermission, lifecycleOwner) {
        var provider: ProcessCameraProvider? = null
        if (hasPermission) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    provider?.unbindAll()
                    provider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("ScanScreen", "Camera bind failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose { provider?.unbindAll() }
    }

    fun scan() {
        scanning = true
        errorMessage = null
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val rotation = image.imageInfo.rotationDegrees
                    val bitmap = try {
                        image.toBitmap()
                    } catch (e: Exception) {
                        null
                    }
                    image.close()
                    if (bitmap == null) {
                        scanning = false
                        errorMessage = "Failed to read the photo. Please try again."
                        return
                    }
                    labeler.process(InputImage.fromBitmap(bitmap, rotation))
                        .addOnSuccessListener { labels ->
                            results = labels
                                .mapNotNull { l ->
                                    lookupFood(l.text)?.let { Candidate(l.text, l.confidence, it) }
                                }
                                .sortedByDescending { it.confidence }
                                .take(6)
                            scanning = false
                        }
                        .addOnFailureListener {
                            scanning = false
                            errorMessage = "Scan failed. Please try again."
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    scanning = false
                    errorMessage = "Camera error. Please try again."
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Scan Fridge", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Point the camera at your food, then tap Scan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Viewfinder: black frame with a thin green border (as in Figma)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
                .border(1.dp, Primary.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (hasPermission) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            } else {
                Text("Camera permission needed", color = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        val current = results
        when {
            !hasPermission -> {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = ButtonShape
                ) { Text("Allow camera", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(12.dp))
                ManualButton(onManualAdd, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                // Info note (as in Figma)
                Surface(
                    color = PrimaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Camera access is needed to scan. You can still add items manually.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }

            scanning -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            current == null -> {
                // Orange primary action (tertiary in the theme)
                Button(
                    onClick = { scan() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = ButtonShape
                ) { Text("Scan", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(12.dp))
                ManualButton(onManualAdd, Modifier.fillMaxWidth())
            }

            current.isEmpty() -> {
                Text(
                    "No food recognized. Try getting closer, or use better lighting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                ScanActions(
                    onScanAgain = { results = null; added = emptySet() },
                    onManualAdd = onManualAdd
                )
            }

            else -> {
                Text(
                    "Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    current.forEach { c ->
                        val isAdded = c.label in added
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Border)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        c.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${(c.confidence * 100).toInt()}% sure · ${c.info.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Green pill button (as in Figma)
                                Button(
                                    enabled = !isAdded,
                                    onClick = {
                                        onAddItem(
                                            FridgeItem(
                                                name = c.label,
                                                quantity = 1,
                                                expiry = LocalDate.now().plusDays(c.info.days.toLong()),
                                                category = c.info.category
                                            )
                                        )
                                        added = added + c.label
                                    },
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Primary,
                                        contentColor = Color.White,
                                        disabledContainerColor = PrimaryContainer,
                                        disabledContentColor = Primary
                                    )
                                ) {
                                    Text(
                                        if (isAdded) "Added ✓" else "Add",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                ScanActions(
                    onScanAgain = { results = null; added = emptySet() },
                    onManualAdd = onManualAdd
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ManualButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = ButtonShape,
        border = BorderStroke(1.dp, Primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
    ) { Text("Add manually", fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun ScanActions(onScanAgain: () -> Unit, onManualAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onScanAgain,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = ButtonShape,
            border = BorderStroke(1.dp, Primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
        ) { Text("Scan again", fontWeight = FontWeight.SemiBold) }
        OutlinedButton(
            onClick = onManualAdd,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = ButtonShape,
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) { Text("Add manually", fontWeight = FontWeight.SemiBold) }
    }
}