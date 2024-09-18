package repository

import domain.user.User

trait UserRepository[F[_]] {
  def create(user: User): F[Long]
  def emailExists(email: String): F[Boolean]
  def getUserOption(email: String): F[Option[User]]
}
