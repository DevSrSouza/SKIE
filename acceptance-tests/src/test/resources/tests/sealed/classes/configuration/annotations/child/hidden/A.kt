package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`child`.`hidden`

import co.touchlab.skie.configuration.annotations.SealedInterop

sealed class A

@SealedInterop.Case.Hidden
class A1(val i: Int) : A()
class A2(val k: Int) : A()
