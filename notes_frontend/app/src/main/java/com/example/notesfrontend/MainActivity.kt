package com.example.notesfrontend

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * MainActivity: Note organizer app, featuring single-column notes list, CRUD, and search.
 * Uses light material design and environment variables via BuildConfig for configuration.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notes: MutableList<Note>
    private lateinit var filteredNotes: MutableList<Note>
    private lateinit var searchBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme before super.onCreate to apply light palette early
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of using environment variables (from BuildConfig) for configuration
        // For demonstration, fallback to hardcoded string if not defined
        val NOTES_APP_API_URL = BuildConfig.API_URL ?: "http://localhost"

        notes = mutableListOf()
        filteredNotes = mutableListOf()

        // UI setup
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_notes)
        notesAdapter = NotesAdapter(filteredNotes,
            onEdit = { note -> editNoteDialog(note) },
            onDelete = { note -> deleteNoteDialog(note) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter

        searchBox = findViewById(R.id.search_box)
        searchBox.addTextChangedListener {
            val query = it.toString().trim()
            filterNotes(query)
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener { showNoteDialog() }

        // Populate with sample data for demonstration
        addSampleData()
    }

    /** Demonstrates populating with a couple of default notes. */
    private fun addSampleData() {
        if (notes.isNotEmpty()) return
        notes.add(Note("Welcome", "Start creating your notes!", nextId()))
        notes.add(Note("Tip", "Use the + button to add a new note.", nextId()))
        filterNotes("")
    }

    // PUBLIC_INTERFACE
    /**
     * Show dialog to create a new note.
     */
    private fun showNoteDialog() {
        showEditDialog(title = "Create Note", note = null)
    }

    // PUBLIC_INTERFACE
    /**
     * Show dialog to edit an existing note.
     */
    private fun editNoteDialog(note: Note) {
        showEditDialog(title = "Edit Note", note = note)
    }

    /**
     * Shared dialog logic for creating or editing a note.
     */
    private fun showEditDialog(title: String, note: Note?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note_edit, null)
        val titleBox = dialogView.findViewById<EditText>(R.id.note_title_box)
        val contentBox = dialogView.findViewById<EditText>(R.id.note_content_box)

        if (note != null) {
            titleBox.setText(note.title)
            contentBox.setText(note.content)
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val nt = titleBox.text.toString().trim()
                val ct = contentBox.text.toString().trim()
                if (nt.isEmpty() && ct.isEmpty()) return@setPositiveButton
                if (note == null) {
                    val newNote = Note(nt, ct, nextId())
                    notes.add(0, newNote)
                } else {
                    note.title = nt
                    note.content = ct
                }
                filterNotes(searchBox.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // PUBLIC_INTERFACE
    /**
     * Show confirmation to delete a note.
     */
    private fun deleteNoteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note?")
            .setMessage("Are you sure you want to delete \"${note.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                notes.remove(note)
                filterNotes(searchBox.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // PUBLIC_INTERFACE
    /**
     * Filter the notes by query and update the list.
     */
    private fun filterNotes(query: String) {
        filteredNotes.clear()
        val q = query.lowercase()
        filteredNotes.addAll(
            if (q.isEmpty()) notes
            else notes.filter {
                it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
            })
        notesAdapter.notifyDataSetChanged()
    }

    /** Generate a new note ID. */
    private fun nextId(): Long = (notes.maxOfOrNull { it.id } ?: 0L) + 1L

    // PUBLIC_INTERFACE
    /**
     * Simple data class for a note.
     */
    data class Note(
        var title: String,
        var content: String,
        val id: Long
    )

    /**
     * RecyclerView adapter for displaying notes.
     */
    class NotesAdapter(
        private val notes: List<Note>,
        private val onEdit: (Note) -> Unit,
        private val onDelete: (Note) -> Unit
    ) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

        inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.item_note_title)
            val content: TextView = itemView.findViewById(R.id.item_note_content)
            val editBtn: ImageButton = itemView.findViewById(R.id.item_edit_btn)
            val deleteBtn: ImageButton = itemView.findViewById(R.id.item_delete_btn)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(view)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = notes[position]
            holder.title.text = note.title
            holder.content.text = note.content

            holder.editBtn.setOnClickListener { onEdit(note) }
            holder.deleteBtn.setOnClickListener { onDelete(note) }
        }

        override fun getItemCount(): Int = notes.size
    }
}
