package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, DebugInterface, ObservationInterface, QueryInterface}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units, Upgrades}
import com.github.shaad.sc2bot.common.{Action, Condition, Sequence, StateFullSequence, Watcher}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.ZergExtensions._


class EarlyBuildOrder(commonMacroNodes: CommonMacroNodes, macroActionQueue: MacroActionQueue)(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {

  import commonMacroNodes._

  val earlyBuildOrder = StateFullSequence(
    buildDrone,
    buildOverlord,
    buildDrone,
    buildDrone,
    buildDrone,
    Watcher(buildExtractor) { () => startGasDependentSequence() },
    buildSpawningPool,
    buildDrone,
    buildDrone,
    buildDrone,
    buildDrone,
    Sequence(
      Condition { () => obs.getMinerals >= 200 },
      Action { () =>
        val nextLocation = nextExpansion

        action.unitCommand(closestFreeDrone(nextLocation), Abilities.MOVE, nextLocation, false)
      }
    ),
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
        Condition(() => myUnits(_.getType == Units.ZERG_EXTRACTOR).exists(_.getBuildProgress == 1.0)),
        Action { () =>
          myUnits(_.getType == Units.ZERG_DRONE, _.getAbility == Abilities.HARVEST_GATHER).take(3)
            .foreach(d => action.unitCommand(d, Abilities.HARVEST_GATHER, myUnits(_.getType == Units.ZERG_EXTRACTOR).next(), false))
        }
      ),
      // after gather enough gas, unassign workers 2/3 from gas
      Sequence(
        Condition(() => canAfford(Upgrades.ZERGLING_MOVEMENT_SPEED)),
        Action { () =>
          unassignDronesFromVespene(myUnits(_.getType == Units.ZERG_EXTRACTOR).next(), 2)
        }
      ),
      // then pool is built, start move upgrade
      Sequence(
        Condition(() => myUnits(_.getType == Units.ZERG_SPAWNING_POOL).exists(_.getBuildProgress == 1.0) && canAfford(Upgrades.ZERGLING_MOVEMENT_SPEED)),
        Action { () =>
          action.unitCommand(myUnits(_.getType == Units.ZERG_SPAWNING_POOL).next(),
            Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST, false)
        }
      )
    )
  }

}
