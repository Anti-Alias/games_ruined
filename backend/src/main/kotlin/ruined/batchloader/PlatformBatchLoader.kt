package ruined.batchloader

import io.vertx.ext.sql.SQLClient
import org.dataloader.BatchLoader
import ruined.domain.Platform
import ruined.util.SQLBuilder
import java.util.concurrent.CompletableFuture

/**
 * Gets all games with the following game IDs
 */
class PlatformBatchLoader(val sqlClient: SQLClient) : BatchLoader<Int, Platform?> {
    override fun load(platformIds: List<Int>): CompletableFuture<List<Platform?>> = SQLBuilder()
        .selectAll()
        .from("platform")
        .where().withinAll("id", platformIds)
        .queryFuture(sqlClient, ::Platform)
}