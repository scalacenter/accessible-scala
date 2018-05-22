package ch.epfl.scala.accessible

case class Offset(value: Int)

object Offset {
  def apply(in: String): Offset = Offset(in.toInt)
}

object Range {
  def apply(start: String, end: String): Range =
    Range(start.toInt, end.toInt)
}

case class Range(start: Int, end: Int)
