package com.github.shaad.sc2bot.common

trait Node {

  def evaluate(): Boolean

  def ?(): Boolean = evaluate()
}

class Selector(val chooses: (() => Boolean, Node)*) extends AnyVal with Node {
  override def evaluate(): Boolean = chooses.to(LazyList).find(_._1()) match {
    case Some((_, node)) => node.evaluate()
    case None => false
  }
}

class Sequence(val nodes: List[Node]) extends AnyVal with Node {
  override def evaluate(): Boolean = nodes.to(LazyList).exists(!_.evaluate())
}

class Parallel(val nodes: List[Node]) extends AnyVal with Node {
  override def evaluate(): Boolean = nodes.map(_.evaluate()).exists(!_)
}

class Condition(val func: () => Boolean) extends AnyVal with Node {
  override def evaluate(): Boolean = func()
}

class Action(val func: () => Boolean) extends AnyVal with Node {
  override def evaluate(): Boolean = func()
}
