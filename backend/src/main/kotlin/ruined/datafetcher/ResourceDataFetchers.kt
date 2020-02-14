package ruined.datafetcher

import graphql.schema.DataFetchingEnvironment
import io.vertx.ext.sql.SQLClient
import ruined.domain.Profile
import ruined.domain.Resource
import ruined.exception.RuinedException
import ruined.util.SQLBuilder
import java.util.concurrent.CompletableFuture

/**
 * Data fetcher for [Resource]s.
 */
class ResourceDataFetchers(val sqlClient: SQLClient) {

    /**
     * Fetches all resources that match a given criteria.
     */
    fun resources(env: DataFetchingEnvironment): CompletableFuture<List<Resource>> {
        val id: String? = env.getArgument("id")
        val name: String? = env.getArgument("name")
        val type: String? = env.getArgument("type")
        if(id == null && name == null) {
            if(type != null)
                throw RuinedException("Parameter 'type' cannot be the only specified.")
            else
                throw RuinedException("No parameters specified.")
        }
        return SQLBuilder()
            .selectAll()
            .from("resource")
            .where().eq("id", id?.toInt())
            .and().eq("name", name)
            .and().eq("type", type)
            .queryFuture(sqlClient, ::Resource)
    }

    /**
     * Fetches profile of a given [Resource].
     */
    fun profile(env: DataFetchingEnvironment): CompletableFuture<Profile?> {
        val resource: Resource = env.getSource()
        return SQLBuilder()
            .select("profile.id", "profile.user_id", "profile.username", "profile.avatar_url")
            .from("profile")
            .join("\"user\"")
            .on("\"user\".id", "profile.user_id")
            .where().eq("\"user\".id", resource.userId)
            .querySingleFuture(sqlClient, ::Profile)
    }

    fun createResource(env: DataFetchingEnvironment): CompletableFuture<Resource?> {
        val input: Map<String, Any> = env.getArgument("input")
        val name: String = input["name"] as String
        val type: String = input["type"] as String
        val userId: String = input["userId"] as String
        return SQLBuilder()
            .insert("resource", "user_id", "name", "type")
            .values(userId.toInt(), name, type)
            .returningAll()
            .querySingleFuture(sqlClient, ::Resource)
    }
}