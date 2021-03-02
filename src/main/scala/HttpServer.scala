package dev.thinkharder

import cats.effect.{ConcurrentEffect, Sync, Timer}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global
import fs2.Stream
import fs2.concurrent.Queue

trait WordCount[F[_]] {
  def get: F[Option[Count]]
}

object WordCount {
  private var _cached: Option[Count] = none

  private def updateCache(count: Count): Unit = _cached = count.some

  def apply[F[_]: Sync](queue: Queue[F, Count]): WordCount[F] = new WordCount[F] {
    override def get: F[Option[Count]] = {
      queue.tryDequeue1.flatMap {
        case Some(count) => count.some.pure[F] <* Sync[F].delay(updateCache(count))
        case None        => _cached.pure[F]
      }
    }
  }
}

object Routes {

  def wordCountRoute[F[_]: Sync](C: WordCount[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case GET -> Root =>
      for {
        count    <- C.get
        response <- count.map(c => Ok(c)).getOrElse(NotFound())
      } yield response
    }
  }

}

object HttpServer {
  def stream[F[_]: ConcurrentEffect](queue: Queue[F, Count])(implicit T: Timer[F]): Stream[F, Nothing] = {
    val app = Routes.wordCountRoute(WordCount[F](queue)).orNotFound
    BlazeServerBuilder[F](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(app)
      .serve
      .drain
  }
}
