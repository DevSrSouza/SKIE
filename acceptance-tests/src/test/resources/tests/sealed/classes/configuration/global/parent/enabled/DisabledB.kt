package `tests`.`sealed`.`classes`.`configuration`.`global`.`parent`.`enabled`

import co.touchlab.skie.configuration.annotations.SealedInterop

@SealedInterop.Disabled
sealed class B

class B1 : B()
class B2 : B()
