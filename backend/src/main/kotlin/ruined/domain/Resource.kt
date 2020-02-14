package ruined.domain

import io.vertx.core.json.JsonObject

/**
 * Resource domain entity.
 * Represents a resource such as a texture or sound that belongs to a [User].
 * @property id ID of the resource.
 * @property userId ID of the user that owns the resource.
 * @property name name of the resource.
 * @property type type of the resource.
 */
data class Resource(
    val id: Int,
    val userId: Int,
    val name: String,
    val type: ResourceType
) {
    constructor(obj: JsonObject) : this(
        obj.getInteger("id"),
        obj.getInteger("user_id"),
        obj.getString("name"),
        ResourceType.valueOf(obj.getString("type"))
    )
}