package lectures.part5typesystem

object SelfTypes extends App {

  // a way of requiring a type to be mixed in
  trait Instrumentalist {
    def play(): Unit
  }

  // SELF TYPE
  // self (can be named anything) is a marker at the language level
  // that forces anything implementing Singer to implement Instrumentalist as well
  trait Singer { self: Instrumentalist =>
    def sing(): Unit
  }

  // this is valid
  class LeadSinger extends Singer with Instrumentalist {
    override def play(): Unit = ???
    override def sing(): Unit = ???
  }

  // Illegal inheritance, self-type Vocalist does not conform to Instrumentalist
//  class Vocalist extends Singer {
//    override def sing(): Unit = ???
//  }

  val jamesHetfield = new Singer with Instrumentalist {
    override def sing(): Unit = ???
    override def play(): Unit = ???
  }

  class Guitarist extends Instrumentalist {
    override def play(): Unit = println("guitar solo")
  }

  // valid since Guitarist extends Instrumentalist
  val ericClapton = new Guitarist with Singer {
    override def sing(): Unit = ???
  }

  // vs inheritance: why not make Singer extend Instrumentalist?
  // we could, but the concepts themselves are not related
  class A
  class B extends A // B is an A

  // instead of saying S is a T, we say that S REQUIRES a T
  trait T
  trait S { self: T => }

  // CAKE PATTERN => "dependency injection"
  class Component {
    // API
  }
  class ComponentA extends Component
  class ComponentB extends Component
  class DependentComponent(val component: Component)
  // using many DI frameworks, the concrete components are injected by the framework at runtime

  // using the cake pattern, dependencies are checked at compile time
  trait ScalaComponent {
    // API
    def action(x: Int): String
  }
  trait ScalaDependentComponent { self: ScalaComponent =>
    // self-typing allows us to call action() from ScalaComponent as if it belongs to this component
    def dependentAction(x: Int): String = action(x) + "extra text"
  }
  trait ScalaApplication { self: ScalaDependentComponent => }

  // layer 1 - small components
  trait Picture extends ScalaComponent
  trait Stats extends ScalaComponent

  // layer 2 - compose components
  trait Profile extends ScalaDependentComponent with Picture
  trait Analytics extends ScalaDependentComponent with Stats

  // layer 3 - app
  trait AnalyticsApp extends ScalaApplication with Analytics
  // at each layer, you can choose which components from the previous layer you want to mix in


  // cyclical dependencies - seemingly possible with self types
//  class X extends Y
//  class Y extends X
  // not allowed with classes, but the below is ok
  trait X { self: Y => }
  trait Y { self: X => }
  // X and Y are separate concepts, but go hand in hand
}
