package ruined.datafetcher

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

/**
 * Class to be extended.
 * Allows for easy access to helper methods that translate coroutine code to CompletableFuture<T> code.
 * @property vertx Vertx instance used for powering coroutines.
 */
open class SuspendingDataFetcher(private val vertx: Vertx) {

    /**
     * Converts a suspending callback to one that returns a [CompletableFuture] instance.
     */
    fun <T> coro(cb: suspend ()->T): CompletableFuture<T> {
        val fut = CompletableFuture<T>()
        GlobalScope.launch(vertx.dispatcher()) {
            try {
                val result: T = cb()
                fut.complete(result)
            }
            catch(t: Throwable) {
                fut.completeExceptionally(t)
            }
        }
        return fut
    }
}