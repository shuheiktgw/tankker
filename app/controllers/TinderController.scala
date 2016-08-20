package controllers

import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import services.{UserService, UserServiceLike}

import scala.concurrent.Future

/**
  * Created by shuhei.kitagawa on 2016/08/20.
  */
class TinderController(val userService: UserService, val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with OptionalAuthElement with AuthConfigImpl{

  import controllers.TinderController._

  def brandNew = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => Future(Ok(views.html.tinder.brandNew(tinderAuthForm)))
      case None => Future(Redirect(routes.LoginController.brandNew))
    }
  }



}

object TinderController{
  case class TinderAuthForm(facebookId: String, authToken: String)

  val tinderAuthForm = Form(
    mapping(
      "facebookId" -> nonEmptyText(),
      "authToken" -> nonEmptyText()
    )(TinderAuthForm.apply)(TinderAuthForm.unapply)
  )
}