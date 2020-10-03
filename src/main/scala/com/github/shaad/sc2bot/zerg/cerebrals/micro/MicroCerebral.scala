package com.github.shaad.sc2bot.zerg.cerebrals.micro

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.Units
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.cerebrals.micro.queens.QueensManager

/**
 * 1. Controls all units except of workers
 * 2. Attacks
 * 3. Defends
 */
class MicroCerebral(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface) {
  private val queensManager = new QueensManager

  def serve(): Unit = {
    queensManager.assignRoles()

    queensManager.manageQueens()
  }

  def onUnitEnterVision(unitInPool: UnitInPool): Unit = {
  }

  def onNydusDetected(): Unit = {
  }

  def onNuclearLaunchDetected(): Unit = {
    val nukes = units(_.getType == Units.TERRAN_NUKE)
  }
}
