package services

import org.junit.Test
import play.api.libs.json.JsObject
import org.junit.Assert._

class DomainJsonTests {

  @Test
  def aTagShouldProduceAndReadJson() {
    val tag = new Tag("myid", "myname")
    jsonRoundTripTest(tag, Tag)
  }
  
  val table = new Table("myid", "myname", "my-style: 100%")
  
  @Test
  def aTableShouldProduceAndReadJson() {
    jsonRoundTripTest(table, Table)
  }
  
  @Test
  def aColorShouldProduceAndReadJson() {
    val color = new Color("myid", "myname")
    jsonRoundTripTest(color, Color)
  }
  
  @Test
  def aColumnShouldProduceAndReadJson() {
    val col = new Column("myid", "myname", "a1", "a2", List())
    jsonRoundTripTest(col, Column)
  }
  
  val colWithTag = new Column("myid", "myname", "1", "2", List(Tag("1", "2")))
  
  @Test
  def aColumnWithATagShouldProduceAndReadJson() {
    jsonRoundTripTest(colWithTag, Column)
  }
  
  @Test
  def aTableAndColumnsShouldProduceAndReadJson() {
    val tAndC = new TableAndColumns(table, colWithTag :: Nil)
    jsonRoundTripTest(tAndC, TableAndColumns)
  }
  
  private def jsonRoundTripTest(tag : Jsonable, comp : JsonableCompanion) : Unit = {
    val json : JsObject = tag.toJson
    assertNotNull(json)
    val tag2 = comp.fromJson(json)
    assertNotNull(tag2)
    assertEquals(tag, tag2)
  }
}