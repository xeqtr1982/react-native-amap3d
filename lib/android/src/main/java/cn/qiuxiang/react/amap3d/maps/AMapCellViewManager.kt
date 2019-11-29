package cn.qiuxiang.react.amap3d.maps

import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

@Suppress("unused")
internal class AMapCellViewManager : ViewGroupManager<AMapCellView>() {
    companion object {
        val ANIMATE_TO = 1
        val CHANGE_RENDER_FIELD = 19
        val ADD_CELLS = 21
        val ADD_CELL = 22
        val REMOVE_CELLS = 23
        val CHANGE_CELLS_VISIBLE = 24
        val CHANGE_CELLS_STYLE = 25
        val SELECT_CELL = 26

    }

    override fun createViewInstance(reactContext: ThemedReactContext): AMapCellView {
        return AMapCellView(reactContext)
    }

    override fun getName(): String {
        return "AMapCellView"
    }

    override fun getCommandsMap(): MutableMap<String, Int> {
        return mutableMapOf("animateTo" to ANIMATE_TO,
                "changeRenderField" to CHANGE_RENDER_FIELD,
                "addCells" to ADD_CELLS,
                "addCell" to ADD_CELL,
                "removeCells" to REMOVE_CELLS,
                "changeCellsVisible" to CHANGE_CELLS_VISIBLE,
                "changeCellsStyle" to CHANGE_CELLS_STYLE,
                "selectCell" to SELECT_CELL)
    }

    override fun receiveCommand(root: AMapCellView?, commandId: Int, args: ReadableArray?) {
        //super.receiveCommand(root, commandId, args)
        when (commandId) {
            ANIMATE_TO -> root?.animateTo(args)
            CHANGE_RENDER_FIELD -> root?.changeRenderField(args)
            ADD_CELLS -> root?.addCells(args)
            REMOVE_CELLS -> root?.removeCells(args)
            ADD_CELL -> root?.addCell(args)
            CHANGE_CELLS_VISIBLE -> root?.changeCellsVisible(args)
            CHANGE_CELLS_STYLE -> root?.changeCellsStyle(args)
            SELECT_CELL -> root?.selectCell(args)
        }
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val eventMap = mutableMapOf<String, Any>("onPress" to mutableMapOf("registrationName" to "onPress"))
        eventMap["onLongPress"] = mutableMapOf("registrationName" to "onLongPress")
        eventMap["onLocation"] = mutableMapOf("registrationName" to "onLocation")
        eventMap["onMarkerPress"] = mutableMapOf("registrationName" to "onMarkerPress")
        eventMap["onStatusChange"] = mutableMapOf("registrationName" to "onStatusChange")
        eventMap["onStatusChangeComplete"] = mutableMapOf("registrationName" to "onStatusChangeComplete")
        eventMap["onLocation"] = mutableMapOf("registrationName" to "onLocation")
        return eventMap
    }

    @ReactProp(name = "showsLocationButton")
    fun setMyLocationButtonEnabled(view: AMapCellView, enabled: Boolean) {
        view.map.uiSettings.isMyLocationButtonEnabled = enabled
    }

    @ReactProp(name = "locationEnabled")
    fun setMyLocationEnabled(view: AMapCellView, enabled: Boolean) {
        view.setLocationEnabled(enabled)
    }

    @ReactProp(name = "maxZoomLevel")
    fun setMaxZoomLevel(view: AMapCellView, zoomLevel: Float) {
        view.map.maxZoomLevel = zoomLevel
    }

    @ReactProp(name = "minZoomLevel")
    fun setMinZoomLevel(view: AMapCellView, zoomLevel: Float) {
        view.map.minZoomLevel = zoomLevel
    }

    @ReactProp(name = "zoomLevel")
    fun setZoomLevel(view: AMapCellView, zoomLevel: Float) {
        view.map.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel))
    }

    @ReactProp(name = "cellVisibleZoomLevel")
    fun setCellVisibleZoomLevel(view: AMapCellView, zoomLevel: Float) {
        view.cellVisibleLevel=zoomLevel
    }

    @ReactProp(name = "zoomEnabled")
    fun setZoomGesturesEnabled(view: AMapCellView, enabled: Boolean) {
        view.map.uiSettings.isZoomGesturesEnabled = enabled
    }

    @ReactProp(name = "coordinate")
    fun moveToCoordinate(view: AMapCellView, coordinate: ReadableMap) {
        view.map.moveCamera(CameraUpdateFactory.changeLatLng(LatLng(
                coordinate.getDouble("latitude"),
                coordinate.getDouble("longitude"))))
    }
}