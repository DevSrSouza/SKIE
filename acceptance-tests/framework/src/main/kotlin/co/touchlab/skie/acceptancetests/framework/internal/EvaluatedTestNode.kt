package co.touchlab.skie.acceptancetests.framework.internal

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TestResult
import java.nio.file.Path

internal sealed interface EvaluatedTestNode {

    val name: String

    data class Test(
        override val name: String,
        val fullName: String,
        val path: Path,
        val resultPath: Path,
        val expectedResult: ExpectedTestResult,
        val actualResult: TestResult,
    ) : EvaluatedTestNode

    data class SkippedTest(
        override val name: String,
    ) : EvaluatedTestNode

    data class Container(
        override val name: String,
        val children: List<EvaluatedTestNode>,
    ) : EvaluatedTestNode
}