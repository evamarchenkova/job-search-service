package service

import cats.{Applicative, Monad}
import cats.implicits.toFunctorOps
import cats.implicits.toFlatMapOps
import client.api.ApiClient
import domain.exception.{ExceptionInfo, UserExceptionInfo}
import domain.filter.PaginationParameter
import domain.filter.Filter
import domain.vacancy.VacancyResponse

trait VacancyService[F[_]] {
  def getByFilter(
      filter: Filter,
      paginationParameter: PaginationParameter
  ): F[Either[ExceptionInfo, List[VacancyResponse]]]
  def isVacancyIdValid(id: BigInt): F[Boolean]
}

object VacancyService {
  private class Impl[F[_]: Monad](
      filterValidationService: FilterValidationService[F],
      apiClient: ApiClient[F]
  ) extends VacancyService[F] {
    override def getByFilter(
        filter: Filter,
        paginationParameter: PaginationParameter
    ): F[Either[ExceptionInfo, List[VacancyResponse]]] =
      if (filterValidationService.isPaginationParameterValid(paginationParameter)) {
        filterValidationService
          .eitherIsFilterValid(filter)
          .flatMap {
            case Left(exception) => Applicative[F].pure(Left(exception))
            case Right(isValid) =>
              if (isValid)
                apiClient.getVacanciesByFilter(filter, paginationParameter)
              else
                Applicative[F].pure(Left(UserExceptionInfo("Incorrect filter")))
          }
      } else {
        Applicative[F].pure(Left(UserExceptionInfo("Wrong pagination parameter")))
      }

    override def isVacancyIdValid(id: BigInt): F[Boolean] =
      apiClient
        .getVacancyById(id)
        .map({
          case Some(_) => true
          case None    => false
        })
  }
  def make[F[_]: Monad](
      filterValidationService: FilterValidationService[F],
      apiClient: ApiClient[F]
  ): VacancyService[F] =
    new Impl[F](filterValidationService, apiClient)
}
