package troy.tast

sealed trait UpdateParam
sealed trait Timestamp[value <: UpdateParamValue] extends UpdateParam
sealed trait Ttl[value <: UpdateParamValue] extends UpdateParam

sealed trait UpdateParamValue
sealed trait UpdateValue[value <: String] extends UpdateParamValue //TODO should be Int?
sealed trait UpdateVariable[bindMarker <: BindMarker] extends UpdateParamValue