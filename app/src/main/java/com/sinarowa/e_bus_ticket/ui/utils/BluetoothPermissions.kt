package com.sinarowa.e_bus_ticket.ui.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestBluetoothPermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                onPermissionGranted()
            }
        } else {
            onPermissionGranted() // ✅ No permission needed for Android 11 and below
        }
    }
}
