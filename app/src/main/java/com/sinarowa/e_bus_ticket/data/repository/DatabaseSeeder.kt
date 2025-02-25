package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.LocationDao
import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.data.local.entities.LocationEntity
import com.sinarowa.e_bus_ticket.data.local.entities.PriceEntity
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    suspend fun seedData(routeDao: RouteDao, priceDao: PriceDao, locationDao: LocationDao, busDao: BusDao) {
        withContext(Dispatchers.IO) {
            // ✅ Insert Routes
            val routes = listOf(
                RouteEntity("1", "HRE-CWR", "Harare CBD", "Waterfalls", "Harare CBD, Causeway, Milton Park, Belvedere, Southerton, Westgate, Waterfalls"),
                RouteEntity("2", "HRE-NNW", "The Avenues", "Belgravia", "The Avenues, Alexandra Park, Arcadia, Arlington, Greendale, Borrowdale, Mount Pleasant, Newlands, Avondale, Highlands, Hatcliffe, Belgravia"), // Route 3: East Route
                RouteEntity("3", "HRE-ERT", "Harare CBD", "Epworth", "Harare CBD, Mbare, Highfield, Chitungwiza, Epworth"),
                RouteEntity("4", "CWR-HRE", "Waterfalls", "Harare CBD", "Waterfalls, Westgate, Southerton, Belvedere, Milton Park, Causeway, Harare CBD") // Return Route
            )
            routeDao.insertRoutes(routes)

            // ✅ Insert Prices (All permutations)
            val prices = listOf(
                // Route 1 (Base rate: 1.0 per stop difference)
// Stops: Harare CBD, Causeway, Milton Park, Belvedere, Southerton, Westgate, Waterfalls
                PriceEntity(fromCity = "Harare CBD", toCity = "Causeway", adultPrice = 1.0),
                PriceEntity(fromCity = "Harare CBD", toCity = "Milton Park", adultPrice = 2.0),
                PriceEntity(fromCity = "Harare CBD", toCity = "Belvedere", adultPrice = 3.0),
                PriceEntity(fromCity = "Harare CBD", toCity = "Southerton", adultPrice = 4.0),
                PriceEntity(fromCity = "Harare CBD", toCity = "Westgate", adultPrice = 5.0),
                PriceEntity(fromCity = "Harare CBD", toCity = "Waterfalls", adultPrice = 6.0),
                PriceEntity(fromCity = "Causeway", toCity = "Milton Park", adultPrice = 1.0),
                PriceEntity(fromCity = "Causeway", toCity = "Belvedere", adultPrice = 2.0),
                PriceEntity(fromCity = "Causeway", toCity = "Southerton", adultPrice = 3.0),
                PriceEntity(fromCity = "Causeway", toCity = "Westgate", adultPrice = 4.0),
                PriceEntity(fromCity = "Causeway", toCity = "Waterfalls", adultPrice = 5.0),

                PriceEntity(fromCity = "Milton Park", toCity = "Belvedere", adultPrice = 1.0),
                PriceEntity(fromCity = "Milton Park", toCity = "Southerton", adultPrice = 2.0),
                PriceEntity(fromCity = "Milton Park", toCity = "Westgate", adultPrice = 3.0),
                PriceEntity(fromCity = "Milton Park", toCity = "Waterfalls", adultPrice = 4.0),

                PriceEntity(fromCity = "Belvedere", toCity = "Southerton", adultPrice = 1.0),
                PriceEntity(fromCity = "Belvedere", toCity = "Westgate", adultPrice = 2.0),
                PriceEntity(fromCity = "Belvedere", toCity = "Waterfalls", adultPrice = 3.0),

                PriceEntity(fromCity = "Southerton", toCity = "Westgate", adultPrice = 1.0),
                PriceEntity(fromCity = "Southerton", toCity = "Waterfalls", adultPrice = 2.0),

                PriceEntity(fromCity = "Westgate", toCity = "Waterfalls", adultPrice = 1.0),


                PriceEntity(fromCity = "Waterfalls", toCity = "Westgate", adultPrice = 1.0),
                PriceEntity(fromCity = "Waterfalls", toCity = "Southerton", adultPrice = 2.0),
                PriceEntity(fromCity = "Waterfalls", toCity = "Belvedere", adultPrice = 3.0),
                PriceEntity(fromCity = "Waterfalls", toCity = "Milton Park", adultPrice = 4.0),
                PriceEntity(fromCity = "Waterfalls", toCity = "Causeway", adultPrice = 5.0),
                PriceEntity(fromCity = "Waterfalls", toCity = "Harare CBD", adultPrice = 6.0),
                PriceEntity(fromCity = "Westgate", toCity = "Southerton", adultPrice = 1.0),
                PriceEntity(fromCity = "Westgate", toCity = "Belvedere", adultPrice = 2.0),
                PriceEntity(fromCity = "Westgate", toCity = "Milton Park", adultPrice = 3.0),
                PriceEntity(fromCity = "Westgate", toCity = "Causeway", adultPrice = 4.0),
                PriceEntity(fromCity = "Westgate", toCity = "Harare CBD", adultPrice = 5.0),

                PriceEntity(fromCity = "Southerton", toCity = "Belvedere", adultPrice = 1.0),
                PriceEntity(fromCity = "Southerton", toCity = "Milton Park", adultPrice = 2.0),
                PriceEntity(fromCity = "Southerton", toCity = "Causeway", adultPrice = 3.0),
                PriceEntity(fromCity = "Southerton", toCity = "Harare CBD", adultPrice = 4.0),

                PriceEntity(fromCity = "Belvedere", toCity = "Milton Park", adultPrice = 1.0),
                PriceEntity(fromCity = "Belvedere", toCity = "Causeway", adultPrice = 2.0),
                PriceEntity(fromCity = "Belvedere", toCity = "Harare CBD", adultPrice = 3.0),

                PriceEntity(fromCity = "Milton Park", toCity = "Causeway", adultPrice = 1.0),
                PriceEntity(fromCity = "Milton Park", toCity = "Harare CBD", adultPrice = 2.0),

                PriceEntity(fromCity = "Causeway", toCity = "Harare CBD", adultPrice = 1.0),


// Route 2 (Base rate: 1.0 per stop difference)
// Stops: The Avenues, Alexandra Park, Arcadia, Arlington, Greendale, Borrowdale, Mount Pleasant, Newlands, Avondale, Highlands, Hatcliffe, Belgravia
            PriceEntity(fromCity = "The Avenues", toCity = "Alexandra Park", adultPrice = 1.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Arcadia", adultPrice = 2.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Arlington", adultPrice = 3.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Greendale", adultPrice = 4.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Borrowdale", adultPrice = 5.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Mount Pleasant", adultPrice = 6.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Newlands", adultPrice = 7.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Avondale", adultPrice = 8.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Highlands", adultPrice = 9.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Hatcliffe", adultPrice = 10.0),
            PriceEntity(fromCity = "The Avenues", toCity = "Belgravia", adultPrice = 11.0),

            PriceEntity(fromCity = "Alexandra Park", toCity = "Arcadia", adultPrice = 1.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Arlington", adultPrice = 2.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Greendale", adultPrice = 3.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Borrowdale", adultPrice = 4.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Mount Pleasant", adultPrice = 5.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Newlands", adultPrice = 6.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Avondale", adultPrice = 7.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Highlands", adultPrice = 8.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Hatcliffe", adultPrice = 9.0),
            PriceEntity(fromCity = "Alexandra Park", toCity = "Belgravia", adultPrice = 10.0),

            PriceEntity(fromCity = "Arcadia", toCity = "Arlington", adultPrice = 1.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Greendale", adultPrice = 2.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Borrowdale", adultPrice = 3.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Mount Pleasant", adultPrice = 4.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Newlands", adultPrice = 5.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Avondale", adultPrice = 6.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Highlands", adultPrice = 7.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Hatcliffe", adultPrice = 8.0),
            PriceEntity(fromCity = "Arcadia", toCity = "Belgravia", adultPrice = 9.0),

            PriceEntity(fromCity = "Arlington", toCity = "Greendale", adultPrice = 1.0),
            PriceEntity(fromCity = "Arlington", toCity = "Borrowdale", adultPrice = 2.0),
            PriceEntity(fromCity = "Arlington", toCity = "Mount Pleasant", adultPrice = 3.0),
            PriceEntity(fromCity = "Arlington", toCity = "Newlands", adultPrice = 4.0),
            PriceEntity(fromCity = "Arlington", toCity = "Avondale", adultPrice = 5.0),
            PriceEntity(fromCity = "Arlington", toCity = "Highlands", adultPrice = 6.0),
            PriceEntity(fromCity = "Arlington", toCity = "Hatcliffe", adultPrice = 7.0),
            PriceEntity(fromCity = "Arlington", toCity = "Belgravia", adultPrice = 8.0),

            PriceEntity(fromCity = "Greendale", toCity = "Borrowdale", adultPrice = 1.0),
            PriceEntity(fromCity = "Greendale", toCity = "Mount Pleasant", adultPrice = 2.0),
            PriceEntity(fromCity = "Greendale", toCity = "Newlands", adultPrice = 3.0),
            PriceEntity(fromCity = "Greendale", toCity = "Avondale", adultPrice = 4.0),
            PriceEntity(fromCity = "Greendale", toCity = "Highlands", adultPrice = 5.0),
            PriceEntity(fromCity = "Greendale", toCity = "Hatcliffe", adultPrice = 6.0),
            PriceEntity(fromCity = "Greendale", toCity = "Belgravia", adultPrice = 7.0),

            PriceEntity(fromCity = "Borrowdale", toCity = "Mount Pleasant", adultPrice = 1.0),
            PriceEntity(fromCity = "Borrowdale", toCity = "Newlands", adultPrice = 2.0),
            PriceEntity(fromCity = "Borrowdale", toCity = "Avondale", adultPrice = 3.0),
            PriceEntity(fromCity = "Borrowdale", toCity = "Highlands", adultPrice = 4.0),
            PriceEntity(fromCity = "Borrowdale", toCity = "Hatcliffe", adultPrice = 5.0),
            PriceEntity(fromCity = "Borrowdale", toCity = "Belgravia", adultPrice = 6.0),

            PriceEntity(fromCity = "Mount Pleasant", toCity = "Newlands", adultPrice = 1.0),
            PriceEntity(fromCity = "Mount Pleasant", toCity = "Avondale", adultPrice = 2.0),
            PriceEntity(fromCity = "Mount Pleasant", toCity = "Highlands", adultPrice = 3.0),
            PriceEntity(fromCity = "Mount Pleasant", toCity = "Hatcliffe", adultPrice = 4.0),
            PriceEntity(fromCity = "Mount Pleasant", toCity = "Belgravia", adultPrice = 5.0),

            PriceEntity(fromCity = "Newlands", toCity = "Avondale", adultPrice = 1.0),
            PriceEntity(fromCity = "Newlands", toCity = "Highlands", adultPrice = 2.0),
            PriceEntity(fromCity = "Newlands", toCity = "Hatcliffe", adultPrice = 3.0),
            PriceEntity(fromCity = "Newlands", toCity = "Belgravia", adultPrice = 4.0),

            PriceEntity(fromCity = "Avondale", toCity = "Highlands", adultPrice = 1.0),
            PriceEntity(fromCity = "Avondale", toCity = "Hatcliffe", adultPrice = 2.0),
            PriceEntity(fromCity = "Avondale", toCity = "Belgravia", adultPrice = 3.0),

            PriceEntity(fromCity = "Highlands", toCity = "Hatcliffe", adultPrice = 1.0),
            PriceEntity(fromCity = "Highlands", toCity = "Belgravia", adultPrice = 2.0),
            PriceEntity(fromCity = "Hatcliffe", toCity = "Belgravia", adultPrice = 1.0),


// Route 3 (Base rate: 1.5 per stop difference)
// Stops: Harare CBD, Mbare, Highfield, Chitungwiza, Epworth
            PriceEntity(fromCity = "Harare CBD", toCity = "Mbare", adultPrice = 1.5),
            PriceEntity(fromCity = "Harare CBD", toCity = "Highfield", adultPrice = 3.0),
            PriceEntity(fromCity = "Harare CBD", toCity = "Chitungwiza", adultPrice = 4.5),
            PriceEntity(fromCity = "Harare CBD", toCity = "Epworth", adultPrice = 6.0),

            PriceEntity(fromCity = "Mbare", toCity = "Highfield", adultPrice = 1.5),
            PriceEntity(fromCity = "Mbare", toCity = "Chitungwiza", adultPrice = 3.0),
            PriceEntity(fromCity = "Mbare", toCity = "Epworth", adultPrice = 4.5),

            PriceEntity(fromCity = "Highfield", toCity = "Chitungwiza", adultPrice = 1.5),
            PriceEntity(fromCity = "Highfield", toCity = "Epworth", adultPrice = 3.0),

            PriceEntity(fromCity = "Chitungwiza", toCity = "Epworth", adultPrice = 1.5)

            )
            priceDao.insertPrices(prices)


            // ✅ Insert Locations
            val locations = listOf(
                LocationEntity("1", "Harare CBD", -17.831519192837742, 31.047380474799684),
                LocationEntity("2", "The Avenues", -17.819990386876434, 31.047064768309873),
                LocationEntity("3", "Belvedere", -17.828183786232724, 31.01984820636389),
                LocationEntity("4", "Mbare", -17.86638916448852, 31.03200284779129),
                LocationEntity("5", "Highfield", -17.88193588141904, 30.981522981920943),
                LocationEntity("6", "Borrowdale", -17.74971450251613, 31.093916900205176),
                LocationEntity("7", "Mount Pleasant", -17.767646942255972, 31.047725911567614),
                LocationEntity("8", "Newlands", -17.810020866183052, 31.083877294030597),
                LocationEntity("9", "Chitungwiza", -18.01782321540995, 31.073007821155777),
                LocationEntity("10", "Epworth", -17.896707124627824, 31.15719768809613),
                LocationEntity("11", "Alexandra Park", -17.79493863816772, 31.056141104405818),
                LocationEntity("12", "Arcadia", -17.84796288305909, 31.053165881483388),
                LocationEntity("13", "Arlington", -17.902423818694437, 31.07798331934576),
                LocationEntity("14", "Greendale", -17.81372074719181, 31.123617495913045),
                LocationEntity("15", "Milton Park", -17.814686511620252, 31.036046580243525),
                LocationEntity("16", "Causeway", -17.824451463094018, 31.051811710319182),
                LocationEntity("17", "Waterfalls", -17.913074, 30.999886),
                LocationEntity("18", "Avondale", -17.802837383146496, 31.038131519440938),
                LocationEntity("19", "Southerton", -17.86829214311744, 31.01466710524981),
                LocationEntity("20", "Westgate", -17.764704169442044, 30.972873930833376),
                LocationEntity("21", "Highlands", -17.81182390078103, 31.100540616352244),
                LocationEntity("22", "Hatcliffe", -17.7850, 31.1050),
                LocationEntity("23", "Belgravia", -17.80711293256058, 31.040088154678738)
            )
            locationDao.insertLocations(locations)

            val buses = listOf(
                BusEntity("B1", "Muridzi Wenyaya", "Govasberg-101", 60),
                BusEntity("B2", "Shumba", "Govasberg-102", 50),
                BusEntity("B3", "Tenzi Wangu", "Govasberg-103", 55)
            )
            busDao.insertBuses(buses)
        }
    }
}
