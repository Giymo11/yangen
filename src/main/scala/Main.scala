
/**
  * Created by giymo11 on 04.04.16.
  */
object Main extends App {
  import ammonite.ops._

  import scala.util.Random

  val generators = ls! cwd |? (_.ext == "gen") | (file => (file.name, interpret(read(file).lines.toSeq).mkString("\n")))
  generators |! (tuple => write(cwd / (tuple._1 + "-result.txt"), tuple._2))

  def interpretResult(generator: Seq[String], result: Int): Seq[String] = {
    val line = generator.head

    if(line.isEmpty)
      Seq("No Result for roll " + result + " found!")
    else {
      val roll = line.split(" ").head
      val (min: Int, max: Int) = if(roll.contains("-")) { // format = "1-5:"
        val min = roll.split("-").head.takeWhile(_.isDigit).toInt
        val max = roll.split("-").drop(1).head.takeWhile(_.isDigit).toInt
        (min, max)
      } else {  // format = "3"
        val res = roll.takeWhile(_.isDigit).toInt
        (res, res)
      }

      if(result >= min && result <= max)
        Seq(line.dropWhile(_ != ' ')) ++ interpret(generator.tail)
      else
        interpretResult(generator.tail, result)
    }
  }

  def interpretRoll(generator: Seq[String]): Seq[String] = {
    val line = generator.head

    if(line.contains("Roll ")) { // format = "Roll 1d20."
      val sides = line.split(" ").drop(1).head.split("d").drop(1).head.takeWhile(_.isDigit).toInt // this should be the number of sides on the die
      val result = Random.nextInt(sides) + 1
      interpretResult(generator.tail, result)
    } else
      Seq("Roll Syntax not correct!")
  }

  def interpret(generator: Seq[String]): Seq[String] = generator match {
    case gen if gen.head.startsWith("-- ") => Seq("", gen.head.drop(3)) ++ interpretRoll(gen.tail)
    case gen if gen.size == 1 => Seq()
    case _ => interpret(generator.tail)
  }
}
