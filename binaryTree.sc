
import zio.json.*


// Simple binary tree representation, serialized using zio json
// left and right nodes a represented by options
case class BinaryTreeNode(value : Int, left : Option[BinaryTreeNode], right : Option[BinaryTreeNode] )

object NodeSerializer {

  implicit lazy val encoder: JsonEncoder[BinaryTreeNode] = DeriveJsonEncoder.gen[BinaryTreeNode]

  implicit lazy val decoder: JsonDecoder[BinaryTreeNode] = DeriveJsonDecoder.gen[BinaryTreeNode]

  def serialize(node : BinaryTreeNode) = node.toJson

  def deserialize(string: String) =  string.fromJson[BinaryTreeNode] match {
    case r@Right(_) => r
    case Left(err) => Left(s"Invalid json in deserializing binary tree, error details: ${err}" )
  }

}


val n1 = BinaryTreeNode(1,Some(BinaryTreeNode(2,None,None)),Some(BinaryTreeNode(3,Some(BinaryTreeNode(4,None,None)) ,Some(BinaryTreeNode(4,None,None)))))

val n1Parsed = NodeSerializer.deserialize(NodeSerializer.serialize(n1))

n1Parsed == Right(n1)

val json = """{"value":42,"left":{"value":-2,"left":{"value":144},"right":{"value":19}},"right":{"value":0,"right":{"value":154}}}"""

val n2 = BinaryTreeNode(42,Some(BinaryTreeNode(-2,Some(BinaryTreeNode(144,None,None)),Some(BinaryTreeNode(19,None,None)))),Some(BinaryTreeNode(0,None,Some(BinaryTreeNode(154,None,None)))))

NodeSerializer.deserialize(json) == Right(n2)