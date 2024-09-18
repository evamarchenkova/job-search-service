package controller

import common.controller.Controller
import domain.filter.Types.{Experience, Location, ProfessionalRole}
import service.VacancyService
import sttp.tapir.endpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir._
import domain.exception.{ApiClientExceptionInfo, ExceptionInfo, UserExceptionInfo}
import domain.filter.{Filter, PaginationParameter}
import domain.vacancy.VacancyResponse
import sttp.model.StatusCode
import sttp.tapir.json.tethysjson.jsonBody

class VacancyController[F[_]](
    vacancyService: VacancyService[F]
) extends Controller[F] {
  val getVacanciesByFilter: ServerEndpoint[Any, F] = {
    val validator: Validator[List[BigInt]] =
      Validator.custom(list => ValidationResult.validWhen(list.forall(_ > 0)))
    endpoint.get
      .summary("A list of vacancies that meet the search parameters")
      .in(
        "api" / "v1" / "vacancies"
          / query[Option[String]]("prompt")
          / query[List[Location]]("location").validate(validator)
          / query[List[ProfessionalRole]]("professional_role").validate(validator)
          / query[Option[Experience]]("experience")
          / query[Option[BigInt]]("salary").validateOption(Validator.min(BigInt(0)))
          / query[Option[Int]]("page").validateOption(Validator.min(0)).default(Some(0))
          / query[Option[Int]]("perPage").validateOption(Validator.min(0)).default(Some(10))
      )
      .mapIn(input =>
        input match {
          case (prompt, location, professionalRole, experience, salary, page, perPage) =>
            (
              Filter(prompt, location, professionalRole, experience, salary),
              PaginationParameter(page, perPage)
            )
        }
      ) { case (filter, paginationParameter) =>
        (
          filter.prompt,
          filter.location,
          filter.professionalRole,
          filter.experience,
          filter.salary,
          paginationParameter.page,
          paginationParameter.perPage
        )
      }
      .errorOut(
        oneOf[ExceptionInfo](
          oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[UserExceptionInfo])),
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[ApiClientExceptionInfo])
          )
        )
      )
      .out(jsonBody[List[VacancyResponse]])
      .serverLogic({ case (filter, paginationParameter) =>
        vacancyService.getByFilter(filter, paginationParameter)
      })
  }

  override def endpoints: List[ServerEndpoint[Any, F]] = List(getVacanciesByFilter)
}

object VacancyController {
  def make[F[_]](vacancyService: VacancyService[F]): VacancyController[F] =
    new VacancyController[F](vacancyService)
}
