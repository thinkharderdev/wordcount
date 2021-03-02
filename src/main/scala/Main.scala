package dev.thinkharder

import cats.effect._
import fs2._
import fs2.concurrent._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Blocker[IO]
      .use { blocker =>
        val program = for {
          queue <- Stream.eval(Queue.synchronous[IO, Count])
          _ <- Blackbox
            .eventStream[IO](blocker)
            .through(queue.enqueue)
          server <- HttpServer.stream[IO](queue)
        } yield server

        program.compile.drain
      }
      .as(ExitCode.Success)
  }
}
