package com.sinarowa.e_bus_ticket.data.dto

import com.google.gson.annotations.SerializedName

data class CreateTripRequest(
    @SerializedName("creationTime")
    val creationTime: String, // Example: "2025-03-03T12:00:00Z"

    @SerializedName("endTime")
    val endTime: String?, // Example: "2025-03-03T14:00:00Z"

    @SerializedName("registrationNumber")
    val registrationNumber: String, // Example: "Govasberg-101"

    @SerializedName("routeName")
    val routeName: String, // Example: "HRE-CWR"

    @SerializedName("tripIdentifier")
    val tripIdentifier: String, // Example: "TRIP_HRE-CWR_12345_20250303_120000"

    @SerializedName("tripStatus")
    val tripStatus: String // Example: "START"
)

