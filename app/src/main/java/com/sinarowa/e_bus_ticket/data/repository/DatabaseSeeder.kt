package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.StationDao
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import com.sinarowa.e_bus_ticket.data.local.entities.Route
import com.sinarowa.e_bus_ticket.data.local.entities.StationCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    suspend fun seedData(routeDao: RouteDao, priceDao: PriceDao, stationDao: StationDao, busDao: BusDao) {
        withContext(Dispatchers.IO) {
            // ✅ Insert Routes

            val routes = listOf(
                Route(routeName = "HRE-CWR", stations = listOf("Harare CBD", "Causeway", "Milton Park", "Belvedere", "Southerton", "Westgate", "Waterfalls")),
                Route(routeName = "HRE-NNW", stations = listOf("The Avenues", "Alexandra Park", "Arcadia", "Arlington", "Greendale", "Borrowdale", "Mount Pleasant", "Newlands", "Avondale", "Highlands", "Hatcliffe", "Belgravia")),
                Route(routeName = "HRE-ERT", stations = listOf("Harare CBD", "Mbare", "Highfield", "Chitungwiza", "Epworth")),
                Route(routeName = "CWR-HRE", stations = listOf("Waterfalls", "Westgate", "Southerton", "Belvedere", "Milton Park", "Causeway", "Harare CBD"))
            )

            for (route in routes) {
                if (routeDao.getRouteByName(route.routeName).isEmpty()) {
                    routeDao.insertRoute(route) // Insert only if not already present
                }
            }

            // ✅ Insert Prices (All permutations)
            val prices = listOf(
                // Route 1 (Base rate: 1.0 per stop difference)
                Price(startStation = "Harare CBD", stopStation = "Causeway", amount = 1.0),
                Price(startStation = "Harare CBD", stopStation = "Milton Park", amount = 2.0),
                Price(startStation = "Harare CBD", stopStation = "Belvedere", amount = 3.0),
                Price(startStation = "Harare CBD", stopStation = "Southerton", amount = 4.0),
                Price(startStation = "Harare CBD", stopStation = "Westgate", amount = 5.0),
                Price(startStation = "Harare CBD", stopStation = "Waterfalls", amount = 6.0),
                Price(startStation = "Causeway", stopStation = "Milton Park", amount = 1.0),
                Price(startStation = "Causeway", stopStation = "Belvedere", amount = 2.0),
                Price(startStation = "Causeway", stopStation = "Southerton", amount = 3.0),
                Price(startStation = "Causeway", stopStation = "Westgate", amount = 4.0),
                Price(startStation = "Causeway", stopStation = "Waterfalls", amount = 5.0),

                Price(startStation = "Milton Park", stopStation = "Belvedere", amount = 1.0),
                Price(startStation = "Milton Park", stopStation = "Southerton", amount = 2.0),
                Price(startStation = "Milton Park", stopStation = "Westgate", amount = 3.0),
                Price(startStation = "Milton Park", stopStation = "Waterfalls", amount = 4.0),

                Price(startStation = "Belvedere", stopStation = "Southerton", amount = 1.0),
                Price(startStation = "Belvedere", stopStation = "Westgate", amount = 2.0),
                Price(startStation = "Belvedere", stopStation = "Waterfalls", amount = 3.0),

                Price(startStation = "Southerton", stopStation = "Westgate", amount = 1.0),
                Price(startStation = "Southerton", stopStation = "Waterfalls", amount = 2.0),

                Price(startStation = "Westgate", stopStation = "Waterfalls", amount = 1.0),

                Price(startStation = "Waterfalls", stopStation = "Westgate", amount = 1.0),
                Price(startStation = "Waterfalls", stopStation = "Southerton", amount = 2.0),
                Price(startStation = "Waterfalls", stopStation = "Belvedere", amount = 3.0),
                Price(startStation = "Waterfalls", stopStation = "Milton Park", amount = 4.0),
                Price(startStation = "Waterfalls", stopStation = "Causeway", amount = 5.0),
                Price(startStation = "Waterfalls", stopStation = "Harare CBD", amount = 6.0),
                Price(startStation = "Westgate", stopStation = "Southerton", amount = 1.0),
                Price(startStation = "Westgate", stopStation = "Belvedere", amount = 2.0),
                Price(startStation = "Westgate", stopStation = "Milton Park", amount = 3.0),
                Price(startStation = "Westgate", stopStation = "Causeway", amount = 4.0),
                Price(startStation = "Westgate", stopStation = "Harare CBD", amount = 5.0),

                Price(startStation = "Southerton", stopStation = "Belvedere", amount = 1.0),
                Price(startStation = "Southerton", stopStation = "Milton Park", amount = 2.0),
                Price(startStation = "Southerton", stopStation = "Causeway", amount = 3.0),
                Price(startStation = "Southerton", stopStation = "Harare CBD", amount = 4.0),

                Price(startStation = "Belvedere", stopStation = "Milton Park", amount = 1.0),
                Price(startStation = "Belvedere", stopStation = "Causeway", amount = 2.0),
                Price(startStation = "Belvedere", stopStation = "Harare CBD", amount = 3.0),

                Price(startStation = "Milton Park", stopStation = "Causeway", amount = 1.0),
                Price(startStation = "Milton Park", stopStation = "Harare CBD", amount = 2.0),

                Price(startStation = "Causeway", stopStation = "Harare CBD", amount = 1.0),

                // Route 2 (Base rate: 1.0 per stop difference)
                Price(startStation = "The Avenues", stopStation = "Alexandra Park", amount = 1.0),
                Price(startStation = "The Avenues", stopStation = "Arcadia", amount = 2.0),
                Price(startStation = "The Avenues", stopStation = "Arlington", amount = 3.0),
                Price(startStation = "The Avenues", stopStation = "Greendale", amount = 4.0),
                Price(startStation = "The Avenues", stopStation = "Borrowdale", amount = 5.0),
                Price(startStation = "The Avenues", stopStation = "Mount Pleasant", amount = 6.0),
                Price(startStation = "The Avenues", stopStation = "Newlands", amount = 7.0),
                Price(startStation = "The Avenues", stopStation = "Avondale", amount = 8.0),
                Price(startStation = "The Avenues", stopStation = "Highlands", amount = 9.0),
                Price(startStation = "The Avenues", stopStation = "Hatcliffe", amount = 10.0),
                Price(startStation = "The Avenues", stopStation = "Belgravia", amount = 11.0),

                Price(startStation = "Harare CBD", stopStation = "Mbare", amount = 1.5),
                Price(startStation = "Harare CBD", stopStation = "Highfield", amount = 3.0),
                Price(startStation = "Harare CBD", stopStation = "Chitungwiza", amount = 4.5),
                Price(startStation = "Harare CBD", stopStation = "Epworth", amount = 6.0),

                Price(startStation = "Mbare", stopStation = "Highfield", amount = 1.5),
                Price(startStation = "Mbare", stopStation = "Chitungwiza", amount = 3.0),
                Price(startStation = "Mbare", stopStation = "Epworth", amount = 4.5),

                Price(startStation = "Highfield", stopStation = "Chitungwiza", amount = 1.5),
                Price(startStation = "Highfield", stopStation = "Epworth", amount = 3.0),

                Price(startStation = "Chitungwiza", stopStation = "Epworth", amount = 1.5)
            )

            priceDao.insertPrices(prices)



            // ✅ Insert Locations
            val locations = listOf(
                StationCoordinates(1, "Harare CBD", -17.831519192837742, 31.047380474799684),
                StationCoordinates(2, "The Avenues", -17.819990386876434, 31.047064768309873),
                StationCoordinates(3, "Belvedere", -17.828183786232724, 31.01984820636389),
                StationCoordinates(4, "Mbare", -17.86638916448852, 31.03200284779129),
                StationCoordinates(5, "Highfield", -17.88193588141904, 30.981522981920943),
                StationCoordinates(6, "Borrowdale", -17.74971450251613, 31.093916900205176),
                StationCoordinates(7, "Mount Pleasant", -17.767646942255972, 31.047725911567614),
                StationCoordinates(8, "Newlands", -17.810020866183052, 31.083877294030597),
                StationCoordinates(9, "Chitungwiza", -18.01782321540995, 31.073007821155777),
                StationCoordinates(10, "Epworth", -17.896707124627824, 31.15719768809613),
                StationCoordinates(11, "Alexandra Park", -17.79493863816772, 31.056141104405818),
                StationCoordinates(12, "Arcadia", -17.84796288305909, 31.053165881483388),
                StationCoordinates(13, "Arlington", -17.902423818694437, 31.07798331934576),
                StationCoordinates(14, "Greendale", -17.81372074719181, 31.123617495913045),
                StationCoordinates(15, "Milton Park", -17.814686511620252, 31.036046580243525),
                StationCoordinates(16, "Causeway", -17.824451463094018, 31.051811710319182),
                StationCoordinates(17, "Waterfalls", -17.913074, 30.999886),
                StationCoordinates(18, "Avondale", -17.802837383146496, 31.038131519440938),
                StationCoordinates(19, "Southerton", -17.86829214311744, 31.01466710524981),
                StationCoordinates(20, "Westgate", -17.764704169442044, 30.972873930833376),
                StationCoordinates(21, "Highlands", -17.81182390078103, 31.100540616352244),
                StationCoordinates(22, "Hatcliffe", -17.7850, 31.1050),
                StationCoordinates(23, "Belgravia", -17.80711293256058, 31.040088154678738)
            )

            stationDao.insertCoordinates(locations)

            val buses = listOf(
                Bus(1, "Muridzi Wenyaya", "Govasberg-101", 60),
                Bus(2, "Shumba", "Govasberg-102", 50),
                Bus(3, "Tenzi Wangu", "Govasberg-103", 55)
            )
            busDao.insertBuses(buses)
        }
    }
}
