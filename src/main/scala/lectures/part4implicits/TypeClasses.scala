package lectures.part4implicits

import java.util.Date

object TypeClasses extends App {

  trait HTMLWritable {
    def toHTML: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    def toHTML: String = s"<div>$name ($age years old) <a href=$email/></div>"
  }

  val john = User("John", 32, "john@rockthejvm.com")
  john.toHTML

  // 2 disadvantages:
  // - Only works for the types WE write
  // - This is only one implementation of many

  // Option 2: use pattern matching
  object HTMLSerializerPM {
    def serializeToHTML(value: Any): String = value match {
      case User(name, age, email) => s"<div>$name ($age years old) <a href=$email/></div>"
      case date: Date => date.toString
      case _ => "???"
    }
  }

  // Disadvantages:
  // - Lost type safety
  // - Need to modify the code whenever we want to render something different
  // - Still one implementation for each possible type

  // Better design
  // - Can define serializers for various types
  // - Can define multiple serializers for a given type
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  implicit object UserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name} (${user.age} years old) <a href=${user.email}/></div>"
  }
  println(UserSerializer.serialize(john))

  object DateSerializer extends HTMLSerializer[Date] {
    def serialize(date: Date): String = s"<div>${date.toString}/></div>"
  }

  object PartialUserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name}</div>"
  }

  // HTMLSerializer is a TYPE CLASS
  // All the objects that extend HTMLSerializer are TYPE CLASS INSTANCES

  // Part 2
  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div style: color=blue>$value</div>"
  }

  // Invoking type class instances:
  // Can create a companion object with an apply() method to surface out the type class instance
  // Or enrich the type with an implicit class (containing method with implicit parameter)

  // given the value of type T, compiler searches for an implicit value of type HTMLSerializer[T]
  println(HTMLSerializer.serialize(42))
  println(HTMLSerializer.serialize(john))

  // access to the entire type class interface
  println(HTMLSerializer[User].serialize(john))

  // Part 3
  implicit class HTMLEnrichment[T](value: T) {
    def ToHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  println(john.ToHTML(UserSerializer)) // println(new HTMLEnrichment[User](john).ToHTML(UserSerializer))
  println(john.ToHTML)
  /*
  - extend to new types
  - choose implementation for same type by importing or passing explicitly
   */
  println(2.ToHTML)
  println(john.ToHTML(PartialUserSerializer))

  /*
  Type class functionality consists of several parts:
  - type class itself (trait)
  - type class instances (some implicit) - UserSerializer, IntSerializer, etc.
  - conversion with implicit classes, allowing the type class instances to be used as implicit parameters - HTMLEnrichment
   */

  // context bounds
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String =
    s"<html><body>${content.ToHTML(serializer)}</body></html>"

  // : HTMLSerializer is a context bound that tells the compiler to inject an implicit parameter of type HTMLSerializer[T]
  // but cannot use serializer by name unless we use implicitly
  def htmlSugar[T : HTMLSerializer](content: T): String = {
    val serializer = implicitly[HTMLSerializer[T]]
    s"<html><body>${content.ToHTML(serializer)}</body></html>"
  }

  // implicitly
  case class Permissions(mask: String)
  implicit val defaultPermissions: Permissions = Permissions("0744")

  // in some other part of the code, want to surface out the implicit val for Permissions
  val standardPerms = implicitly[Permissions]
}
