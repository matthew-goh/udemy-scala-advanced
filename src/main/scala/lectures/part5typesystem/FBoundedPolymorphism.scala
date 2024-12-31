package lectures.part5typesystem

object FBoundedPolymorphism extends App {

  // design problem: in a class hierarchy, how to force a method in the supertype to accept a "current type"
//  trait Animal {
//    def breed: List[Animal]
//  }
//
//  class Cat extends Animal {
//    override def breed: List[Animal] = ??? // want this to be List[Cat]
//  }
//  class Dog extends Animal {
//    override def breed: List[Animal] = ??? // want this to be List[Dog]
//  }

  // Option 1: naive
  // modify the return type for each specific class - valid since List is covariant
  // but we can easily make mistakes
//  trait Animal {
//    def breed: List[Animal]
//  }
//
//  class Cat extends Animal {
//    override def breed: List[Cat] = ???
//  }
//  class Dog extends Animal {
//    override def breed: List[Dog] = ???
//  }

  // Option 2: F-bounded polymorphism
  // Animal appears in its own type signature
//  trait Animal[A <: Animal[A]] { // recursive type: F-bounded polymorphism
//    def breed: List[Animal[A]]
//  }
//
//  class Cat extends Animal[Cat] {
//    override def breed: List[Animal[Cat]] = ???
//  }
//  class Dog extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ???
//  }
//
//  trait Entity[E <: Entity[E]] // often present in ORMs (database APIs)
//  class Person extends Comparable[Person] { // another example of FBP
//    override def compareTo(o: Person): Int = ???
//  }

  // but we can still make mistakes
  // how to enforce that the class being defined and the type it's annotated with are the same?
//  class Crocodile extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ???
//  }


  // Option 3: FBP + self types
//  trait Animal[A <: Animal[A]] { self: A => // anything implementing Animal[A] must also be an A
//    def breed: List[Animal[A]]
//  }
//
//  class Cat extends Animal[Cat] {
//    override def breed: List[Animal[Cat]] = ???
//  }
//  class Dog extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ???
//  }
  // Illegal inheritance, self-type Crocodile does not conform to A
//    class Crocodile extends Animal[Dog] {
//      override def breed: List[Animal[Dog]] = ???
//    }

  // BUT if we bring the hierarchy down one level
  // there is a fundamental limitation and FBP stops being effective
//  trait Fish extends Animal[Fish]
//  class Shark extends Fish {
//    // the return type is List[Animal[Fish]]!
//    override def breed: List[Animal[Fish]] = List(new Cod)
//  }
//  class Cod extends Fish {
//    override def breed: List[Animal[Fish]] = ???
//  }

  // How can we force the correct return type in a different way?
  // Option 4: use type classes!
//  trait Animal
//  trait CanBreed[A] {
//    def breed(a: A): List[A]
//  }
//
//  class Dog extends Animal
//  object Dog {
//    // type class instance
//    implicit object DogsCanBreed extends CanBreed[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//  }
//
//  // implicit conversion to call breed on an instance of Dog
//  implicit class CanBreedOps[A](animal: A) {
//    def breed(implicit canBreed: CanBreed[A]): List[A] = canBreed.breed(animal)
//  }
//
//  val dog = new Dog
//  dog.breed
//  /*
//  new CanBreedOps[Dog](dog).breed(Dog.DogsCanBreed)
//   */
//
//  // what if we make mistakes?
//  class Cat extends Animal
//  object Cat {
//    implicit object CatsCanBreed extends CanBreed[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//  }
//  val cat = new Cat
//  cat.breed // doesn't compile - no implicits found for CanBreed[Cat]

  // small criticism - we split the API between traits Animal and CanBreed
  // what if we make Animal the type class itself?
  // Option 5: pure type classes
  trait Animal[A] {
    def breed(a: A): List[A]
  }

  class Dog
  object Dog {
    implicit object DogAnimal extends Animal[Dog] {
      override def breed(a: Dog): List[Dog] = List()
    }
  }

  class Cat
  object Cat {
    implicit object CatAnimal extends Animal[Dog] {
      override def breed(a: Dog): List[Dog] = List()
    }
  }

  implicit class AnimalOps[A](animal: A) {
    def breed(implicit animalTypeClassInstance: Animal[A]): List[A] = animalTypeClassInstance.breed(animal)
  }

  val dog = new Dog
  dog.breed
  val cat = new Cat
//  cat.breed // doesn't compile - no implicits found for Animal[Cat]
}
