package dev.thinkharder

import fs2._

object Counter {
  def countWords(chunk: Chunk[Event]): Count = {
    chunk.toList
      .groupBy(_.eventType)
      .map { case (eventType, events) =>
        val wordCounts = events
          .groupBy(_.data)
          .map { case (word, events) =>
            word -> events.size
          }
        eventType -> wordCounts
      }
  }
}
