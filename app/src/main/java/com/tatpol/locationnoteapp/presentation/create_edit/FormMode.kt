package com.tatpol.locationnoteapp.presentation.create_edit

import com.tatpol.locationnoteapp.data.model.Note

sealed class FormMode {

    data class EditMode(val note: Note) : FormMode()

    object CreateMode : FormMode()
}
