package lectures.part2advancedfp

object CurriesPAFs extends App {

  // curried functions
  val superAdder: Int => Int => Int =
    x => { y => x + y }

  val add3 = superAdder(3) // Int => Int, function is y => 3 + y
  println(add3(5))
  println(superAdder(3)(5)) // curried function - receives multiple parameter lists

  // curried *method*
  def curriedAdder(x: Int)(y: Int): Int = x + y
  val add4: Int => Int = curriedAdder(4) // doesn't work with no type annotation!
  // compiler expects 2 parameter lists for curriedAdder unless something suggests otherwise

  // what add4 does behind the scenes is called LIFTING (aka ETA-EXPANSION)
  // methods are not instances of FunctionX (functions != methods due to JVM limitation),
  // so we can't use them in HOFs unless they're transformed into function values

  def inc(x: Int): Int = x + 1
  List(1,2,3).map(inc)
  // here, the compiler does ETA-expansion for us and turns the inc method into a function (i.e. a lambda x => inc(x)),
  // then uses that function value on map

  // Partial function applications
  val add5 = curriedAdder(5) _ // the _ tells the compiler to do ETA-expansion after applying the first parameter

  /* EXERCISE
  * Suppose there are 3 different ways of adding 2 numbers.
  * Define a function value add7: Int => Int = y => 7 + y
  * in as many ways as possible
  * */
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int): Int = x + y
  def curriedAddMethod(x: Int)(y: Int): Int = x + y

  val add7 = (y: Int) => simpleAddFunction(7, y) // can use any of the 3 ways above
  val add7_2 = simpleAddFunction.curried(7)

  val add7_3 = curriedAddMethod(7) _ // PAF
  val add7_4 = curriedAddMethod(7)(_) // alternative syntax

  val add7_5 = simpleAddMethod(7, _: Int) // alternative syntax for turning methods into function values, y => simpleAddMethod(7, y)
  val add7_6 = simpleAddFunction(7, _: Int)
  /// END: EXERCISE ///

  // underscores are powerful - can create function values by supplying them anywhere in the function application
  def concatenator(a: String, b: String, c: String): String = a + b + c
  val insertName = concatenator("Hello, I'm ", _, ", how are you?") // x: String => concatenator("Hello, I'm ", x, ", how are you?")
  println(insertName("Daniel"))

  // each underscore is a different parameter
  val fillInTheBlanks = concatenator("Hello, ", _: String, _: String) // (x, y) => concatenator("Hello, ", x, y)
  println(fillInTheBlanks("Daniel", " Scala is awesome"))

  /* EXERCISES
    1. Process a list of numbers and return their string representations with different formats
    - Use %4.2f, %8.6f and %14.12f with a curried formatter function (takes in a format and a number)
    - And apply the formatter to a list of numbers as a HOF

    2. What is the difference between...
    - functions vs methods
    - parameters: by-name vs 0-lambda
    Explore calling byName and byFunction with: int, method, parenMethod, lambda, PAF
    Which cases compile and which don't? Why?
  * */
  println("%8.6f".format(Math.PI))

  def curriedFormatter(formatter: String)(n: Double): String = formatter.format(n)
  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)
  val simpleFormat = curriedFormatter("%4.2f") _ // lift into function value
  val seriousFormat = curriedFormatter("%8.6f") _
  val preciseFormat = curriedFormatter("%14.12f") _
  println(numbers.map(curriedFormatter("%14.12f"))) // compiler does eta-expansion for us

  def byName(n: => Int): Int = n + 1 // argument is a value type
  def byFunction(f: () => Int): Int = f() + 1

  def method: Int = 42
  def parenMethod(): Int = 42

  byName(5) // ok
  byName(method) // ok - method evaluated to 42
  byName(parenMethod()) // ok with parentheses
  byName(parenMethod) // ok but beware - () assumed
//  byName(() => 42) // doesn't compile - function value not accepted
  byName((() => 42)()) // ok - call the lambda
//  byName(parenMethod _) // doesn't compile  - function value not accepted

//  byFunction(5) // doesn't compile - byFunction expects a lambda
//  byFunction(method) // doesn't compile - method is evaluated to its value here! compiler does NOT do eta-expansion for accessor methods
  byFunction(parenMethod) // ok - compiler does eta-expansion for proper methods with parentheses
  byFunction(() => 42) // ok
  byFunction(parenMethod _) // ok, but _ is unnecessary since compiler doesn't need to be explicitly told to do eta-expansion
}
