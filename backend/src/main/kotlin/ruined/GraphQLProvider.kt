package ruined

import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeRuntimeWiring
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ruined.datafetcher.GameDataFetchers
import java.util.concurrent.CompletableFuture

/**
 * Provides GraphQL instance which handles GraphQL requests.
 * @param vertx Vertx application.
 * @param sqlClient SQL client used for fetching/mutating data.
 */
class GraphQLProvider(private val vertx: Vertx, private val sqlClient: SQLClient) {

    /**
     * Provides the GraphQL instance to be used in the application.
     */
    fun provide(): GraphQL {
        val schema = createSchema()                                                         // Schema as a string
        val registry = SchemaParser().parse(schema)                                         // Types from schema
        val runtimeWiring = createRuntimeWiring()                                           // Builds runtime wiring for execution of schema
        val graphqlSchema = SchemaGenerator().makeExecutableSchema(registry, runtimeWiring) // Builds final schema
        return GraphQL.newGraphQL(graphqlSchema).build()                                    // Creates GraphQL instance from schema
    }

    private fun createSchema(): String = javaClass.classLoader.getResource("graphql/schema.graphql")!!.readText()

    /**
     * Wires up the graphql schema with the execution of it.
     */
    private fun createRuntimeWiring(): RuntimeWiring {

        // Creates objects with data fetcher methods
        val gdf = GameDataFetchers(sqlClient)
        return newRuntimeWiring()
            .type("Query") {
                it.dataFetcher("hello", StaticDataFetcher("world"))
                it.suspendingDataFetcher("games") {env -> gdf.games(env) }
            }
            .build()
    }

    /**
     * Helper function for adding suspending lambdas as data fetchers.
     */
    fun <T> TypeRuntimeWiring.Builder.suspendingDataFetcher(
        fieldName: String,
        callback: suspend (DataFetchingEnvironment)-> T
    ): TypeRuntimeWiring.Builder = this.dataFetcher(fieldName) { environ ->
        val fut = CompletableFuture<T>()
        GlobalScope.launch(vertx.dispatcher()) {
            try {
                fut.complete(callback(environ))
            }
            catch(t: Throwable) {
                fut.completeExceptionally(t)
                logger.warn("Data fetching failed", t)
            }
        }
        fut
    }

    companion object {
        val logger = LoggerFactory.getLogger(GraphQLProvider::class.java)
    }
}