package cn.qiuxiang.react.amap3d.dt

/**
 * Created by lee on 2019/4/17.
 */
object GeoUtils {

    fun distance(lat1:Double,lng1:Double,lat2:Double,lng2:Double):Double{

        val calX = lng2-lng1
        val calY = lat2-lat1
        return Math.sqrt((calX * calX + calY * calY).toDouble())
    }
}