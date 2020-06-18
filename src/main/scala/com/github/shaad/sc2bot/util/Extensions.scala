package com.github.shaad.sc2bot.util

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, QueryInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Ability, UnitType, UnitTypeData, Units}
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.ocraft.s2client.protocol.unit.{Alliance, Unit => SC2Unit}

import scala.jdk.CollectionConverters._

object Extensions {
  def mineralCost(unitType: UnitType)(implicit observation: ObservationInterface): Int = observation
    .getUnitTypeData(false).get(unitType).getMineralCost.get()

  def vespeneCost(unitType: UnitType)(implicit observation: ObservationInterface): Int = observation
    .getUnitTypeData(false).get(unitType).getVespeneCost.get()

  def unitData(unitType: UnitType)(implicit observation: ObservationInterface): UnitTypeData = observation
    .getUnitTypeData(false).get(unitType)

  def cargo(unitType: UnitType)(implicit observation: ObservationInterface): Int = observation
    .getUnitTypeData(false).get(unitType).getCargoSize.get()

  implicit def abilityToBuild(unitType: UnitType)(implicit observation: ObservationInterface): Ability = observation
    .getUnitTypeData(false).get(unitType).getAbility.get()

  def canBuild(unit: UnitType, point: Point2d)(implicit queryInterface: QueryInterface, observationInterface: ObservationInterface) =
    queryInterface.placement(unit, point)

  def minerals(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    units(u => u.getType.toString.contains("MINERAL") && filter(u))
  }

  def vespeneGeysers(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    units(u => (
      u.getType.toString.contains("NEUTRAL") && u.getType.toString.contains("VESPEN")) && filter(u))
  }

  def units(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    observation.getUnits({ (u: UnitInPool) => filter(u) }).asScala
  }

  def myUnits(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    observation.getUnits(Alliance.SELF, { (u: UnitInPool) => filter(u) }).asScala
  }

  def myAliveUnits(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    observation.getUnits(Alliance.SELF, { (u: UnitInPool) => u.isAlive && filter(u) }).asScala
  }

  implicit def unitInPoolToUnit(unitInPool: UnitInPool): SC2Unit = unitInPool.unit()

  implicit def unitInPoolToPosition(unitInPool: UnitInPool): Point = unitInPool.unit().getPosition

  implicit def pointToPoint2d(point:Point): Point2d = point.toPoint2d
}

object TerranExtensions {

  import Extensions._

  implicit def mySCVs(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    myAliveUnits(_.unit().getType == Units.TERRAN_SCV)
  }

  implicit def myFreeSCVs(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    myAliveUnits(u => u.unit().getType == Units.TERRAN_SCV && !u.getOrders.asScala.exists(_.getAbility != Abilities.HARVEST_GATHER))
  }

  implicit def myCommandCenters(filter: UnitInPool => Boolean = { _ => true })(implicit observation: ObservationInterface): Iterable[UnitInPool] = {
    myAliveUnits(u => u.unit().getType == Units.TERRAN_COMMAND_CENTER || u.unit().getType == Units.TERRAN_ORBITAL_COMMAND || u.unit().getType == Units.TERRAN_PLANETARY_FORTRESS)
  }
}