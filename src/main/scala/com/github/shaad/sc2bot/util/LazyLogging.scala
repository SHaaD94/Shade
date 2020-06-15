package com.github.shaad.sc2bot.util

import org.slf4j.{Logger, LoggerFactory}

trait LazyLogging {
  lazy val log: Logger = LoggerFactory.getLogger(this.getClass)

}
