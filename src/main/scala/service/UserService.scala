package service

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.std.UUIDGen
import cats.syntax.functor._
import cats.syntax.flatMap._
import domain.exception.ExceptionInfo
import domain.credentials.{Credentials, EmailPassword}
import domain.exception.{AuthorizationExceptionInfo, UserExceptionInfo}
import domain.user.{User, UserResponse}
import repository.{UserRepository, UserVacancyRepository}

trait UserService[F[_]] {
  def register(
      emailPassword: EmailPassword
  ): F[Either[UserExceptionInfo, UserResponse]]
  def authorization(emailPassword: EmailPassword): F[Either[AuthorizationExceptionInfo, User]]
  def saveVacancy(
      user: User,
      vacancyId: BigInt
  ): F[Either[ExceptionInfo, Unit]]
}

object UserService {
  private class Impl[F[_]: UUIDGen: Sync](
      vacancyService: VacancyService[F],
      userRepository: UserRepository[F],
      userVacancyRepository: UserVacancyRepository[F]
  ) extends UserService[F] {
    override def register(
        emailPassword: EmailPassword
    ): F[Either[UserExceptionInfo, UserResponse]] = {
      userRepository
        .emailExists(emailPassword.email)
        .ifM(
          Applicative[F].pure(Left(UserExceptionInfo("User with such email already exists"))),
          for {
            id          <- UUIDGen[F].randomUUID
            credentials <- Credentials.fromEmailPassword(emailPassword)
            user = User(id, emailPassword.email, credentials)
            _ <- userRepository.create(user)
          } yield Right(user.toUserResponse)
        )
    }

    override def saveVacancy(user: User, vacancyId: BigInt): F[Either[ExceptionInfo, Unit]] =
      vacancyService
        .isVacancyIdValid(vacancyId)
        .ifM(
          userVacancyRepository.saveVacancy(user.id, vacancyId.toString()).map(Right(_)),
          Applicative[F].pure(Left(UserExceptionInfo("No such vacancy")))
        )

    override def authorization(
        emailPassword: EmailPassword
    ): F[Either[AuthorizationExceptionInfo, User]] =
      userRepository
        .getUserOption(emailPassword.email)
        .flatMap(
          {
            case Some(user) =>
              Credentials
                .doesPasswordMatch(
                  emailPassword.password,
                  user.credentials.passwordHash,
                  user.credentials.salt
                )
                .ifF(Right(user), Left(AuthorizationExceptionInfo("Wrong password")))
            case None =>
              Applicative[F].pure(Left(AuthorizationExceptionInfo("User doesn't exist")))
          }
        )
  }

  def make[F[_]: UUIDGen: Sync](
      vacancyService: VacancyService[F],
      userRepository: UserRepository[F],
      userVacancyRepository: UserVacancyRepository[F]
  ): UserService[F] =
    new Impl[F](vacancyService, userRepository, userVacancyRepository)
}
