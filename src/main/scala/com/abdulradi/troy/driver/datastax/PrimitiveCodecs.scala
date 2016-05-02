package com.abdulradi.troy.driver.datastax

import com.datastax.driver.core.TypeCodec


object IntCodec extends AdapterCodec[Integer, Int](TypeCodec.cint(), null) {
  override def outMap(value: Integer): Int = value
  override def inMap(value: Int): Integer = value
}

