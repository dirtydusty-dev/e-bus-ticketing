package com.sinarowa.e_bus_ticket.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class BluetoothHelper(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // ✅ API 23+ (Android 6+)
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            bluetoothManager?.adapter
        } else {
            @Suppress("DEPRECATION") // ✅ API < 23 fallback
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    /**
     * ✅ Request Bluetooth permissions at runtime (Android 12+)
     */
    fun requestBluetoothPermissions(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // ✅ Android 12+
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )

            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                val allGranted = results.values.all { it }
                if (!allGranted) {
                    showPermissionDeniedMessage(activity)
                }
            }.launch(permissions)
        }
    }

    /**
     * ✅ Check if Bluetooth permissions are granted
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // ✅ No need for runtime permissions in API < 31
        }
    }

    /**
     * ✅ Checks if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * ✅ Show permission denied message
     */
    private fun showPermissionDeniedMessage(activity: AppCompatActivity) {
        activity.runOnUiThread {
            Toast.makeText(activity, "Bluetooth permissions are required!", Toast.LENGTH_LONG).show()
        }
    }
}
