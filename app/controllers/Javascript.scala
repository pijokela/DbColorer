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
import play.api.Play.current

/**
 * This controller creates a dynamic Javascript file that gives all the
 * static Javascript files access to the dynamic stuff on the server.
 * <P>
 * <li>Localized strings
 */
object Javascript extends Controller {
  
  def index(lang: String) = Action { request =>
    val msgs : Map[String, Map[String, String]] = Messages.messages(Play.current)
    val selectedMsgs = msgs(lang).iterator
    val lines = selectedMsgs.map(e=>"DbColorer.msg_" + e._1.replaceAll("\\.", "_") + " = '" + e._2 + "';\n")
    
    Ok("/* Javascript for " + lang + " */\n\nDbColorer = {};\n\n" + lines.mkString)
  }
  
  def getLang(implicit lang: Lang) = lang
}