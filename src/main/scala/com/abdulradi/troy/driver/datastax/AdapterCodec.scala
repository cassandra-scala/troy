package com.abdulradi.troy.driver.datastax

import java.nio.ByteBuffer
import com.datastax.driver.core.{ProtocolVersion, TypeCodec}

abstract class AdapterCodec[T, U](innerCodec: TypeCodec[T], javaClass: Class[U]) extends TypeCodec[U](innerCodec.getCqlType, javaClass) {
  def outMap(t: T): U
  def inMap(u: U): T

  override def serialize(value: U, protocolVersion: ProtocolVersion): ByteBuffer =
    innerCodec.serialize(inMap(value), protocolVersion)

  override def parse(value: String): U =
    outMap(innerCodec.parse(value))

  override def format(value: U): String =
    innerCodec.format(inMap(value))

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): U =
    outMap(innerCodec.deserialize(bytes, protocolVersion))
}

