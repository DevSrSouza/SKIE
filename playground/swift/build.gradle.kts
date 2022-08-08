import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.configurationcache.extensions.capitalized

val architecture = when (
    val arch = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()
) {
    "arm64" -> "macosArm64"
    "x86_64" -> "macosX64"
    else -> error("Unsupported architecture: $arch")
}

val compileSwift = tasks.register<Exec>("compileSwift") {
    val frameworkDirectory = layout.projectDirectory.dir("../kotlin/build/bin/$architecture/releaseFramework")
    val mainFile = layout.buildDirectory.file("main").get().asFile

    group = "build"
    dependsOn(":playground:kotlin:linkReleaseFramework${architecture.capitalized()}")

    inputs.dir(frameworkDirectory)
    outputs.file(mainFile)

    doFirst {
        mkdir(layout.buildDirectory)
    }
    doFirst {
        println("---------------- Swift compilation ----------------")
    }
    commandLine(
        "swiftc",
        "main.swift",
        "-F",
        frameworkDirectory.asFile.absolutePath,
        "-o",
        mainFile.absolutePath,
    )
}

val clean = tasks.register<Delete>("clean") {
    group = "build"
    delete("build")
    dependsOn(":playground:kotlin:clean")
}

tasks.register<Exec>("runSwift") {
    group = "build"
    dependsOn(clean)
    dependsOn(compileSwift).mustRunAfter(clean)
    doFirst {
        println("---------------- Program output ----------------")
    }
    commandLine("build/main")
}
