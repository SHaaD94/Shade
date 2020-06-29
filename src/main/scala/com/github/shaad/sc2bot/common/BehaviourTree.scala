package com.github.shaad.sc2bot.common

trait Node {

  def eval(): Boolean
}

case class Selector(chooses: (() => Boolean, Node)*) extends Node {
  override def eval(): Boolean = chooses.to(LazyList).find(_._1()) match {
    case Some((_, node)) => node.eval()
    case None => false
  }
}

case class Sequence(nodes: Node*) extends Node {
  override def eval(): Boolean = nodes.to(LazyList).forall(_.eval())
}

case class Parallel(nodes: Node*) extends Node {
  override def eval(): Boolean = nodes.map(_.eval()).exists(!_)
}

case class Condition(func: () => Boolean) extends Node {
  override def eval(): Boolean = func()
}

case class Action(func: () => Unit) extends Node {
  override def eval(): Boolean = {
    func()
    true
  }
}

case class StateFullSequence(nodes: Node*) {
  private var nodesLeft = nodes

  def evalNext(): Boolean = {
    require(!finished(), "Trying to evaluate empty sequence!")

    val headEvaluationResult = nodesLeft.head.eval()
    if (headEvaluationResult) {
      nodesLeft = nodesLeft.tail
    }

    headEvaluationResult
  }

  def finished(): Boolean = nodesLeft.isEmpty
}