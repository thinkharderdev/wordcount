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
          countStream = Blackbox
            .wordCountStream[IO](blocker)
            .through(queue.enqueue)
          server <- HttpServer.stream[IO](queue).concurrently(countStream)
        } yield server

        program.compile.drain
      }
      .as(ExitCode.Success)
  }
}
