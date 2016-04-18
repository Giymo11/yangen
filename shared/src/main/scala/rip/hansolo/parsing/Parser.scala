package rip.hansolo.parsing

import rip.hansolo.model.Table

import scala.collection.immutable.Range.Inclusive

/**
  * Created by giymo11 on 16.04.16.
  */
object Parser {
  def getTables(raw: Seq[String]): Seq[Table] = {

    def getTable(input: Seq[String]): Option[Table] = {
      val table = input.takeWhile(!_.isEmpty)

      if(table.head.startsWith("--") && table.tail.head.startsWith("Roll ")) {
        val name = table.head.dropWhile(!_.isLetterOrDigit)
        val dice = table.tail.head.split(" ").tail.head.split("d").tail.head.takeWhile(_.isDigit).toInt
        val optional = table.head.startsWith("----")
        val distinct = table.head.takeWhile(_ == '-').length % 2 != 0
        val answers = table.drop(2)
        Some(Table(name, dice, interpretRanges(answers), optional, distinct))
      } else
        None
    }

    val input = raw.dropWhile(!_.startsWith("--"))

    if(input.isEmpty) Seq()
    else getTable(input).toSeq ++ getTables(input.tail)
  }

  def interpretRanges(raw: Seq[String]): Seq[(Inclusive, String)] = raw.map(line => {
    val range = line.split(" ").head
    val answer = line.split(" ").tail.mkString(" ")

    val (min: Int, max: Int) = if (range.contains("-")) {
      // format = "1-5:"
      val min = range.split("-").head.takeWhile(_.isDigit).toInt
      val max = range.split("-").tail.head.takeWhile(_.isDigit).toInt
      (min, max)
    } else {
      // format = "3:"
      val res = range.takeWhile(_.isDigit).toInt
      (res, res)
    }

    (min to max, answer)
  })
}
