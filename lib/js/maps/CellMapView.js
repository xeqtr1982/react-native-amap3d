
import React from 'react'
import Component from "../Component";
import PropTypes from "prop-types";
import {
  processColor,
  requireNativeComponent,
  ViewPropTypes
} from "react-native";
import { LatLng, Region } from "../PropTypes";
import { MapStatus, LocationStyle } from "../maps/MapView";

export default class CellMapView extends Component<any>{
    static propTypes = {
        ...ViewPropTypes,
        /**
         * 设置定位图标的样式
         */
        locationStyle: LocationStyle,
    
        /**
         * 设置定位模式 默认 LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER
         *
         * @platform android
         */
        locationType: PropTypes.oneOf([
          "show",
          "locate",
          "follow",
          "map_rotate",
          "location_rotate",
          "location_rotate_no_center",
          "follow_no_center",
          "map_rotate_no_center"
        ]),
    
        /**
         * 是否启用定位
         */
        locationEnabled: PropTypes.bool,
    
        /**
         * 是否显示定位按钮
         *
         * @platform android
         */
        showsLocationButton: PropTypes.bool,
    
        
        /**
         * 最大缩放级别
         */
        maxZoomLevel: PropTypes.number,
    
        /**
         * 最小缩放级别
         */
        minZoomLevel: PropTypes.number,
    
        /**
         * 当前缩放级别，取值范围 [3, 20]
         */
        zoomLevel: PropTypes.number,
    
        /**
         * 设置小区显示的最小级别，取值范围 [3, 20]
         */
        cellVisibleZoomLevel: PropTypes.number,

        /**
         * 中心坐标
         */
        coordinate: LatLng,
    
        /**
         * 显示区域
         */
        region: Region,
        
        /**
         * 是否启用缩放手势，用于放大缩小
         */
        zoomEnabled: PropTypes.bool,
    
        /**
         * 是否启用滑动手势，用于平移
         */
        scrollEnabled: PropTypes.bool,
        /**
         * 点击事件
         *
         * @param {{ nativeEvent: LatLng }}
         */
        onPress: PropTypes.func,
    
        /**
         * 长按事件
         *
         * @param {{ nativeEvent: LatLng }}
         */
        onLongPress: PropTypes.func,
    
        /**
         * 标记物点击事件
         * @param{object}
         */
        onMarkerPress: PropTypes.func,
        /**
         * 定位事件
         *
         * @param {{
         *   nativeEvent: {
         *     timestamp: number,
         *     speed: number,
         *     accuracy: number,
         *     altitude: number,
         *     longitude: number,
         *     latitude: number,
         *   }
         * }}
         */
        onLocation: PropTypes.func,   
        /**
         * 地图状态变化事件
         *
         * @param {{
         *   nativeEvent: {
         *     longitude: number,
         *     latitude: number,
         *     rotation: number,
         *     zoomLevel: number,
         *     tilt: number,
         *   }
         * }}
         */
        onStatusChange: PropTypes.func,
    
        /**
         * 地图状态变化完成事件
         *
         * @param {{
         *   nativeEvent: {
         *     longitude: number,
         *     latitude: number,
         *     longitudeDelta: number,
         *     latitudeDelta: number,
         *     rotation: number,
         *     zoomLevel: number,
         *     tilt: number,
         *   }
         * }}
         */
        onStatusChangeComplete: PropTypes.func
      };


      name = "AMapCellView";

      /**
       * 动画过渡到某个状态（坐标、缩放级别、倾斜度、旋转角度）
       */
      animateTo(target: MapStatus, duration?: number = 500) {
        this.sendCommand("animateTo", [target, duration]);
      }
    
      /******************通用命令部分 */
    
      /**
       * 地图上添加一组对象
       * @param {*} elementType
       * @param {*} target
       * @param {*} size
       * @param {boolean} visible 是否处于列表选中状态
       */
      addCells(
        fixed: boolen=false,
        target: Array,
        size: number = 72,
        visible: boolen = true
      ) {
        if (target && target.length > 0)
          this.sendCommand("addCells", [
            fixed,
            target,
            size,
            visible
          ]);
      }
    
      /**
       * 地图上添加单个对象
       * @param {*} fixed
       * @param {*} target
       * @param {*} size
       */
      addCell(
        fixed: boolen=false,
        target: object,
        size: number = 72
      ) {
        this.sendCommand("addCell", [fixed, target, size]);
      }
    
      /**
       * 设置对象选中
       * @param {*} id
       */
      selectCell( id: string = "") {
        this.sendCommand("selectCell", [id]);
      }
    
      /**
       * 设置一类对象的是否可见
       * @param {*} fixed
       * @param {*} visible
       */
      changeCellsVisible(
        fixed: boolen=false,
        visible: boolean
      ) {
        this.sendCommand("changeCellsVisible", [fixed, visible]);
      }
    
      changeCellsStyle(fixed: boolen=false, style: object) {
        this.sendCommand("changeCellsStyle", [fixed, style]);
      }
    
      removeCells(fixed: boolen=false) {
        this.sendCommand("removeCells", [fixed]);
      }
    
      changeRenderField(field: string, ranges: Array, size: number = 48) {
        this.sendCommand("changeRenderField", [field, size, ranges]);
      }

      render() {
        const props = { ...this.props };
        if (props.locationStyle) {
          if (props.locationStyle.strokeColor) {
            props.locationStyle.strokeColor = processColor(
              props.locationStyle.strokeColor
            );
          }
          if (props.locationStyle.fillColor) {
            props.locationStyle.fillColor = processColor(
              props.locationStyle.fillColor
            );
          }
        }
        return <AMapCellView {...props} />;
      }
    }
    
    const AMapCellView = requireNativeComponent("AMapCellView", CellMapView);
