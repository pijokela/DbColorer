package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.codahale.jerkson.Json._
import services.testTable.TestDataReader
import services.dbTable.DbDataReader
import scala.collection.mutable.ArrayBuffer
import play.api.i18n.Messages
import play.api.i18n.Lang

object Application extends Controller {
  
  val dbService : DbDataReader = new DbDataReader() // new TestDataReader("data.txt")
  val testDataService : TestDataReader = new TestDataReader("data.txt")
  
  def index = Action {
    Ok(views.html.index("Your new application is ready. W00t!", "myApp"))
  }
  
  /*
   * The JSON sent to the browser contains an array of tables.
   */
  def getTables = Action {
    Ok(dbService.read())
  }

  /*
   * The JSON must contain data for 1 table.
   */
  def postTables = Action(parse.json) { request =>
    println("postData")
    dbService.write(request.body)
    Ok(Json.toJson(Messages("response.dataWritten")))
  }
  
  def getTags = Action {
    Ok(dbService.readTags())
  }

  def postTags = Action(parse.json) { request =>
    dbService.writeTags(request.body)
    Ok(Json.toJson(Messages("response.dataWritten")))
  }
  
  /*
   * The JSON must contain data for 1 table.
   */
  def createTestData = Action {
    // Make sure that the database is created:
    dbService.dropDatabaseTables()
    dbService.createDatabaseTables()
    
    val tables: List[JsValue] = testDataService.read()
    tables.foreach (
        (v : JsValue) => dbService.createTable(v)
    )
    
    Ok(Json.toJson("Data written to database!"))
  }
  
}