package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.R

@Composable
fun LoginScreen(
    loginFunction: (String, () -> Unit, (String) -> Unit) -> Unit  // ✅ Accepts function as parameter
) {
    val userId = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),  // ✅ Load the logo
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp).padding(bottom = 16.dp)  // Adjust size & spacing
        )

        OutlinedTextField(
            value = userId.value,
            onValueChange = { userId.value = it },
            label = { Text("Enter User ID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = Color.Gray,
                backgroundColor = MaterialTheme.colors.surface
            ),
            modifier = Modifier.fillMaxWidth(0.9f)  // ✅ Slightly smaller width for aesthetics
        )

        OutlinedTextField(
            value = userId.value,
            onValueChange = { userId.value = it },
            label = { Text("Enter Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = Color.Gray,
                backgroundColor = MaterialTheme.colors.surface
            ),
            modifier = Modifier.fillMaxWidth(0.9f)  // ✅ Slightly smaller width for aesthetics
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (userId.value.isNotEmpty()) {
                loginFunction(
                    userId.value,  // ✅ Pass parameters positionally
                    { /* Success: Navigate to trips */ },
                    { errorMessage.value = it }  // ✅ Pass error message callback
                )
            } else {
                errorMessage.value = "User ID cannot be empty"
            }
        },modifier = Modifier.fillMaxWidth(0.9f),  // ✅ Modern button width
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF1E3A8A),  // ✅ Dark Blue
                contentColor = Color.White)  // ✅ White text for contrast
        ) {
            Text("Login")
        }

        errorMessage.value?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    val fakeLoginFunction: (String, () -> Unit, (String) -> Unit) -> Unit = { userId, successCallback, errorCallback ->
        if (userId == "validUser") {
            successCallback()
        } else {
            errorCallback("Invalid User ID")
        }
    }

    LoginScreen(loginFunction = fakeLoginFunction)
}




