package lectures.part1advancedscala

object AdvancedPatternMatching extends App {

  val numbers = List(1)
  val description = numbers match {
    case head :: Nil => println(s"The only element is $head")
    case _ =>
  }

  /*
  Patterns available for pattern matching:
  - constants
  - wildcards
  - case classes
  - tuples
  - the above
  How can we make our own structures compatible with pattern matching?
  */

  // Suppose there is a class that for some reason can't be a case class
  class Person(val name: String, val age: Int)

  // define a companion object with a special method unapply()
  // it should take as input the object being decomposed (e.g. an instance of Person)
  // and return an Option of a single value or a tuple containing the specific values to match on
  // Note: it could also return a Boolean if only testing conditions (see exercise) or something else (see custom return types)
  object Person {
    def unapply(person: Person): Option[(String, Int)] =
      if (person.age < 21) None
      else Some((person.name, person.age))

    // can overload unapply()
    def unapply(age: Int): Option[String] = Some(if (age < 21) "minor" else "major")
  }

  val bob = new Person("Bob", 25)
  val greeting = bob match {
    case Person(name, age) => s"Hi, my name is $name and I am $age years old."
  }
  println(greeting)
  // if unapply() returns None, then the pattern match doesn't work => MatchError
  // note: the Person being matched on is the object, not the class

  val legalStatus = bob.age match {
    // status is what's returned by unapply()
    case Person(status) => s"My legal status is $status"
  }
  println(legalStatus)

  /*
    Exercise:
    Devise a custom pattern matching solution for conditions on a number (see mathProperty example).
   */
  val n: Int = 8
  val mathProperty = n match {
    case x if (x < 10) => "single digit"
    case x if (x % 2 == 0) => "even number"
    case _ => "no property"
  }

  // define a singleton object with unapply() for each condition
  // in practice, name them in lowercase

  // using an Option, it doesn't matter what is inside, only that it's a Some
  // so better to return a Boolean and call e.g. even(), which the compiler interprets as a single Boolean test
  object even {
    // we only want the pattern to match if the condition holds
    def unapply(n: Int): Boolean = n % 2 == 0
  }

  object singleDigit {
    def unapply(n: Int): Boolean = n > -10 && n < 10
  }

  val mathProperty2 = n match {
    case singleDigit() => "single digit"
    case even() => "even number"
    case _ => "no property"
  }
  println(mathProperty2)


  /// INFIX PATTERNS ///
  case class Or[A, B](a: A, b: B) // our own version of Either
  val either = Or(2, "two")
  val humanDescription = either match {
//    case Or(number, string) => s"$number is written as $string"
    case number Or string => s"$number is written as $string"
  }
  println(humanDescription)


  /// DECOMPOSING SEQUENCES ///
  val vararg = numbers match {
    // _* is a vararg pattern
    case List(1, _*) => "starting with one"
  }
  // here, we are pattern matching against the whole list as a sequence
  // -can have any number of values to decompose
  // How can we do the same for our own classes?
  // -standard methods for unapplying don't work in this case

  abstract class MyList[+A] {
    def head: A = ???
    def tail: MyList[A] = ???
  }
  case object Empty extends MyList[Nothing]
  case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  // define an unapplySeq() method on an object
  // turns the MyList into a Seq with the same elements in the same order
  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
      if (list == Empty) Some(Seq.empty)
      else unapplySeq(list.tail).map(list.head +: _) // want to prepend the head within the Option
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
  val decomposed = myList match {
    case MyList(1, 2, _*) => "starting with 1, 2"
    case _ => "something else"
  }
  println(decomposed)
  // since _* was written, compiler expects unapplySeq()


  /// Custom return types for unapply() - rarely needed! ///
  // The return type for unapply() or unapplySeq() need not be Option
  // It just needs to have the methods isEmpty() and get()
  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      override def isEmpty: Boolean = false
      override def get: String = person.name
    }
  }

  println(bob match {
    case PersonWrapper(name) => s"This person's name is $name"
    case _ => "Unknown"
  })
  // the value inside PersonWrapper is whatever the get method returns when called on the result of unapply()
  // and if isEmpty is true for the result of unapply(), the pattern doesn't match
}
