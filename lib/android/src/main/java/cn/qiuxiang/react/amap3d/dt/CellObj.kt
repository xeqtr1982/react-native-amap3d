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
    var cell_objects = HashMap<String, Marker>()

    //val keys: Array<String> = arrayOf("141_23820061", "129_23866395", "142_23820062", "130_23866396", "129_23866395", "131_23866397")

    fun clearRenderMaps() {
        renderMaps.clear()
    }


    /**
     *
     * @param map
     * @param cellObject
     * @param size LTE宏站尺寸，GSM=LTE*0.7,INNER=LTE*0.5
     */
    fun getMarker(map: AMap, cellObject: JsonObject?, size: Int): Marker? {
        if (cellObject == null)
            return null
        if (cellObject["COVER_TYPE"].isJsonNull || cellObject["NET_NAME"].isJsonNull)
        //|| cellObject["LAT"].isJsonNull || cellObject["LON"].isJsonNull)
            return null
        val siteType = cellObject["COVER_TYPE"].asString
        val netWork = cellObject["NET_NAME"].asString

        if (siteType.isNullOrEmpty())
            return null
        val auchorY = when (siteType == "室内") {
            true -> 0.5f
            false -> 1.0f
        }
        val azimuth = when (cellObject["ANTENNA_ANGLE"].isJsonNull) {
            true -> 0F
            false -> cellObject["ANTENNA_ANGLE"].asFloat
        }
        val rotateAngle: Float = when (siteType == "室内") {
            true -> 0.0f
            false -> 360 - azimuth
        }

        val bitmapDescriptor = getOriginalBitmapDescriptor(siteType, netWork, size)

        val wgslat = cellObject["LAT"].asDouble
        val wgslon = cellObject["LON"].asDouble

        val gcj_latlon = BetrayLatLng.gcj_encrypt(wgslat, wgslon)

        val marker = map.addMarker(MarkerOptions()
                .setFlat(true)
                .icon(bitmapDescriptor)
                .alpha(1f)
                .draggable(false)
                .position(LatLng(gcj_latlon[0], gcj_latlon[1]))
                .anchor(0.5f, auchorY)
                .infoWindowEnable(false)
                .title(cellObject["CELL_NAME"].asString)
                .rotateAngle(rotateAngle)
                //.zIndex(0f)
        )

        val data = ExtraData(cellObject["CGI_TCI"].asString, MapElementType.mark_Cell.value, size, cellObject)
        marker?.`object` = data


        val key = when (netWork) {
            "LTE" -> cellObject["CELL_ID"].asString // cellObject["SITE_ID"].asString + "_" + cellObject["CELL_ID"].asString
            else -> cellObject["CGI_TCI"].asString  //"GSM"
        }
        cell_objects.put(key, marker)

        return marker
    }

    fun getSelectBitmapDescriptor(siteType: String, netWork: String, size: Int): BitmapDescriptor? {
        val key = netWork + "_" + siteType + "_selected"

        if (!renderMaps.containsKey(key)) {
            val cellStyle = ObjRender.CELL_COLOR
            val cellPath = when (siteType == "室内") {
                true -> ObjRender.CELL_GSM_INNER_PATH
                false -> ObjRender.CELL_OUT_PATH_30
            }
            var drawSize = (size * 1.5).toInt()
            var scale = drawSize * 1.0f / cellPath.height
            if (siteType == "室内") {
                scale = scale * 0.7f
                drawSize = (drawSize * 0.7).toInt()
            }

            val paint = Paint()
            paint.color = cellStyle.color
            paint.alpha = 100

            val paintStroke = Paint()
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = Color.rgb(255, 20, 147)
            paintStroke.strokeWidth = 4f

            val path1 = PathParser().createPathFromPathData(cellPath.pathData)
            val path = Path()
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            path.addPath(path1, matrix)

            val bitmap = Bitmap.createBitmap(
                    drawSize, drawSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawPath(path, paint)//填充图形
            canvas.drawPath(path, paintStroke)//外边框
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            if (bitmapDescriptor != null) renderMaps.put(key, bitmapDescriptor)
        }
        return renderMaps[key]
    }

    fun getOriginalBitmapDescriptor(siteType: String, netWork: String, size: Int): BitmapDescriptor? {
        val key = netWork + "_" + siteType

        if (!renderMaps.containsKey(key)) {
            val cellStyle = ObjRender.CELL_COLOR
            val cellPath = when (siteType == "室内") {
                true -> ObjRender.CELL_GSM_INNER_PATH
                false -> ObjRender.CELL_OUT_PATH_30
            }
            var drawSize = size
            var scale = size * 1.0f / cellPath.height
            if (siteType == "室内") {
                scale = scale * 0.5f
                drawSize = size / 2
            } else if (netWork == "LTE") {
                scale = scale * 0.75f
                drawSize = 3 * size / 4
            }

            val paint = Paint()
            paint.color = cellStyle.color
            paint.alpha = 100

            val paintStroke = Paint()
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = cellStyle.strokeColor
            paintStroke.strokeWidth = 2f
            //paintStroke.setPathEffect()

            val path1 = PathParser().createPathFromPathData(cellPath.pathData)
            val path = Path()
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            path.addPath(path1, matrix)

            val bitmap = Bitmap.createBitmap(
                    drawSize, drawSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawPath(path, paint)//填充图形
            canvas.drawPath(path, paintStroke)//外边框
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            if (bitmapDescriptor != null) renderMaps.put(key, bitmapDescriptor)
        }
        return renderMaps[key]
    }

    private fun getOutCellBitmapDescriptor(siteType: String, netWork: String, size: Int): BitmapDescriptor? {
        val key = netWork + "_" + siteType
        if (!renderMaps.containsKey(key)) {
            val cellStyle = ObjRender.CELL_STYLE_LTE
            val cellPath = when (siteType == "室内") {
                true -> ObjRender.CELL_GSM_INNER_PATH
                false -> ObjRender.CELL_LTE_OUT_PATH
            }

            var scale = size * 1.0f / cellPath.height
//            if (siteType == "室内")
//                scale = scale / 2f

            val paint = Paint()
            paint.color = cellStyle.color

            val paintStroke = Paint()
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = cellStyle.strokeColor
            //paintStroke.setPathEffect()

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

    private fun getInnerCellBitmapDescriptor(siteType: String, netWork: String, size: Int): BitmapDescriptor? {
        val key = netWork + "_" + siteType
        if (!renderMaps.containsKey(key)) {
            val cellStyle = ObjRender.CELL_STYLE_LTE
            val cellPath = when (siteType == "室内") {
                true -> ObjRender.CELL_GSM_INNER_PATH
                false -> ObjRender.CELL_LTE_OUT_PATH
            }

            var scale = size * 1.0f / cellPath.height
//            if (siteType == "室内")
//                scale = scale / 2f

            val paint = Paint()
            paint.color = cellStyle.color

            val paintStroke = Paint()
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = cellStyle.strokeColor
            //paintStroke.setPathEffect()

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


    /**
     * 获取相近位置的所有对象
     *  @param markers 查询列表
     *  @param lat
     *  @param lng
     *  @param angle 方向角
     *  @param radius 查询半径，默认值 0.00005
     *  @param radius_angle 方向角容差，默认值 5
     */
    fun getMarkers(markers: MutableList<Marker>?, lat: Double, lng: Double, angle: Float, radius: Double = 0.00005, radius_angle: Float = 5F): MutableList<Marker> {

        val list: MutableList<Marker> = mutableListOf()
        markers?.let {
            val t_radius = radius * 3
            for (marker in it) {
                if (Math.abs(marker.position.latitude - lat) < t_radius && Math.abs(marker.position.longitude - lng) < t_radius) {
                    val dis = GeoUtils.distance(lat, lng, marker.position.latitude, marker.position.longitude)
                    if (dis < radius)
                        list.add(marker)
                }
            }
            val count = list.count()
            if (count > 0) {
                if (count <= 3) {
                    list.removeAll { Math.abs(it.rotateAngle - angle) > radius_angle }
                } else {
                    //同站小区多余3个，返回全部列表
                }
            }
        }

        return list
    }

    /**
     * 获取范围内所有对象
     */
    fun getMarkers(markers: MutableList<Marker>?, min_lat: Double, min_lng: Double, max_lat: Double, max_lng: Double): MutableList<Marker> {
        //return markers.filter { }

        val list: MutableList<Marker> = mutableListOf()
        if (markers != null && markers.count() > 0) {
            for (marker in markers) {
                if (marker.position.latitude > min_lat && marker.position.latitude < max_lat && marker.position.longitude > min_lng && marker.position.longitude < max_lng) {
                    list.add(marker)
                }
            }
        }
        return list
    }

}