package lectures.part4implicits

object ExamplesFromGivens extends App {
  // implicit becomes given for implicit vals and using for implicit parameters

  def extremes[A](list: List[A])(implicit ordering: Ordering[A]): (A, A) = {
    val sortedList = list.sorted // uses implicit ordering
    (sortedList.head, sortedList.last)
  }

  // implicit def to synthesise new implicit values
  trait Combinator[A] {
    def combine(x: A, y: A): A
  }

  implicit def listOrdering[A](implicit simpleOrdering: Ordering[A], combinator: Combinator[A]): Ordering[List[A]] = {
    new Ordering[List[A]] {
      override def compare(x: List[A], y: List[A]): Int = {
        val sumX = x.reduce(combinator.combine)
        val sumY = y.reduce(combinator.combine)
        simpleOrdering.compare(sumX, sumY)
      }
    }
  }

  val aList = List(2, 4, 3, 1)
  val anOrderedList = aList.sorted
  println(anOrderedList)

  // implicit conversions (abused in Scala 2)
  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"
  }

  implicit def stringToPerson(s: String): Person = Person(s)

  val danielsGreet = "Daniel".greet()

  // in Scala 3:
//  import scala.language.implicitConversions
  // given stringToPersonConversion: Conversion[String, Person] with {
  //   override def apply(x: String): Person = Person(x)
  // }
}
