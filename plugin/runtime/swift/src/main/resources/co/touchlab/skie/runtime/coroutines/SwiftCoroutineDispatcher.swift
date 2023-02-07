import Foundation

@available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)
struct SwiftCoroutineDispatcher {

    static func dispatch<T>(
        coroutine: (Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler) -> Void
    ) async throws -> T {
        let cancellationHandler = Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_CancellationHandler()

        return try await withTaskCancellationHandler(operation: {
            try await dispatchCancellable(coroutine: coroutine, cancellationHandler: cancellationHandler)
        }, onCancel: {
            cancellationHandler.cancel()
        })
    }

    private static func dispatchCancellable<T>(
        coroutine: (Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler) -> Void,
        cancellationHandler: Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_CancellationHandler
    ) async throws -> T {
        var result: Swift.Result<T, Swift.Error>? = nil

        let dispatcher = createDispatcher(coroutine: coroutine, cancellationHandler: cancellationHandler) {
            result = $0
        }

        await executeWithoutCancellation(dispatcher: dispatcher)

        return try unwrap(result: result)
    }

    private static func createDispatcher<T>(
        coroutine: (Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler) -> Void,
        cancellationHandler: Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_CancellationHandler,
        onResult: @escaping (Swift.Result<T, Swift.Error>) -> Void
    ) -> AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable> {
        return AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable> { continuation in
            let dispatcherDelegate = AsyncStreamDispatcherDelegate(continuation: continuation)

            let suspendHandler = Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler(
                cancellationHandler: cancellationHandler,
                dispatcherDelegate: dispatcherDelegate,
                onResult: { suspendResult in
                    let result: Result<T, Swift.Error> = convertToResult(suspendResult: suspendResult)

                    onResult(result)

                    dispatcherDelegate.stop()
                }
            )

            coroutine(suspendHandler)
        }
    }

    private static func convertToResult<T>(
        suspendResult: Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendResult
    ) -> Swift.Result<T, Swift.Error> {
        if let suspendResult = suspendResult as? Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendResult_Success {
            if T.self == Swift.Void.self {
                return .success(Swift.Void() as! T)
            } else {
                return .success(suspendResult.value as! T)
            }
        } else if let suspendResult = suspendResult as? Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendResult_Error {
            return .failure(suspendResult.error)
        } else if suspendResult is Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_Skie_SuspendResult_Canceled {
            return .failure(_Concurrency.CancellationError())
        } else {
            fatalError("Unknown suspend result. This is most likely a bug in SKIE.")
        }
    }

    private static func executeWithoutCancellation(dispatcher: AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable>) async {
        await Task {
            for await block in dispatcher {
                block.run()
            }
        }.value
    }

    private static func unwrap<T>(result: Swift.Result<T, Swift.Error>?) throws -> T {
        if let result = result {
            switch result {
            case .success(let value):
                return value
            case .failure(let error):
                throw error
            }
        } else {
            fatalError("Suspend execution ended without result! This is most likely a bug in SKIE.")
        }
    }
}
