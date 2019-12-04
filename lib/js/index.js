import MapView from './maps/MapView'
import SimpleMapView from './maps/SimpleMapView'
import CellMapView  from './maps/CellMapView'
import Marker from './maps/Marker'
import Text from './maps/Text'
import Polyline from './maps/Polyline'
import Polygon from './maps/Polygon'
import Circle from './maps/Circle'
import HeatMap from './maps/HeatMap'
import MultiPoint from './maps/MultiPoint'
import Offline from './Offline'

MapView.Marker = Marker
MapView.Text = Text
MapView.Polyline = Polyline
MapView.Polygon = Polygon
MapView.Circle = Circle
MapView.HeatMap = HeatMap
MapView.MultiPoint = MultiPoint

export default MapView
export {
  MapView,
  SimpleMapView,
  CellMapView,
  Marker,
  Text,
  Polyline,
  Polygon,
  Circle,
  HeatMap,
  MultiPoint,
  Offline,
}
