package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.UserServiceLike

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/03.
  */
class LoginController @Inject()(val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with AuthConfigImpl {

  // TODO CSRF対策
  import LoginController._

  def brandNew = Action.async{ implicit rs =>
    Future(Ok(views.html.login.login(loginForm)))
  }

  def create = Action.async { implicit rs =>
    loginForm.bindFromRequest.fold(
      formWithError  => Future(BadRequest(views.html.login.login(formWithError))),
      form => {
        userServiceLike.authenticate(form).flatMap {
          case Some(user) => gotoLoginSucceeded(user.username).map(_.flashing("success" -> "ログインに成功しました"))
          case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "歌人名もしくはアイコトバに間違いがあります"))
        }
      }
    )
  }

  def delete = Action.async{ implicit rs =>
    gotoLogoutSucceeded.map(_.flashing("success" -> "ログアウトに成功しました"))
  }
}

object LoginController{
  case class LoginForm(username: String, password: String)

  val loginForm = Form(
    // TODO フォームのバリデーションの追加
    mapping(
      "username" -> nonEmptyText(1,16),
      // TODO emailがrequiredになってない
      "password" -> nonEmptyText(8,32)
    )(LoginForm.apply)(LoginForm.unapply)
  )
}
