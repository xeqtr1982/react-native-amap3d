package cn.qiuxiang.react.amap3d.maps

//import com.amap.api.maps.model.BitmapDescriptor
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import cn.qiuxiang.react.amap3d.dt.*
import cn.qiuxiang.react.amap3d.dt.ParamObj.createNewBitmapDescriptor
import cn.qiuxiang.react.amap3d.dt.ParamObj.getTestPointColor
import cn.qiuxiang.react.amap3d.toLatLng
import cn.qiuxiang.react.amap3d.toLatLngBounds
import cn.qiuxiang.react.amap3d.toWritableMap
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.Projection
import com.amap.api.maps.model.*
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Created by lee on 2019/1/23.
 */
class AMapSimpleView(context: Context) : MapView(context) {
    private val eventEmitter: RCTEventEmitter = (context as ThemedReactContext).getJSModule(RCTEventEmitter::class.java)
    private val locationStyle by lazy {
        val locationStyle = MyLocationStyle()
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        locationStyle
    }
    private var map_markers = HashMap<Int, MutableList<Marker>>()
    private val map_lines = HashMap<Int, MutableList<Polyline>>()
    private val map_polygons = HashMap<Int, MutableList<Polygon>>()
    private var save_TestPoints: MutableList<TestPoint> = mutableListOf()

    private val maxTestPointCount = 100 //最多绘制测试点数
    private val maxTestLinesCount = 12 //最多绘制线段数
    private val maxlinePointsCount = 80 //每条线段的点数，必须小于maxTestPointCount
    private val renderFields = arrayOf("RSRP", "RxLev")//private var renderField="RSRP"
    private val renderMaps = HashMap<String, BitmapDescriptor>()
    //private val cellRenderMaps = HashMap<String, BitmapDescriptor>()


    private var _touchCount: Int = 0 //是否手动拖动地图的判断值
    var following: Boolean = false//是否跟随当前测试点
        set(value) {
            field = value
            val event = Arguments.createMap()
            event.putBoolean("following", value)
            emit(id, "onFollowStateChanged", event)
        }
    //get() = field
    private var project: Projection? = null
    private var lastTilt: Float = 0f

    fun addElements(args: ReadableArray?) {
        val elementType = args?.getInt(0)!!
        if (!map_markers.containsKey(elementType))
            map_markers.put(elementType, mutableListOf())
        when (elementType) {
            MapElementType.mark_GPS.value -> addTestPoints(args) //addtestpoints
            MapElementType.mark_Cell.value -> addCells(args) //addcells
            MapElementType.mark_Order.value -> addOrders(args) //addorders
        }
    }

    fun addElement(args: ReadableArray?) {
        val elementType = args?.getInt(0)!!
        if (!map_markers.containsKey(elementType))
            map_markers.put(elementType, mutableListOf())
        when (elementType) {
            MapElementType.mark_GPS.value -> addTestPoint(args) //addtestpoints
        //MapElementType.mark_Cell.value -> addCells(args) //addcells
        //MapElementType.mark_Order.value -> addOrders(args) //addorders
        }
    }

    fun removeElements(args: ReadableArray?) {
        val elementType = args?.getInt(0)!!
        clearElementsByType(elementType)
    }

    fun clearElementsByType(elementType: Int) {
        if (map_markers.containsKey(elementType)) {
            var elements = map_markers[elementType]!!
            elements?.let {
                clearMarkerList(elements)
            }
        }
        if (elementType == MapElementType.mark_GPS.value) {
            var lines = map_lines[MapElementType.line_TestPoint.value]
            lines?.let {
                clearLineList(lines)
            }
            save_TestPoints.clear()
        }
    }

    /**
     * 清除指定标记列表
     */
    private fun clearMarkerList(c_markers: MutableList<Marker>) {
        if (c_markers.any()) {
            for (marker in c_markers) {//List内remove是否会索引报错？？？？
                marker?.remove()
            }
            c_markers.clear()
        }
    }

    /**
     * 清除指定线列表
     * */
    private fun clearLineList(c_lines: MutableList<Polyline>) {
        if (c_lines.any()) {
            for (line in c_lines) {
                line?.remove()
            }
            c_lines.clear()
        }
    }

    fun changeElementsVisible(args: ReadableArray?) {
        val elementType: Int = args?.getInt(0)!!
        val visible: Boolean = args?.getBoolean(1)!!
        if (map_markers.containsKey(elementType)) {
            var elements = map_markers[elementType]!!
            elements?.let {
                if (elements.any()) {
                    for (marker in elements) {
                        marker.isVisible = visible
                    }
                }
            }

        }
        if (elementType == MapElementType.mark_GPS.value) {
            var lines = map_lines[MapElementType.line_TestPoint.value]
            lines?.let {
                if (lines.any()) {
                    for (line in lines) {//List内remove是否会索引报错？？？？
                        line?.isVisible = visible
                    }
                }
            }
        }
    }

    fun changeElementsStyle(args: ReadableArray?) {}

    //region getMarker

//    private fun getMarker(elementType: Int, element: JsonObject?, size: Int): Marker? {
//        var marker: Marker? = null
//        when (elementType) {
//            MapElementType.mark_GPS.value -> marker = getParamMarker(element, size) //addtestpoints
//            MapElementType.mark_Cell.value -> marker = CellObj.getMarker(map,element, size) //addcells
//            MapElementType.mark_Order.value -> marker = getOrderMarker(element, size) //addorders
//        }
//        return marker
//    }

    //endregion


    private fun addOrders(args: ReadableArray?) {
        val elementType = args?.getInt(0)!!
        val targets = args?.getArray(1)!!
        val size = args?.getInt(2)!!
        clearElementsByType(elementType)
        val markers = map_markers[elementType]
        val lats = arrayListOf<Double>()
        val lons = arrayListOf<Double>()
        for (i in 0 until targets.size()) {
            val target = targets.getMap(i)
            val orderObject = JsonObject()
            for ((key, value) in target.toHashMap()) {//as HashMap<String, Any>
                orderObject.addProperty(key, value?.toString())
            }
            lats.add(orderObject["LAT"].asDouble)
            lons.add(orderObject["LON"].asDouble)

            val marker = OrderObj.getMarker(map, orderObject, size) //getCellMarker(cellObject, size)
            marker?.let {
                markers?.add(marker)
            }
        }
        following = false
        if (lats.count() == 1)
            moveTo(LatLng(lats[0], lons[0]))
        else {
            lats.sort()
            lons.sort()
            val minlon = lons.first()
            val minlat = lats.first()
            val maxlon = lons.last()
            val maxlat = lats.last()
            val dlon = (maxlon - minlon) / 4
            val dlat = (maxlat - minlat) / 4

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minlat - dlat, minlon - dlon), LatLng(maxlat + dlat, maxlon + dlon)), 0))
        }
    }

    private fun addCells(args: ReadableArray?) {

        val elementType = args?.getInt(0)!!
        val targets = args?.getArray(1)!!
        val size = args?.getInt(2)!!
        clearElementsByType(elementType)
        val markers = map_markers[elementType]
        for (i in 0 until targets.size()) {
            val target = targets.getMap(i)
            val cellObject = JsonObject()
            for ((key, value) in target.toHashMap()) {//as HashMap<String, Any>
                cellObject.addProperty(key, value?.toString())
            }
            val marker = CellObj.getMarker(map, cellObject, size) //getCellMarker(cellObject, size)
            marker?.let {
                markers?.add(marker)
            }
        }
        //map.runOnDrawFrame()//refresh map

    }

    private fun addTestPoints(args: ReadableArray?) {
        val elementType = args?.getInt(0)!!
        val targets = args?.getArray(1)!!
        val size = args?.getInt(2)!!
        val testStatus = args?.getString(3)
        var markers = map_markers[elementType]!!
        for (target in targets.toArrayList()) {
            val testPoint = JsonObject()
            for ((key, value) in (target as HashMap<String, Any>)) {
                testPoint.addProperty(key, value?.toString())
            }
            _addTestPoint(markers, testPoint, size, testStatus)
        }
    }

    private fun addTestPoint(args: ReadableArray?) {
        //Log.i("android addTestPoint", args.toString())
        //Log.i("AmapSimpleView", "addTestPoint")

        val elementType = args?.getInt(0)!!
        val target = args?.getMap(1)!!
        val size = args?.getInt(2)!!
        val testStatus = args?.getString(3)

        val testPoint = JsonObject()
        for ((key, value) in target.toHashMap())
            testPoint.addProperty(key, value?.toString())
        _addTestPoint(map_markers[elementType]!!, testPoint, size, testStatus)
    }


    private fun _addTestPoint(markers: MutableList<Marker>, testPoint: JsonObject?, size: Int, testStatus: String) {
        when (testStatus) {
            "START" -> clearMarkerList(markers) //clearTestPoints()
            "RUNNING" -> {
                val marker = ParamObj.getMarker(map, testPoint, size,
                        when (markers.any()) {
                            true -> markers.last()
                            false -> null
                        })
                _addMultiTestPoint(markers, marker)
            }
            "STOPPED" -> {
                val marker = ParamObj.getMarker(map, testPoint, size,
                        when (markers.any()) {
                            true -> markers.last()
                            false -> null
                        })
                _addSingleTestPoint(markers, marker)
            }
            "STOPPING", "ERROR" -> _addTestLine(markers)
        }
    }

    private fun _addSingleTestPoint(markers: MutableList<Marker>, marker: Marker?) {
        marker?.let {
            if (markers.count() > 1)
                _addTestLine(markers)
            else
                clearMarkerList(markers)
            markers.add(it)
            moveTo(it.position)
        }
    }

    private fun _addMultiTestPoint(markers: MutableList<Marker>, marker: Marker?) {
        if (marker != null) {
            markers.add(marker)
            if (markers.count() > maxTestPointCount) {
                val testPoints = markers.subList(0, maxlinePointsCount)
                _addTestLine(testPoints)
                //val mk=_paramMarkers[maxTestPointCount];
                val temp = markers.drop(maxlinePointsCount).toMutableList()
                markers.clear()
                markers.addAll(temp)
                //_paramMarkers.removeAll({m->m   })
            }
            moveTo(marker.position)
        }

    }

    private fun _addTestLine(testPoints: MutableList<Marker>?) {
        val linetype = MapElementType.line_TestPoint.value
        if (!map_lines.containsKey(linetype))
            map_lines.put(linetype, mutableListOf())
        val lines = map_lines[linetype]!!
        testPoints?.let {
            if (testPoints.count() > 1) {
                var coordinates: MutableList<LatLng> = mutableListOf()
                var colors: MutableList<Int> = mutableListOf()
                for (index in it.indices) {
                    coordinates.add(it[index]?.position)
                    if (index > 0)
                        colors.add(getTestPointColor((it[index]?.`object` as ExtraData)?.elementValue!!))//(it[index]?.`object` as ExtraData).elementValue["KeyColor"]?.toString()?.toInt())

                    //移除了选中测试点，清除相关内容
                }

                if (lines.count() >= maxTestLinesCount)
                    lines.removeAt(0)
                //_paramLines=_paramLines.drop(1).toMutableList()

                val polyline = map.addPolyline(PolylineOptions()
                        .addAll(coordinates)
                        .colorValues(colors)
                        .width(24f)
                        .useGradient(false)
                        .geodesic(false)
                        .setDottedLine(false)
                        .zIndex(0f))
                //key={new Date().getTime()}
                lines.add(polyline)
            }
            //删除对应测试点
            clearMarkerList(testPoints)
        }


    }

    fun changeRenderField(args: ReadableArray?) {
        ParamObj.clearRenderMaps()
        val field: String = args?.getString(0)!!
        val size: Int = args?.getInt(1)!!
        val ranges: JsonArray = args?.getArray(2)!! as JsonArray
        val markers = map_markers[MapElementType.mark_GPS.value]!!
        if (markers.any()) {
            for (marker in markers) {
                val extData = (marker.`object` as ExtraData)?.elementValue
                extData?.let {
                    val testPoint = it as JsonObject
                    //testPoint.addProperty("KeyField",field)
                    val value = testPoint[field].asFloat

                    var color: Int = Color.GRAY
                    for (i in 0 until ranges.size()) {
                        val range = ranges[i].asJsonObject
                        val minvalue = range.get("miniValue").asFloat
                        val maxvalue = range.get("maxValue").asFloat

                        if (minvalue < value && maxvalue >= value) {
                            color = Color.parseColor(range.get("color").asString)
                            break
                        }
                    }
                    val key = color.toString()
                    if (!renderMaps.containsKey(key)) {
                        val bitmapDescriptor: BitmapDescriptor? = createNewBitmapDescriptor(size, color)
                        if (bitmapDescriptor != null) renderMaps.put(key, bitmapDescriptor)

                    }
                    marker.setIcon(renderMaps[key])
                }
            }
        }
    }

    fun fromScreenXY(args: ReadableArray?) {
//        val latLng = project?.fromScreenLocation(android.graphics.Point(args?.getInt(0)!!, args?.getInt(1)!!))!!
//        val lat = latLng.latitude
//        val lng = latLng.longitude
//        emit(id, "onMapRectSelected", latLng.toWritableMap())
    }

    fun fromScreenRect(args: ReadableArray?) {
        val elementType: Int = args?.getInt(0)!!
        val rect = args?.getMap(1)!!
        val minx: Int = rect.getInt("minx")
        val maxx: Int = rect.getInt("maxx")
        val miny: Int = rect.getInt("miny")
        val maxy: Int = rect.getInt("maxy")

        val leftbottom = project?.fromScreenLocation(android.graphics.Point(minx, maxy))!!
        val righttop = project?.fromScreenLocation(android.graphics.Point(maxx, miny))!!

        val data: WritableMap = Arguments.createMap()
        data.putInt("elementType", elementType)
        data.putDouble("min_lon", leftbottom.longitude)
        data.putDouble("min_lat", leftbottom.latitude)
        data.putDouble("max_lon", righttop.longitude)
        data.putDouble("max_lat", righttop.latitude)
        emit(id, "onMapRectSelected", data)
    }

    init {
        super.onCreate(null)
        project = map.projection

        map.setOnMapClickListener { latLng ->

            //            for (markers in map_markers.values) {
//                for (marker in markers) {
//                    marker.hideInfoWindow()
//                }
//            }

            for (marker: Marker in map_markers[MapElementType.mark_GPS.value]!!) {
                marker.hideInfoWindow()
            }

            emit(id, "onPress", latLng.toWritableMap())
        }

        map.setOnMapLongClickListener { latLng ->
            emit(id, "onLongPress", latLng.toWritableMap())
        }

        map.setOnMyLocationChangeListener { location ->
            val event = Arguments.createMap()
            event.putDouble("latitude", location.latitude)
            event.putDouble("longitude", location.longitude)
            event.putDouble("accuracy", location.accuracy.toDouble())
            event.putDouble("altitude", location.altitude)
            event.putDouble("speed", location.speed.toDouble())
            event.putInt("timestamp", location.time.toInt())
            emit(id, "onLocation", event)
        }
        map.setOnMarkerClickListener { marker ->
            //Log.i("ReactNativeJS", "setOnMarkerClickListener")
            marker.showInfoWindow()
            val data = (marker.`object` as ExtraData) //.toWritableMap()
            val markers = when (data.elementType) {
            //MapElementType.mark_GPS.value-> _paramMarkers
                MapElementType.mark_Cell.value -> CellObj.getMarkers(map_markers[MapElementType.mark_Cell.value]!!, marker.position.latitude, marker.position.longitude, marker.rotateAngle)
                MapElementType.mark_Order.value -> OrderObj.getMarkers(map_markers[MapElementType.mark_Order.value]!!, marker.position.latitude, marker.position.longitude)
                else -> null
            }
            var array = Arguments.createArray()
            for (marker in markers!!) {
                array.pushString((marker.`object` as ExtraData).elementKey)
            }
            var map = data.toWritableMap()
            map.putArray("ids", array)
            emit(id, "onMarkerPress", map)
            true
        }

        map.setOnMapTouchListener { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> _touchCount++
                MotionEvent.ACTION_UP -> {
                    //_touchCount++
                    if (_touchCount > 3) following = false
                    _touchCount = 0
                }
            }
        }

        map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(position: CameraPosition?) {
                emitCameraChangeEvent("onStatusChangeComplete", position)
            }

            override fun onCameraChange(position: CameraPosition?) {
                emitCameraChangeEvent("onStatusChange", position)
            }
        })

        map.setOnMapLoadedListener {
            val location = map.myLocation
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(LatLng(location.latitude, location.longitude), 17f, 0f, 0f))
            map.animateCamera(cameraUpdate, 500, null)//animateCallback)
            //map.moveCamera(CameraUpdateFactory.zoomTo(17f))
        }

//        map.setOnInfoWindowClickListener { marker ->
//            emit(markers[marker.id]?.id, "onInfoWindowPress")
//        }
//
//        map.setOnPolylineClickListener { polyline ->
//            emit(lines[polyline.id]?.id, "onPress")
//        }
//
//        map.setOnMultiPointClickListener { item ->
//            val slice = item.customerId.split("_")
//            val data = Arguments.createMap()
//            data.putInt("index", slice[1].toInt())
//            emit(slice[0].toInt(), "onItemPress", data)
//            false
//        }
//
//        map.setInfoWindowAdapter(AMapInfoWindowAdapter(context, markers))
    }

    fun emitCameraChangeEvent(event: String, position: CameraPosition?) {
        position?.let {
            val data = it.target.toWritableMap()
            data.putDouble("zoomLevel", it.zoom.toDouble())
            data.putDouble("tilt", it.tilt.toDouble())
            data.putDouble("rotation", it.bearing.toDouble())
            if (event == "onStatusChangeComplete") {
                val southwest = map.projection.visibleRegion.latLngBounds.southwest
                val northeast = map.projection.visibleRegion.latLngBounds.northeast
                data.putDouble("latitudeDelta", Math.abs(southwest.latitude - northeast.latitude))
                data.putDouble("longitudeDelta", Math.abs(southwest.longitude - northeast.longitude))

                if (it.tilt != lastTilt && map.mapType == AMap.MAP_TYPE_NORMAL) {
                    val data1 = Arguments.createMap()
                    data1.putDouble("tilt1", lastTilt.toDouble())
                    data1.putDouble("tilt2", it.tilt.toDouble())
                    emit(id, "onMapTiltChanged", data1)

                    lastTilt = it.tilt
                }
            }
            emit(id, event, data)
        }
    }

    fun emit(id: Int?, name: String, data: WritableMap = Arguments.createMap()) {
        id?.let { eventEmitter.receiveEvent(it, name, data) }
    }

    fun animateTo(args: ReadableArray?) {
        val currentCameraPosition = map.cameraPosition
        val target = args?.getMap(0)!!
        val duration = args.getInt(1)

        var coordinate = currentCameraPosition.target
        var zoomLevel = currentCameraPosition.zoom
        var tilt = currentCameraPosition.tilt
        var rotation = currentCameraPosition.bearing

        if (target.hasKey("coordinate")) {
            coordinate = target.getMap("coordinate").toLatLng()
        }

        if (target.hasKey("zoomLevel")) {
            zoomLevel = target.getDouble("zoomLevel").toFloat()
        }

        if (target.hasKey("tilt")) {
            tilt = target.getDouble("tilt").toFloat()
        }

        if (target.hasKey("rotation")) {
            rotation = target.getDouble("rotation").toFloat()
        }

        val cameraUpdate = CameraUpdateFactory.newCameraPosition(
                CameraPosition(coordinate, zoomLevel, tilt, rotation))
        map.animateCamera(cameraUpdate, duration.toLong(), null)//animateCallback)
    }

    fun moveTo(position: LatLng): Unit {
        if (following) {
            val currentCameraPosition = map.cameraPosition
            var zoomLevel = currentCameraPosition.zoom
            var tilt = currentCameraPosition.tilt
            var rotation = currentCameraPosition.bearing
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(position, zoomLevel, tilt, rotation))
            map.animateCamera(cameraUpdate, null)//animateCallback)
        }
    }

    fun maptypeTo(args: ReadableArray?) {
        val target = args?.getString(0)!! //.getMap(0)!!
        when (target) {
            "standard" -> map.mapType = AMap.MAP_TYPE_NORMAL
            "satellite" -> map.mapType = AMap.MAP_TYPE_SATELLITE
            "navigation" -> map.mapType = AMap.MAP_TYPE_NAVI
            "night" -> map.mapType = AMap.MAP_TYPE_NIGHT
            "bus" -> map.mapType = AMap.MAP_TYPE_BUS
        }
    }

    fun setRegion(region: ReadableMap) {
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(region.toLatLngBounds(), 0))
    }

    fun setLimitRegion(region: ReadableMap) {
        map.setMapStatusLimits(region.toLatLngBounds())
    }

    fun setLocationEnabled(enabled: Boolean) {
        map.isMyLocationEnabled = enabled
        map.myLocationStyle = locationStyle
    }

    //region Description

    //    fun addTestPoint(testPointStr: String, testStatus: String) {
//        val testPoint: JsonObject? = Gson().fromJson(testPointStr, JsonObject::class.java)
//        _addTestPoint(testPoint, testStatus)
//    }


    //endregion

}

//data class ExtraData(var elementKey: String, var elementType: Int, var elementValue: HashMap<String, Float>?)
data class ExtraData(var elementKey: String, var elementType: Int, var elementValue: JsonObject?)