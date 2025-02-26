package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.StationDao
import com.sinarowa.e_bus_ticket.data.local.entities.StationCoordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StationRepository @Inject constructor(private val stationDao: StationDao) {

    suspend fun insertStation(station: StationCoordinates) {
        stationDao.insertStation(station)
    }

    fun getStationCoordinates(stationName: String): Flow<StationCoordinates?> {
        return stationDao.getStationCoordinates(stationName)
    }

    fun getAllStations(): Flow<List<StationCoordinates>> {
        return stationDao.getAllStations()
    }

    suspend fun getStationCoordinatesMap(): Map<String, Pair<Double, Double>> {
        return stationDao.getAllStations().first()
            .associate { it.station to Pair(it.latitude, it.longitude) }
    }
}
