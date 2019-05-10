package cn.qiuxiang.react.amap3d.dt

import android.view.View
import com.amap.api.maps.AMap.InfoWindowAdapter
import com.amap.api.maps.model.Marker

/**
 * Created by lee on 2019/5/9.
 */
class CustomInfoWindowAdapter: InfoWindowAdapter {
    override fun getInfoContents(p0: Marker?): View? {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        if(p0!=null){
            return MeasureObj.createCustomInfoWindow(p0.title, p0)
        }
        return null

    }

    override fun getInfoWindow(p0: Marker?): View? {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        if(p0!=null){
            return MeasureObj.createCustomInfoWindow(p0.title, p0)
        }
        return null
    }
}