package com.kdonghee.noteappv2.adapter

import android.content.Context
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kdonghee.noteappv2.R
import com.kdonghee.noteappv2.database.NoteDBHelper
import com.kdonghee.noteappv2.database.NoteEntry
import com.kdonghee.noteappv2.item.ItemChangeListener
import com.kdonghee.noteappv2.item.ItemStatus
import com.kdonghee.noteappv2.item.ItemUtils
import com.kdonghee.noteappv2.item.NoteItem
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
        val viewHolder = NoteRecyclerViewHolder(inflatedItemView)
        viewHolder.imageView.setImageResource(R.drawable.small_arrow_icon)

        inflatedItemView.setOnClickListener { view ->
            val index = viewHolder.adapterPosition
            if (index < 0) {
                Log.e("dh5031", "Trying to click on view which is being removed!")
                return@setOnClickListener
            }
            ThreadPoolManager.execute {
                dbHelper.removeItem(adapterItems[index].id)
                ThreadPoolManager.submitOnMainThread {
                    ItemUtils.notifyItemChanged(adapterItems[index], ItemStatus.REMOVED)
                }
            }
        }

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

    class NoteRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.note_item_image)
        val firstTextView: TextView = itemView.findViewById(R.id.note_item_first_text)
        val secondTextView: TextView = itemView.findViewById(R.id.note_item_second_text)
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
            ItemStatus.REMOVED -> {
                val index = adapterItems.indexOfFirst { it.id == item?.id }
                /*if (index < 0) {
                    Log.e("dh5031", "Item with the given ID cannot be found!")
                    return
                }*/

                adapterItems.removeAt(index)
                notifyItemRemoved(index)
            }
            ItemStatus.CLEARED -> {
                val adapterItemSize = adapterItems.size
                adapterItems.clear()
                notifyItemRangeRemoved(0, adapterItemSize)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
}
