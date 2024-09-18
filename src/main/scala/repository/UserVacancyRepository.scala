package repository

import java.util.UUID

trait UserVacancyRepository[F[_]] {
  def saveVacancy(userId: UUID, vacancyId: String): F[Unit]
}
