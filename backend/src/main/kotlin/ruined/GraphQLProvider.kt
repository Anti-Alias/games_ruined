package ruined

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException
import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import ruined.datafetcher.GameDataFetchers
import ruined.datafetcher.PlatformDataFetchers
import ruined.datafetcher.ResourceDataFetchers
import ruined.datafetcher.UserDataFetchers
import ruined.exception.RuinedError
import ruined.exception.RuinedException

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
        val schema = javaClass.classLoader.getResource("graphql/schema.graphql")!!.readText()   // Schema as a string
        val registry = SchemaParser().parse(schema)                                             // Types from schema
        val runtimeWiring = createRuntimeWiring()                                               // Builds runtime wiring for execution of schema
        val graphqlSchema = SchemaGenerator().makeExecutableSchema(registry, runtimeWiring)     // Builds final schema

        // Creates GraphQL instance from schema
        val exceptionHandler = createExceptionHandler()
        return GraphQL.newGraphQL(graphqlSchema)
            .queryExecutionStrategy(AsyncExecutionStrategy(exceptionHandler))
            .mutationExecutionStrategy(AsyncExecutionStrategy(exceptionHandler))
            .build()
    }

    /**
     * Wires up the graphql schema with the execution of it.
     */
    private fun createRuntimeWiring(): RuntimeWiring {

        // Creates objects with data fetcher methods
        val gameDataFetchers = GameDataFetchers(sqlClient)
        val platformDataFetchers = PlatformDataFetchers(sqlClient)
        val resourceDataFetchers = ResourceDataFetchers(sqlClient)
        val userDataFetchers = UserDataFetchers(vertx, sqlClient)

        // Builds implementation for data fetchers
        return newRuntimeWiring()
            .type("Query") {
                it.dataFetcher("games", gameDataFetchers::games)
                it.dataFetcher("platforms", platformDataFetchers::platforms)
                it.dataFetcher("resources", resourceDataFetchers::resources)
            }
            .type("Mutation") {
                it.dataFetcher("createUser", userDataFetchers::createUser)
                it.dataFetcher("createResource", resourceDataFetchers::createResource)
            }
            .type("Game") {
                it.dataFetcher("platform", gameDataFetchers::platform)
            }
            .type("Resource") {
                it.dataFetcher("profile", resourceDataFetchers::profile)
            }
            .build()
    }

    private fun createExceptionHandler(): DataFetcherExceptionHandler  = object : DataFetcherExceptionHandler {
        override fun onException(handlerParameters: DataFetcherExceptionHandlerParameters): DataFetcherExceptionHandlerResult {
            val exception = handlerParameters.exception
            val message: String? = when(exception) {
                is GenericDatabaseException -> "Please ensure that input data was unique"
                is RuinedException -> exception.message
                else -> "Unexpected exception occurred"
            }
            if(exception !is RuinedException)
                logger.warn("Exception while data fetching", exception)
            val path = handlerParameters.path.toList()
            val error = RuinedError(message, path)
            return DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build()
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(GraphQLProvider::class.java)
    }
}