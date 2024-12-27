package lectures.part4implicits

object OrganisingImplicits extends App {
  // sorted takes an implicit Ordering value and there is already one for Int
  // found in scala.Predef (automatically imported)

  implicit def reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _) // this one takes precedence
//  implicit val normalOrdering: Ordering[Int] = Ordering.fromLessThan(_ < _)
  println(List(1,4,5,3,2).sorted)

  /*
  Implicits potentially used as implicit parameters:
  - val/var
  - objects
  - accessor methods - def with no parentheses
  Must be within an object, class or trait
  */

  // Exercise: Implement an implicit ordering for class Person (e.g. alphabetically by name)
  case class Person(name: String, age: Int)
//  object Person {
//    implicit val alphabeticalOrdering: Ordering[Person] = Ordering.fromLessThan{
//      (p1, p2) => p1.name.compareTo(p2.name) < 0
//    }
//  }

//  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.age < p2.age)

  val persons = List(Person("Steve", 30), Person("Amy", 22), Person("John", 66))
//  println(persons.sorted) // ageOrdering higher priority

  object AlphabeticNameOrdering {
    implicit val alphabeticalOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.name.compareTo(p2.name) < 0)
  }

  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.age < p2.age)
  }

  import AlphabeticNameOrdering._
  println(persons.sorted)

  /*
  Implicit scope
  - normal scope = LOCAL SCOPE (highest priority)
  - imported scope
  - companion objects of all types involved in method signature
    - in sorted example: List, Ordering, all types involved (i.e. A or any supertype)

  When defining an implicit val:
  1. If there is a single possible value that makes sense for the given type
    and you're able to edit the code for this type,
    then define the implicit in the companion object for that type

  2. If there are multiple possible implicit values, but a single good one for most cases
    and you're able to edit the code for the type,
    then define the GOOD implicit in the companion object
    and the other implicits elsewhere (preferably in the local scope or other objects)

  3. If there are multiple good implicit values,
     then package them separately and make the user explicitly import yhe right container
  */
  // def sorted[B >: A](implicit ord: Ordering[B]): List

  /*
  Exercise:
  Add implicit orderings for Purchase by 3 different criteria
  - total price (most used, 50%)
  - unit count (25%)
  - unit price (25%)
  */
  case class Purchase(nUnits: Int, unitPrice: Double)
  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan{
      (a, b) => a.nUnits * a.unitPrice < b.nUnits * b.unitPrice
    }
  }

  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits < b.nUnits)
  }
  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.unitPrice < _.unitPrice)
  }
}
