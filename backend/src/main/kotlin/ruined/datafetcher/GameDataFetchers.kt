package ruined.datafetcher

import graphql.schema.DataFetchingEnvironment
import io.vertx.ext.sql.SQLClient
import ruined.domain.Game
import ruined.util.SQLBuilder

/**
 * Object responsible for implementing data fetchers for [Game] entity objects.
 */
class GameDataFetchers(private val sqlClient: SQLClient) {

    /**
     * Queries for all games.
     */
    suspend fun games(ctx: DataFetchingEnvironment): List<Game> {
        val id: String? = ctx.getArgument("id")
        val name: String? = ctx.getArgument("name")
        val platformId: String? = ctx.getArgument("platformId")
        return SQLBuilder()
            .selectAll()
            .from("game")
            .where().eq("id", id?.toInt())
            .and().eq("name", name)
            .and().eq("platform_id", platformId?.toInt())
            .query(sqlClient) { Game(it) }
    }
}