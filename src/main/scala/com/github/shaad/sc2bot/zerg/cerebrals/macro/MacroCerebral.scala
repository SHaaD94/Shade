package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{Units, Upgrade}
import com.github.ocraft.s2client.protocol.spatial.Point

import scala.jdk.CollectionConverters._
import com.github.shaad.sc2bot.common.Extensions._

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

  def onDroneIdle(unitInPool: UnitInPool): Unit = {}

  def onUpgradeCompleted(upgrade: Upgrade): Unit = {}

  def onUnitCreated(unitInPool: UnitInPool): Unit = {

  }

  def onBuildingCompleted(unitInPool: UnitInPool): Unit = {
    println(unitInPool)
    // whenever hatchery is built, we need a queen in it
    if (unitInPool.getType == Units.ZERG_HATCHERY) {
      macroActionQueue.addSequence(buildQueen(unitInPool))
    }
  }
}
