package services.dbTable
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.Application
import play.api.db.DB
import java.sql.ResultSet
import java.sql.Connection
import scala.collection.mutable.ArrayBuffer
import play.api.libs.json.Json
import play.api.libs.json.JsArray
import play.api.Play._
import org.h2.jdbc.JdbcSQLException

/**
 * Tables and columns have id attributes that are their unique ids.
 * The rest of DbColorer should use them only as that. This data
 * access class must also manipulate them and currently they are
 * created from table and column names.
 */
class DbDataReader {

  /**
   * Parse a String to a JsArray allowing for empty strings.
   */
  def parseTags(str : String) : JsArray = {
    if (str == null || str.length() == 0) return new JsArray(Nil)
    return Json.parse(str).asInstanceOf[JsArray]
  }
  
  def read() : JsObject = {
    
    DB.withConnection { conn =>
      val tables = new ArrayBuffer[Table]()
      def createAndAdd(name : String) : Table = {
        val t = new Table(name)
        tables.+=(t)
        return t
      }
      
      val resultSet = executeSql("select table_name, column_name, column_type, color_name, tags from tables order by table_name asc, column_name asc", conn)
      while (resultSet.next()) {
        val tableName = resultSet.getString(1)
        val columnName = resultSet.getString(2)
        val columnType = resultSet.getString(3)
        val colorName = resultSet.getString(4)
        val tags : JsArray = parseTags(resultSet.getString(5))
        
	    val table : Table = tables.find(_.name == tableName).getOrElse(createAndAdd(tableName))
	    val col = new JsObject(
	        List(
	          "id" -> Json.toJson(tableName + ":" + columnName),
	          "name" -> Json.toJson(columnName),
	          "type" -> Json.toJson(columnType),
	          "color" -> Json.toJson(colorName),
	          "tags" -> tags
	        )
	      )
	      table.cols.+=(col)
      }
      
      val result = new JsObject(List("tables" -> 
        new JsArray(tables.map(_.toJson()))))
      println("Result ready.")
      return result
    }
  }
  
  def createTable(table : JsValue) : Unit = {
    // Make sure that the database is created:
    createDatabaseTables();
    
    // Insert data from the data.txt file:
    val tableName = (table \ "name").as[String]
    println("Writing table " + tableName)
    DB.withConnection { conn =>
      val cols = (table \ "columns").asInstanceOf[JsArray].productIterator.next().asInstanceOf[Iterable[JsObject]]
      for(column <- cols) {
        val colName = (column \ "name").as[String]
        val colType = (column \ "type").as[String]
        val color = (column \ "color").as[String]
        val statement = conn.prepareStatement("INSERT INTO TABLES VALUES('" + tableName + "', '" + colName + "', '" + colType + "', '" + color + "', '')")
        statement.execute()
      }
    }
  }
  
  def createDatabaseTables() {
    createTable(
      "create table tables (table_name varchar(255), column_name varchar(255), column_type varchar(255), color_name varchar(255), tags varchar(2000))", 
      "Cannot create table tables, maybe it is already created? "
    )
  }
  
  /**
   * Try to run an SQL statement and report the given error message if the operation fails.
   * Use this to make sure that the database is created before inserting test data.
   */
  def createTable(createTableSql : String, logErrorMessage : String) {
    try {
      DB.withConnection { conn =>
        val statement = conn.prepareStatement(createTableSql)
        statement.execute()
      }
    } catch {
      case e: JdbcSQLException => {
        println(logErrorMessage)
        println(e.getMessage())
      }
	}
  }
  
  def write(table : JsValue) : Unit = {
    val tableName = (table \ "name").as[String]
    println("Updating table " + tableName)
    DB.withConnection { conn =>
      val cols = (table \ "columns").asInstanceOf[JsArray].productIterator.next().asInstanceOf[Iterable[JsObject]]
      for(column <- cols) {
        updateColumn(column, conn)
      }
    }
  }
  
  def updateColumn(col : JsObject, conn : Connection) {
    val colId = (col \ "id").as[String]
    val color = (col \ "color").as[String]
    val tagsArray = (col \ "tags").asInstanceOf[JsArray]
    val tags = Json.stringify(tagsArray)
    updateColumn(conn, colId, color, tags)
  }
  
  private def executeSql(sql : String, conn : Connection) : ResultSet = {
    val statement = conn.prepareStatement(sql)
    if (statement.execute()) {
      statement.getResultSet()
    } else {
      throw new RuntimeException("Select statement did not return a result set.")
    }
  }
  
  private def updateColumn(conn: java.sql.Connection, colId: String, color: String, tags: String): Unit = {
    println("Update col " + colId + " to color " + color)
    val statement = conn.prepareStatement("UPDATE TABLES SET color_name = ?, tags = ? WHERE TABLE_NAME = ? AND COLUMN_NAME = ?")
    statement.setString(1, color)
    statement.setString(2, tags)
    statement.setString(3, colId.split(":")(0))
    statement.setString(4, colId.split(":")(1))
    val result = statement.executeUpdate()
    print(result + " ")
  }
}
  
class Table(val name : String) {
  val cols = new ArrayBuffer[JsValue]
  
  def toJson() : JsValue = {
    new JsObject(List(
        "id" -> Json.toJson(name),
        "name" -> Json.toJson(name),
        "columns" -> new JsArray(cols)
      )
    )
  }
}