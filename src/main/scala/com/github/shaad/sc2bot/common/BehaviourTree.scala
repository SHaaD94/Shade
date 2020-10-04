package com.github.shaad.sc2bot.common

import java.util.UUID

/**
 * Base BhTree entity
 */
trait Node {
  def eval(id: String): Boolean
}

/**
 * Executes first Node which true predicate
 *
 * @param chooses list of pairs `predicate` -> `Node`
 */
case class Selector(chooses: (() => Boolean, Node)*) extends Node {
  override def eval(id: String): Boolean = chooses.to(LazyList).find(_._1()) match {
    case Some((_, node)) => node.eval(id)
    case None => false
  }
}

/**
 * Entity to execute nodes sequentially
 * It is NOT SAVING state of execution
 *
 * @param nodes nodes to execute
 */
case class Sequence(nodes: Node*) extends Node {
  override def eval(id: String): Boolean = nodes.to(LazyList).forall(_.eval(id))

  def toStateFull: StateFullSequence = StateFullSequence(nodes: _*)
}

//TODO: should not be used, because of problem with resource manager
// problem - stateFullSequence id is used to reserve resources in manager,
// if there will be parallel sequences
//case class Parallel(nodes: Node*) extends Node {
//  override def eval(id: String): Boolean = nodes.map(_.eval(id)).exists(!_)
//}

/**
 * Condition node
 *
 * @param func function to check condition
 */
case class Condition(func: String => Boolean) extends Node {
  override def eval(id: String): Boolean = func(id)
}

/**
 * Action node
 *
 * @param func some function
 */
case class Action(func: String => Unit) extends Node {
  override def eval(id: String): Boolean = {
    func(id)
    true
  }
}

/**
 * Does something then observed node is finished
 *
 * @param node node under watch
 * @param func some action after watching node finished
 */
case class Watcher(node: Node)(func: String => Unit) extends Node {
  override def eval(id: String): Boolean = {
    if (!node.eval(id)) return false

    func(id)
    true
  }
}

/**
 * Sequence which preserving state of nodes
 * whenever node from sequence returned true, it is being removed from sequence
 *
 * @param nodes nodes to execute
 */
case class StateFullSequence(nodes: Node*) extends Node with Identifiable {
  override val id: String = UUID.randomUUID().toString

  private var nodesLeft = nodes

  override def eval(id: String): Boolean = {
    evalNext(Some(id)) && this.finished()
  }

  def evalNext(explicitId: Option[String] = None): Boolean = {
    require(!finished(), "Trying to evaluate empty sequence!")

    val headEvaluationResult = nodesLeft.head.eval(explicitId.getOrElse(id))
    if (headEvaluationResult) {
      nodesLeft = nodesLeft.tail
    }

    headEvaluationResult
  }

  def finished(): Boolean = nodesLeft.isEmpty
}