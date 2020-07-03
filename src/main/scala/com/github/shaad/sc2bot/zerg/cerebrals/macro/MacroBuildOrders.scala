package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.shaad.sc2bot.common.StateFullSequence

class MacroBuildOrders {
  private var ongoingPlans: Seq[StateFullSequence] = Seq()

  def addPlan(plan: StateFullSequence) = {
    ongoingPlans = ongoingPlans :+ plan
  }

  def execute() = {
    val (_, notFinished) = ongoingPlans.partition(_.finished())
    ongoingPlans = notFinished
    ongoingPlans.foreach(_.evalNext())

  }

}
