package domain.vacancy

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}
case class Vacancy(id: BigInt)

object Vacancy {
  implicit val vacancyDecoder: Decoder[Vacancy]   = deriveDecoder[Vacancy]
  implicit val vacancyReader: JsonReader[Vacancy] = jsonReader
  implicit val vacancyWriter: JsonWriter[Vacancy] = jsonWriter
  implicit val vacancySchema: Schema[Vacancy]     = Schema.derived.description("Vacancy")
  def toVacancyResponse(vacancy: Vacancy): VacancyResponse = {
    VacancyResponse(vacancy.id)
  }
}
