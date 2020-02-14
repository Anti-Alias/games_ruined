package ruined.domain

import io.vertx.core.json.JsonObject

/**
 * Profile domain entity.
 * @property id ID of the profile.
 * @property userId ID of the user this profile belongs to.
 * @property username Name of the user.
 * @property avatarURL Optional url to the avatar image of the profile.
 */
data class Profile(
    val id: Int,
    val userId: Int,
    val username: String,
    val avatarURL: String?
) {
    constructor(obj: JsonObject) : this(
        obj.getInteger("id"),
        obj.getInteger("user_id"),
        obj.getString("username"),
        obj.getString("avatar_url", null)
    )
}