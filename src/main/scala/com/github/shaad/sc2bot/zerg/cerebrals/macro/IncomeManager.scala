package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, DebugInterface, ObservationInterface, QueryInterface}
import com.github.shaad.sc2bot.common.StateFullSequence

import scala.collection.mutable

/*
   1. Ensure gas income is enough or not too much
   2. Move workers between expansions
 */
class IncomeManager(goalManager: GoalManager, actionQueue: MacroActionQueue)(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {
//  val requestedVespene =
//  def submitResourceRequest(minerals: Int, vespene: Int) = {
//
//  }
}
