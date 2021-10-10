package com.kdonghee.noteappv2.item

data class NoteItem(val id: Long, val name: String, val amount: Int) {

    override fun equals(other: Any?): Boolean {
        if (other !is NoteItem) {
            return false
        }

        return id == other.id && name == other.name && amount == other.amount
    }
}
