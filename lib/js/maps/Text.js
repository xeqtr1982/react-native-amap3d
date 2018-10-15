import React from "react";
import PropTypes from "prop-types";
import {
  Platform,
  requireNativeComponent,
  StyleSheet,
  ViewPropTypes,
  View
} from "react-native";
import { LatLng, Point } from "../PropTypes";
import Component from "../Component";

const style = StyleSheet.create({
  overlay: {
    position: "absolute"
  }
});

export default class Text extends Component<any> {
  static propTypes = {
    ...ViewPropTypes,

    /**
     * 坐标
     */
    coordinate: LatLng.isRequired,

    /**
     * 标题
     */
    text: PropTypes.string.isRequired,

    /**
     * 描述，显示在标题下方
     */
    description: PropTypes.string,

    /**
     * 默认图标颜色
     */
    color: Platform.select({
      android: PropTypes.oneOf([
        "azure",
        "blue",
        "cyan",
        "green",
        "magenta",
        "orange",
        "red",
        "rose",
        "violet",
        "yellow"
      ]),
      ios: PropTypes.oneOf(["red", "green", "purple"])
    }),

    /**
     * 字体颜色,Int
     */
    fontColor: PropTypes.number,

    /**
     * 字体大小,Float
     */
    fontSize: PropTypes.number,

    visible: PropTypes.bool,

    /**
     * 标记id
     */
    elementKey: PropTypes.string.isRequired,

    /**
     * 标记类别，枚举
     *  mark_GPS: 1,
     *  mark_Event: 2,
     *  mark_Cell: 3,
     *  mark_NaviOrignal:5,//导航起点
     *  mark_NaviDest:6,//导航终点
     */
    elementType: PropTypes.number.isRequired,

    /**
     * 层级
     */
    zIndex: PropTypes.number,

    /**
     * 对齐方式
     */
    align: Point,

    /**
     * 覆盖物偏移位置
     *
     * @link http://a.amap.com/lbs/static/unzip/iOS_Map_Doc/AMap_iOS_API_Doc_3D/interface_m_a_annotation_view.html#a78f23c1e6a6d92faf12a00877ac278a7
     * @platform ios
     */
    centerOffset: Point,

    /**
     * 是否禁用点击，默认不禁用
     */
    clickDisabled: PropTypes.bool,

    /**
     * 点击事件
     */
    onPress: PropTypes.func
  };

  name = "AMapText";

  render() {
    return <AMapText {...this.props} />;
  }
}

const AMapText = requireNativeComponent("AMapText", Text);
