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

  private def readTables(implicit conn : Connection) : List[Table] =
    executeSqlAndProcess("select name, style_attr from tables order by name asc",
      (row) => Table(
        row.getString("name"), // Use name as id
        row.getString("name"),
        row.getString("style_attr")
      )
    )
  
  private def readColumnsForTable(tableId : String)(implicit conn : Connection) : List[Column] =
    executeSqlAndProcess("""
        select column_name, column_type, color_name, tags 
          from columns where table_name = '""" + tableId + """' order by column_name asc
        """,
      (row) => Column(
        tableId + "-" + row.getString("column_name"),
        row.getString("column_name"),
        row.getString("column_type"),
        row.getString("color_name"),
        parseTags(row.getString("tags"))
      )
    )
  
  def read(implicit conn : Connection) : List[TableAndColumns] = {
    readTables.map((t)=>TableAndColumns(t, readColumnsForTable(t.id)))
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
    val statement = conn.prepareStatement("UPDATE COLUMNS SET color_name = ?, tags = ? WHERE TABLE_NAME = ? AND COLUMN_NAME = ?")
    statement.setString(1, col.colorId)
    statement.setString(2, col.writeTagIdString())
    statement.setString(3, col.id.split("-")(0))
    statement.setString(4, col.id.split("-")(1))
    val result = statement.executeUpdate()
  }

  def updateTable(table : Table)(implicit conn : Connection) : Unit = {
    val statement = conn.prepareStatement("UPDATE TABLES SET STYLE_ATTR = ?, tags = ? WHERE NAME = ?")
    statement.setString(1, table.styleAttr)
    statement.setString(2, "")
    statement.setString(3, table.id)
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
    insertTable(t)
    tableAndCols.columns.foreach(insertColumn(t, _))
  }
  
  private def insertColumn(table : Table, column : Column)(implicit conn : Connection) {
      val statement = conn.prepareStatement(
          "INSERT INTO COLUMNS VALUES(?, ?, ?, ?, ?)")
      statement.setString(1, table.id)
      statement.setString(2, column.name)
      statement.setString(3, column.colType)
      statement.setString(4, column.colorId)
      statement.setString(5, column.writeTagIdString)
      statement.execute()
  }

  private def insertTable(table : Table)(implicit conn : Connection) {
      val statement = conn.prepareStatement(
          "INSERT INTO TABLES VALUES(?, ?, ?)")
      statement.setString(1, table.name)
      statement.setString(2, table.styleAttr)
      statement.setString(3, "")
      statement.execute()
  }

  def dropDatabaseTables() {
    createTable(
      "drop table columns", 
      "Cannot drop table columns, maybe it is already dropped? "
    )
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
      """
        create table columns (
          table_name varchar(255), 
          column_name varchar(255), 
          column_type varchar(255), 
          color_name varchar(255), 
          tags varchar(2000)
        )
      """, 
      "Cannot create table columns, maybe it is already created? "
    )
    createTable(
      """
        create table tables (
          name varchar(255), 
          style_attr varchar(4000), 
          tags varchar(2000)
        )
      """, 
      "Cannot create table tables, maybe it is already created? "
    )
    createTable(
      """
        create table tags (
          tag_name varchar(255)
        )
      """, 
      "Cannot create table tags, maybe it is already created? "
    )
  }
  

}