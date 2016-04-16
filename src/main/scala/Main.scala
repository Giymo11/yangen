import scala.collection.immutable.Range.Inclusive
import scala.util.Try
import scala.util.Random

/**
  * Created by giymo11 on 04.04.16.
  */
object Main extends App {
  import ammonite.ops._

  val inputFiles = ls! cwd |? (_.ext == "gen")

  val names = inputFiles | (_.name)
  val results = inputFiles | read | (_.lines.toSeq) | getTables | rollTables

  (names zip results) |! (tuple => write over(cwd / (tuple._1 + "-result.txt"), tuple._2))

  class Table(val name: String, val dice: Int, val map: Seq[Tuple2[Range, String]], val optional: Boolean, val distinct: Boolean) {

    def roll(): Int =
      Random.nextInt(dice) + 1

    def get(roll: Int): Option[String] =
      map.find(_._1.contains(roll)).map(_._2)

    val shortName =
      name.dropWhile(!_.isLetterOrDigit).takeWhile(_.isLetterOrDigit)

    def copy(
              name: String = this.name,
              dice: Int = this.dice,
              map: Seq[Tuple2[Range, String]] = this.map,
              optional: Boolean = this.optional,
              distinct: Boolean = this.distinct) = new Table(name, dice, map, optional, distinct)
  }

  object Table {

    def apply(name: String, dice: Int, raw: Seq[String], optional: Boolean, distinct: Boolean): Table =
      new Table(name, dice, interpretRanges(raw), optional, distinct)

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

  def getTables(raw: Seq[String]): Seq[Table] = {

    def getTable(input: Seq[String]): Option[Table] = {
      val table = input.takeWhile(!_.isEmpty)

      if(table.head.startsWith("--") && table.tail.head.startsWith("Roll ")) {
        val name = table.head.dropWhile(!_.isLetterOrDigit)
        val dice = table.tail.head.split(" ").tail.head.split("d").tail.head.takeWhile(_.isDigit).toInt
        val optional = table.head.startsWith("----")
        val distinct = table.head.takeWhile(_ == '-').length % 2 != 0
        Some(Table(name, dice, table.drop(2), optional, distinct))
      } else
        None
    }

    val input = raw.dropWhile(!_.startsWith("--"))

    if(input.isEmpty) Seq()
    else getTable(input).toSeq ++ getTables(input.tail)
  }

  def rollTables(tables: Seq[Table]): Seq[String] = {
    val mandatory = tables.filter(!_.optional)
    val rolls: Map[Table, Int] = mandatory.map(table => table -> table.roll()).toMap

    var results: Seq[(Table, Int)] = Seq()

    def addRoll(table: Table, roll: Int): Unit = {
      results = results :+ (table -> roll)
      if (!table.distinct) table.get(roll).foreach(interpretLine)
    }

    def deleteResult(table: Table) =
      results = results.filterNot(_._1.shortName == table.shortName)

    def interpretLine(line: String): Unit = {
      val parsed = line.split("##").zipWithIndex
      val extras = parsed.filter(_._2 % 2 != 0).map(_._1.trim) // extracts everything like "##x y##

      extras.foreach(extra => {
        val segments = extra.split(" ")
        val tableName = segments(0)
        val optTable = tables.find(_.shortName.equalsIgnoreCase(tableName))

        def clamp(value: Int, min: Int, max: Int) = Math.max(min, Math.min(max, value))

        optTable.foreach(newTable =>
          if (segments.length > 1)
            segments.tail.head match {
              case change if change.startsWith("+") || change.startsWith("-") =>
                results = results.map(tuple =>
                  if (tuple._1 == newTable) newTable -> clamp(tuple._2 + change.toInt, 1, newTable.dice)
                  else tuple
                )
              case action if action.equalsIgnoreCase("roll") =>
                if (newTable.distinct) deleteResult(newTable)
                addRoll(newTable, newTable.roll())
              case amount if Try(amount.toInt).isSuccess && amount.toInt == 0 =>
                deleteResult(newTable)
              case amount if Try(amount.toInt).isSuccess =>
                deleteResult(newTable)
                addRoll(newTable, amount.toInt)
            }
          else println("Invalid syntax!")
        )
      })
    }

    for(table <- mandatory)
      rolls.get(table).foreach(roll => addRoll(table, roll))

    val lines = for{
      roll <- results
      table = roll._1
      line <- table.get(roll._2)
      parsed = line.split("##").zipWithIndex.filter(_._2 % 2 == 0).map(_._1.trim).mkString(" ")
      header = if(!table.optional) table.name else ""
    } yield header + (if(!header.isEmpty && !parsed.isEmpty) "\n" else "") + "\t" + parsed

    lines.filter(_.length > 1)
  }
}
