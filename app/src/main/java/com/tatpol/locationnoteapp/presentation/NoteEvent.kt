package com.tatpol.locationnoteapp.presentation

import android.os.Parcelable
import com.tatpol.locationnoteapp.data.model.Note
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteEvent(
    val note: Note,
    val type: EventType
) : Parcelable
