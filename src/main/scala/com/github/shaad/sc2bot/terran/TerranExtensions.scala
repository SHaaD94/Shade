package com.github.shaad.sc2bot.terran

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}


object TerranExtensions {

  import com.github.shaad.sc2bot.common.Extensions._

  implicit def mySCVs(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    myAliveUnits(_.unit().getType == Units.TERRAN_SCV)
  }

  implicit def myFreeSCVs(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    myAliveUnits(u => u.unit().getType == Units.TERRAN_SCV && !u.getOrders.asScala.exists(_.getAbility != Abilities.HARVEST_GATHER))
  }

  implicit def myCommandCenters(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    myAliveUnits(u => u.unit().getType == Units.TERRAN_COMMAND_CENTER || u.unit().getType == Units.TERRAN_ORBITAL_COMMAND || u.unit().getType == Units.TERRAN_PLANETARY_FORTRESS)
  }
}