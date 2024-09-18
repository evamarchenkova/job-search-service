package controller

import common.controller.Controller
import domain.credentials.EmailPassword
import domain.exception.{AuthorizationExceptionInfo, ExceptionInfo, UserExceptionInfo}
import sttp.model.StatusCode
import domain.user.{User, UserResponse}
import service.UserService
import sttp.tapir.endpoint
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}
import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.model.UsernamePassword

class UserController[F[_]](userService: UserService[F]) extends Controller[F] {
  private val secureEndpoint
      : PartialServerEndpoint[EmailPassword, User, Unit, AuthorizationExceptionInfo, Unit, Any, F] =
    endpoint
      .securityIn(auth.basic[UsernamePassword]())
      .mapSecurityIn[EmailPassword]((usernamePassword: UsernamePassword) =>
        EmailPassword(usernamePassword.username, usernamePassword.password.getOrElse(""))
      )(emailPassword => UsernamePassword(emailPassword.email, Some(emailPassword.password)))
      .errorOut(statusCode(StatusCode.Unauthorized).and(jsonBody[AuthorizationExceptionInfo]))
      .serverSecurityLogic(userService.authorization)

  private val registerUser: ServerEndpoint[Any, F] =
    endpoint.post
      .summary("Register user")
      .in("api" / "v1" / "register")
      .in(jsonBody[EmailPassword])
      .out(jsonBody[UserResponse])
      .errorOut(statusCode(StatusCode.Conflict).and(jsonBody[UserExceptionInfo]))
      .serverLogic(userService.register)

  private val saveVacancyUser: ServerEndpoint[Any, F] =
    secureEndpoint.post
      .summary("Save interested vacancy")
      .in(
        "api" / "v1" / "saveVacancy" / query[BigInt]("vacancyId")
          .validate(Validator.min(0))
      )
      .errorOutVariant[ExceptionInfo](
        oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[UserExceptionInfo]))
      )
      .serverLogic(user => vacancyId => userService.saveVacancy(user, vacancyId))
  override def endpoints: List[ServerEndpoint[Any, F]] =
    List(registerUser, saveVacancyUser)
}

object UserController {
  def make[F[_]](userService: UserService[F]): UserController[F] =
    new UserController[F](userService)
}
