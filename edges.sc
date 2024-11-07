
// Datatypes to define nodes (logic is different on original nodes than edge nodes)
trait Node

case class OriginNode(n : Int) extends Node {
  override def toString() : String = s"node-${n}"
}

case class EdgeNode(origin: OriginNode, destination: OriginNode, nth : Int) extends Node{
  override def toString() : String = s"edge-${origin.n}-${destination.n}-${nth}"
}


// Represents an Edge
case class Edge(originIndex: Int, destinationIndex: Int, length : Int ) {
  val origin = OriginNode(originIndex)
  val destination = OriginNode(destinationIndex)
  val firstEdgeNode = EdgeNode(origin, destination, 0)
  val maxEdgeId = length-1
  val lastEdgeNode = EdgeNode(origin, destination, maxEdgeId) 
  // Get Nodes define and a edge and its direct neighbour
  def nodesWithNeighbours : List[(Node, List[Node])]= {
    // Origin Node neighbour is first edge node
    (origin, firstEdgeNode :: Nil) :: 
    // Destination Node neighbour is last edge node
    (destination, lastEdgeNode :: Nil) ::
      Range.apply(0, length).toList.map {
        // Special case, there is only one node on the edge, neighbours are origin and destination
        case 0 if length == 1 => (firstEdgeNode, origin :: destination :: Nil)
        // First edge has special neighbours, origin and first edge
        case 0 => (firstEdgeNode, origin :: EdgeNode(origin, destination, 1) :: Nil)
        // Last edge node has special neighbours, destination and the node right before it 
        case max if max == maxEdgeId => (lastEdgeNode, destination :: EdgeNode(origin, destination, maxEdgeId - 1) :: Nil)
        // Basic case, and edge node with two neighbours, one before, one after
        case i => (EdgeNode(origin, destination, i), EdgeNode(origin, destination, i + 1) :: EdgeNode(origin, destination, i - 1) :: Nil)
      }
  }
}

// From a list of Edges, build the a map of Nodes to their direct neighbours
// group node with neighbour for an edge by node, so that we get all neighbour 
def accessibleNodes(edges : List[Edge]) : Map[Node,List[Node]] = (for {
  edge <- edges
  node <- edge.nodesWithNeighbours
} yield {
  node
}).groupMapReduce(_._1)(_._2)(_ ::: _)


// Our graph traversal algorithm in which we will accumulate nodes in a Set
// from a current node, get current neighbours, add them to access Nodes, and recursively access to their neighbours
def getNodes(current : Node, nMoves : Int, accessibleNodes  : Map[Node,List[Node]], accessedNodes : Set[Node] ) : Set[Node] = {
  // No more moves, return result
  if (nMoves <= 0) {
    accessedNodes
  } else {
    // get neighbours
    accessibleNodes.get(current) match {
      // No neighbours, return
      case None => accessedNodes
      // Some neighbours, 
      //TODO: we could also remove nodes that were already accessed
      case Some(nextNodes) => 
        // fold our nodes with current result
        nextNodes.foldLeft(accessedNodes) {
          case (acc, node) =>
            // Recursively build accessed nodes
            getNodes(node, nMoves - 1, accessibleNodes, acc +(node))
        } 
    }
  }
}


// init our program, could be a main too, but i worked with Scala worksheet, like a repl
def numberOfNodes(edges  : List[Edge], nMove : Int) : Int = {
  val origin = OriginNode(0)
  getNodes(origin,nMove,accessibleNodes(edges),Set(origin)).size
}


// example cases
numberOfNodes(Edge(0,1,10) :: Edge(1,2,2) :: Edge(0,2,1) :: Nil, 6)

numberOfNodes(List(Edge(0,1,4),Edge(1,2,6),Edge(0,2,8),Edge(1,3,1)), 10)

numberOfNodes(List(Edge(1,2,4),Edge(1,4,5),Edge(1,3,1),Edge(2,3,4),Edge(3,4,5)), 17)