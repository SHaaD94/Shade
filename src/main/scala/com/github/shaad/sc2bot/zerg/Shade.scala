package com.github.shaad.sc2bot.zerg

import java.util

import com.github.ocraft.s2client.bot.ClientError
import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.{Units, Upgrade}
import com.github.ocraft.s2client.protocol.observation.Alert
import com.github.shaad.sc2bot.BotBase
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.cerebrals.`macro`.MacroCerebral
import com.github.shaad.sc2bot.zerg.cerebrals.micro.MicroCerebral

class Shade extends BotBase {
  private val macroCerebral = new MacroCerebral()
  private val microCerebral = new MicroCerebral()

  override def onStep(): Unit = {
    macroCerebral.serve()
    microCerebral.serve()
  }

  override def onGameFullStart(): Unit = {}

  override def onGameStart(): Unit = {}

  override def onGameEnd(): Unit = {}

  override def onUnitDestroyed(unitInPool: UnitInPool): Unit = {}

  override def onUnitCreated(unitInPool: UnitInPool): Unit = {}

  override def onUnitIdle(unitInPool: UnitInPool): Unit = {
    if (unitInPool.getType == Units.ZERG_DRONE) {
      macroCerebral.onDroneIdle(unitInPool)
    }
  }

  override def onUpgradeCompleted(upgrade: Upgrade): Unit =
    macroCerebral.onUpgradeCompleted(upgrade)

  override def onBuildingConstructionComplete(unitInPool: UnitInPool): Unit =
    macroCerebral.onBuildingCompleted(unitInPool)

  override def onNydusDetected(): Unit = microCerebral.onNydusDetected()

  override def onNuclearLaunchDetected(): Unit = microCerebral.onNuclearLaunchDetected()

  override def onUnitEnterVision(unitInPool: UnitInPool): Unit = microCerebral.onUnitEnterVision(unitInPool)
}
