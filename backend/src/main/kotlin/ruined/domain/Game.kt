package ruined.domain

import io.vertx.core.json.JsonObject

/**
 * Game domain entity.
 * @property id ID of the game.
 * @property name Name of the game.
 */
data class Game(
    val id: Int,
    val name: String
) {
    constructor(obj: JsonObject) : this(
        obj.getInteger("id"),
        obj.getString("name")
    )
}