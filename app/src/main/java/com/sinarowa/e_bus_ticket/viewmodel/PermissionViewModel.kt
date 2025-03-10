package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class PermissionViewModel @Inject constructor() : ViewModel() {
    private val _permissionsGranted = MutableLiveData(false)
    val permissionsGranted: LiveData<Boolean> get() = _permissionsGranted

    fun arePermissionsGranted() = _permissionsGranted.value ?: false

    fun updatePermissionsGranted(granted: Boolean) {
        _permissionsGranted.value = granted
    }
}