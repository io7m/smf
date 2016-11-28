data Axis
  = PositiveX
  | PositiveY
  | PositiveZ
  | NegativeX
  | NegativeY
  | NegativeZ

data AxisSystem = AxisSystem {
  axis_right   :: Axis,
  axis_up      :: Axis,
  axis_forward :: Axis
}

data WindingOrder
  = WindingOrderClockwise
  | WindingOrderCounterClockwise

data CoordinateSystem = CoordinateSystem {
  coords_axes  :: AxisSystem,
  coords_order :: WindingOrder
}
