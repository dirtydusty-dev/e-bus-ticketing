package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.ViewModel
import com.sinarowa.e_bus_ticket.data.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(private val reportsRepository: ReportsRepository) : ViewModel() {

    fun getTripSalesSummary(tripId: Long) = reportsRepository.getTripSalesSummary(tripId)
}
