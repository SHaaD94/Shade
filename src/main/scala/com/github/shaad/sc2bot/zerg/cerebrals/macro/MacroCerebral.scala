package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{Abilities, Units, Upgrade}
import com.github.shaad.sc2bot.common.{Action, Condition, Selector, Sequence, StateFullSequence}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.CommonNodes
import com.github.shaad.sc2bot.zerg.ZergExtensions._


/**
 * 1. Controls workers
 * 2. Constructs buildings
 * 3. Creates new expansions
 */
class MacroCerebral(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug : DebugInterface) {
  val commonNodes = new CommonNodes

  import commonNodes._

  def serve(): Unit = {
    if (!earlyBuildOrder.finished()) {
      earlyBuildOrder.evalNext()
    }

  }

  def onDroneIdle(unitInPool: UnitInPool): Unit = {}

  def onUpgradeCompleted(upgrade: Upgrade): Unit = {}

  def onBuildingCompleted(unitInPool: UnitInPool): Unit = {}
}

case class MacroGoal()