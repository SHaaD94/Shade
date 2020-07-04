package com.github.shaad.sc2bot.zerg

import java.nio.file.Paths

import com.github.ocraft.s2client.bot.S2Coordinator
import com.github.ocraft.s2client.protocol.game.{BattlenetMap, Difficulty, Race}
import com.github.shaad.sc2bot.Constants

object ShadeStarter extends App {

  val bot = new Shade()

  val s2Coordinator = S2Coordinator.setup
    .setProcessPath(Paths.get(Constants.executable))
    .setPortStart(Constants.port)
    .setRealtime(true)
    .setWindowSize(2500, 1500)
    .setParticipants(S2Coordinator.createParticipant(Race.ZERG, bot),
      S2Coordinator.createComputer(Race.TERRAN, Difficulty.HARD))
    .launchStarcraft
    .startGame(BattlenetMap.of("Eternal Empire LE"))

  while (true) {
    s2Coordinator.update()
  }

  s2Coordinator.quit()
}
