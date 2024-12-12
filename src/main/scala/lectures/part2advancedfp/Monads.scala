package lectures.part2advancedfp

object Monads extends App {
  /* Monads are a type (implemented as a trait) with fundamental ops:
    - unit (aka pure, apply)
    - flatMap (aka bind)
    List, Option, Try, Future, Stream, Set are all monads

    Operations must satisfy the monad laws:
    1. left-identity: unit(x).flatmap(f) == f(x)
    2. right-identity: aMonadInstance.flatMap(unit) = aMonadInstance
    3. associativity: m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
  */

  // Our own implementation of the Try monad
  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
  }
  // companion object contains apply method (pure/unit) to build an Attempt from a value
  object Attempt {
    // use call by name as we don't want the argument to be evaluated when we build the Attempt
    // (in case it throws an exception)
    def apply[A](a: => A): Attempt[A] =
      try {
        Success(a) // a gets evaluated here
      } catch {
        case e: Throwable => Fail(e)
      }
  }

  case class Success[+A](value: A) extends Attempt[A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B] =
      try {
        f(value)
      } catch {
        case e: Throwable => Fail(e)
      }
  }

  case class Fail(e: Throwable) extends Attempt[Nothing] {
    def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this
  }

  /*
    left-identity:
    Success(x).flatMap(f) = f(x)
    doesn't make sense for Fail since you can't apply f to a throwable

    right-identity:
    Success(x).flatMap(x => Attempt(x)) = Success(x)
    Fail(e).flatMap(...) = Fail(e)

    associativity: attempt.flatMap(f).flatMap(g) == attempt.flatMap(x => f(x).flatMap(g))
    Fail(e).flatMap(f).flatMap(g) = Fail(e) = Fail(e).flatMap(x => f(x).flatMap(g))
    Success(v).flatMap(f).flatMap(g) =
      f(v).flatMap(g) OR Fail(e) =
      Success(v).flatMap(x => f(x).flatMap(g))
  * */

  val attempt = Attempt(throw new RuntimeException("abc"))
  println(attempt)

  /*
    EXERCISE:
    1. Implement a Lazy[T] monad - abstracts away a computation that will only be executed when it's needed
    - unit/apply in a companion object

    2. An alternative way to define a monad is using: unit + map + flatten
    - Implement map and flatten in terms of flatMap, given a Monad[T]
    (have List in mind)

    Monad[T] {
      def flatMap[B](f: T => Monad[B]): Monad[B] = ...
      def map[B](f: T => B): Monad[B] = flatMap(x => unit(f(x)))
      def flatten(m: Monad[Monad[T]]): Monad[T] = m.flatMap((x: Monad[T]) => x)

      List(1,2,3).map(_ * 2) = List(1,2,3).flatMap(x => List(x * 2))
      List(List(1,2), List(3,4)).flatten = List(List(1,2), List(3,4)).flatMap(x => x) = List(1,2,3,4)
    }
  */

  // Lazy can just be a class since it has no subtypes
  class Lazy[+A](value: => A) {
    // call by need
    private lazy val internalValue = value

    def use: A = value

    // applying f to value causes value to be evaluated...
    // so function type must receive parameter A by name
    def flatMap[B](f: (=> A) => Lazy[B]): Lazy[B] = f(internalValue)
  }
  object Lazy {
    def apply[A](value: => A): Lazy[A] = new Lazy(value)
  }

  val lazyInstance = Lazy {
    println("constructing lazy object")
    42
  } // nothing printed just by declaring this

  val flatMappedInstance = lazyInstance.flatMap(x => Lazy(10 * x))
  val flatMappedInstance2 = lazyInstance.flatMap(x => Lazy(10 * x)) // same as flatMappedInstance

  flatMappedInstance.use // forces evaluation of flatMappedInstance, which forces evaulation of lazyInstance
  flatMappedInstance2.use
  // lazyInstance is evaluated twice - separately for flatMappedInstance and flatMappedInstance2
  // unless the value is made call by need

  /*
  left-identity:
  Lazy(v).flatMap(f) = f(v)

  right-identity:
  Lazy(v).flatMap(x => Lazy(x)) = Lazy(v)

  associativity: attempt.flatMap(f).flatMap(g) == attempt.flatMap(x => f(x).flatMap(g))
  Lazy(v).flatMap(f).flatMap(g) =
    f(v).flatMap(g)  =
    Lazy(v).flatMap(x => f(x).flatMap(g))
* */
}
