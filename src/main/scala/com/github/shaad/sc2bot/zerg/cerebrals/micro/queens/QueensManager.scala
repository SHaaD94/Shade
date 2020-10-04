package com.github.shaad.sc2bot.zerg.cerebrals.micro.queens

import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, ObservationInterface, QueryInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Tag

import scala.collection.mutable
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.ZergExtensions._

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Random

class QueensManager(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface) {
  private lazy val creepMap = (0 until obs.getRawObservation.getRaw.get().getMapState.getCreep.getSize.getX).flatMap { x =>
    (0 until obs.getRawObservation.getRaw.get().getMapState.getCreep.getSize.getY).map { y =>
      Point2d.of(x.toFloat, y.toFloat)
    }
  }.filter(obs.isPathable)


  private var queen2Role = Map[Tag, QueenRole]()

  def assignRoles(): Unit = {
    val queens = myUnits(Units.ZERG_QUEEN).toSeq
    val hatcheries = mainBuildings.toSeq

    val newQueenRoles = mutable.Map[Tag, QueenRole]()

    hatcheries.sortBy(_.getTag.toString).foreach(h =>
      queens.filter(q =>
        !newQueenRoles.contains(q.getTag))
        .minByOption(_.distance(h)).foreach(queen =>
        newQueenRoles.put(queen.getTag, Injections(h.getTag))
      )
    )
    queens.filter(q => !newQueenRoles.contains(q.getTag))
      .foreach(q => newQueenRoles.put(q.getTag, CreepSpread))

    queen2Role = newQueenRoles.toMap
  }

  def manageQueens(): Unit = {
    queen2Role.collect {
      case (tag, Injections(hatchTag)) => obs.getUnit(tag) -> obs.getUnit(hatchTag)
    }.foreach { case (queen, hatchery) =>
      if (queen.abilityAvailable(Abilities.EFFECT_INJECT_LARVA)) {
        action.unitCommand(queen, Abilities.EFFECT_INJECT_LARVA, hatchery, false)
      }
    }

    val tumors = mutable.Set[Point2d]()
    myUnits(u => u.getType.toString.contains("CREEP_TUMOR")).foreach(t => tumors.add(t.getPosition))
    myUnits(Units.ZERG_QUEEN)
      .to(LazyList)
      .flatMap(_.getOrders.asScala.filter(_.getAbility == Abilities.BUILD_CREEP_TUMOR))
      .foreach(_.getTargetedWorldSpacePosition.ifPresent(p => tumors.add(p)))

    queen2Role.collect {
      case (tag, CreepSpread) => obs.getUnit(tag)
    }
      .filter(!_.hasOrderWithAbility(Abilities.BUILD_CREEP_TUMOR))
      .foreach { queen =>
        if (queen.abilityAvailable(Abilities.BUILD_CREEP_TUMOR)) {
          val tumorPosition = getCreepTumorPosition(queen, tumors)
          tumors.add(tumorPosition)
          action.unitCommand(queen, Abilities.BUILD_CREEP_TUMOR, tumorPosition, false)
        }
      }
  }

  private def getCreepTumorPosition(unit: UnitInPool, tumors: mutable.Set[Point2d]): Point2d = {
    //TODO find how to get it from API
    val tumorCreepRange = 5.0

    val mb = mainBuildings.toSeq

    creepMap
      .filter(obs.hasCreep)
      .filter(x => !tumors.exists(_.distance(x) <= tumorCreepRange / 2))
      .map { p =>
        // calculate score by distance to hatcheries
        p -> creepMap.to(LazyList).filter(_.distance(p) < tumorCreepRange)
          .filter(!obs.hasCreep(_))
          .map(pp => mb.map(b => 100.0 / b.toPoint2d.distance(pp)).sum).sum
      }.maxBy(_._2)._1
  }

}

sealed trait QueenRole

case class Injections(hatcheryTag: Tag) extends QueenRole

object CreepSpread extends QueenRole

object Fighting extends QueenRole
