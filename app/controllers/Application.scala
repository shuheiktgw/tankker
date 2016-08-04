package controllers

import com.google.inject.Inject
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import slick.driver.JdbcProfile

class Application  extends Controller{

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}