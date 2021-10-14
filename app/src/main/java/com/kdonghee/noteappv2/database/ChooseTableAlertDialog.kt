package com.kdonghee.noteappv2.database

import android.content.Context
import android.view.LayoutInflater
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.kdonghee.noteappv2.R

class ChooseTableAlertDialog private constructor(context: Context) : AlertDialog(context) {

    companion object {
        fun newInstance(context: Context, tableList: List<String>): AlertDialog {
            val chooseTableAlertDialog = Builder(context).create()

            val tableListLayout = LayoutInflater.from(context).inflate(R.layout.table_list_layout, null)
            val tableListView = tableListLayout.findViewById<ListView>(R.id.table_list_view)

            tableListView.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, tableList)
            tableListView.setOnItemClickListener { parent, view, position, id ->
                if (view is TextView) {
                    DBTableUtils.changeTableTo(view.text.toString())
                }
                chooseTableAlertDialog.dismiss()
            }

            chooseTableAlertDialog.setView(tableListLayout)

            chooseTableAlertDialog.setButton(BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            return chooseTableAlertDialog
        }
    }
}
