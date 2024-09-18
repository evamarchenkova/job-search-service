package domain.exception

import domain.tethys.TethysInstances
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

sealed trait ExceptionInfo

case class ApiClientExceptionInfo(message: String)     extends ExceptionInfo
case class UserExceptionInfo(message: String)          extends ExceptionInfo
case class AuthorizationExceptionInfo(message: String) extends ExceptionInfo

object ApiClientExceptionInfo extends TethysInstances {
  implicit val apiClientExceptionInfoDecoder: Decoder[ApiClientExceptionInfo] =
    deriveDecoder[ApiClientExceptionInfo]
  implicit val apiClientExceptionInfoReader: JsonReader[ApiClientExceptionInfo] = jsonReader
  implicit val apiClientExceptionInfoWriter: JsonWriter[ApiClientExceptionInfo] = jsonWriter
  implicit val apiClientExceptionInfoSchema: Schema[ApiClientExceptionInfo] =
    Schema.derived.description("API client exception information")
}

object UserExceptionInfo extends TethysInstances {
  implicit val userExceptionInfoDecoder: Decoder[UserExceptionInfo] =
    deriveDecoder[UserExceptionInfo]
  implicit val userExceptionInfoReader: JsonReader[UserExceptionInfo] = jsonReader
  implicit val userExceptionInfoWriter: JsonWriter[UserExceptionInfo] = jsonWriter
  implicit val userExceptionInfoSchema: Schema[UserExceptionInfo] =
    Schema.derived.description("User exception information")
}

object AuthorizationExceptionInfo extends TethysInstances {
  implicit val authorizationExceptionInfoDecoder: Decoder[AuthorizationExceptionInfo] =
    deriveDecoder[AuthorizationExceptionInfo]
  implicit val authorizationExceptionInfoReader: JsonReader[AuthorizationExceptionInfo] = jsonReader
  implicit val authorizationExceptionInfoWriter: JsonWriter[AuthorizationExceptionInfo] = jsonWriter
  implicit val authorizationExceptionInfoSchema: Schema[AuthorizationExceptionInfo] =
    Schema.derived.description("Authorization exception information")
}
