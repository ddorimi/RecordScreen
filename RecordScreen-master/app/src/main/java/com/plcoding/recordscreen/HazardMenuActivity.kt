package com.plcoding.recordscreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plcoding.recordscreen.ScreenRecordService.Companion.KEY_RECORDING_CONFIG
import com.plcoding.recordscreen.ScreenRecordService.Companion.START_RECORDING
import com.plcoding.recordscreen.ScreenRecordService.Companion.STOP_RECORDING
import com.plcoding.recordscreen.ui.theme.CoralRed
import com.plcoding.recordscreen.ui.theme.MintGreen

class HazardMenuActivity : ComponentActivity() {
    private val mediaProjectionManager by lazy { getSystemService<MediaProjectionManager>()!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HazardMenuScreen(
                onBack = { finish() },
                mediaProjectionManager = mediaProjectionManager
            )
        }
    }
}

@Composable
fun HazardMenuScreen(onBack: () -> Unit, mediaProjectionManager: MediaProjectionManager) {
    val context = LocalContext.current
    val isServiceRunning by ScreenRecordService.isServiceRunning.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else mutableStateOf(true)
    }

    val screenRecordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data ?: return@rememberLauncherForActivityResult
        val config = ScreenRecordConfig(resultCode = result.resultCode, data = intent)
        val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
            action = START_RECORDING
            putExtra(KEY_RECORDING_CONFIG, config)
        }
        context.startForegroundService(serviceIntent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (hasNotificationPermission && !isServiceRunning) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            } else {
                screenRecordLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D4059))
            .padding(20.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 Back + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Types of Hazard",
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Hazard Grid
        val hazards = listOf(
            HazardItem(R.drawable.pedestrian_logo, "Pedestrians"),
            HazardItem(R.drawable.potholeshumps_logo, "Potholes / Humps"),
            HazardItem(R.drawable.animals_logo, "Animals"),
            HazardItem(R.drawable.roadworks_logo, "Road Works")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(hazards) { item -> HazardButton(item) }
        }

        Spacer(modifier = Modifier.height(50.dp))

        // 🔹 Detect Now Button (Start/Stop Screen Recording)
        Button(
            onClick = {
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    if (isServiceRunning) {
                        Intent(context, ScreenRecordService::class.java).also {
                            it.action = STOP_RECORDING
                            context.startForegroundService(it)
                        }
                    } else {
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } else {
                            screenRecordLauncher.launch(
                                mediaProjectionManager.createScreenCaptureIntent()
                            )
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning) CoralRed else Color(0xFFFD7014),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .height(55.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = if (isServiceRunning) "Stop Recording" else "Detect Now",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 🔹 About Us
        Text(
            text = "About Us",
            fontSize = 14.sp,
            color = Color.White,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .clickable {
                    val intent = Intent(context, AboutUsActivity::class.java)
                    context.startActivity(intent)
                }
        )
    }
}

// 🔹 Model for Hazard Item
data class HazardItem(val imageRes: Int, val label: String)

// 🔹 Reusable Hazard Button
@Composable
fun HazardButton(item: HazardItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFD9D9D9), shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.label,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = item.label, color = Color.White, fontSize = 14.sp)
    }
}
