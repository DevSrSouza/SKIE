package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`disabled`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Disabled
sealed interface A

class A1 : A
class A2 : A