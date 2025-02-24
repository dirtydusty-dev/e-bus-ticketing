package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.ViewModel
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.data.repository.BusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BusViewModel @Inject constructor(private val busRepository: BusRepository) : ViewModel() {

    /*fun getBusById(busId: String): Flow<BusEntity?> = busRepository.getBusById(busId)*/

}
