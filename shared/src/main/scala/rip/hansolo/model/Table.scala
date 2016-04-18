package rip.hansolo.model

import scala.util.Random

/**
  * Created by giymo11 on 16.04.16.
  */
case class Table(name: String, dice: Int, map: Seq[(Range, String)], optional: Boolean, distinct: Boolean) {

  def roll(): Int =
    Random.nextInt(dice) + 1

  def get(roll: Int): Option[String] =
    map.find(_._1.contains(roll)).map(_._2)

  val shortName =
    name.dropWhile(!_.isLetterOrDigit).takeWhile(_.isLetterOrDigit)
}