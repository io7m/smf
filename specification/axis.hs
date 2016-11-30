module Axis where

data Axis
  = PositiveX
  | PositiveY
  | PositiveZ
  | NegativeX
  | NegativeY
  | NegativeZ

data AxisName
  = AxisX
  | AxisY
  | AxisZ

axisName :: Axis -> AxisName
axisName PositiveX = AxisX
axisName NegativeX = AxisX
axisName PositiveY = AxisY
axisName NegativeY = AxisY
axisName PositiveZ = AxisZ
axisName NegativeZ = AxisZ

data AxisSystem = AxisSystem {
  axis_right   :: Axis,
  axis_up      :: Axis,
  axis_forward :: Axis
}

axisValid :: (Axis, Axis, Axis) -> Bool
axisValid (right, up, forward) =
  case (axisName right, axisName up, axisName forward) of
    (AxisX, AxisY, AxisZ) -> True
    (AxisZ, AxisX, AxisY) -> True
    (AxisY, AxisZ, AxisX) -> True
    _                     -> False

data WindingOrder
  = WindingOrderClockwise
  | WindingOrderCounterClockwise

data CoordinateSystem = CoordinateSystem {
  coords_axes  :: AxisSystem,
  coords_order :: WindingOrder
}