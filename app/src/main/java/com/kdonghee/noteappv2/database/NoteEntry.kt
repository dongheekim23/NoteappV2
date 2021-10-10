package com.kdonghee.noteappv2.database

import android.provider.BaseColumns

object NoteEntry : BaseColumns {

    const val TABLE_NAME = "noteList"
    const val COLUMN_NAME = "name"
    const val COLUMN_AMOUNT = "amount"
    const val COLUMN_TIMESTAMP = "timestamp"
}
