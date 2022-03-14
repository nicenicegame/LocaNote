package com.tatpol.locationnoteapp.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.databinding.ListItemNoteBinding

class NoteAdapter(
    private val context: Context,
    private val listener: NoteItemClickListener
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            ListItemNoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ListItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ibMenu.setOnClickListener {
                showMenu(it, R.menu.menu_note_item_popup)
            }
        }

        private fun showMenu(view: View, @MenuRes menuRes: Int) {
            val popup = PopupMenu(context, view)
            popup.menuInflater.inflate(menuRes, popup.menu)
            popup.setForceShowIcon(true)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.showPath -> {
                        listener.onGetNoteRoute(getItem(adapterPosition))
                        true
                    }
                    R.id.editNote -> {
                        listener.onEditNote(getItem(adapterPosition))
                        true
                    }
                    R.id.deleteNote -> {
                        listener.onDeleteNote(getItem(adapterPosition))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        fun bind(note: Note) {
            binding.apply {
                tvNoteTitle.text = note.title
                tvNoteDescription.text = note.description
                tvNoteAddress.text = note.address
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    interface NoteItemClickListener {
        fun onGetNoteRoute(note: Note)
        fun onEditNote(note: Note)
        fun onDeleteNote(note: Note)
    }
}