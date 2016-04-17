package parsing

import model.Table

import scala.collection.mutable
import scala.util.Try

/**
  * Created by giymo11 on 16.04.16.
  */
object Interpreter {

  def clamp(value: Int, min: Int, max: Int) = Math.max(min, Math.min(max, value))

  type TableRoll = (Table, Int)

  def changeResult(results: mutable.Buffer[TableRoll], table: Table, change: Int): Unit = {
    val indices = results.zipWithIndex.filter(_._1._1.shortName == table.shortName)
    indices.foreach(tuple => {
      val tableRoll = tuple._1
      results(tuple._2) = tableRoll._1 -> clamp(tableRoll._2 + change, 1, tableRoll._1.dice)
    })
  }

  def deleteResult(results: mutable.Buffer[TableRoll], table: Table) =
    results --= results.filter(_._1.shortName == table.shortName)

  def interpretLine(results: mutable.Buffer[TableRoll], line: String)(implicit tables: Seq[Table]): Unit = {
    val parsed = line.split("##").zipWithIndex
    val extras = parsed.filter(_._2 % 2 != 0).map(_._1.trim) // extracts everything like "##x y##

    extras.foreach(extra => {
      val segments = extra.split(" ")
      val tableName = segments(0)
      val optTable = tables.find(_.shortName.equalsIgnoreCase(tableName))

      optTable.foreach(newTable =>
        if (segments.length > 1)
          segments.tail.head match {
            case change if change.startsWith("+") || change.startsWith("-") =>
              changeResult(results, newTable, change.toInt)
            case action if action.equalsIgnoreCase("roll") =>
              if (newTable.distinct) deleteResult(results, newTable)
              addRoll(results, newTable, newTable.roll())
            case amount if Try(amount.toInt).isSuccess && amount.toInt == 0 =>
              deleteResult(results, newTable)
              results += (newTable -> 0)
            case amount if Try(amount.toInt).isSuccess =>
              if (newTable.distinct) deleteResult(results, newTable)
              addRoll(results, newTable, amount.toInt)
          }
        else println("Invalid syntax!")
      )
    })
  }

  def addRoll(results: mutable.Buffer[TableRoll], table: Table, roll: Int)(implicit tables: Seq[Table]): Unit = {
    results += (table -> roll)
    table.get(roll).foreach(interpretLine(results, _))
  }

  def rollTables(tables: Seq[Table], mandatory: Seq[Table]): mutable.Buffer[TableRoll] = {
    val results: mutable.Buffer[TableRoll] = mutable.Buffer()

    mandatory.foreach(table => addRoll(results, table, table.roll())(tables))
    results
  }
}
