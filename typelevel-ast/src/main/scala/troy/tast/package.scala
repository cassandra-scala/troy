package troy

import troy.tutils.TOption

package object tast {
  type Identifier = String
  type MaybeKeyspaceName = TOption[Identifier]

  /**
   * Gives the illusion of having a type parameter on an HList
   * The only use of it, is documentation purpose.
   */
  type THList[T] = troy.tutils.THList[T]
}
