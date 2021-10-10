package com.kdonghee.noteappv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kdonghee.noteappv2.adapter.NoteRecyclerViewAdapter
import com.kdonghee.noteappv2.database.NoteDBHelper
import com.kdonghee.noteappv2.database.NoteEntry
import com.kdonghee.noteappv2.database.SECONDARY_TABLE_NAME
import com.kdonghee.noteappv2.item.ItemStatus
import com.kdonghee.noteappv2.item.ItemUtils
import com.kdonghee.noteappv2.item.NoteItem
import com.kdonghee.noteappv2.thread.ThreadPoolManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val addButton: Button = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
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
}
