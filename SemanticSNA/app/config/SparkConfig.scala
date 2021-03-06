package config

import org.apache.spark.{SparkConf, SparkContext}

trait SparkConfig {
  val driverPort = 8080
  val driverHost = "localhost"
  val conf = new SparkConf(false) // skip loading external settings
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("firstSparkApp")
    .set("spark.driver.port", s"$driverPort")
    .set("spark.driver.host", s"$driverHost")
    .set("spark.akka.logLifecycleEvents", "true")
    .set("spark.cassandra.connection.host", "127.0.0.1")

  val sc : SparkContext = SparkContext.getOrCreate(conf)

}
