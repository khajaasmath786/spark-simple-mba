import scopt.OptionParser

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.fpm.FPGrowth

/**
 * Usage: ./bin/spark-submit --class "FPGrowth" --master local[*] fpgrowth-assembly-1.0.jar \
 *   --minSupport 0.4 --minConfidence 0.8 --numPartitions 2 transactions.sample.csv
 */
object FPGrowth {

  case class Params(
    input: String = null,
    minSupport: Double = 0.3,
    minConfidence: Double = 0.7,
    numPartitions: Int = 1) extends AbstractParams[Params]

  def main(args: Array[String]) {
    val defaultParams = Params()

    val parser = new OptionParser[Params]("FPGrowth") {
      head("FP-growth app with Association Rules generation.")
      opt[Double]("minSupport")
        .text(s"minimal support level, default: ${defaultParams.minSupport}")
        .action((x, c) => c.copy(minSupport = x))
      opt[Double]("minConfidence")
        .text(s"minimal confidence level, default: ${defaultParams.minConfidence}")
        .action((x, c) => c.copy(minConfidence = x))
      opt[Int]("numPartitions")
        .text(s"number of partitions, default: ${defaultParams.numPartitions}")
        .action((x, c) => c.copy(numPartitions = x))
      arg[String]("<input>")
        .text("input paths to input data set, whose file format is that each line " +
          "contains a transaction with each item in String and separated by a space")
        .required()
        .action((x, c) => c.copy(input = x))
    }

    parser.parse(args, defaultParams) match {
      case Some(params) => run(params)
      case _ => sys.exit(1)
    }
  }

  def run(params: Params): Unit = {
    val conf = new SparkConf().setAppName(s"FPGrowth and Association Rules generation with $params")
    val sc = new SparkContext(conf)
    val transactions = sc.textFile(params.input).map(_.split(",")).cache()

    println(s"FPGrowth and Association Rules generation with $params")
    println(s"Number of transactions: ${transactions.count()}")

    val model = new FPGrowth()
      .setMinSupport(params.minSupport)
      .setNumPartitions(params.numPartitions)
      .run(transactions)

    println(s"Number of frequent itemsets: ${model.freqItemsets.count()}")

    model.freqItemsets.collect().foreach { itemset =>
      println(itemset.items.mkString("[", ",", "]") + ", " + itemset.freq)
    }

    println("Association Rules:")
    model.generateAssociationRules(params.minConfidence).collect().foreach { rule =>
      println(
        rule.antecedent.mkString("[", ",", "]")
          + " => " + rule.consequent .mkString("[", ",", "]")
          + ", " + rule.confidence)
    }

    sc.stop()
  }
}