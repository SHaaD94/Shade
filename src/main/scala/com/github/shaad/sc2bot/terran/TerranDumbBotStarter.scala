package com.github.shaad.sc2bot.terran

import java.nio.file.Paths

import com.github.ocraft.s2client.bot.S2Coordinator
import com.github.ocraft.s2client.protocol.game.{BattlenetMap, Difficulty, Race}
import com.github.shaad.sc2bot.Constants

object TerranDumbBotStarter extends App {

  val bot = new DumbTerranBot()

  val s2Coordinator = S2Coordinator.setup
    .setProcessPath(Paths.get(Constants.executable))
    .setPortStart(Constants.port)
    .setWindowSize(2500,1500)
    .setParticipants(S2Coordinator.createParticipant(Race.TERRAN, bot),
      S2Coordinator.createComputer(Race.ZERG, Difficulty.HARD))
    .launchStarcraft
    .startGame(BattlenetMap.of("Eternal Empire LE"))

  while (true) {
    s2Coordinator.update()
  }

  s2Coordinator.quit()

}
