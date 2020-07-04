package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.shaad.sc2bot.common.{Node, StateFullSequence}

import scala.collection.mutable

class MacroActionQueue {
  private val ongoingPlans: mutable.Queue[StateFullSequence] = new mutable.Queue[StateFullSequence]()

  def addSequence(nodes: Node*): Unit = {
    addSequence(StateFullSequence(nodes: _*))
  }

  def addSequence(plan: StateFullSequence): Unit = {
    ongoingPlans.enqueue(plan)
  }

  def executeNext(): Unit = {
    while (ongoingPlans.nonEmpty) {
      val plan = ongoingPlans.dequeue()
      if (!plan.finished()) {
        plan.evalNext()
        ongoingPlans.enqueue(plan)
        return
      }
    }
  }
}
