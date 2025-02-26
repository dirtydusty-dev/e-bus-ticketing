package com.sinarowa.e_bus_ticket.reports

// How many people are on board a trip
data class TripPeopleOnBoard(
    val tripId: Long,
    val count: Int
)

// How many people have departed a trip
data class TripPeopleDeparted(
    val tripId: Long,
    val count: Int
)

// How many free seats remain on a trip
data class TripFreeSeats(
    val tripId: Long,
    val availableSeats: Int
)
