package com.tatpol.locationnoteapp.presentation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EventType : Parcelable {
    EDIT_NOTE,
    SHOW_NOTE_ROUTE,
}
