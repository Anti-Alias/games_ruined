package ruined

import io.vertx.core.http.HttpServer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.closeAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Main verticle class that runs the application.
 */
class RuinedVerticle : CoroutineVerticle() {

    private lateinit var httpServer: HttpServer

    /**
     * Invoked when this verticle is to be started.
     * Starts HTTP server and acquires connection to database.
     */
    override suspend fun start() {
        val futServer = async { createHttpServer() }
        this.httpServer = futServer.await()
    }

    /**
     * Invoked when this verticle is to be stopped.
     * Stops HTTP server and closes connections to database.
     */
    override suspend fun stop() {
        httpServer.closeAwait()
        logger.info("Stopped HTTP server!!!")
    }

    private suspend fun createHttpServer(): HttpServer {
        val server = vertx.createHttpServer()
        val router = createRouter()
        server.requestHandler(router)
        val port = 8080
        logger.info("Binding to port $port...")
        server.listenAwait(port, "localhost")
        logger.info("Started HTTP server")
        return server
    }

    private fun createRouter(): Router {
        val router = Router.router(vertx)
        router.get("/healthcheck").handler(::handleHealthCheck)
        router.post("/graphql").handler(wrapHandler(::handleGraphQL))
        return router
    }

    private fun handleHealthCheck(ctx: RoutingContext) {
        logger.info("Here")
        ctx.response().end("Server up and running")
    }

    private suspend fun handleGraphQL(ctx: RoutingContext) {
        ctx.response().end("Ok, it works!")
    }

    /**
     * Helper function to convert suspending handlers to regular handlers
     */
    private fun wrapHandler(handler: suspend (RoutingContext)->Unit): (RoutingContext)->Unit {
        return { ctx ->
            launch(vertx.dispatcher()) {
                handler(ctx)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RuinedVerticle::class.java)
    }
}