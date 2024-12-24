package lectures.part3concurrency

object JVMConcurrencyProblems {
  // How threads on the JVM can cause bugs
  def runInParallel(): Unit = {
    var x = 0

    val thread1 = new Thread(() => {x = 1})

    val thread2 = new Thread(() => {x = 2})

    thread1.start()
    thread2.start()
    Thread.sleep(100)
    println(x) // race condition
    // sometimes prints 1
    // even though thread2 started last, thread1 could be last to finish, depending on OS scheduling
  }

  case class BankAccount(var amount: Int)

  def buy(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    // involves 3 steps: read old value, compute result, write new value
    // need to make them act as one (atomic)
    bankAccount.amount -= price
  }

  def buySafe(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    // synchronised methods don't allow multiple threads to execute the critical section at the same time
    bankAccount.synchronized {
      bankAccount.amount -= price // critical section
    }
  }
  /*
    Example race condition:
    thread1 (shoes)
      - reads amount 50000
      - compute result 50000 - 3000 = 47000
    thread2 (iPhone)
      - reads amount 50000
      - compute result 50000 - 4000 = 46000
    thread1 (shoes)
      - write amount 47000
    thread2 (iPhone)
      - write amount 46000
  */
  def demoBankingProblem(): Unit = {
    (1 to 10000).foreach { _ =>
      // suppose you make 2 purchases from different online stores using the same account
      val account = BankAccount(50000)
      val thread1 = new Thread(() => buy(account, "shoes", 3000))
      val thread2 = new Thread(() => buy(account, "iPhone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join() // make sure that the threads are finished

      // from 10000 cases, will have broken the bank a few times
      // 2 threads may concurrently try to overwrite each other's account.amount
      if(account.amount != 43000) println(s"AHA! Broken the bank: ${account.amount}")
    }
  }

  /*
    Exercises:
    1. Create "inception threads"
      thread 1
        thread2
          thread3
            ...
      each thread prints "hello from thread $i"
      Print all messages in REVERSE ORDER

    2. What is the max/min value of x in minMaxX()?
      max = 100 - each thread increments x by 1
      min = 1 - e.g. all threads read x = 0 at the same time, so all compute and write 0 + 1 = 1

    3. "sleep fallacy": what is the value of message in demoSleepFallacy()?
      usually "Scala is awesome" as its sleep is 1ms shorter
      but not guaranteed: on some processors/OSs, sleep() yields execution,
      i.e. put the thread on hold and schedule some other thread for execution

      main thread:
        message = "Scala sucks"
        awesomeThread.start()
        sleep(1001) - yields execution
      awesome thread:
        sleep(1000) - yields execution
      OS gives the CPU to some important thread, takes > 2s
      OS gives the CPU back to the main thread, sleep exhausted
      -> prints "Scala sucks"
      Then assign message = "Scala is awesome"
  */

  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread = {
    new Thread(() => {
      if (i < maxThreads) {
        val newThread = inceptionThreads(maxThreads, i+1)
        newThread.start()
        newThread.join()
      }
      println(s"hello from thread $i") // only runs after the next thread has finished
    })
  }

  def minMaxX(): Unit = {
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x += 1))
    threads.foreach(_.start())
  }

  def demoSleepFallacy(): Unit = {
    var message = ""
    val awesomeThread = new Thread(() => {
      Thread.sleep(1000)
      message = "Scala is awesome"
    })

    message = "Scala sucks"
    awesomeThread.start()
    Thread.sleep(1001)
    // solution: join the worker thread
    awesomeThread.join()
    println(message)
  }

  def main(args: Array[String]): Unit = {
    demoBankingProblem()

    inceptionThreads(50).start()
    demoSleepFallacy()
  }
}
