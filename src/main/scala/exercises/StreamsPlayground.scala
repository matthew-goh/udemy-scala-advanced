package exercises

import scala.annotation.tailrec

abstract class MyStream[+A] {
  def isEmpty: Boolean
  def head: A
  def tail: MyStream[A]

  def #::[B >: A](elem: B): MyStream[B] // prepend operator
  def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B] // concatenate 2 streams (anotherStream comes after)

  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): MyStream[B]
  def flatMap[B](f: A => MyStream[B]): MyStream[B]
  def filter(predicate: A => Boolean): MyStream[A]

  def take(n: Int): MyStream[A] // returns a stream of the first n elements
  def takeAsList(n: Int): List[A] = take(n).toList()

  // toList must be final so that no other subtypes can override it in a non-tailrec fashion
  @tailrec
  final def toList[B >: A](acc: List[B] = Nil): List[B] =
    if (isEmpty) acc.reverse
    else tail.toList(head :: acc)
}

object MyStream {
  // factory method:
  // generator generates the next value based on the prev known value
  def from[A](start: A)(generator: A => A): MyStream[A] =
    new Cons(start, MyStream.from(generator(start))(generator))
}

object EmptyStream extends MyStream[Nothing] {
  def isEmpty: Boolean = true
  def head: Nothing = throw new NoSuchElementException()
  def tail: MyStream[Nothing] = throw new NoSuchElementException()

  def #::[B >: Nothing](elem: B): MyStream[B] = new Cons(elem, this)
  def ++[B >: Nothing](anotherStream: => MyStream[B]): MyStream[B] = anotherStream

  def foreach(f: Nothing => Unit): Unit = ()
  def map[B](f: Nothing => B): MyStream[B] = this
  def flatMap[B](f: Nothing => MyStream[B]): MyStream[B] = this
  def filter(predicate: Nothing => Boolean): MyStream[Nothing] = this

  def take(n: Int): MyStream[Nothing] = this
//  def takeAsList(n: Int): List[Nothing] = Nil
}

// IMPORTANT: tail must be *by name*
class Cons[+A](h: A, t: => MyStream[A]) extends MyStream[A] {
  def isEmpty: Boolean = false
  override val head: A = h // override as a val so that we just evaluate it once and it can be reused throughout
  override lazy val tail: MyStream[A] = t // call by need: combining a call by name param with a lazy val inside

  /*
  val s = new Cons(1, EmptyStream)
  val prepended = 1 #:: s = new Cons(1, s) // s is lazily evaluated, so the tail of s remains unevaluated
  */
  def #::[B >: A](elem: B): MyStream[B] = new Cons(elem, this)
  def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B] = new Cons(head, tail ++ anotherStream)

  def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }
  /*
  s = new Cons(1, EmptyStream)
  mapped = s.map(_+1) = new Cons(2, s.tail.map(_+1)) // s.tail.map(_+1) only evaluated if mapped.tail is called
  */
  def map[B](f: A => B): MyStream[B] = new Cons(f(head), tail.map(f)) // tail.map(f) not evaluated until needed
  def flatMap[B](f: A => MyStream[B]): MyStream[B] = f(head) ++ tail.flatMap(f)
  def filter(predicate: A => Boolean): MyStream[A] =
    if (predicate(head)) new Cons(head, tail.filter(predicate))
    else tail.filter(predicate) // forces evaluation of the tail's head

  def take(n: Int): MyStream[A] =
    if (n <= 0) EmptyStream
    else if (n == 1) new Cons(head, EmptyStream) // optimisation
    else new Cons[A](head, tail.take(n-1))

//  def takeAsList(n: Int): List[A] = {
//    if (n <= 0) Nil
//    else head :: tail.takeAsList(n-1)
//  }
}

object StreamsPlayground extends App {
  val naturals = MyStream.from(1)(_ + 1)
  println(naturals.head)
  println(naturals.tail.tail.head)

  val startFrom0 = 0 #:: naturals // naturals.#::(0)
  println(startFrom0.head)

  startFrom0.take(10000).foreach(println)

  println(startFrom0.map(_ * 2).take(100).toList())

//  println(startFrom0.flatMap(x => new Cons(x, new Cons(x+1, EmptyStream))).take(10).toList())
  // StackOverflowError! flatMap is evaluated recursively too many times
  // it must be that ++ doesn't preserve lazy evaluation - the parameter is eagerly evaluated!
  // -change it to call by name
  println(startFrom0.flatMap(x => new Cons(x, new Cons(x+1, EmptyStream))).take(10).toList())

//  println(startFrom0.filter(_ < 10).toList())
  // StackOverflowError! Filtering an infinite stream doesn't guarantee that the result is finite
  // So the computer goes through all possible naturals
  println(startFrom0.filter(_ < 10).take(10).toList())

  // BUT println(startFrom0.filter(_ < 10).take(11).toList()) crashes
  // after taking the first 10 elements, the computer tries to find the 11th element that satisfies the filter
  println(startFrom0.filter(_ < 10).take(10).take(20).toList())

  // Exercises on streams
  // 1. stream of Fibonacci numbers [ 1 1 2 3 5 8 13 ... ]
  // 2. stream of prime numbers with Eratosthenes' sieve
  /*
   start with [ 2 3 4 ... ]
   filter out all numbers divisible by 2 - left with [ 2 3 5 7 9 11 ... ]
   then filter out all numbers divisible by 3 - left with [ 2 3 5 7 11 13 17 ... ]
   then filter out all numbers divisible by 5 (next number left not considered yet)
  */

//  def from2[A](prev: A, curr: A)(generator: (A, A) => A): MyStream[A] =
//    new Cons(curr, from2(curr, generator(prev, curr))(generator))
//  /* from2(0, 1)(_ + _)
//  = [1, from2(1, 1)(_ + _)]
//  = [1, 1, from2(1, 2)(_ + _)]
//  = [1, 1, 2, from2(2, 3)(_ + _)]
//  */
//  val fibonacci = from2(0, 1)(_ + _)

  def fibonacci(first: BigInt, second: BigInt): MyStream[BigInt] =
    new Cons(first, fibonacci(second, first + second))

  println(fibonacci(1, 1).take(100).toList())

  val natsFrom2 = MyStream.from(2)(_ + 1)
//  def repeatedlyRemoveMultiplesOfHead(s: MyStream[Int]): MyStream[Int] = {
//    new Cons(s.head, repeatedlyRemoveMultiplesOfHead(s.tail.filter(_ % s.head != 0)))
//  }
//  val primes = repeatedlyRemoveMultiplesOfHead(natsFrom2)
//  println(primes.take(10).toList())

  def eratosthenes(numbers: MyStream[Int]): MyStream[Int] =
    if (numbers.isEmpty) numbers
    else new Cons(numbers.head, eratosthenes(numbers.tail.filter(_ % numbers.head != 0)))

  println(eratosthenes(natsFrom2).take(100).toList())
}
