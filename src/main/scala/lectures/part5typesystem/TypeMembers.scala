package lectures.part5typesystem

object TypeMembers extends App {

  class Animal
  class Dog extends Animal
  class Cat extends Animal

  class AnimalCollection {
    type AnimalType // abstract type member - can use in val definitions and method signatures
    type BoundedAnimal <: Animal
    type SuperBoundedAnimal >: Dog <: Animal // multiple type bounds (supertype of Dog, subtype of Animal)
    type AnimalC = Cat // alias
  }

  val ac = new AnimalCollection
  val dog: ac.AnimalType = ???
  // but there is no constructor or information that allows the compiler to build AnimalType

//  val cat: ac.BoundedAnimal = new Cat

  val pup: ac.SuperBoundedAnimal = new Dog // only Dog works
  val cat: ac.AnimalC = new Cat // ok for aliases

  type CatAlias = Cat // can be used outside
  val anotherCat: CatAlias = new CatAlias

  // alternative to generics
  trait MyList {
    type T
    def add(elem: T): MyList
  }

  class NonEmptyList(value: Int) extends MyList {
    // need to override both type and methods (both abstract)
    override type T = Int
    def add(elem: Int): MyList = ???
  }

  // .type
  type CatsType = cat.type // ac.AnimalC
  val newCat: CatsType = cat
  // but can't instantiate new elements of this type, only do association
  // compiler can't tell if the type is constructable and if so, what arguments it receives
//  new CatsType

  /*
  Exercise: Enforce a type to be applicable to some types only
   */
  // locked - written by someone else
  trait MList {
    type A
    def head: A
    def tail: MList
  }

  // want MList only to be applicable to e.g. numbers
  // so CustomList should not compile but IntList should compile
  // Hint: use the Number type and type member constraints (bounds)
  trait ApplicableToNumbers {
    type A <: Number
  }
  // no error shown in IDE, but if the program is run, get error: incompatible type in overriding

//  class CustomList(hd: String, tl: CustomList) extends MList with ApplicableToNumbers {
//    type A = String
//    def head = hd
//    def tail = tl
//  }

  class IntList(hd: Int, tl: IntList) extends MList {
    type A = Int
    def head = hd
    def tail = tl
  }
}
