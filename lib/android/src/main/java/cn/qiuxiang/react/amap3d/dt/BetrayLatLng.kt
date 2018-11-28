package cn.qiuxiang.react.amap3d.dt

/**
 * Created by lee on 2019/2/27.
 */

object BetrayLatLng {

    val PI = 3.14159265358979324
    val x_pi = 3.14159265358979324 * 3000.0 / 180.0

    fun delta(lat: Double, lon: Double): DoubleArray {
        // Krasovsky 1940
        //
        // a = 6378245.0, 1/f = 298.3
        // b = a * (1 - f)
        // ee = (a^2 - b^2) / a^2;
        val a = 6378245.0 // a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
        val ee = 0.00669342162296594323 // ee: 椭球的偏心率。
        var dLat = transformLat(lon - 105.0, lat - 35.0)
        var dLon = transformLon(lon - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = Math.sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = Math.sqrt(magic)
        dLat = dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * PI)
        dLon = dLon * 180.0 / (a / sqrtMagic * Math.cos(radLat) * PI)
        return doubleArrayOf(dLat, dLon)
    }

    // WGS-84 to GCJ-02
    fun gcj_encrypt(wgsLat: Double, wgsLon: Double): DoubleArray {
        if (outOfChina(wgsLat, wgsLon)) {
            return doubleArrayOf(wgsLat, wgsLon)
        }

        val d = delta(wgsLat, wgsLon)
        return doubleArrayOf(wgsLat + d[0], wgsLon + d[1])
    }

    // GCJ-02 to WGS-84
    fun gcj_decrypt(gcjLat: Double, gcjLon: Double): DoubleArray {
        if (outOfChina(gcjLat, gcjLon))
            return doubleArrayOf(gcjLat, gcjLon)

        val d = delta(gcjLat, gcjLon)
        return doubleArrayOf(gcjLat - d[0], gcjLon - d[1])
    }

    // GCJ-02 to WGS-84 exactly
    fun gcj_decrypt_exact(gcjLat: Double, gcjLon: Double): DoubleArray {
        val initDelta = 0.01
        val threshold = 0.000000001
        var dLat = initDelta
        var dLon = initDelta
        var mLat = gcjLat - dLat
        var mLon = gcjLon - dLon
        var pLat = gcjLat + dLat
        var pLon = gcjLon + dLon
        var wgsLat: Double
        var wgsLon: Double
        var i = 0.0
        while (true) {
            wgsLat = (mLat + pLat) / 2
            wgsLon = (mLon + pLon) / 2
            val tmp = gcj_encrypt(wgsLat, wgsLon)
            dLat = tmp[0] - gcjLat
            dLon = tmp[1] - gcjLon
            if (Math.abs(dLat) < threshold && Math.abs(dLon) < threshold)
                break

            if (dLat > 0)
                pLat = wgsLat
            else
                mLat = wgsLat
            if (dLon > 0)
                pLon = wgsLon
            else
                mLon = wgsLon

            if (++i > 10000)
                break
        }
        // console.log(i);
        return doubleArrayOf(wgsLat, wgsLon)
    }

    // GCJ-02 to BD-09
    fun bd_encrypt(gcjLat: Double, gcjLon: Double): DoubleArray {
        val z = Math.sqrt(gcjLon * gcjLon + gcjLat * gcjLat) + 0.00002 * Math.sin(gcjLat * x_pi)
        val theta = Math.atan2(gcjLat, gcjLon) + 0.000003 * Math.cos(gcjLon * x_pi)
        val bdLon = z * Math.cos(theta) + 0.0065
        val bdLat = z * Math.sin(theta) + 0.006
        return doubleArrayOf(bdLat, bdLon)
    }

    // BD-09 to GCJ-02
    fun bd_decrypt(bdLat: Double, bdLon: Double): DoubleArray {
        val x = bdLon - 0.0065
        val y = bdLat - 0.006
        val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi)
        val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi)
        val gcjLon = z * Math.cos(theta)
        val gcjLat = z * Math.sin(theta)
        return doubleArrayOf(gcjLat, gcjLon)
    }

    // WGS-84 to Web mercator
    // mercatorLat -> y mercatorLon -> x
    fun mercator_encrypt(wgsLat: Double, wgsLon: Double): DoubleArray {
        val x = wgsLon * 20037508.34 / 180.0
        var y = Math.log(Math.tan((90.0 + wgsLat) * PI / 360.0)) / (PI / 180.0)
        y = y * 20037508.34 / 180.0
        return doubleArrayOf(y, x)

    }

    // Web mercator to WGS-84
    // mercatorLat -> y mercatorLon -> x
    fun mercator_decrypt(mercatorLat: Double,
                         mercatorLon: Double): DoubleArray {
        val x = mercatorLon / 20037508.34 * 180.0
        var y = mercatorLat / 20037508.34 * 180.0
        y = 180 / PI * (2 * Math.atan(Math.exp(y * PI / 180.0)) - PI / 2)
        return doubleArrayOf(y, x)

    }

    // two point's distance
    fun distance(latA: Double, lonA: Double, latB: Double,
                 lonB: Double): Double {
        val earthR = 6371000.0
        val x = (Math.cos(latA * PI / 180.0) * Math.cos(latB * PI / 180.0)
                * Math.cos((lonA - lonB) * PI / 180))
        val y = Math.sin(latA * PI / 180.0) * Math.sin(latB * PI / 180.0)
        var s = x + y
        if (s > 1)
            s = 1.0
        if (s < -1)
            s = -1.0
        val alpha = Math.acos(s)
        return alpha * earthR
    }

    fun outOfChina(lat: Double, lon: Double): Boolean {
        if (lon < 72.004 || lon > 137.8347)
            return true
        return if (lat < 0.8293 || lat > 55.8271) true else false
    }

    fun transformLat(x: Double, y: Double): Double {
        var ret = (-100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x)))
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    fun transformLon(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

}
