import model.Table
import parsing._
import view.StringRenderer

/**
  * Created by giymo11 on 04.04.16.
  */
object Main extends App {
  import ammonite.ops._

  val inputFiles = ls! cwd |? (_.ext == "gen")

  def rollTables(pair: (Seq[Table], Seq[Table])) = Interpreter.rollTables(pair._1, pair._2)

  val names: Seq[String] = inputFiles | (_.name)
  val tables: Seq[Seq[Table]] = inputFiles | read | (_.lines.toSeq) | Parser.getTables
  val mandatory: Seq[Seq[Table]] = tables | (_ |? (table => !table.optional))
  val output = (tables zip mandatory) | rollTables | StringRenderer.renderResults | (_.mkString(System.lineSeparator))

  (names zip output) |! (tuple => write.over(cwd/(tuple._1 + "-result.txt"), tuple._2))
}
