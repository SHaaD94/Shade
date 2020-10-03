package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{Abilities, Units, Upgrade}
import com.github.ocraft.s2client.protocol.spatial.Point

import scala.jdk.CollectionConverters._
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.ZergExtensions._

/**
 * 1. Controls workers
 * 2. Constructs buildings
 * 3. Creates new expansions
 */
class MacroCerebral()(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {
  private lazy val expansionLocations: Seq[Point] = query.calculateExpansionLocations(obs).asScala.toSeq
  private val macroActionQueue = new MacroActionQueue()

  private val resourceManager = new ResourceManager
  private val commonNodes = new CommonMacroNodes(resourceManager, expansionLocations)
  private val goalManager = new GoalManager(macroActionQueue, commonNodes)

  import commonNodes._

  macroActionQueue.addSequence(new EarlyBuildOrder(resourceManager, commonNodes, macroActionQueue).earlyBuildOrder)

  def serve(): Unit = {
    goalManager.process()
    macroActionQueue.executeNext()
  }

  def onDroneIdle(unitInPool: UnitInPool): Unit = {
    val hatcheries = mainBuildings
    if (hatcheries.nonEmpty) {
      val closestHatchery = hatcheries.minBy(_.distance(unitInPool))
      action.unitCommand(unitInPool, Abilities.HARVEST_GATHER, minerals().minBy(_.distance(closestHatchery)), false)
    }
  }

  def onUpgradeCompleted(upgrade: Upgrade): Unit = {}

  def onUnitCreated(unitInPool: UnitInPool): Unit = {
    if (unitInPool.getType == Units.ZERG_QUEEN) {
      val hatcheries = mainBuildings.toSeq
      if (myUnits(Units.ZERG_QUEEN).size < hatcheries.size * 3) {
        macroActionQueue.addSequence(
          buildQueen(hatcheries.minBy(_.distance(unitInPool)))
        )
      }
    }
  }

  def onBuildingCompleted(unitInPool: UnitInPool): Unit = {
    // whenever hatchery is built, we need a queen in it
    if (unitInPool.getType == Units.ZERG_HATCHERY) {
      macroActionQueue.addSequence(buildQueen(unitInPool))
    }
  }
}
