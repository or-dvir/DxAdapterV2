package com.hotmail.or_dvir.dxrecyclerview

/**
 * a convenience class containing item visibility listeners.
 *
 * @param onFirstItemVisible a listener to be invoked when the FIRST item on your list is VISIBLE.
 *
 * note that this will trigger immediately (assuming your adapter contains at least 1 item).
 *
 * @param onFirstItemInvisible a listener to be invoked when the FIRST item on your list is INVISIBLE.
 *
 * note that if the entire list fits on the screen, this will NEVER trigger.
 *
 * @param onLastItemVisible a listener to be invoked when the LAST item on your list is VISIBLE.
 * note that if the entire list fits on the screen, this will trigger immediately
 * (assuming your adapter contains at least 1 item).
 *
 * @param onLastItemInvisible a listener to be invoked when the LAST item on your list is INVISIBLE.
 *
 * note that if the entire list does NOT fit on the screen, this will trigger immediately
 * (assuming your adapter contains at least 1 item).
 *
 * note that if the entire list DOES fit on the screen, this will NEVER trigger.
 */
open class DxVisibilityListener(
    val onFirstItemVisible: GenericListener? = null,
    val onFirstItemInvisible: GenericListener? = null,
    val onLastItemVisible: GenericListener? = null,
    val onLastItemInvisible: GenericListener? = null
) {
    internal var flagNotifiedFirstVisible = false
    internal var flagNotifiedFirstInvisible = false
    internal var flagNotifiedLastVisible = false
    internal var flagNotifiedLastInvisible = false

    internal fun atLeastOneListenerSet() = atLeastOneListenerFirst() || atLeastOneListenerLast()
    internal fun atLeastOneListenerFirst() =
        onFirstItemInvisible != null || onFirstItemVisible != null

    internal fun atLeastOneListenerLast() = onLastItemInvisible != null || onLastItemVisible != null
}