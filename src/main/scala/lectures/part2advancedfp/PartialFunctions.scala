package lectures.part2advancedfp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int

  // What if we want to restrict the input domain?

  // this implementation is clunky
  val aFussyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 56
    else if (x == 5) 999
    else throw new FunctionNotApplicableException()

  class FunctionNotApplicableException extends RuntimeException

  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 99
    // and any other value causes a MatchError
  }
  // this function is {1, 2, 5} => Int - a partial function

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 99
  } // partial function value - equivalent to aNicerFussyFunction

  println(aPartialFunction(2))


  /// Partial function utilities ///
  println(aPartialFunction.isDefinedAt(67)) // false

  // can be lifted to total functions returning Options
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(2)) // Some(56)
  println(lifted(9)) // None

  // use orElse to chain partial functions
  // - takes another partial function as argument that runs if the first one fails
  val pfChain = aPartialFunction.orElse[Int, Int]{
    case 45 => 67
  }
  println(pfChain(2))
  println(pfChain(45))

  // partial functions extend total functions
  // - can provide a PF literal
  val aTotalFunction: Int => Int = {
    case 1 => 99
  }

  // HOFs accept partial functions
  val aMappedList = List(1,2,3).map {
    case 1 => 42
    case 2 => 78
    case 3 => 1000
  } // MatchError if any list element isn't covered
  println(aMappedList)

  /*
    Note: PFs can only have one parameter type

    Exercises:
    1. Construct a PF instance by instantiating the trait yourself (anonymous class)
    2. Implement a small dumb chatbot as a PF:
    user submits input in the console, each line is passed to the chatbot, chatbot replies with a message
   */

  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def isDefinedAt(x: Int): Boolean =
      x == 1 || x == 2 || x == 5

    override def apply(x: Int): Int = x match {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
  }

  val chatbot: PartialFunction[String, String] = {
    case "hello" => "Hi, my name is HAL9000"
    case "goodbye" => "Bye!"
    case "call someone" => "Unable to find your phone"
  }
  // keeps prompting for input indefinitely
  scala.io.Source.stdin.getLines().map(chatbot).foreach(println)
//  scala.io.Source.stdin.getLines().foreach(line => println("Chatbot says: " + chatbot(line)))

}
