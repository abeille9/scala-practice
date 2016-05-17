package dao

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import config.SparkConfig
import models.Concept
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

import scala.collection.mutable

class CassandraDB extends Serializable with SparkConfig{
  def write(category: String, termFreqs: mutable.HashMap[String, Int]) = {
    val dateToSave = termFreqs.map(row => (row._1,row._2,category))
    val collection = sc.parallelize(dateToSave.toSeq)
    collection.saveToCassandra("semanticsna","test", SomeColumns("term","tfidf", "category"))
  }

  def insertConceptsTest(seq: Seq[Concept]) = {
    val collection = sc.parallelize(seq)
    collection.saveToCassandra("semanticsna","test", SomeColumns("term", "tfidf","category"))
  }

  def test(): Unit = {

    val collection = sc.parallelize(Seq(("cat", " ",2), ("fox", " ",2)))
    collection.saveToCassandra("semanticsna","test", SomeColumns("term", "category","tfidf"))
    val rdd = sc.cassandraTable("semanticsna","test")
    rdd.collect().foreach(println)
  }

  def insertConcepts(rdd: RDD[(String, Seq[String], scala.collection.Map[String,Double])]) = {

    rdd.saveToCassandra("semanticsna","concept")
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
