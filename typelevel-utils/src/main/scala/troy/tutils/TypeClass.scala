package troy
package tutils

trait TypeClass {
  type Out
}

object TypeClass {
  type Aux[O] = TypeClass { type Out = O }
}
