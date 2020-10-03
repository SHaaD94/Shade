package com.github.shaad.sc2bot.zerg

import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.{Units, Upgrade}
import com.github.shaad.sc2bot.BotBase
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.cerebrals.`macro`.MacroCerebral
import com.github.shaad.sc2bot.zerg.cerebrals.micro.MicroCerebral

class Shade extends BotBase {
  private lazy val macroCerebral = new MacroCerebral()
  private lazy val microCerebral = new MicroCerebral()

  override def onStep(): Unit = {
    macroCerebral.serve()
    microCerebral.serve()
  }

  override def onGameFullStart(): Unit = {
    // clear debug of previous session
  }

  override def onGameStart(): Unit = {
    obs.getAbilityData(true)
    debug().sendDebug()
  }

  override def onGameEnd(): Unit = {}

  override def onUnitDestroyed(unitInPool: UnitInPool): Unit = {}

  override def onUnitCreated(unitInPool: UnitInPool): Unit = {
    if (unitInPool.getType == Units.ZERG_DRONE) {
      macroCerebral.onDroneIdle(unitInPool)
    } else {
      macroCerebral.onUnitCreated(unitInPool)
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
