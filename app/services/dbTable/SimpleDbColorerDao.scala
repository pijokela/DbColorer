package services.dbTable

import services._
import java.sql.Connection
import scala.collection.mutable.ArrayBuffer

/**
 * This is a simple DAO implementation that can be used for testing
 * with H2.
 */
class SimpleDbColorerDao extends DbOps {
  def updateTagsTo(tags : List[Tag])(implicit conn : Connection) {
    executeDelete("DELETE FROM TAGS")
    for (tag <- tags) {
      val statement = conn.prepareStatement("INSERT INTO TAGS VALUES(?)")
      statement.setString(1, tag.id)
      statement.execute
    }
  }

  def read(implicit conn : Connection) : List[TableAndColumns] = {
    val tables = new ArrayBuffer[TableAndColumnsBuilder]()
    def createAndAdd(name : String) : TableAndColumnsBuilder = {
      println();
      println("Loading " + name + " from database.");
      val t = new TableAndColumnsBuilder(name)
      tables.+=(t)
      return t
    }

    val resultSet = executeSql("select table_name, column_name, column_type, color_name, tags from tables order by table_name asc, column_name asc")
    while (resultSet.next()) {
      print(".");
      val tableName = resultSet.getString(1)
      val columnName = resultSet.getString(2)
      val columnType = resultSet.getString(3)
      val colorName = resultSet.getString(4)
      val tags : List[Tag] = parseTags(resultSet.getString(5))

      val table : TableAndColumnsBuilder = tables.find(_.table.name == tableName).getOrElse(createAndAdd(tableName))
      table.cols += Column(tableName + ":" + columnName, columnName, columnType, colorName, tags)
    }
    return tables.map(_.toTableAndColumns).toList
  }

  def readTag(tagId : String)(implicit conn : Connection) : Tag = {
    // This is a cheat implementation, but for now, it'll do.
    return Tag(tagId, tagId)
  }
  
  /**
   * Parse a String to a JsArray allowing for empty strings.
   * Each column stores the tag ids in a single varchar.
   */
  private def parseTags(str : String)(implicit conn : Connection) : List[Tag] = {
    if (str == null) return Nil
    val jsTags = for(strTag <- str.split(" ") if !strTag.trim.isEmpty()) 
      yield readTag(strTag)
    return jsTags.toList
  }

  def updateColumn(col : Column)(implicit conn : Connection) : Unit = {
    val statement = conn.prepareStatement("UPDATE TABLES SET color_name = ?, tags = ? WHERE TABLE_NAME = ? AND COLUMN_NAME = ?")
    statement.setString(1, col.colorId)
    statement.setString(2, col.writeTagIdString())
    statement.setString(3, col.id.split("-")(0))
    statement.setString(4, col.id.split("-")(1))
    val result = statement.executeUpdate()
  }

  def readTags()(implicit conn : Connection) =
    executeSqlAndProcess("select tag_name from tags order by tag_name asc",
      rs => {
        val tagName = rs.getString("tag_name")
        Tag(tagName, tagName)
      }
    )
  
  def createTag(tag : Tag)(implicit conn : Connection) {
    val statement = conn.prepareStatement("INSERT INTO TAGS VALUES(?)")
    statement.setString(1, tag.id)
    statement.execute()
  }

  def removeTag(tagId : String)(implicit conn : Connection) {
    val statement = conn.prepareStatement("DELETE FROM TAGS WHERE TAG_NAME = ?")
    statement.setString(1, tagId)
    statement.execute()
  }

  def insertTableAndColumns(tableAndCols : TableAndColumns)(implicit conn : Connection) {
    val t = tableAndCols.table
    for(c <- tableAndCols.columns) {
      val statement = conn.prepareStatement(
          "INSERT INTO TABLES VALUES(?, ?, ?, ?, '')")
      statement.setString(1, t.name)
      statement.setString(2, c.name)
      statement.setString(3, c.colType)
      statement.setString(4, c.colorId)
      statement.execute()
    }
  }

  def dropDatabaseTables() {
    createTable(
      "drop table tables", 
      "Cannot drop table tables, maybe it is already dropped? "
    )
    createTable(
      "drop table tags", 
      "Cannot drop table tags, maybe it is already dropped? "
    )
  }
  
  def createDatabaseTables() {
    createTable(
      "create table tables (table_name varchar(255), column_name varchar(255), column_type varchar(255), color_name varchar(255), tags varchar(2000))", 
      "Cannot create table tables, maybe it is already created? "
    )
    createTable(
      "create table tags (tag_name varchar(255))", 
      "Cannot create table tags, maybe it is already created? "
    )
  }
  

}