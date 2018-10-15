package cn.qiuxiang.react.amap3d.maps

import android.content.Context
import android.graphics.Color
import android.graphics.Canvas
import android.view.View
import cn.qiuxiang.react.amap3d.toPx
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.views.view.ReactViewGroup

class AMapText(context: Context) : ReactViewGroup(context), AMapOverlay {

    private var alignX: Int = 0
    private var alignY: Int = 0

    var textMarker: Text? = null
        private set
    var position: LatLng? = null
        set(value) {
            field = value
            textMarker?.position = value
        }

    var text = ""
        set(value) {
            field = value
            textMarker?.text = value
        }

    var fontColor: Int = Color.BLACK
        set(value) {
            field = value
            textMarker?.fontColor = value
        }

    var fontSize: Int = 12
        set(value) {
            field = value
            textMarker?.fontSize = value
        }

    var visible: Boolean = true
        set(value) {
            field = value
            textMarker?.isVisible = value
        }

    var zIndex: Float = 0.0f
        set(value) {
            field = value
            textMarker?.zIndex = value
        }

    var elementKey = ""
    var elementType: Int = 0

//    override fun addView(child: View?, index: Int) {
//        super.addView(child, index)
//    }

    override fun add(map: AMap) {
        textMarker = map.addText(TextOptions().position(position).text(text).fontColor(fontColor).fontSize(fontSize).backgroundColor(0X00FFFFFF)
                .zIndex(zIndex))
    }

    override fun remove() {
        textMarker?.destroy()
    }

    fun setAlign(x: Int, y: Int) {
        alignX = x
        alignY = y
        textMarker?.setAlign(x, y)
    }

//    fun lockToScreen(args: ReadableArray?) {
//        if (args != null) {
//            val x = args.getDouble(0).toFloat().toPx()
//            val y = args.getDouble(1).toFloat().toPx()
//            textMarker?.setPositionByPixels(x, y)
//        }
//    }
}