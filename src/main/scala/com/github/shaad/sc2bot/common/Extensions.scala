package com.github.shaad.sc2bot.common

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, QueryInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Ability, UnitType, UnitTypeData, Units}
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.ocraft.s2client.protocol.unit.{Alliance, UnitOrder, Unit => SC2Unit}

import scala.jdk.CollectionConverters._

object Extensions {

  def canAfford(unitType: UnitType)(implicit observation: ObservationInterface): Boolean = {
    mineralCost(unitType) <= observation.getMinerals && vespeneCost(unitType) <= observation.getVespene
  }

  def mineralCost(unitType: UnitType)(implicit observation: ObservationInterface): Int = observation
    .getUnitTypeData(false).get(unitType).getMineralCost.get()

  def vespeneCost(unitType: UnitType)(implicit observation: ObservationInterface): Int = observation
    .getUnitTypeData(false).get(unitType).getVespeneCost.get()

  def unitData(unitType: UnitType)(implicit observation: ObservationInterface): UnitTypeData = observation
    .getUnitTypeData(false).get(unitType)

  def enoughFood(unitType: UnitType)(implicit observation: ObservationInterface): Boolean = observation.getFoodCap - observation.getFoodUsed >= food(unitType)

  def food(unitType: UnitType)(implicit observation: ObservationInterface): Int = observation
    .getUnitTypeData(false).get(unitType).getCargoSize.get()

  implicit def abilityToBuild(unitType: UnitType)(implicit observation: ObservationInterface): Ability = observation
    .getUnitTypeData(false).get(unitType).getAbility.get()

  def canBuild(unit: UnitType, point: Point2d)(implicit queryInterface: QueryInterface, observationInterface: ObservationInterface) =
    queryInterface.placement(unit, point)

  def minerals(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterator[UnitInPool] = {
    units(u => u.getType.toString.contains("MINERAL") && filter(u))
  }

  def units(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterator[UnitInPool] = {
    observation.getUnits({ (u: UnitInPool) => filter(u) }).iterator().asScala
  }

  def vespeneGeysers(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterator[UnitInPool] = {
    units(u => (
      u.getType.toString.contains("NEUTRAL") && u.getType.toString.contains("VESPEN")) && filter(u))
  }

  def myUnits(unitFilter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterator[UnitInPool] = {
    observation.getUnits(Alliance.SELF, { (u: UnitInPool) => unitFilter(u) }).iterator().asScala
  }

  def myUnits(unitFilter: UnitInPool => Boolean,
              orderFilter: UnitOrder => Boolean)(implicit observation: ObservationInterface): Iterator[UnitInPool] = {
    observation.getUnits(Alliance.SELF, { (u: UnitInPool) =>
      unitFilter(u) && u.getOrders.asScala.exists(orderFilter)
    }).iterator().asScala
  }

  @deprecated
  def myAliveUnits(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterator[UnitInPool] = {
    observation.getUnits(Alliance.SELF, { (u: UnitInPool) => u.isAlive && filter(u) }).iterator().asScala
  }

  implicit def unitInPoolToUnit(unitInPool: UnitInPool): SC2Unit = unitInPool.unit()

  implicit def unitInPoolToPosition(unitInPool: UnitInPool): Point = unitInPool.unit().getPosition

  implicit def pointToPoint2d(point: Point): Point2d = point.toPoint2d

  implicit class UnitInPoolImplicits(val unit: UnitInPool) extends AnyVal {
    def isMine(implicit observationInterface: ObservationInterface): Boolean = observationInterface.getPlayerId == unit.getOwner
  }

}
