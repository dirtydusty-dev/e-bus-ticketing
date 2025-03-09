package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinarowa.e_bus_ticket.viewmodel.PermissionViewModel

@Composable
fun RequestPermissionsScreen(
    permissionViewModel: PermissionViewModel = viewModel(),
    requestPermissionLauncher: () -> Unit
) {
    val isPermissionGranted by permissionViewModel.isPermissionGranted.observeAsState(false)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Location Permission Required")
        Spacer(modifier = Modifier.height(16.dp))

        if (!isPermissionGranted) {
            Button(onClick = requestPermissionLauncher) {
                Text("Grant Permission")
            }
        } else {
            Text("Permission Granted! 🎉")
        }
    }
}
