package `tests`.`sealed`.`classes`.`collision`.`nested_and_outer`.`child`.`is_solved_by_renaming`

import co.touchlab.skie.configuration.annotations.SealedInterop

sealed class A {

    @SealedInterop.Case.Name("X")
    class A1 : A()
}

class A1 : A()
