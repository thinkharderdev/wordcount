package dev.thinkharder

import cats.effect._
import fs2.Stream
import io.circe._
import os.SubProcess
import fs2._

import java.time.Instant
import scala.concurrent.duration._

case class Event(eventType: String, data: String, timestamp: Instant)

object Event {
  implicit val decoder: Decoder[Event] = new Decoder[Event] {
    final override def apply(c: HCursor): Decoder.Result[Event] = {
      for {
        eventType    <- c.downField("event_type").as[String]
        data         <- c.downField("data").as[String]
        epochSeconds <- c.downField("timestamp").as[Long]
      } yield Event(eventType, data, Instant.ofEpochSecond(epochSeconds))
    }
  }
}

object Blackbox {
  private val WINDOW = 60.seconds

  private def spawnProcess[F[_]: Sync]: F[SubProcess] = Sync[F].delay(os.proc("blackbox").spawn())

  private def destroyProcess[F[_]: Sync](proc: SubProcess): F[Unit] = Sync[F].delay(proc.destroyForcibly())

  private def countWords[F[_]]: Pipe[F, Chunk[Event], Count] = {
    _.map(batch => Counter.countWords(batch))
  }

  def eventStream[F[_]: ConcurrentEffect: Timer](
      blocker: Blocker
  )(implicit cs: ContextShift[F]): Stream[F, Count] = {
    Stream
      .bracket[F, SubProcess](spawnProcess[F])(destroyProcess[F])
      .flatMap { proc =>
        io
          .readInputStream[F](Sync[F].delay(proc.stdout), 2048, blocker)
          .through(text.utf8Decode[F])
          .through(text.lines[F])
          .map(parser.parse)
          .collect { case Right(parsed) =>
            parsed.as[Event]
          }
          .collect { case Right(event) =>
            event
          }
          .groupWithin[F](Int.MaxValue, WINDOW)
          .through(countWords[F])
      }
  }

}
