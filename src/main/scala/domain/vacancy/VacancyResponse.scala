package domain.vacancy

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto._

case class VacancyResponse(id: BigInt)

object VacancyResponse {
  implicit val vacancyResponseDecoder: Decoder[VacancyResponse]   = deriveDecoder[VacancyResponse]
  implicit val vacancyResponseReader: JsonReader[VacancyResponse] = jsonReader
  implicit val vacancyResponseWriter: JsonWriter[VacancyResponse] = jsonWriter
  implicit val vacancyResponseSchema: Schema[VacancyResponse] = Schema.derived
    .description("Vacancy response")
}
