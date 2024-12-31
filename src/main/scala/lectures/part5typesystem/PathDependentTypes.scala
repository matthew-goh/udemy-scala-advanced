package lectures.part5typesystem

object PathDependentTypes extends App {

  class Outer {
    // path-dependent types
    class Inner
    object InnerObject
    type InnerType

    def print(i: Inner): Unit = println(i)
    def printGeneral(i: Outer#Inner): Unit = println(i)
  }

  // classes can be defined inside methods
  // but types can only be declared as aliases
  def aMethod: Int = {
    class Helper
    type HelperType = String
    2
  }

  // per instance:
  // in order to reference an inner type, you need an outer INSTANCE
  // different instances mean different inner types
  val o = new Outer
  //  val inner = new Inner // cannot - only exists in the context of Outer
  //  val inner = new Outer.Inner // cannot
  val inner = new o.Inner

  val oo = new Outer
  //  val otherInner: oo.Inner = new o.Inner // oo.Inner and o.Inner are different types

  o.print(inner)
  // oo.print(inner) // cannot

  // all the inner types have a common supertype: Outer#Inner
  oo.printGeneral(inner) // ok

  /*
  Exercise:
  Database keyed by Int or String, but want to be able to expand to other key types
  Implement the method get so that the calls below do or don't compile as appropriate

  Hint: use (i) path-dependent types and (ii) abstract type members and/or type aliases
   */

  // add this trait to capture both ItemType and Key
  trait ItemLike {
    type Key
  }

  trait Item[K] extends ItemLike {
    type Key = K // enforce that ItemLike's Key type is equal to the type K that Item is parameterised with
  }
  trait IntItem extends Item[Int]
  trait StringItem extends Item[String]

  def get[ItemType <: ItemLike](key: ItemType#Key): ItemType = ???
  // when ItemType is specified, Key is known and the argument can be checked
  get[IntItem](42) // ok
  get[StringItem]("home") // ok
//  get[IntItem]("home") // not ok
}
