package ruined.domain

import io.vertx.core.json.JsonObject

/**
 * Platform domain entity.
 * @property id ID of the platform.
 * @property name Name of the platform.
 * @property simpleName Simple name of the platform.
 */
class Platform(val id: Int, val name: String, val simpleName: String) {
    constructor(obj: JsonObject) : this(
        obj.getInteger("id"),
        obj.getString("name"),
        obj.getString("simple_name")
    )
}