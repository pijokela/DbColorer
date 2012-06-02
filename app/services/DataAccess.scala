package services
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

trait DataAccess {
  def read() : JsObject
  def write(table : JsValue) : Unit
}