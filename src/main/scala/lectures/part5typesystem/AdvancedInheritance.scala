package lectures.part5typesystem

object AdvancedInheritance extends App {
  // 1. small convenience
  // suppose writing an API for an I/O library
  trait Writer[T] {
    def write(value: T): Unit
  }

  trait Closable { // closable resource
    def close(status: Int): Unit
  }

  trait GenericStream[T] {
    // some methods
    def foreach(f: T => Unit): Unit
  }

  // GenericStream[T] with Writer[T] with Closable is its own type
  // used when we don't know who exactly mixes in these traits
  def processStream[T](stream: GenericStream[T] with Writer[T] with Closable): Unit = {
    stream.foreach(println)
    stream.close(0)
  }

  // 2. how Scala resolves the diamond problem
  trait Animal {
    def name: String
  }
  trait Lion extends Animal {
    override def name: String = "lion"
  }
  trait Tiger extends Animal {
    override def name: String = "tiger"
  }
  // the below compiles even if Lion and Tiger have overrides:
  // it tries to fetch both implementations of name but overrides them
  // but what if we remove the override?
  class Mutant extends Lion with Tiger {
//    override def name: String = "alien"
  }

  val m = new Mutant
  println(m.name) // tiger

  /*
  Compiler does this:
  Mutant
  extends Animal with { override def name: String = "lion" }
  with Animal with { override def name: String = "tiger" }
  LAST OVERRIDE GETS PICKED
   */

  // 3. the super problem + type linearisation
  // recall: super accesses the member or method from a parent class or trait
  trait Cold {
    def print: Unit = println("cold")
  }

  trait Green extends Cold {
    override def print: Unit = {
      println("green")
      super.print
    }
  }

  trait Blue extends Cold {
    override def print: Unit = {
      println("blue")
      super.print
    }
  }

  class Red {
    def print: Unit = println("red")
  }

  class White extends Red with Green with Blue {
    override def print: Unit = {
      println("white")
      super.print // what does this print?
    }
  }

  val color = new White
  color.print // blue, green, cold on separate lines, but no red!
  // Cold = AnyRef with <Cold>
  // Green = AnyRef with <Cold> with <Green>
  // Blue = AnyRef with <Cold> with <Blue>
  // Red = AnyRef with <Red>
  // White = Red with Green with Blue with <White>
  //  = AnyRef with <Red>
  //    with (AnyRef with <Cold> with <Green>)
  //    with (AnyRef with <Cold> with <Blue>)
  //    with <White>
  //  = AnyRef with <Red> with <Cold> with <Green> with <Blue> with <White>
  //    (skip over duplicates)

  // This is a type linearisation for White
  // super calls the method in the type immediately to the left
}
