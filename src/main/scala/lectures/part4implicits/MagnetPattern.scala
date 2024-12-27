package lectures.part4implicits

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MagnetPattern extends App {
  // a use case of type classes that solves some of the problems created by method overloading

  // e.g. P2P network
  class P2PRequest
  class P2PResponse
  class Serializer[T]
  trait Actor {
    def receive(statusCode: Int): Int
    def receive(request: P2PRequest): Int
    def receive(response: P2PResponse): Int
    def receive[T: Serializer](message: T): Int
    def receive[T: Serializer](message: T, statusCode: Int): Int
    def receive(future: Future[P2PRequest]): Int
    // lots of overloads
  }

  /*
  Problems:
  1. type erasure: generic types erased at compile time, can't have another method for Future of another type
  2. lifting doesn't work for all overloads
    val receiveFV = receive _
  3. code duplication
  4. type inference and default args
    actor.receive()  - compiler doesn't know what default arg to use for which method
   */

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet()

  // implicit conversion to MessageMagnet
  // in this case, the method being called on the type we have has the same name as that in the type class
  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic for handling a P2PRequest
      println("Handling P2P request")
      42
    }
  }

  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic for handling a P2PResponse
      println("Handling P2P response")
      24
    }
  }

  receive(new P2PRequest)
  receive(new P2PResponse)

  // Benefits of magnet pattern:
  // 1. no more type erasure problems - Scala looks for implicit conversions before types are erased
  implicit class fromResponseFuture(future: Future[P2PResponse]) extends MessageMagnet[Int] {
    def apply(): Int = 2
  }

  implicit class fromRequestFuture(future: Future[P2PRequest]) extends MessageMagnet[Int] {
    def apply(): Int = 3
  }

  println(receive(Future(new P2PRequest)))
  println(receive(Future(new P2PResponse)))

  // 2. lifting works (with a catch)
  // see MathLib example
  trait MathLib {
    def add1(x: Int): Int = x + 1
    def add1(s: String): Int = s.toInt + 1
    // add1 overloads
  }

  // "magnetise"
  // notice no type parameter - if we had one, the compiler wouldn't know for which type addFV applies to
  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }

  implicit class AddString(s: String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val addFV = add1 _
  println(addFV(1))
  println(addFV("3"))

//  val receiveFV = receive _

  // Drawbacks of magnet pattern:
  // 1. verbose
  // 2. harder to read
  // 3. still can't name or place default arguments - not possible to call receive()
  // 4. call by name doesn't work correctly (exercise: demonstrate it - hint: side effects)
  class Handler {
    def handle(s: => String): Unit = {
      println(s)
      println(s)
    }
    // other overloads
  }

  trait HandleMagnet {
    def apply(): Unit
  }

  def handle(magnet: HandleMagnet): Unit = magnet()

  implicit class StringHandle(s: => String) extends HandleMagnet {
    def apply(): Unit = {
      println(s)
      println(s)
    }
  }

  def sideEffectMethod(): String = {
    println("Hello, Scala")
    "hahaha"
  }

  handle(sideEffectMethod()) // prints twice
  handle({
    println("Hello, Scala")
    "hahaha" // only this line is converted to StringHandle
  }) // Hello, Scala prints only once
  // be careful when printing important info like logs
}
