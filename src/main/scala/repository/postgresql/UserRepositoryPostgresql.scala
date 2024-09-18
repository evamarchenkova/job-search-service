package repository.postgresql

import cats.effect.kernel.MonadCancelThrow
import cats.implicits.toFunctorOps
import domain.user.User
import doobie.Transactor
import io.getquill.SnakeCase
import io.getquill.doobie.DoobieContext
import doobie.implicits._
import repository.UserRepository

class UserRepositoryPostgresql[F[_]: MonadCancelThrow](implicit tr: Transactor[F])
    extends UserRepository[F] {

  private val ctx = new DoobieContext.Postgres(SnakeCase)
  import ctx._

  override def create(user: User): F[Long] =
    run {
      quote(
        querySchema[User]("\"user\"").insertValue(lift(user))
      )
    }.transact(tr)

  override def emailExists(email: String): F[Boolean] =
    run {
      quote(
        querySchema[User]("\"user\"")
          .filter(_.email == lift(email))
          .nonEmpty
      )
    }.transact(tr)

  override def getUserOption(email: String): F[Option[User]] =
    run {
      quote {
        querySchema[User]("\"user\"").filter(_.email == lift(email))
      }
    }.transact(tr).map(_.headOption)
}
