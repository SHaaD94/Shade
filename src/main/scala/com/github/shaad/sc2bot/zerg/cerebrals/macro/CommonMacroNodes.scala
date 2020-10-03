package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import java.awt.geom.{Line2D, Rectangle2D}

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{Abilities, UnitType, Units}
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.common.{Action, Condition, Sequence, StateFullSequence}
import com.github.shaad.sc2bot.zerg.ZergExtensions._

import scala.jdk.CollectionConverters._

class CommonMacroNodes(resourceManager: ResourceManager, val expansionLocations: Seq[Point])(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {
  val buildDrone = buildUnit(Units.ZERG_DRONE)
  val buildOverlord = buildUnit(Units.ZERG_OVERLORD)

  val buildExtractor = Sequence(
    Sequence(
      Action { id => resourceManager.reserveResources(id, Units.ZERG_EXTRACTOR) },
      Condition { id => freeDrones.nonEmpty && resourceManager.removeReservationIfEnough(id) },
      Action { id =>
        val myExtractors = myUnits(_.getType == Units.ZERG_EXTRACTOR).map(_.getPosition).toSeq
        val orderedExtractors = myUnits(_.getType == Units.ZERG_DRONE).flatMap(_.getOrders.asScala)
          .filter(_.getAbility == Abilities.BUILD_EXTRACTOR)
          .map(g => obs.getUnit(g.getTargetedUnitTag.get()).getPosition)

        val busyExtractors = myExtractors ++ orderedExtractors

        val geyser = vespeneGeysers()
          .filter(g => !busyExtractors.contains(g.getPosition))
          .minBy(g => mainBuildings.map(m => pathingDistance(m.getPosition.toPoint2d.sub(buildingRadius(Abilities.BUILD_HATCHERY), 0F), g.getPosition.toPoint2d.sub(buildingRadius(Abilities.BUILD_EXTRACTOR), 0F))).min)
          .unit()

        action.unitCommand(closestFreeDrone(geyser.getPosition), Abilities.BUILD_EXTRACTOR, geyser, false)
      }
    )
  )
  def constructBuild(unitType: UnitType) = Sequence(
    Sequence(
      Action { id => resourceManager.reserveResources(id, Units.ZERG_SPAWNING_POOL) },
      Condition { id => resourceManager.removeReservationIfEnough(id) },
      Action { id =>
        val startLocation = obs.getStartLocation

        val poolRadius = buildingRadius(abilityToBuild(unitType))

        // will be divided by 2
        val distanceFromHatchery = 10

        val lines = (minerals(startLocation.distance(_) < 10.0) ++ vespeneGeysers(startLocation.distance(_) < 10.0))
          .map(_.getPosition)
          .map(p => new Line2D.Float(startLocation.getX, startLocation.getY, p.getX, p.getY))
          .toSeq

        val buildingPoint = (-distanceFromHatchery to distanceFromHatchery).map { x =>
          (-distanceFromHatchery to distanceFromHatchery).map { y =>
            startLocation.toPoint2d.add(Point2d.of(x / 2.0F, y / 2.0F))
          }
            .filter(p => p.distance(startLocation) < distanceFromHatchery / 2.0)
            .filter(p => canBuild(Units.ZERG_SPAWNING_POOL, p))
            .find { p =>
              val rectangle = new Rectangle2D.Float(p.getX - poolRadius, p.getY - poolRadius, poolRadius, poolRadius)
              !lines.exists(rectangle.intersectsLine(_))
            }
        }.collectFirst { case Some(point) => point }.get

        action.unitCommand(closestFreeDrone(buildingPoint), Abilities.BUILD_SPAWNING_POOL, buildingPoint, false)
      }
    )
  )

  def buildQueen(hatchery: UnitInPool) = Sequence(
    Action { id => resourceManager.reserveResources(id, Units.ZERG_QUEEN) },
    Condition { id => resourceManager.removeReservationIfEnough(id) },
    Action { id => action.unitCommand(hatchery, Abilities.TRAIN_QUEEN, false) }
  )

  def buildUnit(unitType: UnitType) = Sequence(
    Action { id => resourceManager.reserveResources(id, unitType) },
    Condition { id => larvas.nonEmpty && resourceManager.removeReservationIfEnough(id) },
    Action { id =>
      require(Units.ZERG_LARVA.getAbilities.contains(abilityToBuild(unitType)), s"Larva doesn't have ability ${abilityToBuild(unitType)}")

      action.unitCommand(larvas.next(), unitType, false)
    }
  )

  def unassignDronesFromVespene(unit: UnitInPool, workers: Int): Unit = {
    require(unit.getType.toString.contains("EXTRACTOR"), s"Wrong unit type ${unit.getType}")
    val closestMainBuilding = mainBuildings.minBy(_.getPosition.distance(unit))
    val closestMineral = minerals().minBy(_.getPosition.distance(closestMainBuilding))
    myUnits(_.getType == Units.ZERG_DRONE, o => o.getTargetedUnitTag.filter(_ == unit.getTag).isPresent).take(workers)
      .foreach(action.unitCommand(_, Abilities.HARVEST_GATHER, closestMineral, false))
  }

  def buildHatchery(point: => Point2d) = {
    Sequence(
      Action { id => resourceManager.reserveResources(id, Units.ZERG_HATCHERY) },
      Condition { id => resourceManager.removeReservationIfEnough(id) },
      Action { id => action.unitCommand(closestDrone(point), Abilities.BUILD_HATCHERY, point, false) },
    )
  }

  def nextExpansion =
    expansionLocations
      .filter(canBuild(Units.ZERG_HATCHERY, _))
      .minBy { p => pathingDistance(obs.getStartLocation.toPoint2d.sub(buildingRadius(Abilities.BUILD_HATCHERY), 0F), p) }
}
