# SwiftGen

IMPORTANT: SwiftGen and other repositories from the SwiftKt project are not publicly released.

SwiftGen is a part of the SwiftKt project.
It is a compiler plugin that generates Swift code wrappers for Objective-C headers provided by the Kotlin compiler.
The goal is to improve interop between Kotlin and Swift.

The plugin internally depends on SwiftPack and SwiftLink (both from the SwiftKt project).
The SwiftKt project is under heavy development and is far from being stable.
Certainly do not use it in any production project.

SwiftGen currently supports the following features:

- Sealed class/interfaces

These features are under active development - expect bugs:

- Exhaustive enums

The discussion about any part of the SwiftKt project happens in the `swiftkt` Slack channel.

## Installation

As the first step make sure that your project uses exactly the same Kotlin compiler version as this plugin (1.7.10).
(Kotlin compiler plugins are in general not stable and break with almost every release.)

At this point make sure that your project compiles before you do anything else (especially if you had to change the Kotlin compiler version).

The SwiftKt project is deployed in a private Touchlab's Maven repository.
To access plugins from that repository, you need to add the following code in the `settings.gradle.kts` file:

```kotlin
pluginManagement {
    repositories {
        maven("https://api.touchlab.dev/public")
    }
}
```

If you want to add a dependency to some SwiftKt artifact (for example to use annotation configuration), then register the repository in the given `build.gradle.kts`:

```kotlin
repositories {
    maven("https://api.touchlab.dev/public")
}
```

To enable SwiftGen, add the plugin in `build.gradle.kts` of the module that builds the native framework:

```kotlin
plugins {
    id("co.touchlab.swiftgen") version "XXX"
}
```

Do not add the plugin to other modules.

You can find the most recent version of the plugin by looking at the tags in this repository.
(The Maven repository does not have a browser at the time of writing.)

The Swift/Xcode side does not require any configuration changes.

## Supported features

### Sealed classes/interfaces

This feature allows to exhaustively switch on sealed Kotlin hierarchies from Swift.
For example, consider the following Kotlin code:

```kotlin
sealed interface A

class A1(val i: Int) : A
class A2(val k: Int) : A
```

In Kotlin you can write this:

```kotlin
when (a) {
    is A1 -> a.i
    is A2 -> a.k
}
```

In the above example, the compiler ensures that the `when` expression lists all possible cases (i.e. that it is exhaustive).
Not only that, but the compiler also smart-casts the `a` expression to the correct type.
The smart-cast allows the developer to access the inner properties of each type without an additional cast.

To support this feature in Swift, we generate code that can be reduced to this:

```swift
enum Enum {
    case A1(A1)
    case A2(A2)
}

func exhaustively(_ self: A) -> Enum {
    if let v = self as? A1 {
        return Enum.A1(v)
    } else if let v = self as? A2 {
        return Enum.A2(v)
    } else {
        fatalError("Unknown subtype. This error should not happen under normal circumstances since A is sealed.")
    }
}
```

The interop utilizes the fact that Swift supports exhaustive switch if used with Swift enums.
Therefore, we use the `exhaustively` function to wrap the Kotlin object in a Swift enum.
To simulate the smart-casting we use an enum with associated value.
Thanks to the above code you can write this:

```swift
switch exhaustively(a) {
case .A1(let a):
    return a.i
case .A2(let a):
    return a.k
}
```

If you do not need the smart-casting, you can write just this:

```swift
switch exhaustively(a) {
case .A1(_):
    print(a)
case .A2(_):
    fatalError()
}
```

### Exhaustive enums

SwiftGen can generate a bridging header for Kotlin enums that maps them to Swift enum.
As a result the Kotlin enums looks like Swift enums, whereas without the bridging they would behave like Obj-c classes.
This feature is under development so not everything works properly.
Namely, we are currently aware of a missing support for suspend functions (they will not be visible from Swift).

## Configuration

SwiftGen plugin makes some opinionated choices that might not work for every use case.
To solve this issue, the plugin provides a way to change some of its default behavior.
There are two different ways to change the configuration:

- locally - using Kotlin annotations
- globally - using Gradle extension provided by the SwiftGen Gradle plugin

### Annotations

The local configuration changes the behavior only for a single declaration.
This makes it for example suitable for suppressing the plugin if it does not work properly because of some bug.
The available annotations can be found in the `:core:api` module.

The following example changes the name of the `exhaustively` function generated for the sealed class interop to `something`:

```kotlin
// A.kt

@SealedInterop.FunctionName("something")
sealed class A {
    ...
}
```

To use these annotations you need to add a dependency to the module in `build.gradle.kts`:

```kotlin
dependencies {
    implementation("co.touchlab.swiftgen:api:XXX")
}
```

(Do not forget to register the internal repository as mentioned before.)

### Gradle

The global configuration can be applied to any class including those from 3rd party dependencies.
This ability makes it a good place for changing the plugin default behavior and to provide configuration for classes that you cannot modify.
The configuration is performed through a `swiftGen` Gradle extension:

```kotlin
// build.gradle.kts

swiftGen {
    configuration {
        group {
            ConfigurationKeys.SealedInterop.FunctionName("something")
        }
    }
}
```

The above example changes the name of the `exhaustively` function to `something` for all sealed classes/interfaces.
All the available configuration options are listed in the `ConfigurationKeys` class located in `:core:configuration` module.
Note that you can add multiple options to a single group.

The configuration can be applied only to some declarations:

```kotlin
// build.gradle.kts

swiftGen {
    configuration {
        group("co.touchlab.") {
            ConfigurationKeys.SealedInterop.FunctionName("something")
        }
    }
}
```

In the above example the configuration is applied only to declarations from the `co.touchlab` package.
The argument represents a prefix that is matched against the declaration's fully qualified name.
You can target all declarations (by passing an empty string), everything in a package or just a single class.

The `group` block can be called multiple times so that declarations can have different configurations.
For example:

```kotlin
// build.gradle.kts

swiftGen {
    configuration {
        group {
            ConfigurationKeys.SealedInterop.FunctionName("something")
        }
        group("co.touchlab.") {
            ConfigurationKeys.SealedInterop.FunctionName("somethingElse")
        }
    }
}
```

If multiple matching groups provide the same configuration key, then the last one added wins.

The local and global configuration can be used at the same time in which case the local configuration takes precedence.
This behavior can be overridden:

```kotlin
// build.gradle.kts

swiftGen {
    configuration {
        group(overridesAnnotations = true) {
            ConfigurationKeys.SealedInterop.FunctionName("something")
        }
    }
}
```

If the `overridesAnnotations` argument is set, then all keys in the group take precedence over the annotations.
The configuration can still be changed by another `group` block.
Annotations can still be used to configure behavior not specified in the overriding group.

Configuration can be loaded from a file:

```kotlin
// build.gradle.kts

swiftGen {
    configuration {
        from(File("config.json"))
    }
}
```

`group` and `from` can be freely mixed together and repeated multiple times.
The file format is identical to `build/swiftgen/config.json` which contains a JSON encoding all applied configuration.
