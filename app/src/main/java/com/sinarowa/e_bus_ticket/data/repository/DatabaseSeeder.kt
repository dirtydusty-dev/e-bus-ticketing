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
                RouteEntity("1", "Harare to Bulawayo", "Harare", "Bulawayo", "Harare,Kadoma,Kwekwe,Gweru,Bulawayo"),
                RouteEntity("2", "Harare to Mutare", "Harare", "Mutare", "Harare,Marondera,Rusape,Mutare"),
                RouteEntity("3", "Bulawayo to Victoria Falls", "Bulawayo", "Victoria Falls", "Bulawayo,Hwange,Victoria Falls")
            )
            routeDao.insertRoutes(routes)

            // ✅ Insert Prices (All permutations)
            val prices = listOf(
                PriceEntity(fromCity = "Harare", toCity = "Bulawayo", adultPrice = 20.0),
                PriceEntity(fromCity = "Harare", toCity = "Kadoma", adultPrice = 5.0),
                PriceEntity(fromCity = "Harare", toCity = "Kwekwe", adultPrice = 10.0),
                PriceEntity(fromCity = "Harare", toCity = "Gweru", adultPrice = 15.0),
                PriceEntity(fromCity = "Kadoma", toCity = "Kwekwe", adultPrice = 5.0),
                PriceEntity(fromCity = "Kadoma", toCity = "Gweru", adultPrice = 10.0),
                PriceEntity(fromCity = "Kwekwe", toCity = "Gweru", adultPrice = 5.0),
                PriceEntity(fromCity = "Gweru", toCity = "Bulawayo", adultPrice = 5.0),
                PriceEntity(fromCity = "Harare", toCity = "Mutare", adultPrice = 15.0),
                PriceEntity(fromCity = "Harare", toCity = "Marondera", adultPrice = 3.0),
                PriceEntity(fromCity = "Harare", toCity = "Rusape", adultPrice = 10.0),
                PriceEntity(fromCity = "Marondera", toCity = "Rusape", adultPrice = 7.0),
                PriceEntity(fromCity = "Rusape", toCity = "Mutare", adultPrice = 5.0)
            )
            priceDao.insertPrices(prices)

            // ✅ Insert Locations
            val locations = listOf(
                LocationEntity("1", "Harare", -17.8252, 31.0335),
                LocationEntity("2", "Bulawayo", -20.1325, 28.6265),
                LocationEntity("3", "Mutare", -18.9718, 32.6700),
                LocationEntity("4", "Gweru", -19.4568, 29.8167),
                LocationEntity("5", "Masvingo", -20.0744, 30.8327),
                LocationEntity("6", "Kwekwe", -18.9281, 29.8149),
                LocationEntity("7", "Kadoma", -18.3302, 29.9156),
                LocationEntity("8", "Chinhoyi", -17.3661, 30.2002),
                LocationEntity("9", "Marondera", -18.1869, 31.5516)
            )
            locationDao.insertLocations(locations)

            val buses = listOf(
                BusEntity("B1", "ZUPCO-101", "ZUPCO-101", 60),
                BusEntity("B2", "CityLink-202", "CityLink-202", 50),
                BusEntity("B3", "InterAfrica-303", "InterAfrica-303", 55),
                BusEntity("B4", "TripTrans-404", "TripTrans-404", 45),
                BusEntity("B5", "Swift-505", "Swift-505", 40)
            )
            busDao.insertBuses(buses)
        }
    }
}
