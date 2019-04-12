package cn.qiuxiang.react.amap3d.dt

import android.graphics.*
import cn.qiuxiang.react.amap3d.maps.ExtraData
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.google.gson.JsonObject

/**
 * Created by lee on 2019/3/19.
 */
object OrderObj {
    private val renderMaps = HashMap<String, BitmapDescriptor>()

    fun clearRenderMaps() {
        renderMaps.clear()
    }

    fun getMarker(map: AMap, orderObject: JsonObject?, size: Int): Marker? {
        if (orderObject == null)
            return null

        val wgslat = orderObject["LAT"].asDouble
        val wgslon = orderObject["LON"].asDouble

        val gcj_latlon = BetrayLatLng.gcj_encrypt(wgslat, wgslon)

        val bitmapDescriptor = getOrderBitmapDescriptor(orderObject, size)
        val marker = map.addMarker(MarkerOptions()
                .setFlat(false)
                .icon(bitmapDescriptor)
                .alpha(1f)
                .draggable(false)
                .position(LatLng(gcj_latlon[0], gcj_latlon[1]))
                .anchor(0.5f, 0.5f)
                .infoWindowEnable(true)
                //.title(cellObject["CELLID"].asString)
                //.rotateAngle(rotateAngle)
                .zIndex(0f)
        )

        val data = ExtraData(orderObject["id"].asString, MapElementType.mark_Order.value, orderObject)
        marker?.`object` = data

        return marker
    }

    private fun getOrderBitmapDescriptor(orderObject: JsonObject, size: Int): BitmapDescriptor? {
        val web_color: String = orderObject["color_fill"].asString
        val color: Int = Color.parseColor(web_color)
        val str_color = color.toString()
        if (!renderMaps.containsKey(str_color)) {
            val fill_color = Color.parseColor(orderObject["color_out"].asString)
            val bitmapDescriptor = createArcBitmapDescriptor(size, fill_color, color)
            renderMaps.put(str_color, bitmapDescriptor)
        }
        return renderMaps[str_color]
    }

    internal fun createArcBitmapDescriptor(width: Int, fillColor: Int, strokeColor: Int): BitmapDescriptor {
        val scale = width * 1.0f / ObjRender.ORDER_INNER_PATH.width

        val paint_fill = Paint()
        paint_fill.color = fillColor

        val paint_stroke = Paint()
        //paint_stroke.setStyle(Paint.Style.STROKE);
        paint_stroke.color = strokeColor

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val path1 = PathParser().createPathFromPathData(ObjRender.ORDER_INNER_PATH.pathData)
        val path_fill = Path()
        path_fill.addPath(path1, matrix)

        val path2 = PathParser().createPathFromPathData(ObjRender.ORDER_OUT_PATH.pathData)
        val path_stroke = Path()
        path_stroke.addPath(path2, matrix)

        val bitmap = Bitmap.createBitmap(
                width, width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawPath(path_fill, paint_fill)
        canvas.drawPath(path_stroke, paint_stroke)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }
}