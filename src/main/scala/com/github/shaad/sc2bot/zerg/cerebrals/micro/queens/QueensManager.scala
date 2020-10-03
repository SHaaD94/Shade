package com.github.shaad.sc2bot.zerg.cerebrals.micro.queens

import com.github.ocraft.s2client.bot.gateway.{ActionInterface, ControlInterface, ObservationInterface, QueryInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Units}
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Tag

import scala.collection.mutable
import com.github.shaad.sc2bot.common.Extensions._
import com.github.shaad.sc2bot.zerg.ZergExtensions._

import scala.util.Random

class QueensManager(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface) {
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

    queen2Role.collect {
      case (tag, CreepSpread) => obs.getUnit(tag)
    }
      .filter(!_.hasOrderWithAbility(Abilities.BUILD_CREEP_TUMOR))
      .foreach { queen =>
        if (queen.abilityAvailable(Abilities.BUILD_CREEP_TUMOR))
          action.unitCommand(queen, Abilities.BUILD_CREEP_TUMOR, getCreepTumorPosition(queen), false)
      }
  }

  private def getCreepTumorPosition(unit: UnitInPool): Point2d = {
    val size = obs.getGameInfo.getStartRaw.get().getMapSize
    val x = size.getX
    val y = size.getY
    LazyList.continually(
      Point2d.of(Random.nextInt(x).toFloat, Random.nextInt(y).toFloat)
    ).find(obs.hasCreep).get
  }

}

sealed trait QueenRole

object CreepSpread extends QueenRole

object Fighting extends QueenRole

case class Injections(hatcheryTag: Tag) extends QueenRole
