package lectures.part3concurrency

import java.util.concurrent.Executors

object Intro extends App {
  // focus on the creation, manipulation and communication of JVM threads
  /* in the package java.lang, there is
    interface Runnable {
      public void run()
    }
    - treat interfaces as traits in Scala
  */

  // instantiate a Thread object with a Runnable object whose method run() prints something in parallel
  val runnable = new Runnable {
    override def run(): Unit = println("Running in parallel...")
  }
  val aThread = new Thread(runnable)

  aThread.start() // gives the signal to the JVM to start a JVM thread, which runs on top of an OS thread
  // the println is executed on a separate JVM thread from the one that evaluates the code in this file
  // Note: call the start() method on the Thread, not the run() method on the Runnable
  runnable.run() // doesn't do anything in parallel!

  aThread.join() // blocks until aThread finishes running
  // make sure that aThread has already run before continuing some computation

  val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start()
  // different runs produce different results

  // executors
  val pool = Executors.newFixedThreadPool(10)
  pool.execute(() => println("something in the thread pool"))
  // the Runnable will be executed by one of the 10 threads managed by the thread pool

  pool.execute(() => {
    Thread.sleep(1000)
    println("done after 1 second")
  })

  pool.execute(() => {
    Thread.sleep(1000)
    println("almost done")
    Thread.sleep(1000)
    println("done after 2 second")
  })
  // the 2 executes above should run concurrently and take only 2 seconds total (not 3)

  pool.shutdown()
//  pool.execute(() => println("should not appear")) // RejectedExecutionException in the calling (main) thread

//  pool.shutdownNow() // interrupts any sleeping threads and causes them to throw exceptions
  println(pool.isShutdown) // returns true even if the actions submitted to the pool are still running (haven't finished)
  // shut down means the pool doesn't accept any MORE actions
}
