package domain.tethys

import tethys.{JsonReader, JsonWriter}
import java.util.UUID

trait TethysInstances {
  implicit val uuidReader: JsonReader[UUID] = JsonReader[String].map(UUID.fromString)
  implicit val uuidWriter: JsonWriter[UUID] = JsonWriter[String].contramap(_.toString)
}
