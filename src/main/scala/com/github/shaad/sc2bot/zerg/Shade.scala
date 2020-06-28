package com.github.shaad.sc2bot.zerg

import com.github.shaad.sc2bot.BotBase
import com.github.shaad.sc2bot.zerg.cerebrals.{MacroCerebral, MicroCerebral}

class Shade extends BotBase {
  private val cerebrals = Seq(
    new MacroCerebral(),
    new MicroCerebral(),
  )

  override def onStep(): Unit = {
    cerebrals.foreach(_.serve())
  }
}
