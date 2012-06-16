package services.dbTable

import services._

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
class DbColorerService(val dao : SimpleDbColorerDao) extends DbOps {

  def read() : JsObject = {
    DB.withConnection { implicit conn =>
      val tables = dao.read
      val jsonObjects = tables.map(_.toJson)
      val result = new JsObject(List("tables" -> 
        new JsArray(jsonObjects)))
      println("Result ready: " + jsonObjects.size + " tables sent.")
      return result
    }
  }
  
  def write(tableJson : JsValue) : Unit = {
    val tableAndCols = TableAndColumns.fromJson(tableJson)
    println("Updating table " + tableAndCols.tableName)
    DB.withConnection { implicit conn =>
      dao.updateTable(tableAndCols.table)
      val cols = tableAndCols.columns
      for(column <- cols) {
        dao.updateColumn(column)
      }
    }
  }

  /**
   * Reads a list of all the tags in the system. Use this for
   * creating the user interface.
   */
  def readTags() : JsObject = {

    DB.withConnection { implicit conn =>
      val tags = dao.readTags
      return new JsObject(List("tags" -> Tag.toJsonArray(tags)))
    }
  }
  
  /**
   * Reads a single tag from the database given the tag id.
   */
  def readTag(tagId : String) : JsObject = {
    DB.withConnection { implicit conn =>
      return dao.readTag(tagId).toJson
    }
  }
  
  /**
   * Write the tags of a single database table to the 
   * {
   *   tags: [{id: 'myTag', name: 'myTag'}, {id: 'myTag2', name: 'myTag2'}]
   * }
   */
  def writeTags(allTags : JsValue) : Unit = {
    val tagsArray = (allTags \ "tags").asInstanceOf[JsArray]
    val tags = Tag.fromJsonArray(tagsArray)
    DB.withConnection { implicit conn =>
      dao.updateTagsTo(tags)
    }
  }

  def createTable(tableJson : JsValue) : Unit = {
    val tableAndCols = TableAndColumns.fromJson(tableJson)
    println("Writing table " + tableAndCols.tableName)
    DB.withConnection { implicit conn =>
      dao.insertTableAndColumns(tableAndCols)
    }
  }
  
  def createTag(tag : Tag) {
    DB.withConnection { implicit conn =>
      dao.createTag(tag)
    }
  }
  
  def removeTag(tagId : String) {
    DB.withConnection { implicit conn =>
      dao.removeTag(tagId)
    }
  }
  
  def updateColumn(colJson : JsObject, conn : Connection) {
    val col = Column.fromJson(colJson)
    DB.withConnection { implicit conn =>
      dao.updateColumn(col)
    }
  }
  
  def recreateDatabase() {
    // These DDL methods contain the connection handling code too:
    dao.dropDatabaseTables()
    dao.createDatabaseTables()
  }
}
  
class TableAndColumnsBuilder(name : String) {
  val table : Table = Table(name, name, "")
  val cols = new ArrayBuffer[Column]
  
  def toTableAndColumns() : TableAndColumns = TableAndColumns(table, cols.toList)
}
