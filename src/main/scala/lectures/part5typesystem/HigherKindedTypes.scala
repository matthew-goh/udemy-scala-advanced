package lectures.part5typesystem

object HigherKindedTypes extends App {
  // deeper generic type with some unknown type parameter at the deepest level
  trait AHigherKindedType[F[_]]

  // all monads have flatMap and other methods that are very similar across all such types
  trait MyList[T] {
    def flatMap[B](f: T => B): MyList[B]
  }
  trait MyOption[T] {
    def flatMap[B](f: T => B): MyOption[B]
  }
  trait MyFuture[T] {
    def flatMap[B](f: T => B): MyFuture[B]
  }

  // combine/multiply: List(1,2) x List("a", "b") => List(1a, 1b, 2a, 2b)
  // this function works in the same way for e.g. options, futures
//  def multiply[A, B](listA: List[A], listB: List[B]): List[(A, B)] = for {
//    a <- listA
//    b <- listB
//  } yield (a, b)

  // would like to create a common API with a common implementation for all monads
  // - use a higher-kinded type class
  trait Monad[F[_], A] { // A is the type parameter that the monad contains
    def flatMap[B](f: A => F[B]): F[B]
    def map[B](f: A => B): F[B]
  }

  // essentially a wrapper over a list
  implicit class MonadList[A](list: List[A]) extends Monad[List, A] {
    override def flatMap[B](f: A => List[B]): List[B] = list.flatMap(f)
    override def map[B](f: A => B): List[B] = list.map(f)
  }

  implicit class MonadOption[A](option: Option[A]) extends Monad[Option, A] {
    override def flatMap[B](f: A => Option[B]): Option[B] = option.flatMap(f)
    override def map[B](f: A => B): Option[B] = option.map(f)
  }

  // to automatically wrap a monad in e.g. MonadList, use implicit conversion!
  def multiply[F[_], A, B](implicit ma: Monad[F, A], mb: Monad[F, B]): F[(A, B)] = for {
    a <- ma
    b <- mb
  } yield (a, b)
  /*
  ma.flatMap(a => mb.map(b => (a, b)))
   */

  val monadList = new MonadList(List(1,2,3))
  monadList.flatMap(x => List(x, x+1))
  // Monad[List, Int] => List[Int]
  monadList.map(_ * 2) // List[Int]

  println(multiply(List(1,2), List("a", "b")))
  println(multiply(Some(2), Some("scala"))) // Some((2,scala))
}
