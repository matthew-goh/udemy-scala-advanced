package lectures.part3concurrency

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success, Try}

object FuturesPromises extends App {
  // Futures are a functional way of computing something in parallel or on another thread
  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  // Future .apply() method requires an implicit ExecutionContext
  // -handles thread allocation of Futures
  val aFuture = Future {
    calculateMeaningOfLife // runs this method on another thread
  }

  println(aFuture.value) // None - returns Option[Try[Int]]
  println("waiting on the future")
  // onComplete returns Unit - used for side effects
  aFuture.onComplete { // partial function
    case Success(meaning) => println(s"The meaning of life is $meaning")
    case Failure(e) => println(s"Failed with $e")
  } // called by SOME thread, don't know which

  Thread.sleep(3000)

  // mini social network
  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile): Unit =
      println(s"${this.name} poking ${anotherProfile.name}")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.2-bill" -> "Bill",
      "fb.id.0-dummy" -> "Dummy"
    )

    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.2-bill"
    )

    val random = new Random()

    // API
    def fetchProfile(id: String): Future[Profile] = Future {
      // simulates fetching from the DB
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

  // client application: mark to poke bill
  val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
//  mark.onComplete{
//    case Success(markProfile) => {
//      val bill = SocialNetwork.fetchBestFriend(markProfile)
//      bill.onComplete{
//        case Success(billProfile) => markProfile.poke(billProfile)
//        case Failure(e) => e.printStackTrace()
//      }
//    }
//    case Failure(ex) => ex.printStackTrace()
//  }

  // functional composition of futures
  // map, flatMap, filter
  val nameOnTheWall = mark.map(profile => profile.name) // Future[String]
  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val marksBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("Z")) // can fail with NoSuchElementException

  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } mark.poke(bill)

  Thread.sleep(1000) // ensure all futures have time to finish

  // fallbacks
  val aProfileAlways = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Dummy")
  }

  val aFetchedProfileAlways = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
  }

  // if the fallback future fails, the FIRST exception will be used
  val fallBackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))

  // Blocking on a future
  // online banking app
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Banking App"

    def fetchUser(name: String): Future[User] = Future {
      // simulate fetching from the DB
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate some processes, e.g. checking merchant status, whether it is a scam
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "Success")
    }

    // returns transaction status
    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      // fetch user from DB, check they have enough money in account
      // create a transaction
      // WAIT for transaction to finish
      val transactionStatusFuture: Future[String] = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      Await.result(transactionStatusFuture, 2.seconds) // implicit conversions - pimp my library
      // Await.ready returns the same future when ready, instead of its contents
    }
  }

  // no need sleeping calls in main thread since this call will block until all required futures are completed
  println(BankingApp.purchase("Daniel", "iPhone12", "Apple Store", 3000))

  // Promises
  // what if we need to specifically set or complete a future at a point of our choosing?
  val promise = Promise[Int]() // "controller" over a future
  val future = promise.future // future containing the value of this promise

  // thread 1 - "consumer"
  future.onComplete{
    case Success(r) => println(s"[consumer] I have the received the value $r")
  }

  // thread 2 - "producer"
  val producer = new Thread(() => {
    println("[producer] crunching numbers...")
    Thread.sleep(500)
    // "fulfilling" the promise
    promise.success(42) // manipulates the internal future to complete with a successful value
    println("[producer] done")
  })

  producer.start() // when producer is done, consumer is automatically triggered
  Thread.sleep(1000)

  /*
  Exercises
  1. Write a future that returns (fulfills) immediately with a value
  2. Run a function that returns a future after a future has finished running
  - inSequence(fa, fb) runs fb after fa has completed
  3. Return a future containing the earliest value returned by 2 futures
  - first(fa, fb) => new future with the first value
  4. last(fa, fb) => new future with the last value
  5. Run an action repeatedly until a condition is met and return the first value that satisfies the condition
  - retryUntil[T](action: () => Future[T], condition: T => Boolean): Future[T]
  */

  def fulfillImmediately[T](value: T): Future[T] = Future(value)
  // or Future.successful(value) - faster as the future is fulfilled synchronously (no thread needed)

  def inSequence[A, B](fa: Future[A], fb: Future[B]): Future[B] =
    fa.flatMap(_ => fb)

  def first[T](fa: Future[T], fb: Future[T]): Future[T] = {
    val promise = Promise[T]

    // if e.g. fb has completed, the promise will already be fulfilled and fa running this causes an exception
//    def tryComplete(promise: Promise[T], result: Try[T]) = result match {
//      case Success(r) => try { promise.success(r) } catch { case _ => () }
//      case Failure(t) => try { promise.failure(t) } catch { case _ => () }
//    }
//
//    fa.onComplete(tryComplete(promise, _))
//    fb.onComplete(tryComplete(promise, _))

    // there is already a tryComplete function as a method on promise!
    fa.onComplete(promise.tryComplete)
    fb.onComplete(promise.tryComplete)

    promise.future
  }

  def last[T](fa: Future[T], fb: Future[T]): Future[T] = {
    // 1st promise: both futures try to complete (the first will succeed, the last will fail)
    // 2nd promise: completed by last future when it fails to complete the 1st promise
    val bothPromise = Promise[T]
    val lastPromise = Promise[T]
    val checkAndComplete = (result: Try[T]) =>
      if(!bothPromise.tryComplete(result)) lastPromise.complete(result)

    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)

    lastPromise.future
  }

  val fast = Future {
    Thread.sleep(100)
    42
  }

  val slow = Future {
    Thread.sleep(200)
    45
  }

  first(fast, slow).foreach(f => println(s"First: $f")) // 42
  last(fast, slow).foreach(l => println(s"Last: $l")) // 45
  // wait for fast and slow to complete
  Thread.sleep(500)

  def retryUntil[T](action: () => Future[T], condition: T => Boolean): Future[T] = {
    action().filter(condition) // if condition doesn't hold, this returns NoSuchElementException
      .recoverWith {
        case _ => retryUntil(action, condition)
      }
  }

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println("generated " + nextValue)
    nextValue
  }
  retryUntil(action, (x: Int) => x < 20).foreach(result => println(s"Settled at $result"))
  Thread.sleep(10000) // sleep for 10 seconds - retryUntil not guaranteed to return a value quickly
}
