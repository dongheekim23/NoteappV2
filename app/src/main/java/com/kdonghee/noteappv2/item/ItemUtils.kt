package com.kdonghee.noteappv2.item

interface ItemChangeListener {

    fun onItemChanged(item: NoteItem?, status: ItemStatus)
}

enum class ItemStatus {
    REMOVED,
    ADDED,
    CLEARED,
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
}
