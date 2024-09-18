package client.api.clients

import cats.MonadThrow
import client.api.ApiClient
import common.controller.RetryUtils
import domain.exception.ExceptionInfo
import domain.filter.Types.{Experience, ProfessionalRole}
import domain.filter.{Filter, PaginationParameter}
import domain.vacancy.VacancyResponse
import retry.{Sleep, retryingOnSomeErrors}

class RetryingApiClient[F[_]: MonadThrow: Sleep](apiClient: ApiClient[F], retryUtils: RetryUtils[F])
    extends ApiClient[F] {
  override def getVacanciesByFilter(
      filter: Filter,
      paginationParameter: PaginationParameter
  ): F[Either[ExceptionInfo, List[VacancyResponse]]] =
    retryingOnSomeErrors[Either[ExceptionInfo, List[VacancyResponse]]](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError
    )(apiClient.getVacanciesByFilter(filter, paginationParameter))

  override def getVacancyById(id: BigInt): F[Option[VacancyResponse]] =
    retryingOnSomeErrors[Option[VacancyResponse]](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError
    )(apiClient.getVacancyById(id))

  def getLocationIds: F[Either[ExceptionInfo, List[BigInt]]] =
    retryingOnSomeErrors[Either[ExceptionInfo, List[BigInt]]](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError
    )(apiClient.getLocationIds)

  override def getProfessionalRoleIds: F[Either[ExceptionInfo, List[ProfessionalRole]]] =
    retryingOnSomeErrors[Either[ExceptionInfo, List[ProfessionalRole]]](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError
    )(apiClient.getProfessionalRoleIds)

  override def getExperienceValues: F[Either[ExceptionInfo, List[Experience]]] =
    retryingOnSomeErrors[Either[ExceptionInfo, List[Experience]]](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError
    )(apiClient.getExperienceValues)
}
