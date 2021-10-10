package com.kdonghee.noteappv2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kdonghee.noteappv2.database.NoteDBHelper
import com.kdonghee.noteappv2.item.ItemStatus
import com.kdonghee.noteappv2.item.ItemUtils
import com.kdonghee.noteappv2.thread.ThreadPoolManager

class AddItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_item)

        supportActionBar?.title = "Add Item"

        val noteDBHelper = NoteDBHelper(this)

        val confirmButton: Button = findViewById(R.id.button_confirm)
        confirmButton.setOnClickListener {
            val editText: EditText = findViewById(R.id.name_edit_text)
            val inputEditText = editText.text.trim().toString()

            if (inputEditText.isEmpty()) {
                Toast.makeText(this, "Enter a word!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ThreadPoolManager.execute {
                val item = noteDBHelper.addItem(inputEditText, (1..10).random())

                ThreadPoolManager.submitOnMainThread {
                    Log.i("dh5031", "${Thread.currentThread().name} - notifyItemChanged(item, ItemStatus.ADDED)")
                    ItemUtils.notifyItemChanged(item, ItemStatus.ADDED)
                }
            }

            finish()
        }
    }
}
