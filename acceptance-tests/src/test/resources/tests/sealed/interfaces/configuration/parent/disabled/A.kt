package tests.sealed.interfaces.configuration.parent.disabled

import co.touchlab.swiftgen.api.SwiftSealed

@SwiftSealed.Disabled
sealed interface A

class A1 : A
class A2 : A