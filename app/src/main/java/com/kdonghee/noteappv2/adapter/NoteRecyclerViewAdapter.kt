package com.kdonghee.noteappv2.adapter

import android.content.Context
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kdonghee.noteappv2.R
import com.kdonghee.noteappv2.database.NoteDBHelper
import com.kdonghee.noteappv2.database.NoteEntry
import com.kdonghee.noteappv2.item.*
import com.kdonghee.noteappv2.thread.ThreadPoolManager

class NoteRecyclerViewAdapter(private val context: Context, private val dbHelper: NoteDBHelper)
    : RecyclerView.Adapter<NoteRecyclerViewAdapter.NoteRecyclerViewHolder>(), ItemChangeListener {

    private var adapterItems: MutableList<NoteItem> = mutableListOf()
    private var recyclerView: RecyclerView? = null

    init {
        updateAdapterItems()
        ItemUtils.registerItemChangeListener(this)
    }

    fun updateAdapterItems() {
        if (adapterItems.isNotEmpty()) {
            val adapterItemsSize = adapterItems.size
            adapterItems.clear()
            notifyItemRangeRemoved(0, adapterItemsSize)
        }

        ThreadPoolManager.execute {
            val db = dbHelper.readableDatabase
            val cursor = dbHelper.getAllItems(db)
            val tempAdapterItems = mutableListOf<NoteItem>()
            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndex(BaseColumns._ID)
                val nameIndex = cursor.getColumnIndex(NoteEntry.COLUMN_NAME)
                val amountIndex = cursor.getColumnIndex(NoteEntry.COLUMN_AMOUNT)

                val id = if (idIndex >= 0) cursor.getLong(idIndex) else -1
                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "EMPTY_NAME"
                val amount = if (amountIndex >= 0) cursor.getInt(amountIndex) else 0

                tempAdapterItems.add(NoteItem(id, name, amount))
            }
            cursor.close()
            db.close()

            ThreadPoolManager.submitOnMainThread {
                adapterItems = tempAdapterItems
                notifyItemRangeInserted(0, adapterItems.size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteRecyclerViewHolder {
        val inflatedItemView = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        val viewHolder = NoteRecyclerViewHolder(inflatedItemView, context, dbHelper)
        viewHolder.imageView.setImageResource(R.drawable.small_arrow_icon)

        return viewHolder
    }

    override fun onBindViewHolder(holder: NoteRecyclerViewHolder, position: Int) {
        val item = adapterItems[position]

        //holder.imageView.setImageDrawable()
        holder.firstTextView.text = item.name
        holder.secondTextView.text = item.amount.toString()
        holder.itemView.tag = item.id
    }

    override fun getItemCount() = adapterItems.size

    class NoteRecyclerViewHolder(itemView: View, private val context: Context,
                                 private val noteDBHelper: NoteDBHelper) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.note_item_image)
        val firstTextView: TextView = itemView.findViewById(R.id.note_item_first_text)
        val secondTextView: TextView = itemView.findViewById(R.id.note_item_second_text)
        private val removeItemButton: Button = itemView.findViewById(R.id.remove_this_item_button)

        init {
            removeItemButton.setOnClickListener {
                val index = adapterPosition
                if (index < 0) {
                    Log.e("dh5031", "Trying to click on view which is being removed!")
                    return@setOnClickListener
                }
                ThreadPoolManager.execute {
                    val itemId = itemView.tag as Long
                    noteDBHelper.removeItem(itemId)
                    ThreadPoolManager.submitOnMainThread {
                        ItemUtils.notifyItemChanged(itemId, ItemStatus.REMOVED)
                    }
                }
            }

            firstTextView.setOnClickListener {
                ItemUtils.createAndShowItemDialog(context, noteDBHelper, DialogPurpose.UPDATE, itemView.tag as Long)
            }
        }
    }

    override fun onItemChanged(item: NoteItem?, status: ItemStatus) {
        when (status) {
            ItemStatus.ADDED -> {
                if (item == null) {
                    Log.e("dh5031", "Item is null! This shouldn't happen for adding an item")
                    return
                }

                adapterItems.add(item)
                val indexOfInsertedItem = adapterItems.size - 1
                notifyItemInserted(indexOfInsertedItem)

                recyclerView?.scrollToPosition(indexOfInsertedItem)
            }
            ItemStatus.UPDATED -> {
                if (item == null) {
                    Log.e("dh5031", "Updated item is null! Do not perform update operation")
                    return
                }

                val index = adapterItems.indexOfFirst { it.id == item.id }
                adapterItems[index] = item
                notifyItemChanged(index)
            }
            ItemStatus.CLEARED -> {
                val adapterItemSize = adapterItems.size
                adapterItems.clear()
                notifyItemRangeRemoved(0, adapterItemSize)
            }
            else -> return
        }
    }

    override fun onItemChanged(itemId: Long, status: ItemStatus) {
        when (status) {
            ItemStatus.REMOVED -> {
                val index = adapterItems.indexOfFirst { it.id == itemId }
                /*if (index < 0) {
                    Log.e("dh5031", "Item with the given ID cannot be found!")
                    return
                }*/

                adapterItems.removeAt(index)
                notifyItemRemoved(index)
            }
            else -> return
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
}
