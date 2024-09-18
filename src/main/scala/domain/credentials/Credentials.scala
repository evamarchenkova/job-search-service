package domain.credentials

import cats.effect.kernel.Sync
import cats.effect.std.SecureRandom
import cats.implicits.toFunctorOps
import cats.implicits.toFlatMapOps
import java.security.MessageDigest
import java.util.Base64
case class Credentials(passwordHash: String, salt: String)

object Credentials {
  def doesPasswordMatch[F[_]: Sync](
      password: String,
      passwordHash: String,
      salt: String
  ): F[Boolean] = {
    for {
      md <- Sync[F].delay(MessageDigest.getInstance("SHA-256"))
      _ = md.update(password.getBytes("UTF-8") ++ Base64.getDecoder.decode(salt))
    } yield Base64.getEncoder.encodeToString(md.digest()) == passwordHash
  }

  def fromEmailPassword[F[_]: Sync](emailPassword: EmailPassword): F[Credentials] =
    for {
      (passwordHash, salt) <- hashWithSalt(emailPassword.password)
    } yield Credentials(passwordHash, salt)

  private def hashWithSalt[F[_]: Sync](password: String): F[(String, String)] =
    for {
      random <- SecureRandom.javaSecuritySecureRandom[F]
      salt   <- random.nextString(16)
      md     <- Sync[F].delay(MessageDigest.getInstance("SHA-256"))
      _ = md.update((password ++ salt).getBytes("UTF-8"))
    } yield (
      Base64.getEncoder.encodeToString(md.digest()),
      Base64.getEncoder.encodeToString(salt.getBytes("UTF-8"))
    )
}
