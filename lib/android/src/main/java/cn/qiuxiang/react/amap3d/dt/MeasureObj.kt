package cn.qiuxiang.react.amap3d.dt

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.*

/**
 * Created by lee on 2019/5/6.
 */
object MeasureObj {
    var context: Context? = null
    var _map: AMap? = null
    private val measurePoints: MutableList<LatLng> = mutableListOf()
    private var line: Polyline? = null
    private val markers: MutableList<Marker> = mutableListOf()

    private var endBitmap: Bitmap? = null
    private var middleBitmap: Bitmap? = null
    private var closeBitMap: Bitmap? = null

    init {
        endBitmap = createCricleBitmap(48, Color.RED, Color.rgb(165, 42, 42))
        middleBitmap = createCricleBitmap(48, Color.rgb(30, 144, 255), Color.BLUE)
        closeBitMap = createBitmap(ObjRender.DELETE_CIRCLE, 72, Color.RED)
    }

    fun addMeasurePoint(map: AMap, position: LatLng) {
        if (_map == null)
            _map = map
        measurePoints.add(position)
        _map?.let {
            createMeasureLine(it)
            createMeasureMarkers(it)
        }

    }

    fun clearMeasurePoints() {
        if (measurePoints.any()) {
            measurePoints.clear()
            line?.remove()
            for (marker in markers) {
                marker.remove()
            }
            markers.clear()
        }
    }

    fun removeMeasurePoint() {
        if (measurePoints.size > 0) {
            measurePoints.removeAt(measurePoints.size - 1)
        }
        _map?.let {
            createMeasureLine(it)
            createMeasureMarkers(it)
        }
    }

    private fun createMeasureLine(map: AMap) {
        Log.i("ReactNativeJS", "createMeasureLine")
        if (measurePoints.size > 1) {
            if (line == null) {
                line = map.addPolyline(PolylineOptions()
                        .addAll(measurePoints)
                        .color(Color.BLUE)
                        .width(12f)
                        .useGradient(false)
                        .geodesic(false)
                        .setDottedLine(false)
                        .zIndex(0f))
            } else
                line?.points = measurePoints.toList()
        } else {
            line?.remove()
            line = null
        }

    }

    private fun createMeasureMarkers(map: AMap) {
        Log.i("ReactNativeJS", "createMeasureMarkers")
        for (marker in markers) {
            marker.remove()
        }
        if (measurePoints.size > 0) {

            if (measurePoints.size == 1) {
                val marker = createMarker(measurePoints[0], "起点", 3)
                marker?.let {
                    markers.add(it)
                    it.showInfoWindow()
                }
            } else {
                var total: Float = 0.0F
                for (i in 0 until measurePoints.size) {
                    val latlng = measurePoints[i]
                    var marker: Marker? = null
                    if (i == 0) {//start icon
                        marker = createMarker(latlng, "起点", 0)
                    } else {
                        val distance: Float = AMapUtils.calculateLineDistance(latlng, measurePoints[i - 1])
                        total = total + distance
                        if (i == measurePoints.size - 1) {
                            marker = createMarker(latlng, total.toInt().toString() + " m", 2)
                            marker?.showInfoWindow()
                        } else
                            marker = createMarker(latlng, total.toInt().toString() + " m", 1)
                    }
                    marker?.let { markers.add(it) }
                }
            }
        }
    }

    /**
     *
     *  @param latLng
     *  @param index 0--start,1--middle,2--end,3--start and end
     */
    private fun createMarker(latLng: LatLng, distance: String, index: Int): Marker? {
        var bitMap: Bitmap? = when (index) {
            2, 3 -> endBitmap //end / start and end
            else -> middleBitmap //0,1 ---- start / middle
        }
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitMap)
        val marker = _map?.addMarker(MarkerOptions()
                .setFlat(false)
                .icon(bitmapDescriptor)
                .alpha(1f)
                .draggable(false)
                .position(latLng)
                .anchor(0.5f, 0.5f)
                //.infoWindowEnable(true)
                .title(distance)
                //.zIndex(0f)
        )

        marker?.`object` = index

        return marker
    }

    private fun createCricleBitmap(width: Int, fillColor: Int, strokeColor: Int): Bitmap {
        val paint_fill = Paint()
        paint_fill.color = fillColor

        val paint_stroke = Paint()
        paint_stroke.style = Paint.Style.STROKE
        paint_stroke.strokeWidth = 4f
        paint_stroke.color = strokeColor

        val bitmap = Bitmap.createBitmap(
                width, width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        //canvas.drawPath(path_fill, paint_fill);
        val half = width / 2
        canvas.drawCircle(half.toFloat(), half.toFloat(), (half - 2).toFloat(), paint_fill)
        canvas.drawCircle(half.toFloat(), half.toFloat(), (half - 2).toFloat(), paint_stroke)
        return bitmap
    }

    private fun createBitmap(cellPath: CellPath, width: Int, fillColor: Int): Bitmap {

        val scale = width * 1.0f / cellPath.width

        val paint_fill = Paint()
        paint_fill.color = fillColor

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val path1 = PathParser().createPathFromPathData(cellPath.pathData)
        val path_fill = Path()
        path_fill.addPath(path1, matrix)

        val bitmap = Bitmap.createBitmap(
                width, width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawPath(path_fill, paint_fill)
        return bitmap
    }

    fun createCustomInfoWindow(info: String, marker: Marker): View {
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val layout = LinearLayout(context)
        layout.layoutParams = lp
        layout.orientation = LinearLayout.HORIZONTAL
        val tv = TextView(context)
        tv.layoutParams = lp
        tv.setText(info)
        layout.addView(tv)
        val index = marker.`object` as Int
        if (index == 2 || index == 3) {
            val iv = ImageView(context)
            iv.setImageBitmap(closeBitMap)
            iv.setOnClickListener { removeMeasurePoint() }//marker.remove()
            layout.addView(iv)
        }


        return layout
    }
}