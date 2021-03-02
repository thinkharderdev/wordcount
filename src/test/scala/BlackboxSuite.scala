package dev.thinkharder

import cats.effect._
import munit.CatsEffectSuite
import org.scalacheck._
import org.scalacheck.Prop
import fs2._

import java.time.Instant

class BlackboxSuite extends CatsEffectSuite {

  val eventGenerator: Gen[Event] = for {
    eventType <- Gen.alphaStr
    data      <- Gen.alphaStr
  } yield Event(eventType, data, Instant.EPOCH)

  val eventsGenerator: Gen[List[Event]] =
    Gen.containerOf[List, Event](eventGenerator)

  def eventTypesInStream(events: List[Event]): Set[String] = events.map(_.eventType).toSet

  def eventTypesInCount(count: Count): Set[String] = count.keys.toList.toSet

  def wordsInStream(events: List[Event]): Set[String] = events.map(_.data).toSet

  def wordsInCount(count: Count): Set[String] = {
    count.toList
      .foldRight(Set.empty[String]) { case ((_, wordCounts), agg) =>
        agg ++ wordCounts.keySet
      }
  }

  def totalWordCount(events: List[Event]): Map[String, Int] = {
    events.groupBy(_.data).map { case (word, events) =>
      word -> events.size
    }
  }

  def totalWordCount(count: Count): Map[String, Int] = {
    count.toList
      .flatMap { case (_, eventWordCount) =>
        eventWordCount.toList
      }
      .foldRight(Map.empty[String, Int]) { case ((word, count), agg) =>
        agg + agg.get(word).map(_ + count).map(word -> _).getOrElse(word -> count)
      }
  }

  def wordCountInEventType(eventType: String, word: String, events: List[Event]): Int = {
    events.count(e => e.eventType == eventType && e.data == word)
  }

  def wordCountInEventType(eventType: String, word: String, count: Count): Int = {
    val maybeCount = for {
      group <- count.get(eventType)
      cnt   <- group.get(word)
    } yield cnt

    maybeCount.getOrElse(0)
  }

  test("capture all event types") {
    Prop.forAll(eventsGenerator) { (events: List[Event]) =>
      val count = Stream
        .emits[IO, Event](events)
        .chunkAll
        .through(Blackbox.countWords)
        .compile
        .lastOrError
        .unsafeRunSync()
      eventTypesInStream(events) == eventTypesInCount(count)
    }
  }

  test("capture all words") {
    Prop.forAll(eventsGenerator) { (events: List[Event]) =>
      val count = Stream
        .emits[IO, Event](events)
        .chunkAll
        .through(Blackbox.countWords)
        .compile
        .lastOrError
        .unsafeRunSync()

      wordsInStream(events) == wordsInCount(count)
    }
  }

  test("preserve total word count across event type groups") {
    Prop.forAll(eventsGenerator) { (events: List[Event]) =>
      val count = Stream
        .emits[IO, Event](events)
        .chunkAll
        .through(Blackbox.countWords)
        .compile
        .lastOrError
        .unsafeRunSync()

      totalWordCount(events) == totalWordCount(count)
    }
  }

  test("correct word count by group") {
    Prop.forAll(eventsGenerator) { (events: List[Event]) =>
      val count = Stream
        .emits[IO, Event](events)
        .chunkAll
        .through(Blackbox.countWords)
        .compile
        .lastOrError
        .unsafeRunSync()

      val checks = for {
        eventType <- eventTypesInStream(events)
        word      <- wordsInStream(events)
      } yield wordCountInEventType(eventType, word, events) == wordCountInEventType(eventType, word, count)

      checks.foldRight(true)(_ && _)
    }
  }
}
