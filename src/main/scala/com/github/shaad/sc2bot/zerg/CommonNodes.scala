package com.github.shaad.sc2bot.zerg

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

  val earlyBuildOrder = StateFullSequence(
    buildDrone,
    buildOverlord,
    buildDrone,
    buildDrone,
    buildDrone,
//    buildSpawningPool,
//    buildGas,
  )


}
