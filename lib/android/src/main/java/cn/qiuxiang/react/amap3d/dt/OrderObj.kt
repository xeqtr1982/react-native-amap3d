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

        val bitmapDescriptor = getOriginalBitmapDescriptor(orderObject, size)
        val marker = map.addMarker(MarkerOptions()
                .setFlat(false)
                .icon(bitmapDescriptor)
                .alpha(1f)
                .draggable(false)
                .position(LatLng(gcj_latlon[0], gcj_latlon[1]))
                .anchor(0.5f, 1.0f)
                //.infoWindowEnable(true)
                //.title(orderObject["id"].asString)
                //.rotateAngle(rotateAngle)
                .zIndex(10f)
        )

        val data = ExtraData(orderObject["id"].asString, MapElementType.mark_Order.value,size, orderObject)
        marker?.`object` = data

        return marker
    }

    /**
     * 获取相近位置的所有工单
     *  @param markers 查询列表
     *  @param lat
     *  @param lng
     *  @param radius 容差半径
     */
    fun getMarkers(markers: MutableList<Marker>?, lat: Double, lng: Double, radius: Double = 0.0001): MutableList<Marker> {
        val list: MutableList<Marker> = mutableListOf()
        markers?.let {
            for (marker in it) {
                val dis = GeoUtils.distance(lat, lng, marker.position.latitude, marker.position.longitude)
                if (dis <= radius)
                    list.add(marker)
            }
        }
        return list
    }

    fun getSelectBitmapDescriptor(orderObject: JsonObject, size: Int): BitmapDescriptor? {
        val fill_color: Int = Color.parseColor(orderObject["color_fill"].asString)
        val stroke_color: Int = Color.parseColor(orderObject["color_stroke"].asString)
        val str_fill_color = fill_color.toString()
        val str_stroke_color = stroke_color.toString()
        val key = str_fill_color + "_" + str_stroke_color + "_selected"
        if (!renderMaps.containsKey(key)) {
            val bitmapDescriptor = createArcBitmapDescriptor(size, fill_color, stroke_color)
            renderMaps.put(key, bitmapDescriptor)
        }
        return renderMaps[key]
    }

    fun getOriginalBitmapDescriptor(orderObject: JsonObject, size: Int): BitmapDescriptor? {
//        val web_fill_color: String = orderObject["color_fill"].asString
//        val web_stroke_color:String=orderObject["color_stroke"].asString
        val fill_color: Int = Color.parseColor(orderObject["color_fill"].asString)
        val stroke_color: Int = Color.parseColor(orderObject["color_stroke"].asString)
        val str_fill_color = fill_color.toString()
        val str_stroke_color = stroke_color.toString()
        val key = str_fill_color + "_" + str_stroke_color
        if (!renderMaps.containsKey(key)) {
            val bitmapDescriptor = createArcBitmapDescriptor(size, fill_color, stroke_color)
            renderMaps.put(key, bitmapDescriptor)
        }
        return renderMaps[key]
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