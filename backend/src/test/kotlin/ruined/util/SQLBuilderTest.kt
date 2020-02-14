package ruined.util

import io.vertx.core.json.JsonArray
import io.vertx.kotlin.core.json.jsonArrayOf
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SQLBuilderTest {

    @Test
    fun testInsert() {
        val expectedSQL = """INSERT INTO table (col1, col2) VALUES (?, ?)"""
        val expectedParams = jsonArrayOf(1, 2)
        val builder = SQLBuilder()
        builder
            .insert("table", "col1", "col2")
            .values(1, 2)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testSelectAll() {
        val expectedSQL = """SELECT * FROM table WHERE col1 = ? AND col2 = ?"""
        val expectedParams = jsonArrayOf(5, 6)
        val builder = SQLBuilder()
        builder
            .selectAll()
            .from("table")
            .where().eq("col1", 5)
            .and().eq("col2", 6)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testSelectAliased() {
        val expectedSQL = """SELECT tbl.col1 AS c1, tbl.col2 AS c2 FROM table tbl WHERE c1 = ? AND c2 = ?"""
        val expectedParams = jsonArrayOf(5, 6)
        val builder = SQLBuilder()
        builder
            .select("tbl.col1 AS c1", "tbl.col2 AS c2")
            .from("table tbl")
            .where().eq("c1", 5)
            .and().eq("c2", 6)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testSelectColumns1() {
        val expectedSQL = """SELECT col1, col2 FROM table WHERE col1 = ? AND col2 <> ?"""
        val expectedParams = jsonArrayOf(5, 6)
        val builder = SQLBuilder()
            .select("col1", "col2")
            .from("table")
            .where().eq("col1", 5)
            .and().neq("col2", 6)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testSelectColumns2() {
        val expectedSQL = """SELECT col1, col2 FROM table WHERE col1 > ? AND col2 < ?"""
        val expectedParams = jsonArrayOf(5, 6)
        val builder = SQLBuilder()
        builder
            .select("col1", "col2")
            .from("table")
            .where().gt("col1", 5)
            .and().lt("col2", 6)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testSelectColumns3() {
        val expectedSQL = """SELECT col1, col2 FROM table WHERE col1 IN (?, ?, ?) OR col2 IN (?, ?, ?)"""
        val expectedParams = jsonArrayOf(1, 2, 3, "A", "B", "C")
        val builder = SQLBuilder()
        builder
            .select("col1", "col2")
            .from("table")
            .where().within("col1", 1, 2, 3)
            .or().within("col2", "A", "B", "C")
            .and().withinAll("col3", null)                 // Since we pass in null, col3 comparison will not be appended.
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testNestedSelect() {
        val expectedSQL = """SELECT * FROM table1 WHERE id IN (SELECT id FROM table2 WHERE name = ?) LIMIT ?"""
        val expectedParams = jsonArrayOf("Joe", 5)
        val builder = SQLBuilder()
        builder
            .selectAll()
            .from("table1")
            .where().within("id").beginParens()
            .select("id")
            .from("table2")
            .where().eq("name", "Joe")
            .endParens()
            .limit(5)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testJoin() {
        val expectedSQL = """SELECT * FROM table1 JOIN table2 ON table1.id = table2.table1_id"""
        val expectedParams = jsonArrayOf()
        val builder = SQLBuilder()
            .selectAll()
            .from("table1")
            .join("table2")
            .on("table1.id", "table2.table1_id")
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testInnerJoin() {
        val expectedSQL = """SELECT * FROM table1 INNER JOIN table2 ON table1.id = table2.table1_id"""
        val expectedParams = jsonArrayOf()
        val builder = SQLBuilder()
            .selectAll()
            .from("table1")
            .innerJoin("table2")
            .on("table1.id", "table2.table1_id")
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testUpdate1() {
        val expectedSQL = """UPDATE table1 SET col1 = ?, col2 = ? WHERE col3 <= ?"""
        val expectedParams = jsonArrayOf(5, 6, 7)
        val builder = SQLBuilder()
            .update("table1")
            .set("col1", 5)                 // set appends 'SET' "col1" = ?
            .andSet("col2", 6)              // andSet appends a comma followed by "col2" = ?
            .where()
            .lte("col3", 7)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testUpdate2() {
        val expectedSQL = """UPDATE table1 SET col1 = ?, col2 = ? WHERE col3 <= ?"""
        val expectedParams = jsonArrayOf(5, 6, 7)
        val builder = SQLBuilder()
            .update("table1")
            .set("col1" to 5, "col2" to 6)
            .where()
            .lte("col3", 7)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testUpdateFail() {
        assertFailsWith<IllegalArgumentException> {
            SQLBuilder()
                .update("table1")
                .set()                  // Set method should have at least 1 argument.
                .where()
                .lte("col3", 7)
        }
    }

    @Test
    fun testDelete() {
        val expectedSQL = """DELETE FROM table1 WHERE col1 IN (?, ?, ?)"""
        val expectedParams = jsonArrayOf("A", "B", "C")
        val builder = SQLBuilder()
            .delete()
            .from("table1")
            .where()
            .within("col1", "A", "B", "C")
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testReturning() {
        val expectedSQL = """INSERT INTO table (col1, col2) VALUES (?, ?) RETURNING col1, col2"""
        val expectedParams = jsonArrayOf(0, 1)
        val builder = SQLBuilder()
            .insert("table", "col1", "col2")
            .values(0, 1)
            .returning("col1", "col2")
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testReturningAll() {
        val expectedSQL = """INSERT INTO table (col1, col2) VALUES (?, ?) RETURNING *"""
        val expectedParams = jsonArrayOf(0, 1)
        val builder = SQLBuilder()
            .insert("table", "col1", "col2")
            .values(0, 1)
            .returningAll()
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testParenthesis() {
        val expectedSQL = """SELECT * FROM table1 WHERE (col1 = ? OR col2 = ?) AND col3 = ?"""
        val expectedParams = jsonArrayOf(1, 2, 3)
        val builder = SQLBuilder()
            .selectAll()
            .from("table1")
            .where()
            .beginParens()
                .eq("col1", 1)
                .or()
                .eq("col2", 2)
            .endParens()
            .and()
            .eq("col3", 3)
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }

    @Test
    fun testDSL() {
        val expectedSQL = """SELECT * FROM table1 WHERE (col1 = ? OR col2 = ?) AND col3 = ?"""
        val expectedParams = jsonArrayOf(1, 2, 3)
        val builder = with(SQLBuilder()) {
            selectAll()
            from("table1")
            where()
            parens {
                eq("col1", 1)
                or()
                eq("col2", 2)
            }
            and()
            eq("col3", 3)
        }
        val actualSQL: String = builder.toString()
        val actualParams: JsonArray = builder.params()
        assertEquals(expectedSQL, actualSQL)
        assertEquals(expectedParams, actualParams)
    }
}