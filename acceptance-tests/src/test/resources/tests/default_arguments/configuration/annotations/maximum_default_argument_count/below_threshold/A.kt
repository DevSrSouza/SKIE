package `tests`.`default_arguments`.`configuration`.`annotations`.`maximum_default_argument_count`.`below_threshold`

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop

@DefaultArgumentInterop.MaximumDefaultArgumentCount(1)
fun foo(i: Int = 0, k: Int): Int = i + k
