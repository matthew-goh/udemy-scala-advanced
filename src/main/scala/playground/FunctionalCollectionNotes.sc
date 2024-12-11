// SEQUENCES are "callable" through an integer index
//trait Seq[+A] extends PartialFunction[Int, A]{
//  def apply(index: Int): A
//}

// -> can call e.g. numbers(2)
// partially defined on the domain [0, ..., length-1]
// partial functions from Int to A


// MAPS are "callable" through their keys
//trait Map[A, +B] extends PartialFunction[A, B]{
//  def apply(key: A):B
//  def get(key: A): Option[B]
//}

// can call e.g. phoneMappings(2) and get "ABC"
// partially defined on the domain of its keys, a subdomain of A
// partial functions from A to B
