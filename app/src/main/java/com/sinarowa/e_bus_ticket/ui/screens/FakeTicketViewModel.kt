package com.sinarowa.e_bus_ticket.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTicketViewModel : ViewModel() {
    private val _ticketCount = MutableStateFlow(5)  // Fake 5 sold tickets
    val ticketCount = _ticketCount.asStateFlow()
}
