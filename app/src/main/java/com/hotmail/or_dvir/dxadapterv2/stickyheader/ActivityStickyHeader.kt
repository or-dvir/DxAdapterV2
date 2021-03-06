package com.hotmail.or_dvir.dxadapterv2.stickyheader

import android.os.Bundle
import com.hotmail.or_dvir.dxadapterv2.BaseFeatureActivity
import com.hotmail.or_dvir.dxadapterv2.BaseItem
import com.hotmail.or_dvir.dxstickyheader.DxStickyHeaderItemDecoration
import kotlinx.android.synthetic.main.activity_base.*

class ActivityStickyHeader : BaseFeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = mutableListOf<BaseItem>()

        var headerCounter = 1
        for (i in 1..100) {
            if (i % 10 == 0) {
                items.add(ItemHeader("Header $headerCounter"))
                headerCounter++
            } else {
                items.add(BaseItem("item $i"))
            }
        }

        val adapter = AdapterStickyHeader(items)
        setAdapter(adapter)

        val featureHeader = MyStickyHeaderFeature(adapter)
        adapter.addFeature(featureHeader)
        activityBase_rv.addItemDecoration(DxStickyHeaderItemDecoration(featureHeader))
    }
}