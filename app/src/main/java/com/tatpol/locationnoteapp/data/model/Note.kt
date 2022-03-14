package com.tatpol.locationnoteapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String? = null,
    val userId: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
