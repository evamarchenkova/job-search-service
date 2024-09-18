package domain.user

import domain.tethys.TethysInstances
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import java.util.UUID

case class UserResponse(id: UUID)

object UserResponse extends TethysInstances {
  implicit val userResponseDecoder: Decoder[UserResponse]   = deriveDecoder[UserResponse]
  implicit val userResponseReader: JsonReader[UserResponse] = jsonReader
  implicit val userResponseWriter: JsonWriter[UserResponse] = jsonWriter
  implicit val userResponseSchema: Schema[UserResponse] =
    Schema.derived.description("User response")
}
