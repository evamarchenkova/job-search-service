package config

case class AppConfig(
    database: PostgresConfig,
    http: HttpServer,
    apiClientConfig: ApiClientConfig,
    retryConfig: RetryConfig
)
