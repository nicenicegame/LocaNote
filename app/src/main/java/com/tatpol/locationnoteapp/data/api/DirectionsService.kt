package com.tatpol.locationnoteapp.data.api

import com.tatpol.locationnoteapp.BuildConfig
import com.tatpol.locationnoteapp.data.model.DirectionsResult
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface DirectionsService {

    @GET("directions/$OUTPUT_FORMAT")
    suspend fun getDirections(
        @QueryMap params: Map<String, String>,
        @Query("key") key: String = BuildConfig.MAPS_API_KEY
    ): DirectionsResult

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/"
        const val OUTPUT_FORMAT = "json"
    }
}