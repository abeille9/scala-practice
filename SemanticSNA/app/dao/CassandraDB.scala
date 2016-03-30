package dao

import classification.SparkConfig
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

object CassandraDB{
  def insertConcepts(rdd: RDD[(String, Seq[String], scala.collection.Map[String,Double])]) = {

    rdd.saveToCassandra("SemanticSNA","concept")
  }


  def createSchema(conf:SparkConf): Unit = {
    CassandraConnector(conf).withSessionDo { session =>
      session.execute(s"DROP KEYSPACE IF EXISTS SemanticSNA")
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS SemanticSNA WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
      session.execute(s"""
             CREATE TABLE IF NOT EXISTS SemanticSNA.Concepts (
                term text PRIMARY KEY,
                tfidf double,
                category text,
            ) WITH CLUSTERING ORDER BY (category ASC)
           """)
    }
  }

//  def insertConcepts(concept: String, terms:Seq[String],tfidf:Map) = {
//
//  }
}
