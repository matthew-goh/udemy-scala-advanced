package lectures.part5typesystem

object Variance extends App {

  trait Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  // what is variance?
  // the problem of "inheritance" (type substitution) of generics
  class Cage[T]
  // should a Cage[Cat] inherit from Cage[Animal]?

  // yes - covariance
  class CCage[+T]
  val ccage: CCage[Animal] = new CCage[Cat] // this is allowed

  // no - invariance
  class ICage[T]
  // val icage: CCage[Animal] = new ICage[Cat] // doesn't compile

  // opposite - contravariance
  class XCage[-T]
  val xcage: XCage[Cat] = new XCage[Animal]

  class InvariantCage[T](val animal: T)

  // covariant and contravariant positions - compiler restrictions
  class CovariantCage[+T](val animal: T) // the generic type of vals are in a COVARIANT POSITION
  // in this position, the compiler accepts a field declared with a covariant type (T is covariant)

//  class ContravariantCage[-T](val animal: T)
  // doesn't compile: Contravariant type T occurs in covariant position in type T of value animal
  // if the compiler could pass this, then we could write:
  /*
    val catCage: ContravariantCage[Cat] = new ContravariantCage[Animal](new Crocodile)
   */

//  class CovariantVariableCage[+T](var animal: T)
  // doesn't compile: Covariant type T occurs in contravariant position in type T of value animal
//  class ContravariantVariableCage[-T](var animal: T)
  // doesn't compile: Contravariant type T occurs in covariant position in type T of value animal

  // the types of vars are in both CONTRAVARIANT POSITION and COVARIANT POSITION!
  /* CONTRAVARIANT POSITION problem:
  val ccage: CovariantVariableCage[Animal] = new CovariantVariableCage[Cat](new Cat)
  ccage.animal = new Crocodile
   */
  // the only acceptable type for a var is invariant
  class InvariantVariableCage[T](var animal: T)


//  trait AnotherCovariantCage[+T] {
//    def addAnimal(animal: T)
//    // Covariant type T occurs in contravariant position in type T of value animal
//    // METHOD ARGUMENTS are in CONTRAVARIANT POSITION
//  }
  /* Problem:
  val ccage: CCage[Animal] = new CCage[Dog]
  ccage.addAnimal(new Cat)
   */

  class AnotherContravariantCage[-T] {
    def addAnimal(animal: T) = true // ok
  }
  val acc: AnotherContravariantCage[Cat] = new AnotherContravariantCage[Animal]
//  acc.addAnimal(new Dog) // not allowed - must receive an argument of type Cat or below
  acc.addAnimal(new Cat)
  class Kitty extends Cat
  acc.addAnimal(new Kitty)

  // to solve this problem with covariant collections:
  class MyList[+A] {
    def add[B >: A](elem: B): MyList[B] = new MyList[B] // widening the type
  }

  val emptyList = new MyList[Kitty]
  val animals = emptyList.add(new Kitty)
  val moreAnimals = animals.add(new Cat) // widen to MyList[Cat]
  val evenMoreAnimals = moreAnimals.add(new Dog) // widen to MyList[Animal]

  // METHOD RETURN TYPES are in COVARIANT POSITION
  class PetShop[-T] {
    // def get(isPuppy: Boolean): T
    // Contravariant type T occurs in covariant position in type T of value get
    // to solve it, return a subtype of the original type:
    def get[S <: T](isPuppy: Boolean, defaultAnimal: S): S = defaultAnimal
  }
  /* Problem:
  val catShop = new PetShop[Animal] {
    def get(isPuppy: Boolean): Animal = new Cat
  }
  val dogShop: PetShop[Dog] = catShop // a PetShop[Animal]
  dogShop.get(true) // returns a Cat!
   */
  val shop: PetShop[Dog] = new PetShop[Animal]
//  val evilCat = shop.get(true, new Cat) // error: inferred type arguments do not conform to method get's type parameter bounds
  class TerraNova extends Dog
  val bigFurry = shop.get(true, new TerraNova) // ok

  /*
  Big rule:
  - method arguments are in contravariant position
    -> if used in a class with a covariant type, must widen the type in the method
  - return types are in covariant position
    -> if used in a class with a contravariant type, must return a subtype

    If possible, when defining a class/trait with generic types,
    make types used as method arguments contravariant
    and types used as return types covariant
   */

  //////
  /*
  Exercises: Parking application to check for illegal parking
  1. Design an invariant, covariant and contravariant version of
  class Parking[T](things: List[T]) {
    def park(vehicle: T)
    def impound(vehicles: List[T])
    def checkVehicles(conditions: String): List[T]
  }
  (T can be vehicles)

  2. How would the above be different if we used someone else's API: IList[T] ?
  3. Say we want to make Parking a Monad
    - add a flatMap method
   */
  class Vehicle
  class Bike extends Vehicle
  class Car extends Vehicle
  class IList[T]

  class IParking[T](vehicles: List[T]) {
    def park(vehicle: T): IParking[T] = ??? // implementation irrelevant
    def impound(vehicles: List[T]): IParking[T] = ???
    def checkVehicles(conditions: String): List[T] = ??? // type of conditions is irrelevant

    def flatMap[S](f: T => IParking[S]): IParking[S] = ???
  }
  // no change if replacing with IList

  class CParking[+T](vehicles: List[T]) {
    def park[S >: T](vehicle: S): CParking[S] = ??? // like adding to a collection

    // List is covariant in that it follows the variance of T,
    // so the whole type List[T] is covariant, appearing in a contravariant position
    // counterintuitive to widen the type if removing vehicles, but in practice, S = T
    def impound[S >: T](vehicles: List[S]): CParking[T] = ???
    def checkVehicles(conditions: String): List[T] = ??? // ok - List[T] is covariant and appears in a covariant position

    def flatMap[S](f: T => CParking[S]): CParking[S] = ???
  }
  class CParking2[+T](vehicles: IList[T]) {
    def park[S >: T](vehicle: S): CParking2[T] = ???
    // covariant type T occurs in invariant position: T is covariant, but IList is invariant
    def impound[S >: T](vehicles: IList[S]): CParking2[T] = ???
    def checkVehicles[S >: T](conditions: String): IList[S] = ???
  }

  class XParking[-T](vehicles: List[T]) {
    def park(vehicle: T): XParking[T] = ??? // ok - T is contravariant and occurs in a contravariant position
    // List is covariant in that it follows the variance of T,
    // so the whole type List[T] is contravariant, appearing in a contravariant position
    def impound(vehicles: List[T]): XParking[T] = ???
    def checkVehicles[S <: T](conditions: String): List[S] = ??? // List[T] is contravariant, appearing in a covariant position

    // Contravariant type T occurs in covariant position
    // method argument (T => XParking[S]) is in a contravariant position
    // the argument is actually Function1[T, XParking[S]] and Function1 is contravariant in T,
    // i.e. it's in an inverse variance relationship to T, which means T is in covariant position in this case
    def flatMap[R <: T, S](f: R => XParking[S]): XParking[S] = ???
  }
  class XParking2[-T](vehicles: IList[T]) {
    def park(vehicle: T): XParking2[T] = ???
    // contravariant type T occurs in invariant position: T is contravariant, but IList is invariant
    def impound[S <: T](vehicles: IList[S]): XParking2[S] = ???
    def checkVehicles[S <: T](conditions: String): IList[S] = ???
  }

  /*
  Rule of thumb:
  - use a covariant type for a COLLECTION OF THINGS
  - use a contravariant type for a GROUP OF ACTIONS

  For Parking, it is a GROUP OF ACTIONS on a single list
   */
}
