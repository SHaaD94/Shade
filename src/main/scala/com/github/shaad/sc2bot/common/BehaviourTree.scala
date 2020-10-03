package com.github.shaad.sc2bot.common

import java.util.UUID

trait Node {

  def eval(id: String): Boolean
}

case class Selector(chooses: (() => Boolean, Node)*) extends Node {
  override def eval(id: String): Boolean = chooses.to(LazyList).find(_._1()) match {
    case Some((_, node)) => node.eval(id)
    case None => false
  }
}

case class Sequence(nodes: Node*) extends Node {
  override def eval(id: String): Boolean = nodes.to(LazyList).forall(_.eval(id))
}

//TODO: should not be used, because of problem with resource manager
//case class Parallel(nodes: Node*) extends Node {
//  override def eval(id: String): Boolean = nodes.map(_.eval(id)).exists(!_)
//}

case class Condition(func: String => Boolean) extends Node {
  override def eval(id: String): Boolean = func(id)
}

case class Action(func: String => Unit) extends Node {
  override def eval(id: String): Boolean = {
    func(id)
    true
  }
}

// Does something then observed node is finished
case class Watcher(node: Node)(func: (String) => Unit) extends Node {
  override def eval(id: String): Boolean = {
    if (!node.eval(id)) return false

    func(id)
    true
  }
}

case class StateFullSequence(nodes: Node*) extends Identifiable {
  override val id: String = UUID.randomUUID().toString

  private var nodesLeft = nodes

  def evalNext(): Boolean = {
    require(!finished(), "Trying to evaluate empty sequence!")

    val headEvaluationResult = nodesLeft.head.eval(id)
    if (headEvaluationResult) {
      nodesLeft = nodesLeft.tail
    }

    headEvaluationResult
  }

  def finished(): Boolean = nodesLeft.isEmpty
}