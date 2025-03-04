package com.sinarowa.e_bus_ticket.service

import com.sinarowa.e_bus_ticket.data.dto.CreateTripRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("createTrip")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<Void>
}
