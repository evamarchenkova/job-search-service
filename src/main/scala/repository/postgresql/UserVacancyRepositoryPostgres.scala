package repository.postgresql

import cats.effect.kernel.MonadCancelThrow
import cats.implicits.toFunctorOps
import domain.user.UserVacancy
import doobie.Transactor
import doobie.implicits._
import io.getquill.SnakeCase
import io.getquill.doobie.DoobieContext
import repository.UserVacancyRepository
import java.util.UUID

class UserVacancyRepositoryPostgres[F[_]: MonadCancelThrow](implicit tr: Transactor[F])
    extends UserVacancyRepository[F] {
  private val ctx = new DoobieContext.Postgres(SnakeCase)
  import ctx._

  override def saveVacancy(userId: UUID, vacancyId: String): F[Unit] = {
    run {
      quote {
        query[UserVacancy].insertValue(UserVacancy(lift(userId), lift(vacancyId))).onConflictIgnore
      }
    }.transact(tr).void
  }
}
