package com.github.shaad.sc2bot.terran

import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.ocraft.s2client.protocol.unit.{Alliance, Unit => SC2Unit}
import com.github.shaad.sc2bot.BotBase
import com.github.shaad.sc2bot.util.Extensions._
import com.github.shaad.sc2bot.util.TerranExtensions._

import scala.jdk.CollectionConverters._

class DumbTerranBot extends BotBase {
  private var attacking = false

  override def onGameStart(): Unit = {
    log.info("Game started")
  }

  override def onUnitIdle(unitInPool: UnitInPool): Unit = {
    if (unitInPool.unit().getType == Units.TERRAN_SCV) {
      val closestMineral = minerals().minBy(_.unit().getPosition.distance(unitInPool))
      actions().unitCommand(unitInPool, Abilities.HARVEST_GATHER_SCV, closestMineral, false)
    }
  }

  override def onStep(): Unit = {
    morphOrbitalCommand()
    buildSupply()
    buildBarracks()
    buildRefinery()
    assignGatheringGas()

    trainSCVs()
    trainMarines()

    throwMules()

    attack()

    lowerDepos()
  }

  def assignGatheringGas() = {
//    ???
  }

  def buildRefinery(): Unit = {
    val refineries = units(_.getType == Units.TERRAN_REFINERY)

    if (refineries.size == 1) return

    actions().unitCommand(myFreeSCVs().head, Abilities.BUILD_REFINERY,
      vespeneGeysers().toSeq.minBy(_.distance(observation().getStartLocation)), false)
  }

  def throwMules(): Unit = {
    myUnits(_.getType == Units.TERRAN_ORBITAL_COMMAND).foreach { o =>
      val closestMineral = minerals().minBy(o.distance(_))
      actions().unitCommand(o, Abilities.EFFECT_CALL_DOWN_MULE, closestMineral, false)
    }
  }

  def morphOrbitalCommand(): Unit = {
    myUnits(_.getType == Units.TERRAN_COMMAND_CENTER).foreach(x => actions().unitCommand(x, Abilities.MORPH_ORBITAL_COMMAND, true))
  }

  private def lowerDepos(): Unit = myUnits(_.getType == Units.TERRAN_SUPPLY_DEPOT).foreach(
    actions().unitCommand(_, Abilities.MORPH_SUPPLY_DEPOT_LOWER, false)
  )

  private def attack(): Unit = {
    val marines = myAliveUnits {
      _.unit().getType == Units.TERRAN_MARINE
    }

    if (marines.size > 20) attacking = true

    if (attacking) {
      val enemyLocation = expansionLocations.maxBy(_.distance(observation().getStartLocation))
      marines.foreach {
        actions().unitCommand(_, Abilities.ATTACK_ATTACK, enemyLocation.toPoint2d, false)
      }
    }
  }

  private def trainMarines(): Unit = {
    myAliveUnits {
      _.getUnit.get().getType == Units.TERRAN_BARRACKS
    }
      .map(_.getUnit.get()).filter(o => o.getOrders.isEmpty)
      .foreach(actions().unitCommand(_, Abilities.TRAIN_MARINE, false))
  }

  private def trainSCVs(): Unit = {
    val ccs = myCommandCenters()

    if (myUnits(_.getType == Units.TERRAN_SCV).size > 25 * ccs.size) return
    ccs.foreach { cc =>
      val nearestMineral = minerals().minBy(_.distance(cc))

      actions.unitCommand(cc, Abilities.RALLY_COMMAND_CENTER, nearestMineral, false)

      if (!cc.getOrders.asScala.exists(_.getAbility == Abilities.TRAIN_SCV)) {
        actions().unitCommand(cc, Abilities.TRAIN_SCV, false)
      }
    }
  }

  private def buildSupply() = {
    val cc: SC2Unit = myCommandCenters().head

    val gameLoop = observation.getGameLoop

    val notAssignedBuildingOfDepo = mySCVs()
      .flatMap(_.getOrders.asScala.find(_.getAbility == Abilities.BUILD_SUPPLY_DEPOT)).isEmpty

    if (gameLoop % 50 == 0 && notAssignedBuildingOfDepo) {
      val scv: SC2Unit = myFreeSCVs().head
      if (observation().getFoodUsed + 8 >= observation().getFoodCap && observation().getMinerals >= mineralCost(Units.TERRAN_SUPPLY_DEPOT)) {
        var (x, y) = (-cc.getRadius, cc.getRadius)
        var t = 0
        while (!canBuild(Units.TERRAN_SUPPLY_DEPOT, cc.getPosition.toPoint2d.add(x, y))) {
          x = x + 1
          if (t == 5) {
            y = y + 1
            x = -cc.getRadius
            t = 0
          }
          t = t + 1
        }

        actions().unitCommand(scv, Abilities.BUILD_SUPPLY_DEPOT, cc.getPosition.toPoint2d.add(x, y), true)
      }
    }
  }

  private def buildBarracks(): Unit = {
    val allCCs = myAliveUnits { u =>
      u.unit().getType == Units.TERRAN_COMMAND_CENTER
    }

    val numberOfBarracks = myAliveUnits { u =>
      u.unit().getType == Units.TERRAN_BARRACKS
    }.size

    if (numberOfBarracks >= allCCs.size * 4) return

    var startingPoint = observation().getStartLocation.toPoint2d.add(0, 5)
    while (!canBuild(Units.TERRAN_BARRACKS, startingPoint)) {
      startingPoint = startingPoint.add(3, 0)
    }
    val scv = myFreeSCVs().head

    actions().unitCommand(scv, Abilities.BUILD_BARRACKS, startingPoint, false)
  }
}