package cn.qiuxiang.react.amap3d.dt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import cn.qiuxiang.react.amap3d.maps.ExtraData
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Created by lee on 2019/3/19.
 */
object ParamObj {
    val renderMaps = HashMap<String, BitmapDescriptor>()
    fun clearRenderMaps() {
        renderMaps.clear()
    }

    fun getMarker(map: AMap, testPoint: JsonObject?, size: Int, last: Marker?): Marker? {
        if (testPoint != null) {
            val lat = testPoint["GCJ_LAT"].asDouble
            val lon = testPoint["GCJ_LON"].asDouble
            val value = testPoint[testPoint["KeyField"].asString]
            if (isSameWithLast(last, lat, lon, value)) return null
            Log.i("ReactNativeJS","native add new marker")
            val bitmapDescriptor = getBitmapDescriptorByValue(testPoint, size)
            val title = testPoint["KeyField"].asString + "：" + when (value.isJsonNull) {
                true -> ""
                false -> value.toString()
            }

            val marker = map.addMarker(MarkerOptions()
                    .setFlat(false)
                    .icon(bitmapDescriptor)
                    .alpha(1f)
                    .draggable(false)
                    .position(LatLng(lat, lon))
                    .anchor(0.5f, 0.5f)
                    //.infoWindowEnable(true)
                    .title(title)
                    //.snippet("")
                    .zIndex(0.0f)
            )

            //MapElementType.valueOf("mark_Cell")
            //    MapElementType.mark_GPS.name
            //    MapElementType.mark_Event.ordinal
            val key = testPoint["frameNum"].asString
            val data = ExtraData(key, MapElementType.mark_GPS.value, size, testPoint)
            marker?.`object` = data
            marker.isClickable = true

            return marker
        } else
            return null
    }

    /**
     * 改变前一个对象关键属性marker,返回null，如last=null，则新建并返回一个marker
     */
    fun changeMarker(map: AMap, testPoint: JsonObject?, size: Int, last: Marker?): Marker? {
        if (testPoint != null) {
            val lat = testPoint["GCJ_LAT"].asDouble
            val lon = testPoint["GCJ_LON"].asDouble
            val value = testPoint[testPoint["KeyField"].asString]
            val title = testPoint["KeyField"].asString + "：" + when (value.isJsonNull) {
                true -> ""
                false -> value.toString()
            }

            when (isSameAsLast(last, lat, lon, value)) {
                ChangeType.KeyValueChange -> {
                    last?.let {
                        val bitmapDescriptor = getBitmapDescriptorByValue(testPoint, size)
                        if (bitmapDescriptor != it.icons[0])
                            it.setIcon(bitmapDescriptor)
                        it.title = title
                    }

                    return null
                }
                ChangeType.LocationChange -> {
                    last?.let {
                        it.position = LatLng(lat, lon)
                    }
                    return null
                }
                ChangeType.BothChange -> {
                    last?.let {
                        val bitmapDescriptor = getBitmapDescriptorByValue(testPoint, size)
                        if (bitmapDescriptor != it.icons[0])
                            it.setIcon(bitmapDescriptor)
                        it.title = title
                        it.position = LatLng(lat, lon)
                    }
                    return null
                }
                ChangeType.NewParamChange -> {
                    val bitmapDescriptor = getBitmapDescriptorByValue(testPoint, size)
                    val marker = map.addMarker(MarkerOptions()
                            .setFlat(false)
                            .icon(bitmapDescriptor)
                            .alpha(1f)
                            .draggable(false)
                            .position(LatLng(lat, lon))
                            .anchor(0.5f, 0.5f)
                            .title(title)
                            .zIndex(0.0f)
                    )
                    val key = testPoint["frameNum"].asString
                    val data = ExtraData(key, MapElementType.mark_GPS.value, size, testPoint)
                    marker?.`object` = data
                    //marker.isClickable = true

                    return marker
                }
                ChangeType.NoChange -> return null
            }
        } else
            return null
    }


    /*
    * 判断当前点和上一个点主要参数是否相同，目前之比较纬度、经度、渲染参数
    * */
    private fun isSameAsLast(last: Marker?, lat: Double, lon: Double, value: JsonElement): ChangeType {
        if (last == null)
            return ChangeType.NewParamChange
        else {
            val lastTestPoint = (last.`object` as ExtraData).elementValue!!
            val last_value = lastTestPoint[lastTestPoint["KeyField"].asString]
            if ((lat != last.position.latitude || lon != last.position.longitude) && !value.equals(last_value))
                return ChangeType.BothChange
            else if (lat != last.position.latitude || lon != last.position.longitude)
                return ChangeType.LocationChange
            else if (!value.equals(last_value))
                return ChangeType.KeyValueChange
            else
                return ChangeType.NoChange
        }
    }

    /*
        * 判断当前点和上一个点主要参数是否相同，目前之比较纬度、经度、渲染参数
        * */
    private fun isSameWithLast(last: Marker?, lat: Double, lon: Double, value: JsonElement): Boolean {
        if (last == null)
            return false
        else {
            val lastTestPoint = (last.`object` as ExtraData).elementValue!!
            val last_value = lastTestPoint[lastTestPoint["KeyField"].asString]
            return (lat == last.position.latitude && lon == last.position.longitude && value == last_value)
        }
    }

    fun getTestPointColor(testPoint: JsonObject): Int {

        var color: Int = Color.GRAY
        val colorstr = testPoint["KeyColor"].toString()

        if (!colorstr.isNullOrEmpty())
            color = Color.parseColor(colorstr.replace("\"", "", true))
        return color

    }

    fun getBitmapDescriptorByValue(testPoint: JsonObject?, size: Int): BitmapDescriptor? {
        if (testPoint == null)
            return null
        val color: Int = getTestPointColor(testPoint)
        val key = color.toString()
        if (!renderMaps.containsKey(key)) {
            val bitmapDescriptor: BitmapDescriptor? = createNewBitmapDescriptor(size, color)// BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN) //
            if (bitmapDescriptor != null) renderMaps.put(key, bitmapDescriptor)

        }

        return renderMaps[key]
    }

    fun createNewBitmapDescriptor(width: Int, color: Int): BitmapDescriptor? {
        val paint = Paint()
        paint.color = color

        val bitmap = Bitmap.createBitmap(
                width, width, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        //canvas.drawColor(Color.BLUE)
        val radius: Float = (width / 2).toFloat()
        canvas.drawCircle(radius, radius, radius, paint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    fun getPolyline(map: AMap, testpoints: MutableList<Marker>): Polyline? {

//        var coordinates: MutableList<LatLng> = mutableListOf()
//        var colors: MutableList<Int> = mutableListOf()
//        for (index in it.indices) {
//            coordinates.add(it[index]?.position)
//            if (index > 0)
//                colors.add(getTestPointColor((it[index]?.`object` as ExtraData)?.elementValue!!))//(it[index]?.`object` as ExtraData).elementValue["KeyColor"]?.toString()?.toInt())
//
//            //移除了选中测试点，清除相关内容
//        }
//
//        if (lines.count() >= maxTestLinesCount)
//            lines.removeAt(0)
//        //_paramLines=_paramLines.drop(1).toMutableList()
//
//        val polyline = map.addPolyline(PolylineOptions()
//                .addAll(coordinates)
//                .colorValues(colors)
//                .width(24f)
//                .useGradient(false)
//                .geodesic(false)
//                .setDottedLine(false)
//                .zIndex(0f))
//        //key={new Date().getTime()}
        return null
    }


    enum class ChangeType(val value: Int) {
        NoChange(1),
        NewParamChange(2),
        LocationChange(4),
        KeyValueChange(8),
        BothChange(12)
    }
}