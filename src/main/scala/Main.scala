import model.Table
import parsing.Interpreter.TableRoll
import parsing.{Interpreter, Parser}

/**
  * Created by giymo11 on 04.04.16.
  */
object Main extends App {
  import ammonite.ops._

  val inputFiles = ls! cwd |? (_.ext == "gen")

  val names = inputFiles | (_.name)
  val results = inputFiles | read | (_.lines.toSeq) | Parser.getTables | rollTables

  (names zip results) |! (tuple => write.over(cwd/(tuple._1 + "-result.txt"), tuple._2))

  def rollTables(tables: Seq[Table]): Seq[String] = {

    val mandatory = tables.filter(!_.optional)
    val results: Seq[TableRoll] = Interpreter.rollTables(tables, mandatory)

    val lines = for{
      roll <- results
      table = roll._1
      line = table.get(roll._2).getOrElse("")
      header = if(!table.optional) table.name else ""
      nothing = println(table.shortName)
      parsed = line.split("##").zipWithIndex.filter(_._2 % 2 == 0).map(_._1).mkString(" ").trim
    } yield header + (if(!header.isEmpty && !parsed.isEmpty) "\n" else "") + "\t" + parsed

    lines.filter(_.length > 1)
  }
}
