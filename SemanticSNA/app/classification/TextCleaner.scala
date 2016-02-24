package classification

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.apache.spark.streaming.dstream.DStream
import twitter4j.{Status, User}

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

object TextCleaner extends Serializable{

  private def createNLPPipeline(): StanfordCoreNLP = {
    val props: Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    new StanfordCoreNLP(props)
  }

  private def plainTextToLemmas(text: String, stopWords: Set[String],
                                pipeline: StanfordCoreNLP): Seq[String] = {
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences;
         token <- sentence.get(classOf[TokensAnnotation])) {
      val lemma = token.get(classOf[LemmaAnnotation])
      if (lemma.length > 2 && !stopWords.contains(lemma)
        && isOnlyLetters(lemma)) {
        lemmas += lemma
      }
    }
    lemmas
  }

  private def isOnlyLetters(str: String): Boolean = {
    str.forall(c => Character.isLetter(c))
  }

  def clear(stream: DStream[Status], stopWords: Set[String]): DStream[(User, Seq[String])] = {
    stream.map(status => (status.getUser, plainTextToLemmas(status.getText, stopWords, createNLPPipeline())))
  }
}

