package com.github.shaad.sc2bot.zerg

import java.awt.{Polygon, Rectangle}
import scala.jdk.CollectionConverters._
import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, ObservationInterface, QueryInterface}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.shaad.sc2bot.common.{Action, Condition, Sequence, StateFullSequence}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.ZergExtensions._

class CommonNodes(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface) {

  val buildDrone = Sequence(
    Condition { () => enoughFood(Units.ZERG_DRONE) && larvas.nonEmpty && canAfford(Units.ZERG_DRONE) },
    Action { () => action.unitCommand(larvas.next(), Abilities.TRAIN_DRONE, false) }
  )

  val buildOverlord = Sequence(
    Sequence(
      Condition { () => canAfford(Units.ZERG_OVERLORD) },
      Action { () => action.unitCommand(larvas.next(), Abilities.TRAIN_OVERLORD, false) }
    )
  )

  val buildExtractor = Sequence(
    Sequence(
      Condition { () => canAfford(Units.ZERG_EXTRACTOR) && freeDrones.nonEmpty },
      Action { () =>
        val myExtractors = myUnits(_.getType == Units.ZERG_EXTRACTOR).map(_.getPosition).toSeq
        val orderedExtractors = myUnits(_.getType == Units.ZERG_DRONE).flatMap(_.getOrders.asScala)
          .filter(_.getAbility == Abilities.BUILD_EXTRACTOR)
          .map(g => obs.getUnit(g.getTargetedUnitTag.get()).getPosition)

        val busyExtractors = myExtractors ++ orderedExtractors

        val geyser = vespeneGeysers()
          .toSeq
          .filter(g => !busyExtractors.contains(g.getPosition))
          // TODO figure out the problem with pathing distance
          //          .minBy(g => mainBuildings.map(m => query.pathingDistance(m.getPosition, g.getPosition.toPoint2d)).min)
          .minBy(g => mainBuildings.map(m => m.getPosition.distance(g.getPosition)).min)
          .unit()

        val freeDroneClosestToGeyser = freeDrones.minBy(query.pathingDistance(_, geyser.getPosition.toPoint2d))

        action.unitCommand(freeDroneClosestToGeyser, Abilities.BUILD_EXTRACTOR, geyser, false)
      }
    )
  )

  val buildSpawningPool = Sequence(
    Sequence(
      Condition { () => canAfford(Units.ZERG_SPAWNING_POOL) },
      Action { () =>

        val x = obs.getStartLocation.getX - 10.0
        val y = obs.getStartLocation.getY - 10.0

        val poolRadius = buildingSize(Abilities.BUILD_SPAWNING_POOL)

        (0 until 20).foreach { x =>
          (0 until 20).foreach { y =>

          }
        }
        //        Polygon()
        //        val positionForPool = rand

      }
    )
  )

  val earlyBuildOrder = StateFullSequence(
    buildDrone,
    buildOverlord,
    buildDrone,
    buildDrone,
    buildDrone,
    buildExtractor
    //    buildSpawningPool,
    //    buildGas,
  )


}
