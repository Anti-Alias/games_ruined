package ruined.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.ext.sql.queryWithParamsAwait

class SQLBuilder {
    private val builder = StringBuilder()
    private val params = JsonArray()
    private var buffer: String? = null

    fun select(vararg columnNames: String): SQLBuilder {
        builder.append("SELECT ")
        val iter = columnNames.iterator()
        while(iter.hasNext()) {
            val columnName: String = iter.next()
            builder.append('"').append(columnName).append('"')
            if(iter.hasNext())
                builder.append(", ")
        }
        return this
    }

    fun from(tableName: String): SQLBuilder {
        builder.append(" FROM \"").append(tableName).append("\" ")
        return this
    }


    fun selectAll(): SQLBuilder {
        builder.append("SELECT * ")
        return this
    }

    fun where(): SQLBuilder {
        if(buffer == null)
            buffer = "WHERE "
        return this
    }

    fun and(): SQLBuilder {
        if(buffer == null)
            buffer = "AND "
        return this
    }

    fun or(): SQLBuilder {
        if(buffer == null)
            buffer = "OR "
        return this
    }

    /**
     * Compares a column to a value using the specified operation.
     * @param columnName Name of the comlumn to compare.
     * @param operation Operation to use. IE: <, >, <=, >=, !=, <>
     */
    fun compare(columnName: String, operation: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder {
        if(!ignoreNull || value != null) {
            flush()
            builder.append('"').append(columnName).append("\" ").append(operation).append(" ? ")
            params.add(value)
        }
        return this
    }
    /**
     * Checks if column name is withing the collection of values supplied.
     * @param columnName Name of the column to check.
     * @param values Values to check. If null, ignores operation entirely.
     */
    fun within(columnName: String, values: Collection<Any>?): SQLBuilder {
        if(values != null) {
            flush()
            builder.append('"').append(columnName).append("\" IN (")
            val iter = values.iterator()
            while(iter.hasNext()) {
                builder.append('?')
                params.add(iter.next())
                if(iter.hasNext())
                    builder.append(", ")
            }
            builder.append(") ")
        }
        return this
    }

    /**
     * Appends the string 'IN ', and nothing more. Expected to follow up call with being(), ..., end().
     * Useful for subqueries.
     */
    fun within(columnName: String): SQLBuilder {
        flush()
        builder.append('"').append(columnName).append("\" ").append("IN ")
        return this
    }

    /**
     * Appends character '('. Useful for starting subqueries.
     */
    fun begin(): SQLBuilder {
        builder.append('(')
        return this
    }

    /**
     * Appends character ')'. Useful for terminating subqueries.
     */
    fun end(): SQLBuilder {
        builder.append(')')
        return this
    }


    /**
     * Equivalent of compare(columnName, "==", value, ignoreNull)
     */
    fun eq(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        compare(columnName, "=", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, "<>", value, ignoreNull)
     */
    fun neq(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        compare(columnName, "<>", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, "<", value, ignoreNull)
     */
    fun lt(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        compare(columnName, "<", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, "<=>", value, ignoreNull)
     */
    fun lte(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        compare(columnName, "<=", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, ">", value, ignoreNull)
     */
    fun gt(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        compare(columnName, ">", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, ">=", value, ignoreNull)
     */
    fun gte(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        compare(columnName, ">=", value, ignoreNull)

    /**
     * Defensive copy of all parameters passed in.
     */
    fun params(): JsonArray = params.copy()

    /**
     * Makes a query to a database using an SQL client.
     */
    suspend inline fun <T> query(sqlClient: SQLClient, converter: (JsonObject)->T): List<T> {
        val query: String = toString()
        val rs = sqlClient.queryWithParamsAwait(query, params())
        return rs.rows
            .map { it as JsonObject }
            .map { obj -> converter(obj) }
    }

    private fun flush() {
        if(buffer != null)
            builder.append(buffer)
    }

    override fun toString(): String = builder.toString()
}