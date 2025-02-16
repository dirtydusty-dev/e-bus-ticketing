package com.sinarowa.e_bus_ticket.ui.screens


class FakeLoginViewModel {
    fun loginUser(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (userId == "validUser") {
            onSuccess()
        } else {
            onError("Invalid User ID")
        }
    }
}


