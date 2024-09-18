import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp}
import client.api.clients.{HttpApiClient, RetryingApiClient}
import com.comcast.ip4s.{Host, Port}
import common.controller.RetryUtilsImpl
import config.AppConfig
import controller.{UserController, VacancyController}
import database.FlywayMigration
import database.transactor.makeTransactor
import doobie.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import repository.postgresql.{UserRepositoryPostgresql, UserVacancyRepositoryPostgres}
import service.{FilterValidationService, UserService, VacancyService}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    implicit def logger[IO[_]: Sync]: Logger[IO] = Slf4jLogger.getLogger[IO]
    val conf                                     = ConfigSource.default.loadOrThrow[AppConfig]
    val sttpBackend                              = AsyncHttpClientCatsBackend[IO]().unsafeRunSync()
    val retryingApiClient = new RetryingApiClient(
      new HttpApiClient(sttpBackend, conf.apiClientConfig),
      new RetryUtilsImpl[IO](logger, conf.retryConfig)
    )
    makeTransactor[IO](conf.database).use { implicit xa: Transactor[IO] =>
      val userRepo = new UserRepositoryPostgresql[IO]
      val userVacancyRepo = new UserVacancyRepositoryPostgres[IO]

      val filterValidationService = FilterValidationService.make[IO](retryingApiClient)
      val vacancyService = VacancyService.make[IO](filterValidationService, retryingApiClient)
      val userService    = UserService.make[IO](vacancyService, userRepo, userVacancyRepo)

      for {
        _ <- FlywayMigration.clean[IO](conf.database)
        _ <- FlywayMigration.migrate[IO](conf.database)
        endpoints <- IO.delay {
          List(
            VacancyController.make(vacancyService),
            UserController.make[IO](userService)
          ).flatMap(_.endpoints)
        }

        swagger = SwaggerInterpreter()
          .fromServerEndpoints[IO](endpoints, "job-search-service", "1.0.0")
        routes = Http4sServerInterpreter[IO]().toRoutes(swagger ++ endpoints)
        port <- IO.fromOption(Port.fromInt(conf.http.port))(new Exception("Invalid http port"))
        _    <- IO.println(s"Go to http://localhost:${conf.http.port}/docs to open SwaggerUI")

        _ <- EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString("localhost").get)
          .withPort(port)
          .withHttpApp(Router("/" -> routes).orNotFound)
          .build
          .useForever
      } yield ()
    }
  }
}
