package playground

import lectures.part4implicits.TypeClasses.User

object EqualityPlayground extends App {

  /*
    Exercise 1: Equality
      - 2 instances of the type class that compare users
    Exercise 2: Implement the type class pattern for Equality
    Exercise 3: Improve it with an implicit conversion class with methods:
      ===(another value: T)
      !==(another value: T)
    */
  trait Equal[T] {
    def apply(a: T, b: T): Boolean
  }

  object Equal {
    def apply[T](a: T, b: T)(implicit equalityChecker: Equal[T]): Boolean = {
      equalityChecker(a, b) // equalityChecker.apply(a, b)
    }
  }

  implicit object NameEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name
  }

  object FullEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }

  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)(implicit equalityChecker: Equal[T]): Boolean = equalityChecker(value, other)
    def !==(other: T)(implicit equalityChecker: Equal[T]): Boolean = !equalityChecker(value, other)
  }

  val john = User("John", 32, "john@rockthejvm.com")
  val anotherJohn = User("John", 45, "anotherjohn@rockthejvm.com")
  println(Equal(john, anotherJohn))
  // AD-HOC polymorphism

  println(john === anotherJohn)
  println(john !== anotherJohn)
  /*
  john.===(anotherJohn)
  new TypeSafeEqual[User](john).===(anotherJohn)
  new TypeSafeEqual[User](john).===(anotherJohn)(NameEquality)
   */
  // TYPE SAFE: println(john === 43) will not compile
}
