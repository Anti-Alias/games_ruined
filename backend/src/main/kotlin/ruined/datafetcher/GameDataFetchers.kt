package ruined.datafetcher

import graphql.schema.DataFetchingEnvironment
import io.vertx.ext.sql.SQLClient
import ruined.domain.Game
import ruined.domain.Platform
import ruined.util.SQLBuilder
import java.util.concurrent.CompletableFuture

/**
 * Data fetchers for [Game]s.
 */
class GameDataFetchers(private val sqlClient: SQLClient) {

    /**
     * Queries for all games.
     */
    fun games(env: DataFetchingEnvironment): CompletableFuture<List<Game>> {
        val id: String? = env.getArgument("id")
        val name: String? = env.getArgument("name")
        val platformId: String? = env.getArgument("platformId")
        return SQLBuilder()
            .selectAll()
            .from("game")
            .where().eq("id", id?.toInt())
            .and().eq("name", name)
            .and().eq("platform_id", platformId?.toInt())
            .queryFuture(sqlClient, ::Game)
    }

    /**
     * Queries for the platform of a game.
     */
    fun platform(env: DataFetchingEnvironment): CompletableFuture<Platform?> {
        val game: Game = env.getSource()
        return SQLBuilder()
            .selectAll()
            .from("platform")
            .where().eq("id", game.platformId)
            .limit(1)
            .querySingleFuture(sqlClient, ::Platform)
    }
}