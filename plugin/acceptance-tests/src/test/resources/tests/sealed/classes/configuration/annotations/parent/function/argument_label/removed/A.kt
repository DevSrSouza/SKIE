package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`function`.`argument_label`.`removed`

import co.touchlab.swiftgen.api.SealedInterop

@SealedInterop.Function.ArgumentLabel("_")
sealed class A

class A1 : A()
class A2 : A()