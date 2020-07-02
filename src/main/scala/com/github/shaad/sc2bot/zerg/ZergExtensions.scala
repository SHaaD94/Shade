package com.github.shaad.sc2bot.zerg

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.shaad.sc2bot.common.Extensions._

object ZergExtensions {
  def larvas(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(_.getType == Units.ZERG_LARVA)

  def freeDrones(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(_.getType == Units.ZERG_DRONE, _.getAbility == Abilities.HARVEST_GATHER)

  def closestFreeDrone(point: Point)(implicit observationInterface: ObservationInterface): UnitInPool =
    closestFreeDrone(point.toPoint2d)

  def closestFreeDrone(point: Point2d)(implicit observationInterface: ObservationInterface): UnitInPool =
    myUnits(_.getType == Units.ZERG_DRONE, _.getAbility == Abilities.HARVEST_GATHER)
      .minBy(_.getPosition.distance(point))

  def mainBuildings(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(x => x.getType == Units.ZERG_HATCHERY || x.getType == Units.ZERG_LAIR || x.getType == Units.ZERG_HIVE)
}
