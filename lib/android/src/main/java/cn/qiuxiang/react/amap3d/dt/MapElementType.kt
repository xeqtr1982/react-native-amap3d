package cn.qiuxiang.react.amap3d.dt

/**
 * Created by lee on 2019/2/12.
 */
enum class MapElementType(val value:Int) {
    mark_GPS(1),
    mark_Event (2),
    mark_Cell(3),
    mark_Selection(4),
    mark_NaviOrignal(5), //导航起点
    mark_NaviDest(6) , //导航终点
    mark_Location (7), //定位点
    line_Connection(11),
    line_CellSelection(12),
    line_GPSSelection(13),
    line_Navigation(14), //导航线路
    region_Grid(21)
}

enum class NetWork(val value:Int){
    GSM(1),
    LTE(3)
}

enum class SiteType(val value:Int){
    OUTER(1),
    INNER(2)
}
