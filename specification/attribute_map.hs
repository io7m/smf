import qualified Attribute as A

attributeTypeOrdinal :: A.ComponentType -> Integer
attributeTypeOrdinal A.IntegerSigned   = 0
attributeTypeOrdinal A.IntegerUnsigned = 1
attributeTypeOrdinal A.FloatingPoint   = 2