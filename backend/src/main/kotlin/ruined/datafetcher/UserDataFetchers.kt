package ruined.datafetcher

import graphql.schema.DataFetchingEnvironment
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.setAutoCommitAwait
import ruined.domain.Profile
import ruined.domain.User
import ruined.exception.RuinedException
import ruined.util.SQLBuilder
import java.util.concurrent.CompletableFuture

/**
 * Data fetcher for [User]s.
 */
class UserDataFetchers(val vertx: Vertx, val sqlClient: SQLClient) : SuspendingDataFetcher(vertx) {

    /**
     * Creates a single user.
     */
    fun createUser(env: DataFetchingEnvironment): CompletableFuture<User?> = coro {
        val input: Map<String, Any?> = env.getArgument("input")
        val email: String = input["email"] as String
        val password: String = input["password"] as String
        val username: String = input["username"] as String
        val conn: SQLConnection = sqlClient.getConnectionAwait()
        conn.use {
            conn.setAutoCommitAwait(false)
            val user: User? = SQLBuilder()
                .insert("\"user\"", "email", "password", "salt")
                .values(email, password, "1234")
                .returningAll()
                .querySingle(conn, ::User)
            user ?: throw RuinedException("User $username was not created.")
            val profile: Profile? = SQLBuilder()
                .insert("profile", "user_id", "username")
                .values(user.id, username)
                .returningAll()
                .querySingle(conn, ::Profile)
            profile ?: throw RuinedException("Profile $username was not created.")
            user
        }
    }
}