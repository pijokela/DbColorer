package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.codahale.jerkson.Json._
import services.DataAccess
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
  def getData = Action {
    Ok(dbService.read())
  }
  
  /*
   * The JSON must contain data for 1 table.
   */
  def postData = Action(parse.json) { request =>
    println("postData")
    dbService.write(request.body)
    Ok(Json.toJson(Messages("response.dataWritten")))
  }
  
  /*
   * The JSON must contain data for 1 table.
   */
  def createTestData = Action {
    val tablesObj = testDataService.read()
    val tablesArray : JsArray = (tablesObj \ "tables").asInstanceOf[JsArray]
    val it : ArrayBuffer[JsObject] = tablesArray.productIterator.next().asInstanceOf[ArrayBuffer[JsObject]]
    it.foreach(
        (v : JsObject) => dbService.createTable(v)
    )
    
    Ok(Json.toJson("Data written to database!"))
  }
  
}