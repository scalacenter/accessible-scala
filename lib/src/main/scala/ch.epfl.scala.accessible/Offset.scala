package ch.epfl.scala.accessible

case class Offset(value: Int)

object Offset {
  def apply(in: String): Offset = Offset(in.toInt)
}