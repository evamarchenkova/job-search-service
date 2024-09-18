package client.api.clients
import cats.data.EitherT
import cats.effect.kernel.Async
import cats.implicits.{toBifunctorOps, toFunctorOps, toTraverseOps}
import client.api.ApiClient
import config.ApiClientConfig
import domain.exception.{ApiClientExceptionInfo, ExceptionInfo}
import domain.filter.{Filter, PaginationParameter}
import domain.filter.Types.{Experience, Location, ProfessionalRole}
import domain.vacancy.VacancyResponse
import io.circe.Json
import sttp.client3.circe.asJsonAlways
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

class HttpApiClient[F[_]: Async](
    sttpBackend: SttpBackend[F, Any],
    apiClientConfig: ApiClientConfig
) extends ApiClient[F] {
  private def fromFilterToUrl(filter: Filter, paginationParameter: PaginationParameter): Uri =
    uri"${apiClientConfig.vacanciesUrl}?text=${filter.prompt}&area=${filter.location}&professionalRole=${filter.professionalRole}&experience=${filter.experience}&salary=${filter.salary}&page=${paginationParameter.page}&per_page=${paginationParameter.perPage}"

  override def getVacanciesByFilter(
      filter: Filter,
      paginationParameter: PaginationParameter
  ): F[Either[ExceptionInfo, List[VacancyResponse]]] = {
    val getVacanciesByFilterUrl = fromFilterToUrl(filter, paginationParameter)
    println(getVacanciesByFilterUrl)
    basicRequest
      .get(getVacanciesByFilterUrl)
      .response(
        asJsonAlways[Json]
          .map(
            _.bimap(
              _ => ApiClientExceptionInfo("Json deserialization fault"),
              right =>
                right.hcursor
                  .get[List[VacancyResponse]]("items")
                  .leftMap(_ => ApiClientExceptionInfo("No \"items\" field in json"))
            )
          )
      )
      .send(sttpBackend)
      .map(_.body.flatten)
  }

  override def getVacancyById(id: BigInt): F[Option[VacancyResponse]] = {
    val getVacancyByIdUrl: Uri =
      uri"${apiClientConfig.baseUrl}/vacancies/$id"
    basicRequest
      .get(getVacancyByIdUrl)
      .response(asJsonAlways[VacancyResponse].map(_.toOption))
      .send(sttpBackend)
      .map(_.body)
  }

  override def getLocationIds: F[Either[ExceptionInfo, List[Location]]] = {
    val locationsUrl = uri"${apiClientConfig.locationsUrl}"
    basicRequest
      .get(locationsUrl)
      .response(
        asJsonAlways[Json]
          .map(
            _.bimap(
              _ => ApiClientExceptionInfo("Json deserialization fault"),
              json =>
                json
                  .findAllByKey("id")
                  .traverse(x => x.as[Location])
                  .leftMap(_ => ApiClientExceptionInfo("No \"id\" field in json"))
            )
          )
          .map(_.flatten)
      )
      .send(sttpBackend)
      .map(_.body)
  }

  override def getProfessionalRoleIds: F[Either[ExceptionInfo, List[ProfessionalRole]]] = {
    val professionalRolesUrl = uri"${apiClientConfig.professionalRolesUrl}"
    EitherT(
      basicRequest
        .get(professionalRolesUrl)
        .response(asJsonAlways[Json])
        .send(sttpBackend)
        .map(_.body)
    ).map(
      _.hcursor
        .downField("categories")
        .as[List[Json]]
        .bimap(
          _ => ApiClientExceptionInfo("Json deserialization fault"),
          right =>
            right
              .map(
                _.hcursor
                  .downField("roles")
                  .focus match {
                  case Some(json) =>
                    Right(
                      json
                        .findAllByKey("id")
                        .map(
                          _.as[ProfessionalRole]
                            .leftMap(_ => ApiClientExceptionInfo("Json deserialization fault"))
                        )
                        .sequence
                    ).flatten
                  case None => Left(ApiClientExceptionInfo("No \"roles\" field in json"))
                }
              )
              .flatSequence
        )
        .flatten
    ).value
      .map(
        _.leftMap(_ => ApiClientExceptionInfo("Json deserialization fault")).flatten
      )
  }

  override def getExperienceValues: F[Either[ExceptionInfo, List[Experience]]] = {
    val dictionariesUrl = uri"${apiClientConfig.dictionariesUrl}"
    basicRequest
      .get(dictionariesUrl)
      .response(
        asJsonAlways[Json]
          .map(
            _.map(
              _.hcursor
                .downField("experience")
                .focus
                .toRight(ApiClientExceptionInfo("No \"experience\" field in json"))
                .map(
                  _.findAllByKey("id")
                    .map(
                      _.as[Experience].leftMap(_ =>
                        ApiClientExceptionInfo("Json deserialization fault")
                      )
                    )
                    .sequence
                )
            )
              .leftMap(_ => ApiClientExceptionInfo("Json deserialization fault"))
              .flatten
              .flatten
          )
      )
      .send(sttpBackend)
      .map(_.body)
  }
}
