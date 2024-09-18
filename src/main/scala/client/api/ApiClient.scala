package client.api

import domain.exception.ExceptionInfo
import domain.filter.Types.{Experience, Location, ProfessionalRole}
import domain.filter.{Filter, PaginationParameter}
import domain.vacancy.VacancyResponse

trait ApiClient[F[_]] {
  def getVacanciesByFilter(
      filter: Filter,
      paginationParameter: PaginationParameter
  ): F[Either[ExceptionInfo, List[VacancyResponse]]]
  def getVacancyById(id: BigInt): F[Option[VacancyResponse]]
  def getLocationIds: F[Either[ExceptionInfo, List[Location]]]
  def getProfessionalRoleIds: F[Either[ExceptionInfo, List[ProfessionalRole]]]
  def getExperienceValues: F[Either[ExceptionInfo, List[Experience]]]
}
