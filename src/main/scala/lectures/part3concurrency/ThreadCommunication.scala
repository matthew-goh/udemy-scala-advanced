package lectures.part3concurrency

import scala.collection.mutable
import scala.jdk.FunctionWrappers.FromJavaUnaryOperator
import scala.util.Random

object ThreadCommunication extends App {
  /*
    the producer-consumer problem
    -start with a small container that wraps a single value
    -thread producer has the sole purpose of setting the value inside the container
    -thread consumer has the sole purpose of extracting the value from the container
    producer -> [ ? ] -> consumer
  */

  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0

    // producing method
    def set(newValue: Int): Unit = {
      value = newValue
    }

    // consuming method (resets value)
    def get: Int = {
      val result = value
      value = 0
      result
    }
  }

  def naiveProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      while(container.isEmpty) { // busy waiting
        println("[consumer] actively waiting...")
      }
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500) // simulate long computation
      val value = 42
      println("[producer] I have produced, after long work, the value " + value)
      container.set(value)
    })

    consumer.start()
    producer.start()
  }

//  naiveProdCons()

  // note: only AnyRefs can have synchronised blocks
  // wait() and notify() within synchronised expressions:
  // waiting on an object's monitor suspends the thread indefinitely (releases the lock during this time)
  // notify signals ONE sleeping thread waiting on the object's monitor that they may continue AFTER they acquire the lock
  def smartProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      container.synchronized {
        container.wait() // releases lock on container until producer signals that it can continue
      }
      // now container must have some value
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] Hard at work...")
      Thread.sleep(2000) // simulate long computation
      val value = 42
      container.synchronized {
        println("[producer] I'm producing " + value)
        container.set(value)
        container.notify() // signals the consumer thread to wake up when lock on container is available
      }
    })

    consumer.start()
    producer.start()
  }

//  smartProdCons()

  /*
    producer (value placed in any spot in the buffer) -> [ ? ? ? ] -> consumer (extracts any value that is new)

    both producer and consumer could block each other
    if buffer is full, then producer must block until consumer has finished extracting values
    if buffer is empty, then consumer must block until producer fills in new values
  */

  def prodConsLargeBuffer(): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    val consumer = new Thread(() => {
      val random = new Random()

      while(true) { // consumer does this forever until thread is stopped
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty, waiting...")
            buffer.wait()
          }

          // now there must be at least 1 value in the buffer
          val x = buffer.dequeue()
          println(s"[consumer] consumed $x")

          buffer.notify() // in case producer is sleeping, send a signal that it can continue
        }
        Thread.sleep(random.nextInt(500)) // simulate a computation the consumer needs to do
      }
    })

    val producer = new Thread(() => {
      val random = new Random()
      var i = 0

      while(true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full, waiting...")
            buffer.wait()
          }

          // now there must be at least 1 empty space in the buffer
          println(s"[producer] producing $i")
          buffer.enqueue(i)

          buffer.notify() // in case consumer is sleeping, send a signal that it can continue
          i += 1
        }
        Thread.sleep(random.nextInt(500)) // if producer 2x faster than consumer, usually block on buffer full
      }
    })

    consumer.start()
    producer.start()
  }

//  prodConsLargeBuffer()

  /*
    multiple producers -> [ ? ? ? ] -> multiple consumers
  */

  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      // suppose 2 consumers are waiting when a value is produced
      // notify() will notify ONE consumer
      // then this consumer's buffer.notify() notifies the other consumer - wrong!
      // - use while (buffer.isEmpty) instead of if
      while(true) { // consumer does this forever until thread is stopped
        buffer.synchronized {
          while (buffer.isEmpty) { // even if woken up from wait, goes back to waiting if buffer is empty
            println(s"[consumer $id] buffer empty, waiting...")
            buffer.wait()
          }

          // now there must be at least 1 value in the buffer
          val x = buffer.dequeue()
          println(s"[consumer $id] consumed $x")

          buffer.notify() // somebody wakes up - could be a producer or another consumer
        }
        Thread.sleep(random.nextInt(250))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0

      while(true) {
        buffer.synchronized {
          while (buffer.size == capacity) { // even if woken up from wait, goes back to waiting if buffer is full
            println(s"[producer $id] buffer is full, waiting...")
            buffer.wait()
          }

          // now there must be at least 1 empty space in the buffer
          println(s"[producer $id] producing $i")
          buffer.enqueue(i)

          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500)) // if producer 2x faster than consumer, usually block on buffer full
      }
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 20

    (1 to nConsumers).foreach(i => new Consumer(i, buffer).start())
    (1 to nProducers).foreach(i => new Producer(i, buffer, capacity).start())
  }

  // if consumers are twice as fast, but there are twice as many producers, rarely block
//  multiProdCons(nConsumers = 3, nProducers = 6)

  /*
    Exercises
    1. Find an example where notifyAll() acts in a different way from notify()
    notifyAll() prevents a possible deadlock
    2. Create a deadlock
    3. Create a livelock - threads yield execution to each other in such a way that none can continue
  */

  def testNotifyAll(): Unit = {
    val bell = new Object

    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized{
        println(s"[thread $i] waiting...")
        bell.wait()
        println(s"[thread $i] woken up")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[announcer] ring")
      bell.synchronized{
        bell.notifyAll()
        // with notify(), only one thread will wake up and print
      }
    }).start()
  }
//  testNotifyAll()

  // deadlock example
  case class Friend(name: String) {
    def bow(other: Friend): Unit = {
      this.synchronized{
        println(s"$this: I am bowing to my friend $other")
        other.rise(this)
        println(s"$this: My friend $other has risen")
      }
    }

    def rise(other: Friend): Unit = {
      this.synchronized{
        println(s"$this: I am rising to my friend $other")
      }
    }

    var side = "right"
    def switchSide(): Unit = {
      if (side == "right") side = "left"
      else side = "left"
    }

    def pass(other: Friend): Unit = {
      while(this.side == other.side){
        println(s"$this: Switching sides to make way for $other")
        switchSide()
        Thread.sleep(1000)
      }
    }
  }

  val sam = Friend("Sam")
  val pierre = Friend("Pierre")

//  new Thread(() => sam.bow(pierre)).start() // locks sam, then needs pierre's lock
//  new Thread(() => pierre.bow(sam)).start() // locks pierre, then needs sam's lock

  // livelock example
  new Thread(() => sam.pass(pierre)).start()
  new Thread(() => pierre.pass(sam)).start()
}
