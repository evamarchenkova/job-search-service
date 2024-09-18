package config

import scala.concurrent.duration.FiniteDuration

case class RetryConfig(retryDuration: FiniteDuration, amount: Int)
