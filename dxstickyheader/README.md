# DxStickyHeader
This module adds sticky headers to your  adapter.

For a complete example, please see the sample app.

## Import
in your `build.gradle` file, add:

```
repositories {
    //if already added, skip this one
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.or-dvir.DxAdapterV2:dxstickyheader:<latest release>'
}
```

## Your Adapter and RecyclerView
1. Extend the class `DxFeatureStickyHeader<>`.
2. Pass it to `DxStickyHeaderItemDecoration()`.
3. Add `DxStickyHeaderItemDecoration()` to your RecyclerView.

```
val featureHeader = MyStickyHeaderFeature(...)
adapter.addFeature(featureHeader)

val decoration = DxStickyHeaderItemDecoration(featureHeader)
recyclerView.addItemDecoration(decoration)
```

## Your Items
1. Create a layout for your header.
2. Your item must implement the interface `IDxItemHeader`.

## Dependencies Exposed to User
None.

## Depends On
* [DxAdapter](https://github.com/or-dvir/DxAdapterV2/tree/master/dxadapter)
