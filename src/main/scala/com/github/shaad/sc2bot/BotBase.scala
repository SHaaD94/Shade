package com.github.shaad.sc2bot

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, QueryInterface}
import com.github.ocraft.s2client.protocol.observation.raw.MapState
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.shaad.sc2bot.util.LazyLogging

import scala.jdk.CollectionConverters._

class BotBase extends S2Agent with LazyLogging {
  protected implicit def obs: ObservationInterface = observation()

  protected implicit def queryInterface: QueryInterface = query()

  protected lazy val expansionLocations: Seq[Point] = query().calculateExpansionLocations(observation()).asScala.toSeq

  protected def map(): MapState = observation().getRawObservation.getRaw.get().getMapState

}
