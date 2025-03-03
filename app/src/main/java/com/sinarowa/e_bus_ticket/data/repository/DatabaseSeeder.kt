package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.StationDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteStopDao
import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.StationEntity
import com.sinarowa.e_bus_ticket.data.local.entities.RouteStop
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seedData(routeDao: RouteDao, stationDao: StationDao, routeStopDao: RouteStopDao, priceDao: PriceDao, busDao: BusDao) {
        withContext(Dispatchers.IO) {

            // ✅ Insert Routes
            val routes = listOf(
                RouteEntity(routeId = "HRE-CWR", routeName = "HRE-CWR"),
                RouteEntity(routeId = "HRE-NNW", routeName = "HRE-NNW"),
                RouteEntity(routeId = "HRE-ERT", routeName = "HRE-ERT"),
                RouteEntity(routeId = "CWR-HRE", routeName = "CWR-HRE")
            )

            for (route in routes) {
                if (routeDao.getRouteByName(route.routeName).isEmpty()) {
                    routeDao.insert(route) // Insert only if not already present
                }
            }

            // ✅ Insert Stations
            val stations = listOf(
                StationEntity(stationId = "1", name = "Harare CBD", latitude = -17.8315, longitude = 31.0474),
                StationEntity(stationId = "2", name = "Causeway", latitude = -17.8245, longitude = 31.0518),
                StationEntity(stationId = "3", name = "Milton Park", latitude = -17.8147, longitude = 31.0360),
                StationEntity(stationId = "4", name = "Belvedere", latitude = -17.8281, longitude = 31.0198),
                StationEntity(stationId = "5", name = "Southerton", latitude = -17.8682, longitude = 31.0147)
            )

            for (station in stations) {
                if (stationDao.getStationByName(station.name).isEmpty()) {
                    stationDao.insertStation(station) // Insert only if not already present
                }
            }

            // ✅ Insert RouteStops (Route -> Station mapping with stop order)
            val routeStops = listOf(
                RouteStop(routeId = "HRE-CWR", stationId = "1", stopOrder = 1),
                RouteStop(routeId = "HRE-CWR", stationId = "2", stopOrder = 2),
                RouteStop(routeId = "HRE-CWR", stationId = "3", stopOrder = 3),
                RouteStop(routeId = "HRE-CWR", stationId = "4", stopOrder = 4),
                RouteStop(routeId = "HRE-CWR", stationId = "5", stopOrder = 5)
            )

            for (routeStop in routeStops) {
                if (routeStopDao.getRouteStopByRouteIdAndStationId(routeStop.routeId, routeStop.stationId) == null) {
                    routeStopDao.insert(routeStop) // Insert only if not already present
                }
            }

            // ✅ Insert Prices (Using station IDs instead of names)
            val prices = listOf(
                Price(priceId = "price1", startStationId = "1", destinationStationId = "2", amount = 1.0),
                Price(priceId = "price2", startStationId = "1", destinationStationId = "3", amount = 2.0),
                Price(priceId = "price3", startStationId = "1", destinationStationId = "4", amount = 3.0),
                Price(priceId = "price4", startStationId = "1", destinationStationId = "5", amount = 4.0),
                Price(priceId = "price5", startStationId = "2", destinationStationId = "3", amount = 1.5),
                Price(priceId = "price6", startStationId = "2", destinationStationId = "4", amount = 2.5)
            )

            priceDao.insertPrices(prices)

            val buses = listOf(
                Bus(busId = "1", busName = "Muridzi Wenyaya", busNumber = "Govasberg-101", capacity = 60),
                Bus(busId = "2", busName = "Shumba", busNumber = "Govasberg-102", capacity = 50),
                Bus(busId = "3", busName = "Tenzi Wangu", busNumber = "Govasberg-103", capacity = 55)
            )

            // Insert the buses into the database (if they don't already exist)
            for (bus in buses) {
                val existingBus = busDao.getBusById(bus.busId)
                if (existingBus == null) {
                    busDao.insertBus(bus) // Insert bus if it doesn't exist
                }
            }

        }
    }
}
