package services

import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray

trait Jsonable {
  def toJson() : JsObject
}

trait JsonableCompanion {
  def fromJson(jsValue : JsValue) : Jsonable
}


/**
 * Table: A table of columns in the DbColorer UI.
 */
case class Table(id : String, name : String, styleAttr : String) extends Jsonable {
  override def toJson() : JsObject = new JsObject(
	List(
      "id" -> Json.toJson(id),
      "name" -> Json.toJson(name),
      "styleAttr" -> Json.toJson(styleAttr)
    )
  )
}

object Table extends JsonableCompanion {
  override def fromJson(json : JsValue) : Table = 
    Table(
      (json \ "id").as[String],
      (json \ "name").as[String],
      (json \ "styleAttr").as[String]
    )
}


/**
 * Column: A columns in a table in the DbColorer UI.
 */
case class Column(id : String, name : String, colType : String, colorId : String, tags: List[Tag]) extends Jsonable {
  override def toJson() : JsObject = new JsObject(
	List(
      "id" -> Json.toJson(id),
      "name" -> Json.toJson(name),
	  "type" -> Json.toJson(colType),
	  "colorId" -> Json.toJson(colorId),
	  "tags" -> new JsArray(tags.map(_.toJson))
    )
  )
}

object Column extends JsonableCompanion {
  override def fromJson(json : JsValue) : Column = 
    Column(
      (json \ "id").as[String],
      (json \ "name").as[String],
      (json \ "type").as[String],
      (json \ "colorId").as[String],
      Tag.fromJsonArray((json \ "tags").asInstanceOf[JsArray])
    )
    
  def fromJsonArray(colsArray : JsArray) : List[Column] = 
    colsArray.value.map(Column.fromJson(_)).toList

}


/**
 * TableAndColumns: A table and all the columns in it.
 */
case class TableAndColumns(table : Table, columns : List[Column]) extends Jsonable {
  override def toJson() : JsObject = 
    table.toJson ++ 
      new JsObject(List("columns" -> new JsArray(columns.map(_.toJson))))

}

object TableAndColumns extends JsonableCompanion {
  override def fromJson(json : JsValue) : TableAndColumns = 
    TableAndColumns(
      Table.fromJson(json),
      Column.fromJsonArray((json \ "columns").asInstanceOf[JsArray])
    )
}


/**
 * Tag: A tag is a text marker that can be added to a column.
 */
case class Tag(id : String, name : String) extends Jsonable {
  override def toJson() : JsObject = new JsObject(
	List(
      "id" -> Json.toJson(id),
      "name" -> Json.toJson(name)
    )
  )
}

object Tag extends JsonableCompanion {
  override def fromJson(json : JsValue) : Tag = 
    Tag(
      (json \ "id").as[String],
      (json \ "name").as[String]
    )
  
  def fromJsonArray(tagsArray : JsArray) : List[Tag] = 
    tagsArray.value.map(Tag.fromJson(_)).toList
}


/**
 * Color: A color is one of the colors that can be used to color the columns.
 * There is a static list of colors available in DbColorer.
 */
case class Color(id : String, name : String) extends Jsonable {
  override def toJson() : JsObject = new JsObject(
	List(
      "id" -> Json.toJson(id),
      "name" -> Json.toJson(name)
    )
  )
}

object Color extends JsonableCompanion {
  def fromJson(json : JsValue) : Color = 
    Color(
      (json \ "id").as[String],
      (json \ "name").as[String]
    )
}