package cn.qiuxiang.react.amap3d.maps

import android.content.Context
import android.view.View
import cn.qiuxiang.react.amap3d.toLatLng
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

@Suppress("unused")
internal class AMapTextManager :ViewGroupManager<AMapText>(){

    override fun getName(): String {
        return "AMapText"
    }
    override fun createViewInstance(reactContext: ThemedReactContext?): AMapText {
        return  AMapText(reactContext as Context)
    }


    @ReactProp(name = "text")
    fun setText(marker: AMapText, text: String) {
        marker.text = text
    }

    @ReactProp(name = "coordinate")
    fun setCoordinate(marker: AMapText, coordinate: ReadableMap) {
        marker.position = coordinate.toLatLng()
    }

    @ReactProp(name = "fontColor")
    fun setFontColor(marker: AMapText, fontColor: Int) {
        marker.fontColor = fontColor
    }

    @ReactProp(name = "fontSize")
    fun setFontSize(marker: AMapText, fontSize: Int) {
        marker.fontSize = fontSize
    }

    @ReactProp(name = "visible")
    fun setFontSize(marker: AMapText, visible: Boolean) {
        marker.visible = visible
    }

    @ReactProp(name = "zIndex")
    fun setZIndez(marker: AMapText, zIndex: Float) {
        marker.zIndex = zIndex
    }

    @ReactProp(name = "align")
    fun setAlign(marker: AMapText, align: ReadableMap) {
        marker.setAlign(align.getInt("x"), align.getInt("y"))
    }

    @ReactProp(name = "elementKey")
    fun setKey(marker: AMapText, elementKey: String) {
        marker.elementKey = elementKey
    }

    @ReactProp(name = "elementType")
    fun setElementType(marker: AMapText, elementType: Int) {
        marker.elementType = elementType
    }

}