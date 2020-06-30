package com.github.shaad.sc2bot.zerg

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.shaad.sc2bot.common.Extensions._

object ZergExtensions {
  def larvas(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(_.getType == Units.ZERG_LARVA)

  def freeDrones(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(_.getType == Units.ZERG_DRONE, _.getAbility == Abilities.HARVEST_GATHER)

  def mainBuildings(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(x => x.getType == Units.ZERG_HATCHERY || x.getType == Units.ZERG_LAIR || x.getType == Units.ZERG_HIVE)
}
