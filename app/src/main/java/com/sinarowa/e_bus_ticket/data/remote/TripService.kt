package com.sinarowa.e_bus_ticket.data.remote
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

interface TripService {

    // ✅ Fetch assigned trips from REST API
    @GET("trips/{userId}")  // Adjust the URL if needed
    suspend fun getAssignedTrips(@Path("userId") userId: String): Response<List<TripResponse>>

    // ✅ Upload tickets (send JSON to existing API)
    @POST("upload/tickets")
    suspend fun uploadTickets(@Body request: TicketUploadRequest): Response<Unit>  // ✅ Expecting a single object

    // ✅ Upload expenses (send JSON to existing API)
    @POST("upload/expenses")  // Adjust the API URL
    suspend fun uploadExpenses(@Body request: ExpenseUploadRequest): Response<Unit>
}
