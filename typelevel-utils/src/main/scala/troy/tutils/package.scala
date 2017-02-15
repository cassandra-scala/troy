package troy

package object tutils {
  /**
   * Gives the illusion of having a type parameter on an HList
   * The only use of it, is documentation purpose.
   */
  type THList[T] = shapeless.HList

  // Shapeless imports
  type HNil = shapeless.HNil
  type ::[+H, +T <: shapeless.HList] = shapeless.::[H, T]
}