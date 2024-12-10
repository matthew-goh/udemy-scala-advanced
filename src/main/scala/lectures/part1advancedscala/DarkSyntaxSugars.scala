package lectures.part1advancedscala

import scala.util.Try

object DarkSyntaxSugars extends App {

  // 1. methods with single parameters
  def singleArgMethod(arg: Int): String = s"$arg little ducks..."

  val description = singleArgMethod {
    // write some code and return the result as the parameter
    42
  }

  // example 1: the Try apply method
  val aTry = Try { //  similar to Java's try {...}
    throw new RuntimeException()
  }

  // example 2: collection functionals (map, flatMap, filter)
  List(1,2,3).map { x =>
    x + 1
  }

  // 2. single abstract method pattern:
  // instances of traits with single methods can be reduced to lambdas
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = new Action {
    override def act(x: Int): Int = x + 1
  }

  val aFunkyInstance: Action = (x: Int) => x + 1

  // example: instantiating traits with Runnables
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hello, Scala")
  })

  val aSweeterThread = new Thread(() => println("Hello, Scala"))

  // also works for abstract classes with only one unimplemented member
  abstract class AnAbstractType {
    def implemented: Int = 23
    def f(a: Int): Unit
  }

  val anAbstractInstance: AnAbstractType = (a: Int) => println("sweet")

  // 3. the :: and #:: methods (#:: is prepend operator on streams) are right-associative
  val prependedList = 2 :: List(3, 4)
  // normally (for infix methods), this syntax would be converted to 2.::(List(3,4))
  // in fact, the compiler does List(3,4).::(2)

  // Scala specification says: the associativity of a method is determined by the operator's last character
  // if it ends in a :, then it's right-associative
  1 :: 2 :: 3 :: List(4, 5)
  List(4, 5).::(3).::(2).::(1) // equivalent

  class MyStream[T] {
    def -->:(value: T): MyStream[T] = this // should provide actual implementation
  }
  val myStream = 1 -->: 2 -->: 3 -->: new MyStream[Int]

  // 4. multi-word method naming
  class TeenGirl(name: String) {
    def `and then said`(gossip: String): Unit = println(s"$name said $gossip")
  }
  // can infix this method since it has a single parameter:
  val lily = new TeenGirl("Lily")
  lily `and then said` "Scala is good!"

  // 5. infix generic types
  class Composite[A, B]
//  val composite: Composite[Int, String] = ???
  val composite: Int Composite String = ???

  class -->[A, B]
  val towards: Int --> String = ???

  // 6. update() method is special, much like apply()
  val anArray = Array(1,2,3)
  anArray(2) = 7 // rewritten to anArray.update(2, 7) // .update(index, newValue)
  // used in mutable collections - consider providing an update() method if implementing own mutable container

  // 7. setters for mutable containers
  // e.g. create a mutable wrapper over an Int
  class Mutable {
    private var internalMember: Int = 0 // private for OO encapsulation
    def member = internalMember // "getter"
    def member_=(value: Int): Unit =
      internalMember = value // "setter"
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // rewritten as aMutableContainer.member_=(42)

}
