package cn.qiuxiang.react.amap3d

import android.content.res.Resources
import cn.qiuxiang.react.amap3d.maps.ElementKeyData
import cn.qiuxiang.react.amap3d.maps.ExtraData
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.google.gson.JsonObject

fun Float.toPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun ReadableMap.toLatLng(): LatLng {
    return LatLng(this.getDouble("latitude"), this.getDouble("longitude"))
}

fun ReadableArray.toLatLngList(): ArrayList<LatLng> {
    return ArrayList((0..(this.size() - 1)).map { this.getMap(it).toLatLng() })
}

fun LatLng.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    map.putDouble("latitude", this.latitude)
    map.putDouble("longitude", this.longitude)
    return map
}

//fun ReadableMap.toElementKeyData(): ElementKeyData {
//    return ElementKeyData(this.getString("key"), this.getInt("elementType"))
//}

fun ElementKeyData.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    map.putString("elementKey", this.elementKey)
    map.putInt("elementType", this.elementType)
    return map
}

fun ExtraData.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    map.putString("elementKey", this.elementKey)
    map.putInt("elementType", this.elementType)
    map.putInt("elementSize", this.elementSize)
    map.putMap("elementValue", this.elementValue?.toWritableMap())
    return map
}

//只针对简单对象，测试点
fun JsonObject.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    for ((key, value) in this.entrySet()) {
        //if (value is JsonObject)
        val str_value: String = when (value.isJsonNull) {
            true -> ""
            false -> value.asString
        }
        map.putString(key, str_value)
//        else if (value is JsonArray)
//            map.putArray(key, null)
    }

    return map
}

fun ReadableMap.toLatLngBounds(): LatLngBounds {
    val latitude = this.getDouble("latitude")
    val longitude = this.getDouble("longitude")
    val latitudeDelta = this.getDouble("latitudeDelta")
    val longitudeDelta = this.getDouble("longitudeDelta")
    return LatLngBounds(
            LatLng(latitude - latitudeDelta / 2, longitude - longitudeDelta / 2),
            LatLng(latitude + latitudeDelta / 2, longitude + longitudeDelta / 2)
    )
}