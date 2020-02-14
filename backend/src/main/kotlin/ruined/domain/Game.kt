package ruined.domain

import io.vertx.core.json.JsonObject

/**
 * Game domain entity.
 * @property id ID of the game.
 * @property name Name of the game.
 * @property platformId ID of the platform this game belongs to.
 */
data class Game(
    val id: Int,
    val name: String,
    val platformId: Int
) {
    constructor(obj: JsonObject) : this(
        obj.getInteger("id"),
        obj.getString("name"),
        obj.getInteger("platform_id")
    )
}