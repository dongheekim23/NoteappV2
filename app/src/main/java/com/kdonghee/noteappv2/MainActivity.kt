package com.kdonghee.noteappv2

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kdonghee.noteappv2.adapter.NoteRecyclerViewAdapter
import com.kdonghee.noteappv2.database.NoteDBHelper
import com.kdonghee.noteappv2.database.NoteEntry
import com.kdonghee.noteappv2.database.SECONDARY_TABLE_NAME
import com.kdonghee.noteappv2.database.TableNameChangeListener
import com.kdonghee.noteappv2.item.ItemStatus
import com.kdonghee.noteappv2.item.ItemUtils
import com.kdonghee.noteappv2.item.NoteItem
import com.kdonghee.noteappv2.thread.ThreadPoolManager

class MainActivity : AppCompatActivity(), TableNameChangeListener {

    private var titleTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NoteDBHelper.registerTableNameChangeListener(this)

        val recyclerView: RecyclerView = findViewById(R.id.note_recycler_view)
        val dbHelper = NoteDBHelper(this)
        val adapter = NoteRecyclerViewAdapter(this, dbHelper)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = viewHolder.itemView.tag as Long
                ThreadPoolManager.execute {
                    dbHelper.removeItem(id)
                    ThreadPoolManager.submitOnMainThread {
                        ItemUtils.notifyItemChanged(NoteItem(id, "", 0), ItemStatus.REMOVED)
                    }
                }
            }
        }).attachToRecyclerView(recyclerView)

        titleTextView = findViewById(R.id.note_title)
        titleTextView?.text = NoteDBHelper.currentTableName

        val addButton: Button = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            //startActivity(Intent(this, AddItemActivity::class.java))
            createAndShowAddItemDialog(this, dbHelper)
        }

        val addRandomButton: Button = findViewById(R.id.add_random_button)
        addRandomButton.setOnClickListener {
            ThreadPoolManager.execute {
                val item = dbHelper.addRandomItem()

                ThreadPoolManager.submitOnMainThread {
                    Log.i("dh5031", "${Thread.currentThread().name} - notifyItemChanged(item, ItemStatus.ADDED)")
                    ItemUtils.notifyItemChanged(item, ItemStatus.ADDED)
                }
            }
        }

        val clearButton: Button = findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            ThreadPoolManager.execute {
                dbHelper.clearAllItems()
                ThreadPoolManager.submitOnMainThread {
                    ItemUtils.notifyItemChanged(null, ItemStatus.CLEARED)
                }
            }
        }

        val changeTableButton: Button = findViewById(R.id.change_table_button)
        changeTableButton.setOnClickListener {
            dbHelper.swapTables()
            adapter.updateAdapterItems()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NoteDBHelper.deregisterTableNameChangeListener(this)
    }

    private fun createAndShowAddItemDialog(context: Context, noteDBHelper: NoteDBHelper) {
        val editTextContainer = LayoutInflater.from(context).inflate(R.layout.item_add_edit_text, null)
        val editText: EditText = editTextContainer.findViewById(R.id.add_item_edit_text)

        val addItemDialog = AlertDialog.Builder(this).create()
        addItemDialog.apply {
            setView(editTextContainer)
            setTitle("Add Item")
            setCancelable(true)

            setButton(DialogInterface.BUTTON_POSITIVE, "Add") clickListener@ { _, _ ->
                val inputEditText = editText.text.trim().toString()
                if (inputEditText.isEmpty()) {
                    Toast.makeText(context, "Enter a word!", Toast.LENGTH_SHORT).show()
                    return@clickListener
                }

                ThreadPoolManager.execute {
                    val item = noteDBHelper.addItem(inputEditText, (1..10).random())

                    ThreadPoolManager.submitOnMainThread {
                        Log.i("dh5031", "${Thread.currentThread().name} - notifyItemChanged(item, ItemStatus.ADDED)")
                        ItemUtils.notifyItemChanged(item, ItemStatus.ADDED)
                    }
                }

                dismiss()
            }

            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                dismiss()
            }
        }

        addItemDialog.show()
    }

    override fun onTableNameChanged(newName: String) {
        titleTextView?.text = newName
    }
}
