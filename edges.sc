
// Datatypes to
trait Node

case class OriginNode(n : Int) extends Node {
  override def toString() : String = s"node-${n}"
}

case class EdgeNode(origin: OriginNode, destination: OriginNode, nth : Int) extends Node{
  override def toString() : String = s"edge-${origin.n}-${destination.n}-${nth}"
}


case class Edge(originIndex: Int, destinationIndex: Int, length : Int ) {
  val origin = OriginNode(originIndex)
  val destination = OriginNode(destinationIndex)
  def nodes : List[(Node, List[Node])]= {
    (origin, EdgeNode(origin, destination, 0) :: Nil) :: (destination, EdgeNode(origin, destination, length-1) :: Nil) ::
      Range.apply(0, length).toList.map {
        case 0 if length == 1 => (EdgeNode(origin, destination, 0), origin :: destination :: Nil)
        case 0 => (EdgeNode(origin, destination, 0), origin :: EdgeNode(origin, destination, 1) :: Nil)
        case max if max == length - 1 => (EdgeNode(origin, destination, max), destination :: EdgeNode(origin, destination, max - 1) :: Nil)
        case i => (EdgeNode(origin, destination, i), EdgeNode(origin, destination, i + 1) :: EdgeNode(origin, destination, i - 1) :: Nil)
      }
  }
}


def accessibleNodes(edges : List[Edge]) : Map[Node,List[Node]] = (for {
  edge <- edges
  node <- edge.nodes
} yield {
  node
}).groupMapReduce(_._1)(_._2)(_ ::: _)


def getNodes(current : Node, nMoves : Int, accessibleNodes  : Map[Node,List[Node]], accessedNodes : Set[Node] ) : Set[Node] = {
  if (nMoves == 0) {
    accessedNodes
  } else {
    accessibleNodes.get(current) match {
      case None => accessedNodes
      case Some(nextNodes) => nextNodes.foldLeft(accessedNodes) {
        case (acc, node) =>
          getNodes(node, nMoves - 1, accessibleNodes, acc +(node))
      }
    }
  }
}



def numberOfNodes(edges  : List[Edge], nMove : Int) : Int = {
  val origin = OriginNode(0)
  getNodes(origin,nMove,accessibleNodes(edges),Set(origin)).size

}

numberOfNodes(Edge(0,1,10) :: Edge(1,2,2) :: Edge(0,2,1) :: Nil, 6)

numberOfNodes(List(Edge(0,1,4),Edge(1,2,6),Edge(0,2,8),Edge(1,3,1)), 10)

numberOfNodes(List(Edge(1,2,4),Edge(1,4,5),Edge(1,3,1),Edge(2,3,4),Edge(3,4,5)), 17)