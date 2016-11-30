import qualified Axis as A

axisOrdinal :: A.Axis -> Integer
axisOrdinal A.PositiveX = 0
axisOrdinal A.PositiveY = 1
axisOrdinal A.PositiveZ = 2
axisOrdinal A.NegativeX = 3
axisOrdinal A.NegativeY = 4
axisOrdinal A.NegativeZ = 5

windingOrdinal :: A.WindingOrder -> Integer
windingOrdinal A.WindingOrderClockwise        = 0
windingOrdinal A.WindingOrderCounterClockwise = 1
