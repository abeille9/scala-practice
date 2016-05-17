package classification

import java.util.Properties

import config.SparkConfig
import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.apache.spark.streaming.dstream.DStream
import twitter4j.{Status, User}

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

object TextCleaner extends Serializable with SparkConfig {
  def clear(text: String) = {
    plainTextToLemmas(text, stopWords, createNLPPipeline())
  }

  def clear(stream: DStream[Status]): DStream[(User, Seq[String])] = {
    stream.map(status => (
      status.getUser,
      plainTextToLemmas(status.getText, stopWords, createNLPPipeline())
      )
    )
  }

  private def createNLPPipeline(): StanfordCoreNLP = {
    val props: Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    new StanfordCoreNLP(props)
  }

  private def plainTextToLemmas(text: String,
                                stopWords: Set[String],
                                pipeline: StanfordCoreNLP): Seq[String] = {
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences;
         token <- sentence.get(classOf[TokensAnnotation])) {
      val lemma = token.get(classOf[LemmaAnnotation])
      if (lemma.length > 2 && !stopWords.contains(lemma) && isOnlyLetters(lemma)) {
        lemmas += lemma.toLowerCase()
      }
    }
    lemmas
  }

  private def isOnlyLetters(str: String): Boolean = {
    str.forall(c => Character.isLetter(c))
  }

  private val stopWords = sc.broadcast(
    scala.io.Source.fromFile("public/data/stopwords.txt").getLines().toSet).value

}

