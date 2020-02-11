package ruined

import graphql.ExecutionInput
import graphql.GraphQL
import graphql.GraphQLError
import io.vertx.config.ConfigRetriever
import io.vertx.core.http.HttpServer
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.http.closeAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.closeAwait
import io.vertx.kotlin.ext.sql.queryAwait
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ruined.exception.RuinedException
import java.net.ConnectException

/**
 * Main verticle class that runs the application.
 */
class RuinedVerticle : CoroutineVerticle() {

    private lateinit var httpServer: HttpServer
    private lateinit var sqlClient: AsyncSQLClient
    private lateinit var graphQL: GraphQL

    /**
     * Invoked when this verticle is to be started.
     * Starts HTTP server and acquires connection to database.
     */
    override suspend fun start() {
        val config: JsonObject = ConfigRetriever.create(vertx).getConfigAwait()
        val httpConfig: JsonObject = config.getJsonObject("http")
        val dbConfig: JsonObject = config.getJsonObject("db")
        val futServer = async { createHttpServer(httpConfig) }
        val futSQLClient = async { createSQLClient(dbConfig) }
        this.graphQL = GraphQLProvider().provide()
        this.httpServer = futServer.await()
        this.sqlClient = futSQLClient.await()
    }

    /**
     * Invoked when this verticle is to be stopped.
     * Stops HTTP server and closes connections to database.
     */
    override suspend fun stop() {
        val httpTask = async { httpServer.closeAwait() }
        val dbTask = async { sqlClient.closeAwait() }
        httpTask.await()
        dbTask.await()
    }

    private suspend fun createHttpServer(config: JsonObject): HttpServer {
        val server = vertx.createHttpServer()
        val router = createRouter()
        server.requestHandler(router)
        val host = config.getString("host")
        val port = config.getInteger("port")
        server.listenAwait(port, host)
        return server
    }

    suspend fun createSQLClient(config: JsonObject, retries: Int = 5): AsyncSQLClient {
        if(retries <= 0)
            throw RuinedException("retries must be > 0. Got $retries.")
        logger.info("Creating SQL client")
        repeat(retries) { attempt ->
            val client = PostgreSQLClient.createShared(vertx, config)
            try {
                val host: String = config.getString("host")
                val port: Int = config.getInteger("port")
                logger.info("Attempt $attempt: Testing database connectivity at $host:$port...")
                client.queryAwait("SELECT 1")
            } catch (e: ConnectException) {
                logger.info("Database connectivity failed")
                client.closeAwait()
                delay(1000)
            }
            logger.info("Created SQL client")
            return client
        }
        throw RuinedException("Failed to create SQL Client")
    }

    private fun createRouter(): Router {
        val router = Router.router(vertx)
        router.get("/healthcheck").handler { handleHealthCheck(it) }
        router.post("/graphql").handler(BodyHandler.create())
        router.post("/graphql").handler { handleGraphQL(it) }
        return router
    }

    fun handleHealthCheck(ctx: RoutingContext) {
        ctx.response().end("Server up and running!!!")
    }

    fun handleGraphQL(ctx: RoutingContext) {
        try {
            // Parses body
            val body: JsonObject = ctx.bodyAsJson

            // Builds execution input to pass into GraphQL instance
            val operationName: String? = body.getString("operationName", null)
            val variables: Map<String, Any>? = parseVariables(body)
            val inputBuilder = ExecutionInput.newExecutionInput().query(body.getString("query"))
            if(operationName != null)
                inputBuilder.operationName(operationName)
            if(variables != null)
                inputBuilder.variables(variables)

            // Executes input
            val fut = graphQL.executeAsync(inputBuilder.build())

            // Handles result asynchronously
            fut.thenAccept { result ->
                val data: Map<String, Any>? = result.getData()
                val errors: List<GraphQLError> = result.errors
                val response = JsonObject()
                if(data != null) response.put("data", data)
                if(errors.isNotEmpty()) response.put("errors", errors)
                ctx.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(response.toString())
            }
        }
        catch(e: DecodeException) {
            ctx.response()
                .setStatusCode(400)
                .end("Could not parse JSON body")
        }
        catch(t: Throwable) {
            ctx.fail(500)
            logger.warn("Uncaught Exception", t)
        }
    }

    private fun parseVariables(body: JsonObject): Map<String, Any>? {
        val variables: Any? = body.getValue("variables", null)
        return when(variables) {
            is String -> JsonObject(variables).map
            is JsonObject -> variables.map
            null -> null
            else -> throw RuinedException(
                "Type of 'variables' in GraphQL request was of type ${variables.javaClass.name}. Json object expected."
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RuinedVerticle::class.java)
    }

    /**
     * Helper function that adds the method 'suspendingHandler' to the Route class.
     * This is useful when one wishes to set a suspending handler to a Route.
     */
    private fun Route.suspendingHandler(cb: suspend (RoutingContext)->Unit) {
        this.handler { ctx ->
            launch(vertx.dispatcher()) {
                cb(ctx)
            }
        }
    }
}