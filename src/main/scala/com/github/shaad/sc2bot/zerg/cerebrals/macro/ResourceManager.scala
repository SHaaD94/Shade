package com.github.shaad.sc2bot.zerg.cerebrals.`macro`

import com.github.ocraft.s2client.bot.gateway._
import com.github.ocraft.s2client.protocol.data.{UnitType, Upgrade}

import scala.collection.mutable
import com.github.shaad.sc2bot.common.Extensions._

private case class ResourceReservation(minerals: Int, gas: Int, supply: Int)

class ResourceManager(implicit obs: ObservationInterface, query: QueryInterface, action: ActionInterface, control: ControlInterface, debug: DebugInterface) {
  private val reservations = mutable.LinkedHashMap[String, ResourceReservation]()

  def reserveResources(id: String, upgrade: Upgrade): Unit = {
    reserveResources(id, mineralCost(upgrade), vespeneCost(upgrade), 0)
  }

  def reserveResources(id: String, unit: UnitType): Unit = {
    reserveResources(id, mineralCost(unit), vespeneCost(unit), foodCost(unit).round)
  }

  def reserveResources(id: String, minerals: Int, gas: Int, supply: Int): Unit = {
    if (!reservations.contains(id)) reservations.put(id, ResourceReservation(minerals, gas, supply))
  }

  def removeReservationIfEnough(id: String): Boolean = {
    val enoughResources = this.enoughResources(id)
    if (enoughResources) freeResources(id)
    enoughResources
  }

  def enoughResources(reservationId: String): Boolean = {
    val reservation = reservations.getOrElse(reservationId, throw new RuntimeException(s"Reservation not found $reservationId"))

    val total = reservations
      .takeWhile { case (curId, _) => curId != reservationId }.values
      .fold(ResourceReservation(0, 0, 0)) { case (l, r) =>
        ResourceReservation(l.minerals + r.minerals, l.gas + r.gas, l.supply + r.supply)
      }

    //check minerals
    obs.getMinerals - total.minerals >= reservation.minerals &&
      //check gas
      obs.getVespene - total.gas >= reservation.gas &&
      //check supply
      obs.getFoodCap - obs.getFoodUsed - total.supply >= reservation.supply
  }

  /**
   * Free reserved resources
   *
   * @param id reservation id
   */
  def freeResources(id: String): Unit = {
    reservations.remove(id)
  }

}
