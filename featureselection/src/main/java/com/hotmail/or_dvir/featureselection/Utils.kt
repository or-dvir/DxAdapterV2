package com.hotmail.or_dvir.featureselection

/**
 * a listener called when the selected state of an item has changed
 * @param view the view that was changed
 * @param adapterPosition the adapter position of the changed item
 * @param isSelected the new selected state of the item at [adapterPosition]
 */
typealias OnItemSelectionChangedListener = (adapterPosition: Int, isSelected: Boolean) -> Unit