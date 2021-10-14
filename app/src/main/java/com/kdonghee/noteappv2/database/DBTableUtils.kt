package com.kdonghee.noteappv2.database

import kotlin.properties.Delegates

interface TableNameChangeListener {

    fun onTableNameChanged(newName: String)
}

object DBTableUtils {

    private val tableNameChangeListeners: MutableSet<TableNameChangeListener> = mutableSetOf()

    var currentTableName: String by Delegates.observable(NoteEntry.TABLE_NAME) { _, _, newValue ->
        tableNameChangeListeners.forEach { it.onTableNameChanged(newValue) }
    }
        private set

    fun registerTableNameChangeListener(listener: TableNameChangeListener) {
        tableNameChangeListeners.add(listener)
    }

    fun deregisterTableNameChangeListener(listener: TableNameChangeListener) {
        tableNameChangeListeners.remove(listener)
    }

    fun getTableNameList(): List<String> {
        return listOf("noteList", "noteList2")
    }

    fun swapTables() {
        currentTableName = if (currentTableName == NoteEntry.TABLE_NAME) SECONDARY_TABLE_NAME else NoteEntry.TABLE_NAME
    }

    fun changeTableTo(newTableName: String) {
        if (currentTableName == newTableName) {
            return
        }

        currentTableName = newTableName
    }
}
