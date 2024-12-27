package lectures.part4implicits

object ImplicitsIntro extends App {
  // how does this compile? there is no arrow method on the String or any other class
  // -> is an implicit method - a method from an implicit class
  val pair = "Daniel" -> "555"

  case class Person(name: String) {
    def greet = s"Hi, my name is $name!"
  }

  implicit def fromStringToPerson(str: String): Person = Person(str)

  // compiler looks for all implicits that can turn the string into something that has a greet method
  println("Peter".greet) // println(fromStringToPerson("Peter").greet)

  // if there are multiple possible implicits, compilation fails
//  class A {
//    def greet: Int = 2
//  }
//  implicit def fromStringToA(str: String): A = new A

  // implicit parameters
  def increment(x: Int)(implicit amount: Int) = x + amount
  implicit val defaultAmount: Int = 10 // good practice to add type to implicit val
  increment(2) // compiler fetches an implicit value for the second parameter list
  // NOT the same as default args
}
