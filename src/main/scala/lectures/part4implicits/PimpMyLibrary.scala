package lectures.part4implicits

import scala.annotation.tailrec

object PimpMyLibrary extends App {
  // type enrichment = pimping
  // e.g. 2.isPrime

  // implicit classes must take exactly one argument
  // extends AnyVal is for memory optimisation, then must make the argument a val
  implicit class RichInt(val value: Int) extends AnyVal {
    def isEven: Boolean = value % 2 == 0
    def sqrt: Double = Math.sqrt(value)

    def times(f: () => Unit): Unit = {
      @tailrec
      def timesAux(reps: Int): Unit = {
        if (reps <= 0) ()
        else {
          f()
          timesAux(reps - 1)
        }
      }
      timesAux(value)
    }

    def *[T](list: List[T]): List[T] = {
      @tailrec
      def concatAux(acc: List[T], reps: Int = value): List[T] = {
        if (reps <= 0) acc
        else concatAux(acc ++ list, reps - 1)
      }
      concatAux(List())
    }
  }

  implicit class RicherInt(richInt: RichInt) {
    def isOdd: Boolean = richInt.value % 2 != 0
  }

  new RichInt(42).sqrt
  42.isEven
  // compiler searches for something it can wrap 42 into that has the method isEven

  // this is how we can use (1 to 10) and 3.seconds
  import scala.concurrent.duration._
  3.seconds

  // compiler doesn't do multiple implicit searches - can't do 42.isOdd

  /*
  Exercise:
  Enrich the String class
  - asInt
  - encrypt (Caesar cipher)
    e.g. John => Lqjp (+2 letters)

  Keep enriching the Int class
  - times(function)
    e.g. 3.times(() => ...)
  - * with a list as argument
    e.g. 3 * List(1,2) => List(1,2,1,2,1,2)
  */

  implicit class RichString(val s: String) extends AnyVal {
    def asInt: Int = Integer.valueOf(s) // returns java.lang.Integer, which Scala converts to an Int
    def encrypt(shift: Int): String = s.map(c => (c + shift).asInstanceOf[Char])
  }
  println("3".asInt + 4)
  println("John".encrypt(2))
  println("Zzz".encrypt(2))

  println(3.times(() => println("Scala rocks")))
  println(4 * List(1,2))

  // what if we want to do "3" + 4 ?
  // implicit conversion (more general than implicit classes)
  implicit def stringToInt(s: String): Int = Integer.valueOf(s)
  println("6" / 2)

  // equivalent to implicit class RichAltInt(value: Int)
  class RichAltInt(value: Int)
  implicit def enrich(value: Int): RichAltInt = new RichAltInt(value)

  // although the implicit conversions with methods are more powerful, they are discouraged
  // danger: if something goes wrong, very hard to trace the error
  implicit def intToBoolean(i: Int): Boolean = i == 1
  val aConditionedValue = if(3) "OK" else "Something wrong"
  println(aConditionedValue) // Something wrong = implicit conversion called on 3 to make it a Boolean, returns false
}
