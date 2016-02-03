package classification

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, TokensAnnotation, SentencesAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.apache.spark.streaming.dstream.DStream
import twitter4j.Status

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

object TextCleaner {
  private def createNLPPipeline(): StanfordCoreNLP = {
    val props:Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    new StanfordCoreNLP(props)
  }

  private def isOnlyLetters(str: String): Boolean = {
    str.forall(c => Character.isLetter(c))
  }

  private def plainTextToLemmas(text: String,stopWords: Set[String],
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
        lemmas += lemma.toLowerCase
      }
    }
    lemmas
  }

  def clear(stream: DStream[Status],stopWords: Set[String]) = {
    stream.flatMap(status=>plainTextToLemmas(status.getText,stopWords,createNLPPipeline()))
  }
}
