package com.github.shaad.sc2bot.zerg

import java.awt.geom.{Line2D, Rectangle2D}
import java.awt.{Polygon, Rectangle}

import scala.jdk.CollectionConverters._
import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, DebugInterface, ObservationInterface, QueryInterface}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.shaad.sc2bot.common.{Action, Condition, Sequence, StateFullSequence}
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.ZergExtensions._

class CommonNodes(val expansionLocations: Seq[Point])(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {

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
          .filter(g => !busyExtractors.contains(g.getPosition))
          .minBy(g => mainBuildings.map(m => pathingDistance(m.getPosition.toPoint2d.sub(buildingRadius(Abilities.BUILD_HATCHERY), 0F), g.getPosition.toPoint2d.sub(buildingRadius(Abilities.BUILD_EXTRACTOR), 0F))).min)
          .unit()

        action.unitCommand(closestFreeDrone(geyser.getPosition), Abilities.BUILD_EXTRACTOR, geyser, false)
      }
    )
  )
  val buildSpawningPool = Sequence(
    Sequence(
      Condition { () => canAfford(Units.ZERG_SPAWNING_POOL) },
      Action { () =>
        val startLocation = obs.getStartLocation

        val poolRadius = buildingRadius(Abilities.BUILD_SPAWNING_POOL)

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
  val earlyBuildOrder = StateFullSequence(
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

  def buildHatchery(point: => Point2d) = {
    Sequence(
      Condition { () => canAfford(Units.ZERG_HATCHERY) },
      Action { () => action.unitCommand(closestDrone(point), Abilities.BUILD_HATCHERY, point, false) },
    )
  }

  private def nextExpansion =
    expansionLocations
      .filter(canBuild(Units.ZERG_HATCHERY, _))
      .minBy { p => pathingDistance(obs.getStartLocation.toPoint2d.sub(buildingRadius(Abilities.BUILD_HATCHERY), 0F), p) }

}
