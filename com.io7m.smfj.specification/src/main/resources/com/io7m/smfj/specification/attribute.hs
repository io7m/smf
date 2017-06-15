module Attribute where

data ComponentType =
    IntegerSigned
  | IntegerUnsigned
  | FloatingPoint

data AttributeType = AttributeType {
  attribute_component_type  :: ComponentType,
  attribute_component_count :: Word32,
  attribute_component_size  :: Word32
}

data Attribute = Attribute {
  attribute_name :: String,
  attribute_type :: AttributeType
}
