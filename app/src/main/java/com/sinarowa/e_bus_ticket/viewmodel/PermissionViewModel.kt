package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PermissionViewModel : ViewModel() {
    private val _isPermissionGranted = MutableLiveData(false)
    val isPermissionGranted: LiveData<Boolean> get() = _isPermissionGranted

    fun updatePermissionStatus(granted: Boolean) {
        _isPermissionGranted.value = granted
    }
}
