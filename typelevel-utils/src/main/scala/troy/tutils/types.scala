package troy.tutils

sealed trait TOption[+T]
sealed trait TSome[+T] extends TOption[T]
sealed trait TNone extends TOption[Nothing]

sealed trait TBoolean
sealed trait TTrue extends TBoolean
sealed trait TFalse extends TBoolean

sealed trait TEither[+A, +B]
sealed trait TLeft[+A] extends TEither[A, Nothing]
sealed trait TRight[+B] extends TEither[Nothing, B]
