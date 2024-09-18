package service

import cats.{Applicative, Monad}
import cats.data.EitherT
import cats.implicits.toFunctorOps
import cats.implicits.toTraverseOps
import client.api.ApiClient
import domain.exception.ExceptionInfo
import domain.filter.{Filter, PaginationParameter}
import domain.filter.Types.{Experience, Location, ProfessionalRole}

trait FilterValidationService[F[_]] {
  def eitherIsFilterValid(filter: Filter): F[Either[ExceptionInfo, Boolean]]
  def isPaginationParameterValid(paginationParameter: PaginationParameter): Boolean
}

object FilterValidationService {
  class Impl[F[_]: Monad](apiClient: ApiClient[F]) extends FilterValidationService[F] {
    private val eitherLocationIds: F[Either[ExceptionInfo, Set[Location]]] =
      EitherT(apiClient.getLocationIds).map(_.toSet).value
    private val eitherProfessionalRoleIds: F[Either[ExceptionInfo, Set[ProfessionalRole]]] =
      EitherT(apiClient.getProfessionalRoleIds).map(_.toSet).value
    private val eitherExperienceValues: F[Either[ExceptionInfo, Set[Experience]]] =
      EitherT(apiClient.getExperienceValues).map(_.toSet).value

    override def eitherIsFilterValid(filter: Filter): F[Either[ExceptionInfo, Boolean]] = {
      val eitherLocationFieldValid: F[Either[ExceptionInfo, Boolean]] = filter.location match {
        case Nil => Applicative[F].pure(Right(true))
        case list =>
          eitherLocationIds.map(_.map(locationIds => list.forall(locationIds.contains)))
      }
      val eitherProfessionalRoleFieldValid: F[Either[ExceptionInfo, Boolean]] =
        filter.professionalRole match {
          case Nil => Applicative[F].pure(Right(true))
          case list =>
            eitherProfessionalRoleIds.map(
              _.map(professionalRoleIds => list.forall(professionalRoleIds.contains))
            )
        }
      val eitherExperienceFieldValid: F[Either[ExceptionInfo, Boolean]] =
        filter.experience match {
          case Some(experience) =>
            eitherExperienceValues.map(
              _.map(experienceValues => experienceValues.contains(experience))
            )
          case None => Applicative[F].pure(Right(true))
        }
      List(
        eitherLocationFieldValid,
        eitherProfessionalRoleFieldValid,
        eitherExperienceFieldValid
      ).sequence
        .map {
          case l if l.contains(Right(false)) => Right(false)
          case l if l.exists(_.isLeft)       => l.filter(_.isLeft).head
          case _                             => Right(true)
        }
    }

    override def isPaginationParameterValid(paginationParameter: PaginationParameter): Boolean = {
      val MAXIMAL_DEPTH          = 2000
      val MAXIMAL_PER_PAGE_VALUE = 100
      val DEFAULT_PER_PAGE_VALUE = 20
      paginationParameter match {
        case PaginationParameter(Some(perPage), Some(page))
            if perPage <= MAXIMAL_PER_PAGE_VALUE && perPage * page + perPage <= MAXIMAL_DEPTH =>
          true
        case PaginationParameter(Some(perPage), None) if perPage <= MAXIMAL_PER_PAGE_VALUE => true
        case PaginationParameter(None, Some(page))
            if DEFAULT_PER_PAGE_VALUE * page + DEFAULT_PER_PAGE_VALUE <= MAXIMAL_DEPTH =>
          true
        case PaginationParameter(None, None) => true
        case _                               => false
      }
    }
  }
  def make[F[_]: Monad](apiClient: ApiClient[F]): FilterValidationService[F] =
    new Impl[F](apiClient)
}
