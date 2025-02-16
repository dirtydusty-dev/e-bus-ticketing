package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun loginUser(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Store user session
                prefs.edit()
                    .putString("user_id", userId)
                    .putLong("login_time", System.currentTimeMillis()) // Store login timestamp
                    .apply()

                // ✅ No API calls, just navigate to home
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Login failed")
            }
        }
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun isSessionValid(): Boolean {
        val loginTime = prefs.getLong("login_time", 0)
        val currentTime = System.currentTimeMillis()
        val sessionDuration = 24 * 60 * 60 * 1000  // 24 hours in milliseconds
        return getUserId() != null && (currentTime - loginTime) < sessionDuration
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
