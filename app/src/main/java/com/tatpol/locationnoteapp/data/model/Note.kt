package com.tatpol.locationnoteapp.data.model

data class Note(
    val id: String? = null,
    val userId: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
