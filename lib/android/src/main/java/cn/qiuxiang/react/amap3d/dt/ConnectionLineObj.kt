package cn.qiuxiang.react.amap3d.dt

import android.graphics.Color
import android.graphics.Point
import cn.qiuxiang.react.amap3d.maps.ExtraData
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions

/**
 * Created by lee on 2019/5/13.
 */
public class ConnectionLine constructor(latLng: LatLng, isService: Boolean, cell: Marker) {

    val testPoint: LatLng = latLng
    val simpleCell: SimpleCell = SimpleCell(cell)
    val isService: Boolean = isService

//    init {
//    }

    class SimpleCell(marker: Marker) {

        val extraData = marker.`object` as ExtraData
        val cellObject = extraData.elementValue!!
        var SiteType: String = cellObject["COVER_TYPE"].asString //室内

        var NetWork: String = cellObject["NET_NAME"].asString//lte、gsm

        val key = when (NetWork) {
            "LTE" -> cellObject["SITE_ID"].asString + "_" + cellObject["CELL_ID"].asString
            else -> cellObject["LAC_TAC"].asString + "_" + cellObject["CELL_ID"].asString //"GSM"
        }

        var ID: String = key //cellObject["CGI_TCI"].asString

        var Lon: Double = marker.position.longitude // cellObject["LON"].asDouble

        var Lat: Double = marker.position.latitude//cellObject["LAT"].asDouble

        var Azimuth: Float = when (cellObject["ANTENNA_ANGLE"].isJsonNull) {
            true -> 0F
            false -> cellObject["ANTENNA_ANGLE"].asFloat
        }


        var Size: Int = extraData.elementSize
    }
}

object ConnectionLineObj {
    var _map: AMap? = null
    val polylines: MutableList<Polyline> = mutableListOf()
    val clines: MutableList<ConnectionLine> = mutableListOf()


    /**
     * 新增线数据
     */
    fun addConnectionLine(map: AMap, connectionLine: ConnectionLine) {
        if (_map == null) _map = map
        clines.add(connectionLine)
    }

    /**
     * 清除线对象
     */
    private fun clearLines() {
        if(polylines.any()){
            for (line: Polyline in polylines) {
                line.remove()
            }
            polylines.clear()
        }
    }

    /**
     * 清除线对象和线数据
     */
    fun clearData() {
        if(clines.any()){
            clines.clear()
            clearLines()
        }
    }

    /**
     * 刷新线对象
     */
    fun refreshLines() {
        clearLines()
        for (cline in clines) {
            createLine(cline)
        }
    }

    private fun createLine(connectionLine: ConnectionLine) {
        _map?.let {
            val list = mutableListOf<LatLng>()
            list.add(connectionLine.testPoint)
            val cellLatLng = getCellCenter(connectionLine.simpleCell)
            list.add(cellLatLng)
            val color = when (connectionLine.isService) {
                true -> Color.RED
                false -> Color.YELLOW
            }

            val line = it.addPolyline(PolylineOptions()
                    .addAll(list)
                    .color(color)
                    .width(8f)
                    .useGradient(false)
                    .geodesic(false)
                    .setDottedLine(false)
                    .zIndex(0f))

            polylines.add(line)
        }

    }

    private fun getCellCenter(simpleCell: ConnectionLine.SimpleCell): LatLng {
        if (simpleCell.SiteType == "室内") {
            return LatLng(simpleCell.Lat, simpleCell.Lon)
        } else {
            val projection = _map!!.getProjection()
            val c_p = projection.toScreenLocation(LatLng(simpleCell.Lat, simpleCell.Lon))
            val angle = (simpleCell.Azimuth + 270) * Math.PI / 180.0//double radLat = lat / 180.0 * PI;
            var size = when (simpleCell.NetWork) {
                "LTE" -> simpleCell.Size * 3 / 4
                else -> simpleCell.Size
            }
            val dx = (Math.cos(angle) * size / 2).toInt()
            val dy = (Math.sin(angle) * size / 2).toInt()
            val c_x = c_p.x + dx
            val c_y = c_p.y + dy
            return projection.fromScreenLocation(Point(c_x, c_y))
        }
    }
}