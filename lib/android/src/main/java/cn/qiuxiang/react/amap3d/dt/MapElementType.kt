package cn.qiuxiang.react.amap3d.dt

/**
 * Created by lee on 2019/2/12.
 */
enum class MapElementType(val value:Int) {
    mark_GPS(1),
    mark_Event (2),
    mark_Cell(3),
    mark_Order(4),
    mark_Selection(11),
    mark_NaviOrignal(12), //导航起点
    mark_NaviDest(13) , //导航终点
    mark_Location (14), //定位点
    mark_Distance(15),//测距点
    mark_DistanceClose(16),//测距点删除按钮
    line_TestPoint(101),
    line_Connection(111),
    line_CellSelection(112),
    line_GPSSelection(113),
    line_Navigation(114), //导航线路
    line_Distance(115),//测距线路
    region_Grid(201)
}

enum class NetWork(val value:Int){
    GSM(1),
    LTE(3)
}

enum class SiteType(val value:Int){
    OUTER(1),
    INNER(2)
}
