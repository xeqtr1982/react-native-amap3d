package cn.qiuxiang.react.amap3d.dt

import android.graphics.*
import cn.qiuxiang.react.amap3d.maps.ExtraData
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.google.gson.JsonObject

/**
 * Created by lee on 2019/3/19.
 */
object CellObj {
    private val renderMaps = HashMap<String, BitmapDescriptor>()
    //private val cellRender = ObjRender()

    fun clearRenderMaps() {
        renderMaps.clear()
    }

    fun getMarker(map: AMap, cellObject: JsonObject?, size: Int): Marker? {
        if (cellObject == null)
            return null
        val siteType = cellObject["SITETYPE"].asString //2--inner,1--outer

        if (siteType.isNullOrEmpty())
            return null
        val auchorY = when (siteType == "2") {
            true -> 0.5f
            false -> 1.0f
        }
        val azimuth = cellObject["AZIMUTH"].asFloat
        val rotateAngle: Float = when (siteType == "2") {
            true -> 0.0f
            false -> 360 - azimuth
        }
        val wgslat = cellObject["LAT"].asDouble
        val wgslon = cellObject["LON"].asDouble

        val gcj_latlon = BetrayLatLng.gcj_encrypt(wgslat, wgslon)

        val bitmapDescriptor = getCellBitmapDescriptor(siteType, "LTE", size)
        val marker = map.addMarker(MarkerOptions()
                .setFlat(true)
                .icon(bitmapDescriptor)
                .alpha(1f)
                .draggable(false)
                .position(LatLng(gcj_latlon[0], gcj_latlon[1]))
                .anchor(0.5f, auchorY)
                .infoWindowEnable(true)
                .title(cellObject["CELLID"].asString)
                .rotateAngle(rotateAngle)
                //.zIndex(0f)
        )

        val data = ExtraData(cellObject["CELLID"].asString, MapElementType.mark_Cell.value, cellObject)
        marker?.`object` = data

        return marker
    }

    /**
     * 获取相近位置的所有工单
     *  @param markers 查询列表
     *  @param lat
     *  @param lng
     *  @param angle 方向角
     *  @param radius 查询半径，默认值 0.00005
     *  @param radius_angle 方向角容差，默认值 5
     */
    fun getMarkers(markers: MutableList<Marker>, lat: Double, lng: Double, angle: Float, radius: Double = 0.00005, radius_angle: Float = 5F): MutableList<Marker> {

        val list: MutableList<Marker> = mutableListOf()
        for (marker in markers) {
            val dis = GeoUtils.distance(lat, lng, marker.position.latitude, marker.position.longitude)
            if (dis > radius)
                continue
            if (Math.abs(marker.rotateAngle - angle) < radius_angle)
                list.add(marker)
        }
        return list
    }

    private fun getCellBitmapDescriptor(siteType: String, netWork: String, size: Int): BitmapDescriptor? {
        val key = netWork + "_" + siteType
        if (!renderMaps.containsKey(key)) {
            val cellStyle = ObjRender.CELL_STYLE_LTE
            val cellPath = when (siteType == "2") {true -> ObjRender.CELL_INNER_PATH_ADJUST
                false -> ObjRender.CELL_OUT_PATH_30
            }

            val scale = size * 1.0f / cellPath.height

            val paint = Paint()
            paint.color = cellStyle.color

            val paintStroke = Paint()
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = cellStyle.strokeColor

            val path1 = PathParser().createPathFromPathData(cellPath.pathData)
            val path = Path()
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            path.addPath(path1, matrix)

            val bitmap = Bitmap.createBitmap(
                    size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawPath(path, paint)//填充图形
            canvas.drawPath(path, paintStroke)//外边框
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            if (bitmapDescriptor != null) renderMaps.put(key, bitmapDescriptor)
        }
        return renderMaps[key]
    }
}