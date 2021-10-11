package com.kdonghee.noteappv2.item

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.kdonghee.noteappv2.R
import com.kdonghee.noteappv2.database.NoteDBHelper
import com.kdonghee.noteappv2.thread.ThreadPoolManager

interface ItemChangeListener {

    fun onItemChanged(item: NoteItem?, status: ItemStatus)

    fun onItemChanged(itemId: Long, status: ItemStatus)
}

enum class DialogPurpose {
    ADD,
    UPDATE,
}

enum class ItemStatus {
    REMOVED,
    ADDED,
    CLEARED,
    UPDATED,
}

object ItemUtils {

    private val itemNames: List<String> = listOf("Banana", "Apple", "Pineapple", "Orange", "Grape")

    private val itemChangeListeners: MutableList<ItemChangeListener> = mutableListOf()

    fun getRandomItemName() = itemNames.random()

    fun getRandomAmount() = (1..10).random()

    fun registerItemChangeListener(listener: ItemChangeListener) {
        itemChangeListeners.add(listener)
    }

    fun deregisterItemChangeListener(listener: ItemChangeListener) {
        itemChangeListeners.remove(listener)
    }

    fun notifyItemChanged(item: NoteItem?, status: ItemStatus) {
        itemChangeListeners.forEach { it.onItemChanged(item, status) }
    }

    fun notifyItemChanged(itemId: Long, status: ItemStatus) {
        itemChangeListeners.forEach { it.onItemChanged(itemId, status) }
    }

    fun createAndShowItemDialog(context: Context, noteDBHelper: NoteDBHelper, dialogPurpose: DialogPurpose, itemId: Long? = null) {
        val editTextContainer = LayoutInflater.from(context).inflate(R.layout.item_add_edit_text, null)
        val editText: EditText = editTextContainer.findViewById(R.id.add_item_edit_text)

        val addItemDialog = AlertDialog.Builder(context).create()
        addItemDialog.apply {
            setView(editTextContainer)

            val title: String
            val positiveButtonText: String
            when (dialogPurpose) {
                DialogPurpose.ADD -> {
                    title = "Add Item"
                    positiveButtonText = "Add"
                }
                DialogPurpose.UPDATE -> {
                    title = "Update Item"
                    positiveButtonText = "Update"
                }
            }
            setTitle(title)
            setCancelable(true)

            setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText) clickListener@ { _, _ ->
                val inputEditText = editText.text.trim().toString()
                if (inputEditText.isEmpty()) {
                    Toast.makeText(context, "Enter a word!", Toast.LENGTH_SHORT).show()
                    return@clickListener
                }

                ThreadPoolManager.execute {
                    val item = when (dialogPurpose) {
                        DialogPurpose.ADD -> noteDBHelper.addItem(inputEditText, (1..10).random())
                        DialogPurpose.UPDATE -> itemId?.let { noteDBHelper.updateItem(inputEditText, (1..10).random(), it) }
                    }

                    ThreadPoolManager.submitOnMainThread {
                        //Log.i("dh5031", "${Thread.currentThread().name} - notifyItemChanged(item, ItemStatus.ADDED)")
                        when (dialogPurpose) {
                            DialogPurpose.ADD -> notifyItemChanged(item, ItemStatus.ADDED)
                            DialogPurpose.UPDATE -> item?.let { notifyItemChanged(item, ItemStatus.UPDATED) }
                        }
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
}
