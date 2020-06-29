package com.github.shaad.sc2bot

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, ObservationInterface, QueryInterface}
import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.observation.raw.MapState
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.shaad.sc2bot.common.LazyLogging

import scala.jdk.CollectionConverters._

class BotBase extends S2Agent with LazyLogging {
  protected implicit def obs: ObservationInterface = observation()

  protected implicit def queryInterface: QueryInterface = query()

  protected implicit def controlInterface: ControlInterface = control()

  protected implicit def actionInterface: ActionInterface = actions()

  protected lazy val expansionLocations: Seq[Point] = query().calculateExpansionLocations(observation()).asScala.toSeq
  protected lazy val heightMap = initHeightMap()

  protected def map(): MapState = observation().getRawObservation.getRaw.get().getMapState

  private def initHeightMap(): Map[Point2d, Float] = {
    val p0 = obs.getGameInfo.getStartRaw.get().getMapSize
    (0 until p0.getX * 2).flatMap(x =>
      (0 until p0.getY * 2).map { y =>
        val actualX = x / 2.0f
        val actualY = y / 2.0f

        Point2d.of(actualX, actualY)
      }
    ).filter(observation().isPathable)
      .map(x => x -> (observation().terrainHeight(x) * 100).toInt / 100.0F)
      .toMap
  }
}
