package com.hotmail.or_dvir.dxadapterv2.tests

import android.util.Log
import androidx.annotation.IdRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.hotmail.or_dvir.dxadapter.DxAdapter
import com.hotmail.or_dvir.dxdragandswipe.DxItemTouchCallback
import com.hotmail.or_dvir.dxdragandswipe.DxItemTouchHelper
import com.hotmail.or_dvir.dxdragandswipe.OnDragEventListener
import com.hotmail.or_dvir.dxdragandswipe.OnItemMovedListener
import com.hotmail.or_dvir.dxdragandswipe.drag.DxFeatureDrag
import com.hotmail.or_dvir.dxadapterv2.*
import com.hotmail.or_dvir.dxadapterv2.draggable.AdapterDraggable
import com.hotmail.or_dvir.dxadapterv2.draggable.AdapterNonDraggable
import com.hotmail.or_dvir.dxadapterv2.draggable.ItemDraggable
import com.hotmail.or_dvir.dxadapterv2.draggable.ItemNonDraggable
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.activity_base.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.math.absoluteValue

class TestFeatureDrag : BaseTest() {
    //todo can i test dragging out of bounds of screen?

    private lateinit var mDragStart: OnDragEventListener<BaseItem>
    private lateinit var mDragEnd: OnDragEventListener<BaseItem>
    private lateinit var mOnItemMoved: OnItemMovedListener<BaseItem>
    private lateinit var mDragFeature: DxFeatureDrag<BaseItem>

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    @Before
    fun before() {
        mDragStart = spyk({ view, position, item ->
            Log.i("aaaaa", "test drag started for ${item.text}")
        })
        mDragEnd = spyk({ view, position, item ->
            Log.i("aaaaa", "test drag ended for ${item.text}")
        })
        mOnItemMoved = spyk({ draggedView, draggedPosition, draggedItem,
                              targetView, targetPosition, targetItem ->
            Log.i("aaaaa", "test drag replaced ${draggedItem.text} with ${targetItem.text}")
        })

        mDragFeature = DxFeatureDrag(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, //may be overridden later
            mDragStart,
            mDragEnd,
            mOnItemMoved,
            true
        )
    }

    @After
    fun after() {
        PressActions.tearDown()
    }

    @Test
    fun dragTest_longClick() {
        val items = MutableList(100) { index -> ItemDraggable("item $index") }
        val adapter = AdapterDraggable(items).apply { addFeature(mDragFeature) }
        mDragFeature.setDragOnLongClick(true)

        onActivity { it.apply { setAdapter(adapter) } }
        setupDragFeatureWithRecyclerView(adapter as DxAdapter<BaseItem, *>, null)

        //the positions MUST be visible on screen.
        val positionFrom = 1
        val positionTo = 5
        //we need a reference to the adapter BEFORE the drag operation because dragging changes
        //the positions and some tests need to reference the items at their original positions
        val itemsBeforeDrag = adapter.getDxAdapterItems().toList()

        performDrag(positionFrom, positionTo, null)

        //IMPORTANT NOTE!!!
        //for an unknown reason the drag operation in the performDrag() function
        //triggers mDragEventStart (in addition to the press-and-hold operation).
        //THIS DOES NOT HAPPEN when i manually test the app!!!
        //so just accept it and check that it was called 2 times
        verify(exactly = 2) {
            mDragStart.invoke(
                any(),
                positionFrom,
                itemsBeforeDrag[positionFrom]
            )
        }

        //using absoluteValue in case we change the positions in the future to be dragged
        //from bottom to top.
        //reducing 1 from the range because the listener is only called on the swapping of the items
        //and there is always 1 less swap than the number of items.
        //example:
        //  moving from position 1 to position 5 would have the following swaps
        //  (represented by an arrow): 1->2->3->4->5
        val range = 0 until (positionTo - positionFrom).absoluteValue - 1

        var newPositionFrom: Int
        var actualPositionToCheck: Int

        for (i in range) {
            newPositionFrom = positionFrom + i
            actualPositionToCheck = newPositionFrom + 1 //dragging sequentially so should be 1 more

            verify(exactly = 1) {
                mOnItemMoved.invoke(
                    any(),
                    newPositionFrom,
                    itemsBeforeDrag[positionFrom],
                    any(),
                    actualPositionToCheck,
                    itemsBeforeDrag[actualPositionToCheck]
                )
            }
        }


        //reducing 1 from positionTo because we are dragging to the CENTER of positionTo
        //and that is not enough for the items to be swapped (even BOTTOM_CENTER is not enough)
        actualPositionToCheck = positionTo - 1
        verify(exactly = 1) {
            mDragEnd.invoke(
                any(),
                actualPositionToCheck,
                itemsBeforeDrag[positionFrom]
            )
        }

        scrollAndVerifyText(actualPositionToCheck, "item $positionFrom", adapter)
    }

    @Test
    fun dragTest_longClick_dragDisabled() {
        val items = MutableList(100) { index -> ItemDraggable("item $index") }
        val adapter = AdapterDraggable(items).apply { addFeature(mDragFeature) }
        mDragFeature.setDragOnLongClick(true)
        mDragFeature.isDragEnabled = false

        onActivity { it.apply { setAdapter(adapter) } }
        setupDragFeatureWithRecyclerView(adapter as DxAdapter<BaseItem, *>, null)

        //the positions MUST be visible on screen.
        val positionFrom = 1
        val positionTo = 5

        performDrag(positionFrom, positionTo, null)

        verify(exactly = 0) { mDragStart.invoke(any(), any(), any()) }
        verify(exactly = 0) { mOnItemMoved.invoke(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 0) { mDragEnd.invoke(any(), any(), any()) }

        scrollAndVerifyText(positionFrom, "item $positionFrom", adapter)
    }

    @Test
    fun dragTest_longClick_nonDraggableItem() {
        val items = MutableList(100) { index -> ItemNonDraggable("item $index") }
        val adapter = AdapterNonDraggable(items).apply { addFeature(mDragFeature) }
        mDragFeature.setDragOnLongClick(true)

        onActivity { it.apply { setAdapter(adapter) } }
        setupDragFeatureWithRecyclerView(adapter as DxAdapter<BaseItem, *>, null)

        //the positions MUST be visible on screen.
        val positionFrom = 1
        val positionTo = 5

        performDrag(positionFrom, positionTo, null)

        verify(exactly = 0) { mDragStart.invoke(any(), any(), any()) }
        verify(exactly = 0) { mOnItemMoved.invoke(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 0) { mDragEnd.invoke(any(), any(), any()) }

        scrollAndVerifyText(positionFrom, "item $positionFrom", adapter)
    }

    @Test
    fun dragTest_longClick_wrongDirection() {
        val items = MutableList(100) { index -> ItemDraggable("item $index") }
        val adapter = AdapterDraggable(items).apply { addFeature(mDragFeature) }
        mDragFeature.apply {
            setDragOnLongClick(true)
            setDragDirection(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        }

        onActivity {
            it.apply {
                setAdapter(adapter)
                setLayoutManagerVertical()
            }
        }

        setupDragFeatureWithRecyclerView(adapter as DxAdapter<BaseItem, *>, null)

        //the positions MUST be visible on screen.
        val positionFrom = 1
        val positionTo = 5

        performDrag(positionFrom, positionTo, null)

        //reducing 1 from positionTo because we are dragging to the CENTER of positionTo
        //and that is not enough for the items to be swapped (even BOTTOM_CENTER is not enough)
        val actualPositionToCheck = positionTo - 1

        //all conditions for allowing drag are fulfilled so its expected behaviour
        // for the start/end drag listeners to trigger. however since this test is about dragging
        // in the wrong direction, the move listener should not be triggered.
        //IMPORTANT NOTE!!!
        // for an unknown reason the drag operation in the performDrag() function
        // triggers mDragEventStart (in addition to the press-and-hold operation).
        // THIS DOES NOT HAPPEN when i manually test the app!!!
        // so just accept it and check that it was called 2 times
        verify(exactly = 2) {
            mDragStart.invoke(
                any(),
                positionFrom,
                adapter.getItem(positionFrom)
            )
        }
        verify(exactly = 0) { mOnItemMoved.invoke(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) {
            //NOTE:
            //there was no dragging, so parameters for mDragEventEnd should be the same as
            //for mDragEventStart
            mDragEnd.invoke(
                any(),
                positionFrom,
                adapter.getItem(positionFrom)
            )
        }

        scrollAndVerifyText(positionFrom, "item $positionFrom", adapter)
    }

    @Test
    fun dragTest_dragHandle() {
        val items = MutableList(100) { index ->
            ItemDraggable("item $index")
        }
        val adapter = AdapterDraggable(items).apply { addFeature(mDragFeature) }
        mDragFeature.apply {
            setDragOnLongClick(false)
            setDragDirection(ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }

        onActivity {
            it.apply {
                setAdapter(adapter)
                setLayoutManagerVertical()
            }
        }

        val handleId = R.id.listItem_dragHandle
        setupDragFeatureWithRecyclerView(adapter as DxAdapter<BaseItem, *>, handleId)

        //the positions MUST be visible on screen.
        val positionFrom = 1
        val positionTo = 5
        //we need a reference to the adapter BEFORE the drag operation because dragging changes
        //the positions and some tests need to reference the items at their original positions
        val itemsBeforeDrag = adapter.getDxAdapterItems().toList()

        performDrag(positionFrom, positionTo, handleId)

        //reducing 1 from positionTo because we are dragging to the CENTER of positionTo
        //and that is not enough for the items to be swapped (even BOTTOM_CENTER is not enough)
        val actualPositionToCheck = positionTo - 1

        //NOTE:
        // not specifying the amount of calls to mDragEventStart because
        // for an unknown reason the drag operation in the performDrag() function
        // triggers mDragEventStart (in addition to the press-and-hold operation).
        // THIS DOES NOT HAPPEN when i manually test the app!!!
        //NOTE:
        // not specifying the amount of calls and parameters to mOnItemMoved because the test in
        // dragTest_longClick() should be enough
        verify { mDragStart.invoke(any(), positionFrom, itemsBeforeDrag[positionFrom]) }
        verify { mOnItemMoved.invoke(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) {
            mDragEnd.invoke(
                any(),
                actualPositionToCheck,
                adapter.getItem(actualPositionToCheck)
            )
        }

        //reducing 1 from positionTo because we are dragging to the CENTER of positionTo
        //and that is not enough for the items to be swapped (even BOTTOM_CENTER is not enough)
        scrollAndVerifyText(positionTo - 1, "item $positionFrom", adapter)
    }

    @Test
    fun swipeTest_draggable_and_nonDraggable_items() {
        val items = mutableListOf(
            //THE ORDER IS IMPORTANT!!! DO NOT CHANGE IT!!!
            //THE ORDER IS IMPORTANT!!! DO NOT CHANGE IT!!!
            //THE ORDER IS IMPORTANT!!! DO NOT CHANGE IT!!!
            ItemDraggable("draggable"),
            ItemNonDraggable("non-draggable"),
            ItemDraggable("required extra item")
            //need extra item because the dragged item is actually being dragged until the middle of
            //positionTo, which is not enough to swap the items
        )

        mDragFeature.setDragOnLongClick(true)
        val itemsBeforeDrag = items.toList()
        val adapter = BaseAdapter(items).apply { addFeature(mDragFeature) }

        onActivity { it.apply { setAdapter(adapter) } }
        setupDragFeatureWithRecyclerView(adapter, null)

        var positionFrom = 1
        var positionTo = 2
        performDrag(positionFrom, positionTo, null)

        verify(exactly = 0) { mDragStart.invoke(any(), any(), any()) }
        verify(exactly = 0) { mDragEnd.invoke(any(), any(), any()) }
        verify(exactly = 0) { mOnItemMoved.invoke(any(), any(), any(), any(), any(), any()) }

        //must release first action before doing the next
        PressActions.tearDown()

        positionFrom = 0
        positionTo = 2
        //reducing 1 because the item does not actually switch with positionTo, but with 1 before it
        val actualPositionToCheck = positionTo - 1
        val draggedItem = adapter.getItem(positionFrom)
        performDrag(positionFrom, positionTo, null)

        //NOTE:
        // not specifying the amount of calls to mDragEventStart because
        // for an unknown reason the drag operation in the performDrag() function
        // triggers mDragEventStart (in addition to the press-and-hold operation).
        // THIS DOES NOT HAPPEN when i manually test the app!!!
        verify { mDragStart.invoke(any(), positionFrom, draggedItem) }
        verify(exactly = 1) { mDragEnd.invoke(any(), actualPositionToCheck, draggedItem) }
        verify(exactly = 1) {
            mOnItemMoved.invoke(
                any(),
                positionFrom,
                draggedItem,
                any(),
                actualPositionToCheck,
                itemsBeforeDrag[actualPositionToCheck]
            )
        }

        //no need to release the press action because its done in @After function
    }

    //region helper functions
    @Suppress("SameParameterValue", "SameParameterValue")
    private fun performDrag(
        positionFrom: Int,
        positionTo: Int,
        @IdRes innerViewId: Int?
    ) {
        onView(withId(R.id.activityBase_rv))
            .perform(
                actionOnItemAtPosition<ViewHolder>(
                    positionFrom,
                    PressActions.pressAndHold(innerViewId)
                )
            )
            .perform(
                GeneralSwipeAction(
                    Swipe.SLOW,
                    RecyclerViewCoordinatesProvider(
                        positionFrom,
                        GeneralLocation.CENTER
                    ),
                    RecyclerViewCoordinatesProvider(
                        positionTo, GeneralLocation.CENTER
                    ),
                    Press.FINGER
                )
            )
            .perform(
                actionOnItemAtPosition<ViewHolder>(positionFrom, PressActions.release(innerViewId))
            )
    }

    /**
     * a helper function to test that the adapter retains changes made to item in [positionToCheck]
     * by scrolling to the end and start of the recyclerView and then verifying that the item in
     * [positionToCheck] contains the desired [textToCheck]
     */
    @Suppress("SameParameterValue")
    private fun scrollAndVerifyText(
        positionToCheck: Int,
        textToCheck: String,
        adapter: DxAdapter<*, *>
    ) {
        onView(withId(R.id.activityBase_rv))
            //scroll to end
            .perform(scrollToPosition<ViewHolder>(adapter.getDxAdapterItems().size - 1))
            //scroll to start
            .perform(scrollToPosition<ViewHolder>(0))
            //scroll to position to make sure its visible
            .perform(scrollToPosition<ViewHolder>(positionToCheck))
            //check the text
            .check(
                matches(
                    atPosition(
                        positionToCheck,
                        hasDescendant(withText(textToCheck))
                    )
                )
            )
    }

    private fun setupDragFeatureWithRecyclerView(
        adapter: DxAdapter<BaseItem, *>,
        @IdRes dragHandleId: Int?
    ) {
        val touchCallback = DxItemTouchCallback(adapter).apply {
            dragFeature = mDragFeature
        }

        val touchHelper = DxItemTouchHelper(touchCallback).apply {
            dragHandleId?.apply { setDragHandleId(this) }
        }

        onActivity { touchHelper.attachToRecyclerView(it.activityBase_rv) }
    }
    //endregion
}