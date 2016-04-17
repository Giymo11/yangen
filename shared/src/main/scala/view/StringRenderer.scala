package view

import parsing.Interpreter.TableRoll

/**
  * Created by giymo11 on 16.04.16.
  */
object StringRenderer {
  def renderResults(results: Seq[TableRoll]): Seq[String] = {
    val lines = for {
      roll <- results
      table = roll._1
      line = table.get(roll._2).getOrElse("")
      header = if (!table.optional) table.name else ""
      nothing = println(table.shortName)
      parsed = line.split("##").zipWithIndex.filter(_._2 % 2 == 0).map(_._1).mkString(" ").trim
    } yield header + (if (!header.isEmpty && !parsed.isEmpty) "\n" else "") + "\t" + parsed

    lines.filter(_.length > 1)
  }
}
