package lectures.part2advancedfp

import javax.sql.rowset.Predicate

object LazyEvaluation extends App {
  // lazy values are evaluated once, but only when used for the first time
  // - delays the evaluation of values
  lazy val e: Int = throw new RuntimeException() // program doesn't crash unless e is used
  lazy val x: Int = {
    println("Hello")
    42
  }
  println(x) // prints both Hello and 42 as x is evaulated for the first time
  println(x) // only prints 42, the value of x

  // Examples of implications:
  // 1. side effects
  def sideEffectCondition: Boolean = {
    println("Boo")
    true
  }
  def simpleCondition: Boolean = false

  lazy val lazyCondition = sideEffectCondition
  println(if (simpleCondition && lazyCondition) "yes" else "no") // only prints no - lazyCondition not evaluated since simpleCondition is false

  // 2. in conjunction with call by name
  def byNameMethod(n: => Int): Int = {
//    n + n + n + 1
    // CALL BY NEED
    lazy val t = n // only evaluated once
    t + t + t + 1
  }
  def retrieveMagicValue: Int = {
    // side effect or long computation
    Thread.sleep(1000)
    println("waiting")
    42
  }
  println(byNameMethod(retrieveMagicValue))
  // without lazy vals, retrieveMagicValue is evaluated 3 times (waiting is printed 3 times)!
  // using lazy vals with a call by name parameter is called CALL BY NEED:
  // useful when you want to evaluate the param only when needed but use the same value in the rest of the code

  // 3. filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }
  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1, 25, 40, 5, 23)
  val lt30 = numbers.filter(lessThan30) // prints for all 5 numbers
  val gt20 = lt30.filter(greaterThan20) // prints for the 4 remaining numbers
  println(gt20)

  // withFilter is a function on collections that uses lazy values under the hood
  val lt30lazy = numbers.withFilter(lessThan30)
  val gt20lazy = lt30lazy.withFilter(greaterThan20)
  println()
  println(gt20lazy)
  // other than scala.collection.IterableOps$WithFilter@4524411f, nothing was printed!
  gt20lazy.foreach(println) // this forces the filtering to take place
  // but each element is checked against the predicates in sequence before moving to the next element - checking on a by need basis

  // for comprehensions use withFilter with guards
  for {
    a <- List(1,2,3) if a % 2 == 0 // use lazy vals!
  } yield a + 1
  List(1,2,3).withFilter(_ % 2 == 0).map(_ + 1) // map turns it back into a List

  /*
  Exercise: See StreamsPlayground in exercises package
  * Implement a lazily evaluated, singly linked, potentially infinite STREAM of elements
  * The head of a stream is always evaluated and available, but the tail is lazily evaluated and available only on demand
  *
  * naturals = MyStream.from(1)(x => x + 1) // stream of natural numbers (infinite)
  * naturals.take(100) // lazily evaluated stream of 1-100 (finite)
  * naturals.foreach(println) // will crash
  * naturals.map(_ * 2) // stream of all positive even numbers
  * */


}
