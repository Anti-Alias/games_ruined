package ruined.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import java.util.concurrent.CompletableFuture

/**
 * Class that assists in building SQL strings.
 * Can also be used for executing them.
 */
class SQLBuilder private constructor(
    private val parent: SQLBuilder?,
    private val builder: StringBuilder,
    private val params: JsonArray
) {
    private var spacer = false
    private var clause: String? = null

    /**
     * Simple no-arg constructor.
     * Instantiates root SQLBuilder.
     */
    constructor() : this(null, StringBuilder(), JsonArray())

    /**
     * Appends 'INSERT' statement.
     * @param tableName Name of table to insert into.
     * @param columnNames Value of the columns.
     * @return this object.
     */
    fun insert(tableName: String, vararg columnNames: String): SQLBuilder {
        append("INSERT INTO ").append(tableName)
        if(columnNames.isNotEmpty()) {
            builder.append(" (")
            val iter = columnNames.iterator()
            while(iter.hasNext()) {
                val columnName = iter.next()
                builder.append(columnName)
                if(iter.hasNext())
                    builder.append(", ")
            }
            builder.append(')')
        }
        return this
    }

    /**
     * Appends 'VALUES' statement. To be invoked after [insert].
     * @param columnValues Values of columns.
     * @return this object.
     */
    fun values(vararg columnValues: Any?): SQLBuilder {
        append("VALUES (")
        val iter = columnValues.iterator()
        while(iter.hasNext()) {
            val value = iter.next()
            builder.append('?')
            if(iter.hasNext())
                builder.append(", ")
            params.add(value)
        }
        builder.append(')')
        return this
    }

    /**
     * Appends 'SELECT' statement.
     * @return this object.
     */
    fun select(vararg columnNames: String): SQLBuilder {
        append("SELECT ")
        val iter = columnNames.iterator()
        while(iter.hasNext()) {
            val columnName: String = iter.next()
            builder.append(columnName)
            if(iter.hasNext())
                builder.append(", ")
        }
        return this
    }

    /**
     * Appends 'SELECT *'.
     * @return this object.
     */
    fun selectAll(): SQLBuilder {
        append("SELECT *")
        return this
    }

    /**
     * Appends 'UPDATE' statement.
     * @param tableName Name of the table to update.
     * @return this object.
     */
    fun update(tableName: String): SQLBuilder {
        append("UPDATE ").append(tableName)
        return this
    }

    /**
     * Appends 'SET' statement.
     * @param columnName Name of the column to update.
     * @param value Value to set it to.
     * @return this object.
     */
    fun set(columnName: String, value: Any?): SQLBuilder {
        append("SET ").append(columnName).append(" = ?")
        params.add(value)
        return this
    }

    /**
     * Appends 'SET' statement.
     * @param values Values to set.
     * @return this object.
     */
    fun set(vararg values: Pair<String, Any?>): SQLBuilder {
        require(values.isNotEmpty()) { "Values must have at least 1 element. Got 0." }
        append("SET ")
        val iter = values.iterator()
        while(iter.hasNext()) {
            val (key, value) = iter.next()
            builder.append(key).append(" = ?")
            params.add(value)
            if(iter.hasNext())
                builder.append(", ")
        }
        return this
    }

    /**
     * Appends 'DELETE' statement.
     * @return this object.
     */
    fun delete(): SQLBuilder {
        append("DELETE")
        return this
    }

    /**
     * Appends 'USING' statement.
     * @param columnName name of the column to use.
     * @return this object.
     */
    fun using(columnName: String): SQLBuilder {
        append("USING(").append(columnName).append(")")
        return this
    }

    /**
     * Appends 'JOIN' statement.
     * @param tableName Name of the table to join on.
     * @return this object.
     */
    fun join(tableName: String): SQLBuilder {
        append("JOIN ").append(tableName)
        return this
    }

    /**
     * Appends 'INNER JOIN' statement.
     * Functionally equivalent to [join].
     * @param tableName Name of the table to join on.
     * @return this object.
     */
    fun innerJoin(tableName: String): SQLBuilder {
        append("INNER JOIN ").append(tableName)
        return this
    }

    /**
     * Appends 'ON' statement.
     * @param column1 Name of the first column.
     * @param column2 Name of the second column.
     * @return this object.
     */
    fun on(column1: String, column2: String): SQLBuilder {
        this
            .append("ON ")
            .append(column1)
            .append(" = ")
            .append(column2)
        return this
    }

    /**
     * Appends 'SET' statement. To be invoked after [set]
     * if there are more columns that should be set in statement.
     */
    fun andSet(columnName: String, value: Any?): SQLBuilder {
        builder.append(", ").append(columnName).append(" = ?")
        params.add(value)
        return this
    }

    /**
     * Appends 'FROM' statement.
     * @param tableName Name of the table.
     * @return this object.
     */
    fun from(tableName: String): SQLBuilder {
        append("FROM ").append(tableName)
        return this
    }

    /**
     * Appends 'WHERE'.
     * @return this object.
     */
    fun where(): SQLBuilder {
        if(clause == null)
            clause = "WHERE"
        return this
    }

    /**
     * Appends 'AND'.
     * @return this object.
     */
    fun and(): SQLBuilder {
        if(clause == null)
            clause = "AND"
        return this
    }

    /**
     * Appends 'OR'.
     * @return this object.
     */
    fun or(): SQLBuilder {
        if(clause == null)
            clause = "OR"
        return this
    }

    /**
     * Appends a comparison statement.
     * @param columnName Name of the column to compare.
     * @param operation Operation to use. IE: <, >, <=, >=, !=, <>
     * @param value Value to compare to.
     * @param ignoreNull If true, entire statement will not be appended if [value] is null.
     * @return this object.
     */
    fun cmp(columnName: String, operation: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder {
        if(!ignoreNull || value != null) {
            appendClause()
            append(columnName).append(' ').append(operation).append(" ?")
            params.add(value)
        }
        return this
    }

    /**
     * Appends 'IN' statement.
     * @param columnName Name of the column to check.
     * @param values Values to check. If null, ignores operation entirely.
     * @return this object.
     */
    fun withinAll(columnName: String, values: Collection<Any?>?): SQLBuilder {
        if(values != null) {
            appendClause()
            append(columnName).append(" IN (")
            val iter = values.iterator()
            while(iter.hasNext()) {
                builder.append('?')
                params.add(iter.next())
                if(iter.hasNext())
                    builder.append(", ")
            }
            builder.append(")")
        }
        return this
    }

    /**
     * Variation of [withinAll] that uses a vararg of values rather than
     */
    fun within(columnName: String, vararg values: Any?): SQLBuilder = withinAll(columnName, values.toList())

    /**
     * Appends the string 'IN' and nothing more.
     * Expecting follow up calls to [beginParens], ..., and [endParens].
     * Useful for subqueries.
     * @return this object.
     */
    fun within(columnName: String): SQLBuilder {
        appendClause()
        append(columnName).append(" ").append("IN")
        return this
    }

    /**
     * Appends character '('. Useful for starting subqueries.
     * @return child SQLBuilder instance.
     */
    fun beginParens(): SQLBuilder {
        appendClause()
        append('(')
        return SQLBuilder(this, builder, params)
    }

    /**
     * DSL helper. Wraps subsequent calls within parenthesis.
     */
    inline fun parens(cb: SQLBuilder.()->Unit) {
        val child = beginParens()
        child.cb()
        child.endParens()
    }

    /**
     * Appends character ')'. Useful for terminating subqueries.
     * @return parent SQLBuilder instance.
     * @throws IllegalStateException when call to end() does not follow a call to begin().
     */
    fun endParens(): SQLBuilder {
        if(parent == null)
            throw IllegalStateException("Call to end() must follow a call to begin().")
        clause = null
        builder.append(')')
        return parent
    }

    /**
     * Equivalent of compare(columnName, "==", value, ignoreNull)
     * @return this object.
     */
    fun eq(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        cmp(columnName, "=", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, "<>", value, ignoreNull)
     * @return this object.
     */
    fun neq(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        cmp(columnName, "<>", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, "<", value, ignoreNull)
     * @return this object.
     */
    fun lt(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        cmp(columnName, "<", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, "<=>", value, ignoreNull)
     * @return this object.
     */
    fun lte(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        cmp(columnName, "<=", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, ">", value, ignoreNull)
     * @return this object.
     */
    fun gt(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        cmp(columnName, ">", value, ignoreNull)

    /**
     * Equivalent of compare(columnName, ">=", value, ignoreNull)
     * @return this object.
     */
    fun gte(columnName: String, value: Any?, ignoreNull: Boolean = true): SQLBuilder =
        cmp(columnName, ">=", value, ignoreNull)

    /**
     * Appends limit statement.
     * @return this object.
     */
    fun limit(limit: Int): SQLBuilder {
        append("LIMIT ?")
        params.add(limit)
        return this
    }

    /**
     * Appends 'RETURNING' statement.
     * @return this object.
     */
    fun returning(vararg columnNames: String): SQLBuilder {
        append("RETURNING ")
        val iter = columnNames.iterator()
        while(iter.hasNext()) {
            builder.append(iter.next())
            if(iter.hasNext())
                builder.append(", ")
        }
        return this
    }

    /**
     * Appends 'RETURNING' statement.
     * @return this object.
     */
    fun returningAll(): SQLBuilder {
        append("RETURNING *")
        return this
    }

    /**
     * Defensive copy of all parameters passed in.
     * @return this object.
     */
    fun params(): JsonArray = params.copy()

    /**
     * Makes a suspending query to a database using an SQL client.
     * @param sqlClient [SQLClient] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Deserialized list of results.
     */
    suspend inline fun <T> query(sqlClient: SQLClient, converter: (JsonObject)->T): List<T> {
        val query: String = toString()
        logQuery(query)
        val rs = sqlClient.queryWithParamsAwait(query, params())
        return rs.rows
            .map { it as JsonObject }
            .map { obj -> converter(obj) }
    }

    /**
     * Makes a suspending query to a database using an SQL client.
     * @param sqlConnection [SQLConnection] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Deserialized list of results.
     */
    suspend inline fun <T> query(sqlConnection: SQLConnection, converter: (JsonObject)->T): List<T> {
        val query: String = toString()
        logQuery(query)
        val rs = sqlConnection.queryWithParamsAwait(query, params())
        return rs.rows
            .map { it as JsonObject }
            .map { obj -> converter(obj) }
    }

    /**
     * Makes a suspending query to a database using an SQL client.
     * @param sqlClient [SQLClient] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Deserialized optional result.
     */
    suspend inline fun <T> querySingle(sqlClient: SQLClient, converter: (JsonObject)->T): T? = this
        .query(sqlClient, converter)
        .firstOrNull()

    /**
     * Makes a suspending query to a database using an SQL client.
     * @param sqlConnection [SQLConnection] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Deserialized optional result.
     */
    suspend inline fun <T> querySingle(sqlConnection: SQLConnection, converter: (JsonObject)->T): T? = this
        .query(sqlConnection, converter)
        .firstOrNull()

    /**
     * Makes a query to a database using an SQL client.
     * @param sqlClient [SQLClient] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Future deserialized list of results.
     */
    fun <T> queryFuture(sqlClient: SQLClient, converter: (JsonObject)->T): CompletableFuture<List<T>> {
        val query: String = toString()
        logQuery(query)
        val fut = CompletableFuture<List<T>>()
        sqlClient.queryWithParams(query, params()) { result ->
            if(result.succeeded()) {
                val rs = result.result()
                val value: List<T> = rs.rows
                    .map { it as JsonObject }
                    .map { obj -> converter(obj) }
                fut.complete(value)
            }
            else {
                fut.completeExceptionally(result.cause())
            }
        }
        return fut
    }

    /**
     * Makes a query to a database using an SQL client.
     * @param sqlConnection [SQLConnection] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Future deserialized list of results.
     */
    fun <T> queryFuture(sqlConnection: SQLConnection, converter: (JsonObject)->T): CompletableFuture<List<T>> {
        val query: String = toString()
        logQuery(query)
        val fut = CompletableFuture<List<T>>()
        sqlConnection.queryWithParams(query, params()) { result ->
            if(result.succeeded()) {
                val rs = result.result()
                val value: List<T> = rs.rows
                    .map { it as JsonObject }
                    .map { obj -> converter(obj) }
                fut.complete(value)
            }
            else {
                fut.completeExceptionally(result.cause())
            }
        }
        return fut
    }

    /**
     * Makes a query to a database using an SQL client.
     * @param sqlConnection [SQLConnection] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Future of first result, or null if not found.
     */
    fun <T> querySingleFuture(sqlConnection: SQLConnection, converter: (JsonObject)->T): CompletableFuture<T?> = this
        .queryFuture(sqlConnection, converter)
        .thenApply { it.firstOrNull() }

    /**
     * Makes a query to a database using an SQL client.
     * @param sqlClient [SQLClient] to use.
     * @param converter Callback function to convert the json objects to domain objects.
     * @return Future of first result, or null if not found.
     */
    fun <T> querySingleFuture(sqlClient: SQLClient, converter: (JsonObject)->T): CompletableFuture<T?> = this
        .queryFuture(sqlClient, converter)
        .thenApply { it.firstOrNull() }

    private fun append(str: String): StringBuilder {
        if(!spacer)
            spacer = true
        else
            builder.append(' ')
        return builder.append(str)
    }

    private fun append(char: Char): StringBuilder {
        if(!spacer)
            spacer = true
        else
            builder.append(' ')
        return builder.append(char)
    }

    private fun appendClause() {
        val clause = clause
        if(clause != null) {
            append(clause)
            this.clause = null
        }
    }

    override fun toString(): String =
        if(builder.startsWith(' '))
            builder.substring(1)
        else
            builder.toString()

    fun logQuery(query: String) {
        logger.debug("Executing SQL: ''{0}''", query)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SQLBuilder::class.java)
    }
}