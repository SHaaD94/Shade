package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.protocol.data.{UnitType, Upgrade}

sealed trait MacroGoal

case class BuildUnitGoal(unit: UnitType, number: Int) extends MacroGoal

case class TechnologyGoal(upgrade: Upgrade) extends MacroGoal

case class WorkersOnVespeneGoal(workers: Int) extends MacroGoal

case class ExpansionGoal() extends MacroGoal

