package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{Abilities, Units, Upgrades}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.common._
import com.github.shaad.sc2bot.zerg.ZergExtensions._


class EarlyBuildOrder(resourceManager: ResourceManager,
                      commonMacroNodes: CommonMacroNodes,
                      macroActionQueue: MacroActionQueue)(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {

  import commonMacroNodes._

  val earlyBuildOrder = StateFullSequence(
    buildDrone,
    buildOverlord,
    buildDrone,
    buildDrone,
    buildDrone,
    Watcher(buildExtractor) { id => startGasDependentSequence() },
    constructBuild(Units.ZERG_SPAWNING_POOL),
    buildDrone,
    buildDrone,
    buildDrone,
    buildDrone,
    buildHatchery(nextExpansion),
    buildQueen(mainBuildings.minBy(_.distance(obs.getStartLocation))),
    buildUnit(Units.ZERG_ZERGLING),
    buildUnit(Units.ZERG_ZERGLING),
    buildUnit(Units.ZERG_ZERGLING),
    buildOverlord,
  )

  private def startGasDependentSequence(): Unit = {
    macroActionQueue.addSequence(
      // then extractor is built, assign workers
      Sequence(
        Condition(id => myUnits(_.getType == Units.ZERG_EXTRACTOR).exists(_.getBuildProgress == 1.0)),
        Action { id =>
          myUnits(_.getType == Units.ZERG_DRONE, _.getAbility == Abilities.HARVEST_GATHER).take(3)
            .foreach(d => action.unitCommand(d, Abilities.HARVEST_GATHER, myUnits(_.getType == Units.ZERG_EXTRACTOR).next(), false))
        }
      ),
      // after gather enough gas, unassign workers 2/3 from gas
      Sequence(
        Action { id => resourceManager.reserveResources(id, Upgrades.ZERGLING_MOVEMENT_SPEED) },
        Condition(id => resourceManager.enoughResources(id)),
        Action { id =>
          unassignDronesFromVespene(myUnits(_.getType == Units.ZERG_EXTRACTOR).next(), 2)
        }
      ),
      // then pool is built, start move upgrade
      Sequence(
        Condition(id => haveBuilding(Units.ZERG_SPAWNING_POOL)),
        Action(id => resourceManager.reserveResources(id, Units.ZERG_ZERGLING)),
        Condition(id => resourceManager.removeReservationIfEnough(id)),
        Action { id =>
          action.unitCommand(myUnits(_.getType == Units.ZERG_SPAWNING_POOL).next(),
            Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST, false)
        }
      )
    )
  }

}
