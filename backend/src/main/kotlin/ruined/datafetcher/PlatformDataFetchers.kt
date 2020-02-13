package ruined.datafetcher

import graphql.schema.DataFetchingEnvironment
import io.vertx.ext.sql.SQLClient
import ruined.domain.Platform
import ruined.util.SQLBuilder
import java.util.concurrent.CompletableFuture

/**
 * Data fetchers for [Platform]s.
 */
class PlatformDataFetchers(val sqlClient: SQLClient) {

    /**
     * Queries for all platforms.
     */
    fun platforms(env: DataFetchingEnvironment): CompletableFuture<List<Platform>> {
        val id: String? = env.getArgument("id")
        val name: String? = env.getArgument("name")
        val simpleName: String? = env.getArgument("simpleName")
        return SQLBuilder()
            .selectAll()
            .from("platform")
            .where().eq("id", id?.toInt())
            .and().eq("name", name)
            .and().eq("simple_name", simpleName)
            .queryFuture(sqlClient, ::Platform)
    }

}