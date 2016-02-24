package dao

import classification.SparkConfig
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import org.apache.spark.SparkConf

object CassandraDB{

  val rdd = SparkConfig.sc.cassandraTable("test", "kv")

  def printTestData(): Unit ={
    println(rdd.count)
    println(rdd.first)
    println(rdd.map(_.getInt("value")).sum)
  }

  def createSchema(conf:SparkConf): Unit = {
    CassandraConnector(conf).withSessionDo { session =>
      /*session.execute(s"DROP KEYSPACE IF EXISTS $CassandraKeyspace")
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS $CassandraKeyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
      session.execute(s"""
             CREATE TABLE IF NOT EXISTS $CassandraKeyspace.$CassandraTable (
                topic text,
                interval text,
                mentions counter,
                PRIMARY KEY(topic, interval)
            ) WITH CLUSTERING ORDER BY (interval DESC)
           """)*/
    }
  }
}
