import scala.util.Try

import scala.util.Random

/**
  * Created by giymo11 on 04.04.16.
  */
object Main extends App {
  import ammonite.ops._

  val generators = ls! cwd |? (_.ext == "gen") | (file => (file.name, rollTables(getTables(read(file).lines.toSeq)).mkString("\n")))
  generators |! (tuple => write over(cwd / (tuple._1 + "-result.txt"), tuple._2))

  // 1. Get tables to roll at
  // 2. Roll in order, add new (optional) tables to roll at. Record / modify rolls
  // 3. Print all results

  class Table(val name: String, val dice: Int, val map: Seq[Tuple2[Range, String]], val optional: Boolean, val distinct: Boolean) {
    def get(roll: Int): Option[String] = map.find(_._1.contains(roll)).map(_._2)
    val shortName = name.dropWhile(!_.isLetterOrDigit).takeWhile(_.isLetterOrDigit)
    def copy(
              name: String = this.name,
              dice: Int = this.dice,
              map: Seq[Tuple2[Range, String]] = this.map,
              optional: Boolean = this.optional,
              distinct: Boolean = this.distinct) = new Table(name, dice, map, optional, distinct)
  }

  object Table {
    def apply(name: String, dice: Int, raw: Seq[String], optional: Boolean, distinct: Boolean): Table = {
      val resultMap = raw.map(line => {
        val roll = line.split(" ").head
        val (min: Int, max: Int) = if(roll.contains("-")) { // format = "1-5:"
        val min = roll.split("-").head.takeWhile(_.isDigit).toInt
          val max = roll.split("-").drop(1).head.takeWhile(_.isDigit).toInt
          (min, max)
        } else {  // format = "3"
          val res = roll.takeWhile(_.isDigit).toInt
          (res, res)
        }
        (min to max, line.dropWhile(_ != ' ').dropWhile(_.isWhitespace))
      })
      new Table(name, dice, resultMap, optional, distinct)
    }
  }

  def getTables(raw: Seq[String]): Seq[Table] = {

    def getTable(input: Seq[String]): Option[Table] = {
      val table = input.takeWhile(line => !line.isEmpty)

      if(table(0).startsWith("--") && table(1).startsWith("Roll ")) {
        val name = table(0).dropWhile(!_.isLetterOrDigit)
        val dice = table(1).split(" ").drop(1).head.split("d").drop(1).head.takeWhile(_.isDigit).toInt
        val optional = table(0).startsWith("----")
        val distinct = table(0).takeWhile(_ == '-').length % 2 != 0
        Some(Table(name, dice, table.drop(2), optional, distinct))
      } else
        None
    }

    val input = raw.dropWhile(!_.startsWith("--"))

    if(input.isEmpty)
      Seq()
    else {
      val firstTable: Option[Table] = getTable(input)
      firstTable.toSeq ++ getTables(input.tail)
    }
  }

  def rollTables(tables: Seq[Table]) = {
    val mandatory = tables.filter(!_.optional)
    val rolls: Map[Table, Int] = mandatory.map(table => table -> (Random.nextInt(table.dice) + 1)).toMap

    // find mandatory tables
    // roll
    // interpret results
    // roll optionals / change results (IN ORDER)

    var results: Seq[(Table, Int)] = Seq()

    def interpretLine(line: String): Unit = {
      val parsed = line.split("##").zipWithIndex
      val extras = parsed.filter(_._2 % 2 != 0).map(_._1.trim) // extracts everything like "##x y##
      val normals = parsed.filter(_._2 % 2 == 0).map(_._1.trim)

      //println("interpreting " + line)

      extras.foreach(extra => {
        val segments = extra.split(" ")
        val tableName = segments(0)
        val optTable = tables.find(_.shortName.equalsIgnoreCase(tableName))

        optTable.foreach(newTable =>
          if (segments.length > 1)
            segments(1) match {
              case change if change.startsWith("+") || change.startsWith("-") =>
                results = results.map(tuple =>
                  if (tuple._1 == newTable) {
                    val newRoll = tuple._2 + change.toInt
                    newTable -> Math.max(1, Math.min(newTable.dice, newRoll))
                  }
                  else tuple
                )
              case action if action.equalsIgnoreCase("roll") =>
                if (newTable.distinct) results = results.filterNot(_._1.shortName == newTable.shortName)
                val newRoll = Random.nextInt(newTable.dice) + 1
                results = results :+ (newTable/*.copy(optional = true)*/ -> newRoll)
                if (!newTable.distinct) newTable.get(newRoll).foreach(interpretLine)
              case amount if Try(amount.toInt).isSuccess && amount.toInt == 0 =>
                results = results.filterNot(_._1.shortName == newTable.shortName)
              case amount if Try(amount.toInt).isSuccess =>
                if (newTable.distinct) results = results.filterNot(_._1.shortName == newTable.shortName)
                val newRoll = amount.toInt
                results = results :+ (newTable -> newRoll)
                if (!newTable.distinct) newTable.get(newRoll).foreach(interpretLine)
            }
          else println("Invalid extra syntax!")
          //rolls = rolls.updated(newTable, Random.nextInt(newTable.dice) + 1)
        )
      })
    }

    for(table <- mandatory) {
      rolls.get(table).foreach(roll => {
        results = results :+ (table -> roll)

        rolls.get(table).flatMap(x => table.get(x)) match {
          case Some(s) => interpretLine(s)
          case None => println(s"No roll in ${table.shortName} for ${rolls.get(table)}")
        }
      })
    }

    val lines = for{
      roll <- results
      table = roll._1
      line <- table.get(roll._2)
      parsed = line.split("##").zipWithIndex.filter(_._2 % 2 == 0).map(_._1.trim).mkString(" ")
      header = if(!table.optional) table.name else ""
    } yield header + (if(!header.isEmpty && !parsed.isEmpty) "\n" else "") + "\t" + parsed

    val cleaned = lines.filter(_.length > 3)

    //println(cleaned.zip(cleaned.map(_.length)))

    cleaned
  }
}
