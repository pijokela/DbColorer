package services.dbTable
import services.DataAccess
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

class DbDataReader extends DataAccess {

  override def read() : JsObject = {
    
    DB.withConnection { conn =>
      val resultSet = executeSql("select table_name, column_name, column_type, color_name from tables order by table_name asc, column_name asc", conn)
      
      val tables = new ArrayBuffer[Table]()
      def createAndAdd(name : String) : Table = {
        val t = new Table(name)
        tables.+=(t)
        return t
      }
      
      while (resultSet.next()) {
        val tableName = resultSet.getString(1)
        val columnName = resultSet.getString(2)
        val columnType = resultSet.getString(3)
        val colorName = resultSet.getString(4)
        
	    val table : Table = tables.find(_.name == tableName).getOrElse(createAndAdd(tableName))
	    val col = new JsObject(
	        List(
	          "name" -> Json.toJson(columnName),
	          "type" -> Json.toJson(columnType),
	          "color" -> Json.toJson(colorName)
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
    val tableName = (table \ "name").as[String]
    println("Writing table " + tableName)
    DB.withConnection { conn =>
      val cols = (table \ "columns").asInstanceOf[JsArray].productIterator.next().asInstanceOf[Iterable[JsObject]]
      for(column <- cols) {
        val colName = (column \ "name").as[String]
        val colType = (column \ "type").as[String]
        val color = (column \ "color").as[String]
        val statement = conn.prepareStatement("INSERT INTO TABLES VALUES('" + tableName + "', '" + colName + "', '" + colType + "', '" + color + "')")
        statement.execute()
      }
    }
  }
  
  override def write(table : JsValue) : Unit = {
    val tableName = (table \ "name").as[String]
    println("Updating table " + tableName)
    DB.withConnection { conn =>
      val cols = (table \ "columns").asInstanceOf[JsArray].productIterator.next().asInstanceOf[Iterable[JsObject]]
      for(column <- cols) {
        val colName = (column \ "name").as[String]
        val colType = (column \ "type").as[String]
        val color = (column \ "color").as[String]
        println("Update col " + colName + " to color " + color)
        val statement = conn.prepareStatement("UPDATE TABLES SET color_name = '" + color + "' WHERE TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = '" + colName + "'")
        statement.execute()
      }
    }
  }
  
  private def executeSql(sql : String, conn : Connection) : ResultSet = {
    val statement = conn.prepareStatement(sql)
    if (statement.execute()) {
      statement.getResultSet()
    } else {
      throw new RuntimeException("Select statement did not return a result set.")
    }
  }
}
  
class Table(val name : String) {
  val cols = new ArrayBuffer[JsValue]
  
  def toJson() : JsValue = {
    new JsObject(List(
        "name" -> Json.toJson(name),
        "columns" -> new JsArray(cols)
      )
    )
  }
}