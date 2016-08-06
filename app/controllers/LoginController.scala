package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{Tables, UserRepo, UserRepoLike}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}


import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/03.
  */
class LoginController @Inject()(val userRepoLike: UserRepoLike, val usersRepo: UserRepo, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with AuthConfigImpl {

  // TODO UsersRepoLikeとUsersRepoの2つインポートするのダサいからどうにかしよう
  // TODO CSRF対策
  // TODO Injection対策
  import LoginController._

  def brandNew = Action.async{ implicit rs =>
    Future(Ok(views.html.login.login(loginForm)))
  }


  def create = Action.async { implicit rs =>
    loginForm.bindFromRequest.fold(
      error => Future.successful(BadRequest(views.html.login.login(loginForm))),
      user => gotoLoginSucceeded(user.username)
    )
  }

  def delete = Action.async{ implicit rs =>
    gotoLogoutSucceeded
  }

  val loginForm = Form(
    // TODO フォームのバリデーションの追加
    mapping(
      "username" -> nonEmptyText(1,16),
      // TODO emailがrequiredになってない
      "password" -> nonEmptyText(8,32)
    )(LoginForm.apply)(LoginForm.unapply) verifying("Invalid email or password", fields => fields match{
      case userData => usersRepo.authenticate(userData.username, userData.password).isDefined
    })
  )
}

object LoginController{
  case class LoginForm(username: String, password: String)

  impot 
}
