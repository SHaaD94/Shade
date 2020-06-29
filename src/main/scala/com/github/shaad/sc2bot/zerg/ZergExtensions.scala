package com.github.shaad.sc2bot.zerg

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.Units
import com.github.shaad.sc2bot.common.Extensions._

object ZergExtensions {
  def larvas(implicit observationInterface: ObservationInterface): Iterator[UnitInPool] =
    myUnits(_.getType == Units.ZERG_LARVA)
}
