package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.shaad.sc2bot.common.StateFullSequence

import scala.collection.mutable

class GoalManager(macroBuildOrders: MacroActionQueue, commonNodes: CommonMacroNodes) {

  import commonNodes._

  private val goalsQueue = new mutable.Queue[MacroGoal]()

  def addGoal(macroGoal: MacroGoal): Unit = {
    goalsQueue.enqueue(macroGoal)
  }

  def process(): Unit = {
    goalsQueue.removeHeadOption() match {
      case None =>
      case Some(goal) => goal match {
        case BuildUnitGoal(unit, number) =>
        case TechnologyGoal(upgrade) =>
        case WorkersOnVespeneGoal(workers) =>
        case ExpansionGoal() => macroBuildOrders.addSequence(StateFullSequence(buildHatchery(nextExpansion.toPoint2d)))
      }
    }
  }
}
