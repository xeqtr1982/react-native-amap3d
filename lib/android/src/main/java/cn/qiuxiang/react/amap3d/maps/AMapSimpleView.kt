package cn.qiuxiang.react.amap3d.maps

//import com.amap.api.maps.model.BitmapDescriptor
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import cn.qiuxiang.react.amap3d.dt.*
import cn.qiuxiang.react.amap3d.toLatLng
import cn.qiuxiang.react.amap3d.toLatLngBounds
import cn.qiuxiang.react.amap3d.toWritableMap
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
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
    private val markers = HashMap<String, Marker>()
    //private val lines = HashMap<String, Polyline>()

    private var _cellMarkers: MutableList<Marker> = mutableListOf()//长度可变List
    private var _paramMarkers: MutableList<Marker> = mutableListOf()
    private var _paramLines: MutableList<Polyline> = mutableListOf()
    private var _connectionLines:MutableList<Polyline> = mutableListOf()

    private val maxTestPointCount = 100 //最多绘制测试点数
    private val maxTestLinesCount = 12 //最多绘制线段数
    private val maxlinePointsCount = 80 //每条线段的点数，必须小于maxTestPointCount
    private val _gps_size: Int = 48
    private val renderFields = arrayOf("RSRP", "RxLev")//private var renderField="RSRP"
    private val renderMaps = HashMap<Int, BitmapDescriptor>()

    private val cellRenderMaps = HashMap<String, BitmapDescriptor>()


    private var _touchCount: Int = 0 //是否手动拖动地图的判断值
    var following: Boolean = false//是否跟随当前测试点
        set(value) {
            field = value
            val event = Arguments.createMap()
            event.putBoolean("following", value)
            emit(id, "onFollowStateChanged", event)
        }
    //get() = field


    fun addCells(args: ReadableArray?) {

        //cellsDataStr: String
        //val cells = Gson().fromJson<List<Cell>>(cellsDataStr, object : TypeToken<List<Cell>>() {}.type)

        removeAllCells()

        val targets = args?.getArray(0)!!
        val size = args?.getInt(1)!!

        for (i in 0 until targets.size()) {
            val target = targets.getMap(i)
            val cellObject = JsonObject()
            for ((key, value) in target.toHashMap()) {//as HashMap<String, Any>
                cellObject.addProperty(key, value?.toString())
            }
            val marker = getCellMarker(cellObject, size)
            marker?.let {
                _cellMarkers.add(marker)
            }
        }
        //map.runOnDrawFrame()//refresh map

    }

    private fun getCellMarker2(cell: Cell, size: Int): Marker? {
        if (cell == null)
            return null
        else {
            val marker = map.addMarker(MarkerOptions()
                    .setFlat(false)
                    //.icon(bitmapDescriptor)
                    .alpha(1f)
                    .draggable(false)
                    .position(LatLng(cell.Lat, cell.Lon))
                    .anchor(0.5f, if (cell.SiteType == 0) 0.5f else 1.0f)
                    //.infoWindowEnable(true)
                    .title(cell.CellName)
                    .rotateAngle(if (cell.SiteType == 0) 0f else 360 - cell.Azimuth)
            )

            //MapElementType.valueOf("mark_Cell")
            //    MapElementType.mark_GPS.name
            //    MapElementType.mark_Event.ordinal
            val data = ExtraData(cell.CellID, MapElementType.mark_Cell.value, null)
            marker?.`object` = data

            return marker
        }
    }

    private fun getCellMarker(cellObject: JsonObject?, size: Int): Marker? {
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
                .setFlat(false)
                .icon(bitmapDescriptor)
                .alpha(1f)
                .draggable(false)
                .position(LatLng(gcj_latlon[0], gcj_latlon[1]))
                .anchor(0.5f, auchorY)
                .infoWindowEnable(true)
                .title(cellObject["CELLID"].asString)
                .rotateAngle(rotateAngle)
                .zIndex(0f)
        )

        val data = ExtraData(cellObject["CELLID"].asString, MapElementType.mark_Cell.value, cellObject)
        marker?.`object` = data

        return marker
    }

    private fun getCellBitmapDescriptor(siteType: String, netWork: String, size: Int): BitmapDescriptor? {
        val key = netWork + "_" + siteType
        if (!cellRenderMaps.containsKey(key)) {
            val cellRender = CellRender()
            val cellStyle = cellRender.CELL_STYLE_LTE
            val cellPath = when (siteType == "2") {true -> cellRender.INNER_CELL_PATH_ADJUST
                false -> cellRender.OUT_CELL_PATH_30
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
            if (bitmapDescriptor != null) cellRenderMaps.put(key, bitmapDescriptor)
        }
        return cellRenderMaps[key]
    }

    fun removeAllCells(): Unit {
        if (_cellMarkers.any()) {
            for (marker in _cellMarkers) {
                marker?.remove()
            }
            _cellMarkers.clear()
        }
    }

    fun changeCellsVisible(args: ReadableArray?): Unit {

        val visible: Boolean = args?.getBoolean(0)!!
        if (_cellMarkers.any()) {
            for (marker in _cellMarkers) {
                marker.isVisible = visible
            }
        }
    }

    fun changeCellStyle(args: ReadableArray?): Unit {

    }

    fun getTestPointColor(testPoint: JsonObject): Int {

        var color: Int = Color.GRAY
        val colorstr = testPoint["KeyColor"].toString()

        if (!colorstr.isNullOrEmpty())
            color = Color.parseColor(colorstr.replace("\"", "", true))
        return color

    }

    fun getBitmapDescriptorByValue(testPoint: JsonObject?): BitmapDescriptor? {
        if (testPoint == null)
            return null
        var color: Int = getTestPointColor(testPoint)
        //Log.i("color", color.toString())
        if (!renderMaps.containsKey(color)) {
            val bitmapDescriptor: BitmapDescriptor? = createNewBitmapDescriptor(_gps_size, color)// BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN) //
            if (bitmapDescriptor != null) renderMaps.put(color, bitmapDescriptor)

        }

        return renderMaps[color]
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

    /*
    * 判断当前点和上一个点主要参数是否相同，目前之比较纬度、经度、渲染参数
    * */
    private fun isSameAsLast(lat: Double, lon: Double, value: JsonElement): Boolean {
        if (_paramMarkers.count() == 0)
            return false
        else {
            val last = _paramMarkers.last()
            val lastTestPoint = (last.`object` as ExtraData).elementValue!!
            val last_value = lastTestPoint[lastTestPoint["KeyField"].asString]
            if (lat == last.position.latitude && lon == last.position.longitude && value.equals(last_value))
                return true
        }
        return false
    }

    private fun getParamMarker(testPoint: JsonObject?): Marker? {
        if (testPoint != null) {
            val lat = testPoint["GCJ_LAT"].asDouble
            val lon = testPoint["GCJ_LON"].asDouble
            val value = testPoint[testPoint["KeyField"].asString]
            if (isSameAsLast(lat, lon, value)) return null
            val bitmapDescriptor = getBitmapDescriptorByValue(testPoint)
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
            val data = ExtraData(key, MapElementType.mark_GPS.value, testPoint)
            marker?.`object` = data
            marker.isClickable = true

            return marker
        } else
            return null
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

    /**
     * 清除全部测试相关内容，包括测试点和测试线
     */
    fun clearTestPoints() {
        //Log.i("AmapSimpleView", "clearTestPoints")

        if (_paramMarkers.any()) {
            for (marker in _paramMarkers) {//List内remove是否会索引报错？？？？
                marker?.remove()
            }
            _paramMarkers.clear()
        }
        if (_paramLines.any()) {
            for (line in _paramLines) {//List内remove是否会索引报错？？？？
                line?.remove()
            }
            _paramLines.clear()
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

    fun addTestPoints(args: ReadableArray?) {
        //Log.i("android addTestPoints", args.toString())
        //Log.i("AmapSimpleView", "addTestPoints")
        val targets = args?.getArray(0)!!
        val testStatus = args?.getString(1)
        for (target in targets.toArrayList()) {
            val testPoint = JsonObject()
            for ((key, value) in (target as HashMap<String, Any>)) {
                testPoint.addProperty(key, value?.toString())
            }
            _addTestPoint(testPoint, testStatus)
        }

    }

    fun addTestPoint(args: ReadableArray?) {
        //Log.i("android addTestPoint", args.toString())
        //Log.i("AmapSimpleView", "addTestPoint")
        val testPoint = JsonObject()
        val target = args?.getMap(0)!!
        val testStatus = args?.getString(1)

        for ((key, value) in target.toHashMap())
            testPoint.addProperty(key, value?.toString())
        _addTestPoint(testPoint, testStatus)
    }

    fun addTestPoint(testPointStr: String, testStatus: String) {
        val testPoint: JsonObject? = Gson().fromJson(testPointStr, JsonObject::class.java)
        _addTestPoint(testPoint, testStatus)
    }

    fun _addTestPoint(testPoint: JsonObject?, testStatus: String) {
        when (testStatus) {
            "START" -> clearTestPoints()
            "RUNNING" -> {
                val marker = getParamMarker(testPoint)
                _addMultiTestPoint(marker)
            }
            "STOPPED" -> {
                val marker = getParamMarker(testPoint)
                _addSingleTestPoint(marker)
            }
            "STOPPING", "ERROR" -> _addTestLine(_paramMarkers)
        }
    }

    fun _addSingleTestPoint(marker: Marker?) {
        marker?.let {
            if (_paramMarkers.count() > 1)
                _addTestLine(_paramMarkers)
            else
                clearMarkerList(_paramMarkers)
            _paramMarkers.add(it)
            moveTo(it.position)
        }
    }

    fun _addMultiTestPoint(marker: Marker?) {
        if (marker != null) {
            _paramMarkers.add(marker)
            if (_paramMarkers.count() > maxTestPointCount) {
                val testPoints = _paramMarkers.subList(0, maxlinePointsCount)
                _addTestLine(testPoints)
                //val mk=_paramMarkers[maxTestPointCount];
                _paramMarkers = _paramMarkers.drop(maxlinePointsCount).toMutableList()
                //_paramMarkers.removeAll({m->m   })
            }
            moveTo(marker.position)
        }

    }

    fun _addTestLine(testPoints: MutableList<Marker>?) {
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

                if (_paramLines.count() >= maxTestLinesCount)
                    _paramLines.removeAt(0)
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
                _paramLines.add(polyline)
            }
            //删除对应测试点
            clearMarkerList(testPoints)
        }


    }

    fun changeRenderField(args: ReadableArray?): Unit {
        renderMaps.clear()
        val field: String = args?.getString(0)!!
        val ranges: JsonArray = args?.getArray(1)!! as JsonArray
        if (_paramMarkers.any()) {
            for (marker in _paramMarkers) {
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
                    if (!renderMaps.containsKey(color)) {
                        val bitmapDescriptor: BitmapDescriptor? = createNewBitmapDescriptor(_gps_size, color)
                        if (bitmapDescriptor != null) renderMaps.put(color, bitmapDescriptor)

                    }
                    marker.setIcon(renderMaps[color])
                }
            }
        }
    }

    fun changeTestPointVisible(args: ReadableArray?): Unit {
        val visible: Boolean = args?.getBoolean(0)!!
        if (_paramMarkers.any()) {
            for (marker in _paramMarkers) {
                marker.isVisible = visible
            }
        }
        if (_paramLines.any()) {
            for (polylin in _paramLines) {
                polylin.isVisible = visible
            }
        }
    }

    init {
        super.onCreate(null)
        renderMaps.clear()
        map.moveCamera(CameraUpdateFactory.zoomTo(17f))
        map.setOnMapClickListener { latLng ->
            for (marker in _cellMarkers) {
                marker.hideInfoWindow()
            }
            for (marker in _paramMarkers) {
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

            marker.showInfoWindow()
            val data = (marker.`object` as ExtraData).toWritableMap()
//            val markers = when(data.elementType){
//                MapElementType.mark_GPS.value-> _paramMarkers
//                MapElementType.mark_Cell.value->_cellMarkers
//                else ->null
//            }
            //Log.i("marker",data.toString())
            emit(id, "onMarkerPress", data)
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

    fun maptypeTo(args: ReadableArray?) {
        val target = args?.getMap(0)!!

        if (target.hasKey("maptype")) {
            val mapType = target.getString("maptype")
            when (mapType) {
                "standard" -> map.mapType = AMap.MAP_TYPE_NORMAL
                "satellite" -> map.mapType = AMap.MAP_TYPE_SATELLITE
                "navigation" -> map.mapType = AMap.MAP_TYPE_NAVI
                "night" -> map.mapType = AMap.MAP_TYPE_NIGHT
                "bus" -> map.mapType = AMap.MAP_TYPE_BUS
            }
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

}

//data class ExtraData(var elementKey: String, var elementType: Int, var elementValue: HashMap<String, Float>?)
data class ExtraData(var elementKey: String, var elementType: Int, var elementValue: JsonObject?)