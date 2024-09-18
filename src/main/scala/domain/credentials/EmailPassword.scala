package domain.credentials

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

case class EmailPassword(
    email: String,
    password: String
)

object EmailPassword {
  implicit val emailPasswordDecoder: Decoder[EmailPassword]   = deriveDecoder[EmailPassword]
  implicit val emailPasswordReader: JsonReader[EmailPassword] = jsonReader
  implicit val emailPasswordWriter: JsonWriter[EmailPassword] = jsonWriter
  implicit val emailPasswordSchema: Schema[EmailPassword] = Schema.derived
    .description("Email and password")
}
