package services.testTable

import services._
import play.api.libs.json._
import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer

class TestDataReader(val fileName: String ) extends DataAccess {
  
  override def read() : JsObject = {
    println("Reading file: " + fileName)
    val file = new File(fileName)
    val reader = new BufferedReader(new FileReader(file))
    
    val tables = new ArrayBuffer[Table]()
    def createAndAdd(name : String) : Table = {
      val t = new Table(name)
      tables.+=(t)
      return t
    }
    
    // 4 line header:
    reader.readLine()
    reader.readLine()
    reader.readLine()
    reader.readLine()
    
    var line = reader.readLine()
    while(line != null) {
      // There are some empty lines:
      if (line.trim().length() > 0) {
        val parts = line.split("\\s+")
        val table : Table = 
          tables.find(_.name == parts(0)).getOrElse(createAndAdd(parts(0)))
        val col = new JsObject(
            List(
                "name" -> Json.toJson(parts(1)),
                "type" -> Json.toJson(parts(2)),
                "color" -> Json.toJson(parts(3))
            )
        )
        table.cols.+=(col)
      }
      print(".")
      line = reader.readLine()
    }
    
    val result = new JsObject(List("tables" -> 
      new JsArray(tables.map(_.toJson()))))
    println("Result ready.")
    return result
  }
  
  override def write(data: JsValue) : Unit = {
    println("Writing! " + (data \ "name"))
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