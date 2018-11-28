package cn.qiuxiang.react.amap3d.dt

/**
 * Created by lee on 2019/2/12.
 */
class TestPoint {

    /*******自定义字段******/
    var frameNum: Int = 0
    var keyField = ""
    var keyColor: Float = 0.0f

    /*******End******/

    //@SerializedName("MCC")
    var MCC = ""
    var MNC = ""
    var Network = ""
    var ECI = ""
    var PCI = ""
    var TAC = ""
    var CellID = ""
    var eNodeBID = ""
    var LAT = ""
    var LON = ""
    var GCJ_LAT = ""
    var GCJ_LON = ""
    var RSRP = ""
    var SINR = ""

    var LAC = ""
    var CI = ""
    var RxLev = ""

//    if(cell.type.toString().equals("WCDMA")){
//        networkParams.putString("WLAC", cell.lac != null ? cell.lac.toString() : "");
//        networkParams.putString("WCI", cell.ci != null ? cell.ci.toString() : "");
//        networkParams.putString("RSCP", cell.rxlev != null ? cell.rxlev.toString() : "");
//        networkParams.putString("PSC", cell.psc != null ? cell.psc.toString() : "");
//    }else{
//        networkParams.putString("LAC", cell.lac != null ? cell.lac.toString() : "");
//        networkParams.putString("CI", cell.ci != null ? cell.ci.toString() : "");
//        networkParams.putString("RxLev", cell.rxlev != null ? cell.rxlev.toString() : "");
//    }
//    if(cell.type.toString().equals("WCDMA")){
//        networkParams.putString("Nid", cell.nid != null ? cell.nid.toString() : "");
//        networkParams.putString("Bid", cell.bid != null ? cell.bid.toString() : "");
//        networkParams.putString("Sid", cell.sid != null ? cell.sid.toString() : "");
//    }else{
//        networkParams.putString("Nid", cell.nid != null ? cell.nid.toString() : "");
//        networkParams.putString("Bid", cell.bid != null ? cell.bid.toString() : "");
//        networkParams.putString("Sid", cell.sid != null ? cell.sid.toString() : "");
//    }

}