package com.sinarowa.e_bus_ticket.domain.models

import androidx.room.Embedded
import androidx.room.Relation
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket

data class PriceWithTicket(
    @Embedded var ticket: Ticket, // The ticket details

    @Relation(
        parentColumn = "ticket_priceId", // Should be the column from Ticket
        entityColumn = "priceId" // Should be the column from Price
    )
    var price: Price // The price details associated with the ticket
) {
    // Room requires an empty constructor for data classes
    constructor() : this(
        ticket = Ticket(),
        price = Price()
    )
}
