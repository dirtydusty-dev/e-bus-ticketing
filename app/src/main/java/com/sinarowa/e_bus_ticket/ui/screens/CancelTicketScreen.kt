/*
package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CancelTicketScreen(
    navController: NavController,
    ticketViewModel: TicketViewModel = viewModel(),
    activeTripId: String // Ensure only tickets from the active trip are considered
) {
    var ticketId by remember { mutableStateOf("") }
    var cancelReason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false)}
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cancel Ticket", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ticketId,
            onValueChange = { ticketId = it },
            label = { Text("Ticket ID") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1565C0),
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cancelReason,
            onValueChange = { cancelReason = it },
            label = { Text("Reason for Cancellation") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1565C0),
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (ticketId.isNotBlank() && cancelReason.isNotBlank()) {
                    scope.launch {
                        val result = ticketViewModel.cancelTicket(ticketId, cancelReason, activeTripId)
                        if (result) {
                            showConfirmation = true
                            delay(1500) // ✅ Show confirmation for 1.5 seconds
                            navController.popBackStack()
                        } else {
                            errorMessage = "Ticket not found or does not belong to the active trip."
                        }
                    }
                } else {
                    errorMessage = "Please enter a valid Ticket ID and Reason."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel Ticket", color = Color.Black, fontSize = 18.sp)
        }

    }

   if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Success", color = Color(0xFF1565C0), fontSize = 20.sp) },
            text = { Text("Ticket successfully cancelled.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1565C0))
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}
*/
