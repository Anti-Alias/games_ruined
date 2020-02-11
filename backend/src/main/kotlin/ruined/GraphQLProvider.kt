package ruined

import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser

class GraphQLProvider {

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

    private fun createSchema(): String = "type Query { hello: String }"

    /**
     * Wires up the graphql schema with the execution of it.
     */
    private fun createRuntimeWiring(): RuntimeWiring = newRuntimeWiring()
        .type("Query") {
            it.dataFetcher("hello", StaticDataFetcher("world"))
        }
        .build()

}