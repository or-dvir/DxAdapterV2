package com.hotmail.or_dvir.dxlibraries

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.hotmail.or_dvir.dxadapter.DxAdapter
import com.hotmail.or_dvir.dxdragandswipe.DxItemTouchCallback
import com.hotmail.or_dvir.dxdragandswipe.OnItemSwipedListener
import com.hotmail.or_dvir.dxdragandswipe.OnSwipeEventListener
import com.hotmail.or_dvir.dxdragandswipe.swipe.DxFeatureSwipe
import com.hotmail.or_dvir.dxlibraries.swipeable.AdapterNonSwipeable
import com.hotmail.or_dvir.dxlibraries.swipeable.AdapterSwipeable
import com.hotmail.or_dvir.dxlibraries.swipeable.ItemNonSwipeable
import com.hotmail.or_dvir.dxlibraries.swipeable.ItemSwipeable
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class TestFeatureSwipe : BaseTest() {

    //NOTE:
    //the following features are simple variables to return to google's default
    //implementation (in other words: there is no custom logic behind them),
    //and therefore do not need to be tested.
    // swipeThreshold.
    // swipeEscapeVelocity.
    // swipeEscapeVelocityMultiplier.

    //IMPORTANT NOTE:
    //do to the way ItemTouchCallback works, you MUST do something with the item once its swiped!
    //(e.g. remove, reset). if you don't, listeners will be called for wrong items.
    //therefore each test function should define the listener on its own
    private lateinit var mSwipeStart: OnSwipeEventListener<BaseItem>
    private lateinit var mSwipeEnd: OnSwipeEventListener<BaseItem>
    private lateinit var mSwipeFeature: DxFeatureSwipe<BaseItem>

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    @Before
    fun before() {
        mSwipeStart = spyk({ view, position, item -> })
        mSwipeEnd = spyk({ view, position, item -> })

        mSwipeFeature = DxFeatureSwipe(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, //may be overridden later
            mSwipeStart,
            mSwipeEnd,
            { _, _, _, _ ->
                //this will be overridden in each test function, but must be supplied here
            }
        )
    }

    @After
    fun after() {
        PressActions.tearDown()
    }

    @Test
    fun swipeTest() {
        val items = MutableList(100) { index ->
            ItemSwipeable("item $index")
        }
        val adapter = AdapterSwipeable(items).apply { addFeature(mSwipeFeature) }
        val swipeListener = getResetItemSwipeListener(adapter)

        onActivity { it.apply { setAdapter(adapter) } }
        setupSwipeFeatureWithRecyclerView(adapter)

        val swipePosition = 1
        val swipeItem = adapter.getItem(swipePosition)
        performSwipe(swipePosition, GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT)

        verify(exactly = 1) { mSwipeStart.invoke(any(), swipePosition, swipeItem) }
        verify(exactly = 1) { mSwipeEnd.invoke(any(), swipePosition, swipeItem) }
        verify(exactly = 1) { swipeListener.invoke(any(), swipePosition, ItemTouchHelper.RIGHT, swipeItem) }
    }

    @Test
    fun swipeTest_swipeDisabled() {
        val items = MutableList(100) { index ->
            ItemSwipeable("item $index")
        }
        val adapter = AdapterSwipeable(items).apply { addFeature(mSwipeFeature) }
        val swipeListener = getResetItemSwipeListener(adapter)
        mSwipeFeature.isSwipeEnabled = false


        onActivity { it.apply { setAdapter(adapter) } }
        setupSwipeFeatureWithRecyclerView(adapter)

        performSwipe(1, GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT)

        verify(exactly = 0) { mSwipeStart.invoke(any(), any(), any()) }
        verify(exactly = 0) { mSwipeEnd.invoke(any(), any(), any()) }
        verify(exactly = 0) { swipeListener.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun swipeTest_nonSwipeable() {
        val items = MutableList(100) { index ->
            ItemNonSwipeable("item $index")
        }
        val adapter = AdapterNonSwipeable(items).apply { addFeature(mSwipeFeature) }
        val swipeListener = getResetItemSwipeListener(adapter)

        onActivity { it.apply { setAdapter(adapter) } }
        setupSwipeFeatureWithRecyclerView(adapter)

        performSwipe(1, GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT)

        verify(exactly = 0) { mSwipeStart.invoke(any(), any(), any()) }
        verify(exactly = 0) { mSwipeEnd.invoke(any(), any(), any()) }
        verify(exactly = 0) { swipeListener.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun swipeTest_wrongDirection() {
        val items = MutableList(100) { index ->
            ItemSwipeable("item $index")
        }
        val adapter = AdapterSwipeable(items).apply { addFeature(mSwipeFeature) }
        val swipeListener = getResetItemSwipeListener(adapter)
        mSwipeFeature.setSwipeDirections(ItemTouchHelper.RIGHT)

        onActivity { it.apply { setAdapter(adapter) } }
        setupSwipeFeatureWithRecyclerView(adapter)

        //swiping in the wrong direction
        performSwipe(1, GeneralLocation.CENTER_RIGHT, GeneralLocation.CENTER_LEFT)

        verify(exactly = 0) { mSwipeStart.invoke(any(), any(), any()) }
        verify(exactly = 0) { mSwipeEnd.invoke(any(), any(), any()) }
        verify(exactly = 0) { swipeListener.invoke(any(), any(), any(), any()) }
    }

    //region helper functions
    @Suppress("SameParameterValue")
    private fun performSwipe(
        position: Int,
        locationFrom: GeneralLocation,
        locationTo: GeneralLocation
    ) {
        onView(ViewMatchers.withId(R.id.activityMain_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    PressActions.pressAndHold(null)
                )
            )
            .perform(
                GeneralSwipeAction(
                    Swipe.SLOW,
                    RecyclerViewCoordinatesProvider(
                        position,
                        locationFrom
                    ),
                    RecyclerViewCoordinatesProvider(
                        position,
                        locationTo
                    ),
                    Press.FINGER
                )
            )
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    PressActions.release(null)
                )
            )
    }

    private fun <ITEM: BaseItem> getResetItemSwipeListener(adapter: DxAdapter<ITEM, *>): OnItemSwipedListener<BaseItem> {
        val listener: OnItemSwipedListener<BaseItem> = spyk({ view, adapterPosition, direction, item ->
            adapter.notifyItemChanged(adapterPosition)
        })

        mSwipeFeature.setOnItemSwipedListener(listener)

        return listener
    }

    private fun <ITEM: BaseItem> setupSwipeFeatureWithRecyclerView(adapter: DxAdapter<ITEM, *>) {
        val touchCallback = DxItemTouchCallback(adapter).apply {
            swipeFeature = mSwipeFeature
        }

        onActivity { ItemTouchHelper(touchCallback).attachToRecyclerView(it.activityMain_rv) }
    }
    //endregion

    //todo
    // how do i test backgrounds? the drawing is done on a canvas and not the item itself so
    // i cannot use hasBackground(). do i have access to the view behind the item?
    // can i test that a color appears anywhere on screen????
}