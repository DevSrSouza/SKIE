package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`disabled`

import co.touchlab.skie.configuration.annotations.SealedInterop

@SealedInterop.Disabled
sealed class A

class A1 : A()
class A2 : A()
