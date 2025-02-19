/*
package com.sinarowa.e_bus_ticket.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(*/
/**//*

    private val syncRepository: SyncRepository
) : ViewModel() {

    fun syncData() {*/
/**//*

        viewModelScope.launch {
            syncRepository.syncTickets()
            syncRepository.syncExpenses()
        }
    }
}
*/
