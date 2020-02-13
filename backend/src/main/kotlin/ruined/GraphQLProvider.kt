package ruined

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient
import ruined.datafetcher.GameDataFetchers
import ruined.datafetcher.PlatformDataFetchers

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
        return GraphQL.newGraphQL(graphqlSchema).build()                                        // Creates GraphQL instance from schema
    }

    /**
     * Wires up the graphql schema with the execution of it.
     */
    private fun createRuntimeWiring(): RuntimeWiring {

        // Creates objects with data fetcher methods
        val gameDataFetchers = GameDataFetchers(sqlClient)
        val platformDataFetchers = PlatformDataFetchers(sqlClient)

        // Builds implementation for data fetchers
        return newRuntimeWiring()
            .type("Query") {
                it.dataFetcher("games", gameDataFetchers::games)
                it.dataFetcher("platforms", platformDataFetchers::platforms)
            }
            .type("Game") {
                it.dataFetcher("platform", gameDataFetchers::platform)
            }
            .build()
    }
}