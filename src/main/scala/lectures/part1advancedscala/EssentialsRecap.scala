package lectures.part1advancedscala

import scala.annotation.tailrec

object EssentialsRecap extends App {

  // declaring vals and vars
  val aCondition: Boolean = false
  // expressions
  val aConditionedVal = if (aCondition) 42 else 65
  // instructions (imperative) vs expressions (FP)

  // code blocks
  // compiler infers types
  val aCodeBlock = {
    if (aCondition) 54 // unused expression
    56
  }

  // Unit = void
  val theUnit = println("Hello Scala")

  // functions
  def aFunction(x: Int): Int = x + 1

  // recursion: stack and tail
  @tailrec
  def factorial(n: Int, acc: Int = 1): Int = {
    if (n <= 0) acc
    else factorial(n - 1, n * acc)
  }

  // object-orientated programming - everything in Scala is a class/object/package object
  class Animal
  // single inheritance
  class Dog extends Animal
  val aDog: Animal = new Dog // subtyping polymorphism

  // abstract data types: abstract classes and traits
  trait Carnivore {
    def eat(a: Animal): Unit
  }

  // mixing traits into classes
  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Crunch!")
  }

  // method notations
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog // natural language

  // anonymous classes - supply an implementation of an abstract data type on the spot
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar!")
  }

  // generics
  abstract class MyList[+A] // covariance
  // singletons and companions
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // exceptions and try/catch/finally
  val throwsException = throw new RuntimeException() // type is Nothing
  val aPotentialFailure = try {
    throw new RuntimeException()
  } catch {
    case e: Exception => "caught an exception"
  } finally {
    println("some logs")
  }

  // packaging and imports

  // functional programming
  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  // syntactic sugar that reduces to a Function1 instance
  val anonymousIncrementer = (x: Int) => x + 1
  List(1,2,3).map(anonymousIncrementer) // map is a HOF
  // map, flatMap, filter
  // for comprehensions
  val pairs = for {
    num <- List(1,2,3) // can add if condition
    char <- List('a','b','c')
  } yield num + "-" + char

  // Collections: Seqs, Arrays, Lists, Vectors, Maps, Tuples
  val aMap = Map("Daniel" -> 789, "Jess" -> 555)

  // "collections": Option, Try
  val anOption = Some(2)

  // Pattern matching
  val x = 2
  val order = x match {
    case 1 => "first"
    case 2 => "second"
    case 3 => "third"
    case _ => x + "th"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(name, _) => s"Hi, my name is $name"
  }
}
