package `tests`.`apinotes`.`function_renaming`.`unnecessary_renamings_are_fixed`.`class`.`extension_and_method`

class A {

    fun foo(i: Int) = i
}

fun A.foo(i: String) = i.toInt()