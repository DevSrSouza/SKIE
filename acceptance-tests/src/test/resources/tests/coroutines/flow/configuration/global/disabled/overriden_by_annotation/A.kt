package `tests`.`coroutines`.`flow`.`configuration`.`global`.`disabled`.`overriden_by_annotation`

import co.touchlab.skie.configuration.annotations.FlowInterop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@FlowInterop.Enabled
fun foo(): Flow<Int> = flowOf(1, 2, 3)