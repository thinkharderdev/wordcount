package dev

import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

package object thinkharder {
  type Count = Map[String, Map[String, Int]]

  implicit def entityEncoder[F[_]]: EntityEncoder[F, Count] = {
    jsonEncoderOf[F, Count]
  }

  val EmptyCount: Map[String, Map[String, Int]] = Map.empty[String, Map[String, Int]]
}
