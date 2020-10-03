package com.github.shaad.sc2bot.common

import com.github.ocraft.s2client.bot.gateway.{ObservationInterface, QueryInterface, UnitInPool}
import com.github.ocraft.s2client.protocol.data.{Abilities, Ability, UnitType, UnitTypeData, Units, Upgrade}
import com.github.ocraft.s2client.protocol.game.Race
import com.github.ocraft.s2client.protocol.spatial.{Point, Point2d}
import com.github.ocraft.s2client.protocol.unit.{Alliance, UnitOrder, Unit => SC2Unit}

import scala.jdk.CollectionConverters._

object Extensions {
  implicit def abilityToBuild(unitType: UnitType)(implicit observationInterface: ObservationInterface): Ability = {
    observationInterface.getUnitTypeData(false).get(unitType).getAbility.orElseThrow(() =>
      new RuntimeException(s"Didn't found an ability to build unit $unitType")
    )
  }

  def pathingDistance(from: Point2d, to: Point2d)(implicit queryInterface: QueryInterface) = {
    val distance = queryInterface.pathingDistance(from, to)
    if (distance == 0.0) Float.MaxValue else distance
  }

  def buildingRadius(buildAbility: Ability)(implicit observation: ObservationInterface): Float = {
    observation.getAbilityData(false).get(buildAbility).getFootprintRadius.orElse(0F)
  }

  //  def canAfford(unitType: UnitType)(implicit observation: ObservationInterface): Boolean = {
  //    mineralCost(unitType) <= observation.getMinerals && vespeneCost(unitType) <= observation.getVespene
  //  }

  //  def canAfford(upgrade: Upgrade)(implicit observation: ObservationInterface): Boolean = {
  //    mineralCost(upgrade) <= observation.getMinerals && vespeneCost(upgrade) <= observation.getVespene
  //  }

  def mineralCost(upgrade: Upgrade)(implicit observation: ObservationInterface): Int = {
    observation.getUpgradeData(false).get(upgrade).getMineralCost.orElse(0)
  }

  def vespeneCost(upgrade: Upgrade)(implicit observation: ObservationInterface): Int = {
    observation.getUpgradeData(false).get(upgrade).getVespeneCost.orElse(0)
  }

  def mineralCost(unitType: UnitType)(implicit observation: ObservationInterface): Int = {
    val unitInfo = observation.getUnitTypeData(false).get(unitType)
    val droneCost = if (unitInfo.getRace.filter(_ == Race.ZERG).isPresent && unitInfo.getFoodRequired.isEmpty && unitType != Units.ZERG_OVERLORD) mineralCost(Units.ZERG_DRONE) else 0
    val result = unitInfo.getTechAliases.asScala.headOption match {
      case Some(alias) => unitInfo.getMineralCost.get() - observation.getUnitTypeData(false).get(alias).getMineralCost.get()
      case None => unitInfo.getMineralCost.get() - droneCost
    }
    //TODO come up with better solution for building zerglings, using requiredfood < 0.5, is not applicable because of baneling
    if (unitType == Units.ZERG_ZERGLING) result * 2 else result
  }

  def vespeneCost(unitType: UnitType)(implicit observation: ObservationInterface): Int = {
    val unitInfo = observation.getUnitTypeData(false).get(unitType)
    unitInfo.getTechAliases.asScala.headOption match {
      case Some(alias) => unitInfo.getMineralCost.get() - observation.getUnitTypeData(false).get(alias).getVespeneCost.get()
      case None => unitInfo.getVespeneCost.get()
    }
  }

  def unitData(unitType: UnitType)(implicit observation: ObservationInterface): UnitTypeData = observation
    .getUnitTypeData(false).get(unitType)

  def enoughFood(unitType: UnitType)(implicit observation: ObservationInterface): Boolean = observation.getFoodCap - observation.getFoodUsed >= foodCost(unitType)

  def foodCost(unitType: UnitType)(implicit observation: ObservationInterface): Float = {
    val typeData = observation.getUnitTypeData(false)
    val unitFood = typeData.get(unitType).getFoodRequired.orElse(0F)
    typeData.get(unitType).getTechAliases.asScala.headOption match {
      case Some(foodForInitialUnit) => unitFood - typeData.get(foodForInitialUnit).getFoodRequired.orElse(0F)
      case None => unitFood
    }
  }

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

  def haveBuilding(unitType: UnitType)(implicit observation: ObservationInterface): Boolean = {
    myUnits(_.getType == unitType).exists(_.getBuildProgress == 1.0)
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

  def ramps(implicit observation: ObservationInterface) = units(_.getType.toString.contains("RAMP"))

  @deprecated("used in terran bot, Shade should not use it")
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
