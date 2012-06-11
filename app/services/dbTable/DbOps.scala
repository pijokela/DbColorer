package services.dbTable

import java.sql._
import play.api.db.DB
import play.api.Play._
import org.h2.jdbc.JdbcSQLException
import scala.collection.mutable.ArrayBuffer

trait DbOps {

  def executeSql(sql : String, conn : Connection) : ResultSet = {
    val statement = conn.prepareStatement(sql)
    if (statement.execute()) {
      statement.getResultSet()
    } else {
      throw new RuntimeException("Select statement did not return a result set.")
    }
  }
  
  def executeSqlAndProcess[E](sql : String, fn : (ResultSet)=>E) : List[E] = {
    val results = new ArrayBuffer[E]()
    DB.withConnection { conn =>
      val r : ResultSet = executeSql(sql, conn)
      while (r.next()) {
        results += fn(r)
      }
    }
    return results.toList
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
  

}