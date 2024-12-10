package exercises

import scala.annotation.tailrec

trait MySet[A] extends (A => Boolean) {
  /*
    Exercise: Implement a functional set
    MySet is not a collection in the classical sense, but a function!
    Need an apply() method for the A => Boolean trait, e.g MySet(1,2,3)(2) = true since 2 is in the set
    Create some collection-specific methods:
  */
  def apply(elem: A): Boolean = contains(elem)

  def contains(elem: A): Boolean
  def +(elem: A): MySet[A]
  def ++(anotherSet: MySet[A]): MySet[A] // union

  def map[B](f: A => B): MySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B]
  def filter(predicate: A => Boolean): MySet[A] // hint: MySet is already an A => Boolean type
  def forEach(f: A => Unit): Unit

  /* EXERCISE 2
    - removing an element
    - intersection with another set
    - difference with another set
   */
  def -(elem: A): MySet[A]
  def --(anotherSet: MySet[A]): MySet[A] // set difference
  def &(anotherSet: MySet[A]): MySet[A] // intersection

  // EXERCISE 3: new operator, the negation of a set
  // e.g. set[1,2,3] => set of everything but 1,2,3
  // !MySet(1,2,3) is x => !(MySet(1,2,3) contains x)
  def unary_! : MySet[A]
}

// EmptySet needs to be a class because MySet is invariant, not covariant
class EmptySet[A] extends MySet[A] {
  def contains(elem: A): Boolean = false
  def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)
  def ++(anotherSet: MySet[A]): MySet[A] = anotherSet

  def map[B](f: A => B): MySet[B] = new EmptySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]
  def filter(predicate: A => Boolean): MySet[A] = this
  def forEach(f: A => Unit): Unit = ()

  def -(elem: A): MySet[A] = this
  def --(anotherSet: MySet[A]): MySet[A] = this
  def &(anotherSet: MySet[A]): MySet[A] = this

  // universal set
  def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

//class AllInclusiveSet[A] extends MySet[A] {
//  override def contains(elem: A): Boolean = true
//  override def +(elem: A): MySet[A] = this
//  override def ++(anotherSet: MySet[A]): MySet[A] = this
//
//  // consider: naturals = allinclusive[Int]
//  // naturals.map(x => x mod 3) returns [0 1 2]
//  // how do we go from an infinite set to a finite set?
//  override def map[B](f: A => B): MySet[B] = ???
//  override def flatMap[B](f: A => MySet[B]): MySet[B] = ???
//  override def forEach(f: A => Unit): Unit = ???
//
//  // property-based set; could have infinitely many elements satisfying the predicate
//  override def filter(predicate: A => Boolean): MySet[A] = ???
//  override def -(elem: A): MySet[A] = ???
//  override def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet)
//  override def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet)
//
//  override def unary_! : MySet[A] = new EmptySet[A]
//}

// use this instead of AllInclusiveSet as we can't define certain methods there
// denotes all elements of type A that satisfy the property
// i.e. { x in A | property(x) }
class PropertyBasedSet[A](property: A => Boolean) extends MySet[A] {
  def contains(elem: A): Boolean = property(elem)
  def +(elem: A): MySet[A] = new PropertyBasedSet[A](x => property(x) || x == elem)
  def ++(anotherSet: MySet[A]): MySet[A] = new PropertyBasedSet[A](x => property(x) || anotherSet(x))

  // problem: can't really map a property-based set since we don't know what will be obtained
  // (whether resulting set is finite or not)
  def map[B](f: A => B): MySet[B] = politelyFail
  def flatMap[B](f: A => MySet[B]): MySet[B] = politelyFail
  def forEach(f: A => Unit): Unit = politelyFail

  def filter(predicate: A => Boolean): MySet[A] = new PropertyBasedSet[A](x => property(x) && predicate(x))
  def -(elem: A): MySet[A] = filter(_ != elem)
  def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet)
  def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet)

  def unary_! : MySet[A] = new PropertyBasedSet[A](!property(_))

  def politelyFail = throw new IllegalArgumentException("Really deep rabbit hole!")
}

class NonEmptySet[A](head: A, tail: MySet[A]) extends MySet[A] {
  def contains(elem: A): Boolean =
    elem == head || tail.contains(elem)

  def +(elem: A): MySet[A] =
    if (this contains elem) this
    else new NonEmptySet[A](elem, this)

  // call ++ recursively on the tail, then add the head
  // base case is adding anotherSet to EmptySet
  def ++(anotherSet: MySet[A]): MySet[A] =
    tail ++ anotherSet + head

  def map[B](f: A => B): MySet[B] = (tail map f) + f(head)
  def flatMap[B](f: A => MySet[B]): MySet[B] = (tail flatMap f) ++ f(head)

  def filter(predicate: A => Boolean): MySet[A] = {
    val filteredTail = tail filter predicate
    if (predicate(head)) filteredTail + head
    else filteredTail
  }

  def forEach(f: A => Unit): Unit = {
    f(head)
    tail forEach f
  }

  def -(elem: A): MySet[A] =
    if (head == elem) tail
    else tail - elem + head

  // apply(x) is the same as contains(x)
  // so x => anotherSet.contains(x) reduces to just anotherSet
  // intersection = filtering because the set is functional
  def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet)
  def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet)

  // new operator
  def unary_! : MySet[A] = new PropertyBasedSet[A](!this.contains(_))
}

// companion object for creating instances MySet
object MySet {
  // vararg method
  def apply[A](values: A*): MySet[A] = {
    @tailrec
    def buildSet(valSeq: Seq[A], acc: MySet[A]): MySet[A] =
      if (valSeq.isEmpty) acc
      else buildSet(valSeq.tail, acc + valSeq.head)

    buildSet(values, new EmptySet[A])
  }
}


// TESTING
object MySetPlayground extends App {
  val s = MySet(1,2,3,4)
  s + 5 ++ MySet(-1,-2) + 3 flatMap (x => MySet(x, 10 * x)) filter (_ % 2 == 0) forEach println

  val negative = !s // s.unary_!
  println(negative(2)) // true
  println(negative(5)) // false

  val negativeEven = negative.filter(_ % 2 == 0)
  println(negativeEven(5)) // false

  val negativeEven5 = negativeEven + 5
  println(negativeEven5(5)) // true
}
