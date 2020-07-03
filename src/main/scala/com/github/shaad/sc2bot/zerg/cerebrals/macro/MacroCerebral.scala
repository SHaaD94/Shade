package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{Abilities, Units, Upgrade}
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.shaad.sc2bot.common.{Action, Condition, Selector, Sequence, StateFullSequence, Watcher}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.CommonNodes
import com.github.shaad.sc2bot.zerg.ZergExtensions._

import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
 * 1. Controls workers
 * 2. Constructs buildings
 * 3. Creates new expansions
 */
class MacroCerebral(macroBuildOrders: MacroBuildOrders)(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {
  private lazy val expansionLocations: Seq[Point] = query.calculateExpansionLocations(obs).asScala.toSeq

  private val commonNodes = new CommonNodes(expansionLocations)

  import commonNodes._

  private val earlyBuildOrder = StateFullSequence(
    buildDrone,
    buildOverlord,
    buildDrone,
    buildDrone,
    buildDrone,
    buildExtractor,
    buildSpawningPool,
    Sequence(
      Condition { () => obs.getMinerals >= 200 },
      Action { () =>
        val nextLocation = nextExpansion

        action.unitCommand(closestFreeDrone(nextLocation), Abilities.MOVE, nextLocation, false)
      }
    ),
    buildHatchery(nextExpansion)
  )

  macroBuildOrders.addPlan(earlyBuildOrder)

  def serve(): Unit = {
    macroBuildOrders.execute()
  }

  def onDroneIdle(unitInPool: UnitInPool): Unit = {}

  def onUpgradeCompleted(upgrade: Upgrade): Unit = {}

  def onBuildingCompleted(unitInPool: UnitInPool): Unit = {}
}

case class MacroGoal()
