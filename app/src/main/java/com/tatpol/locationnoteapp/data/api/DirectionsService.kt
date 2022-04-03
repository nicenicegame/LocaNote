package com.tatpol.locationnoteapp.data.api

import retrofit2.http.GET

interface DirectionsService {

    @GET("")
    suspend fun getDirections() {

    }

    companion object {
        const val BASE_URL = ""
    }
}