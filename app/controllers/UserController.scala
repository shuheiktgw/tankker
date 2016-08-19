package controllers


import java.sql.Timestamp

import controllers.FirstPartController.firstPartForm
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.i18n.{I18nSupport, MessagesApi}
import models.Tables
import javax.inject.Inject

import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import models.Tables.UserRow
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.{FirstPartRepo, FollowingRepo, UserRepo}
import services.{TimelineService, UserService, UserServiceLike}
import slick.driver.JdbcProfile
import views.models.UserHenkasCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by shuhei.kitagawa on 2016/08/02.
  */
class UserController @Inject()(val userService: UserService, val userServiceLike: UserServiceLike, val firstPartRepo: FirstPartRepo, val followingRepo: FollowingRepo, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with OptionalAuthElement with AuthConfigImpl{

  // TODO Admin権限に設定
  // TODO FLGがtrue のユーザーがログイン出来ないようにしなくてはいけない
  def index = Action.async{ implicit rs =>
    userService.getAll.map{ users =>
      Ok(views.html.user.index(users))
    }
  }

  def show(username: String) = AsyncStack { implicit rs =>
      loggedIn match {
        case Some(currentUser) => {
          userService.fetchUserData(currentUser, username) map{
            case Some(carrier) => Ok(views.html.user.show(carrier))
            case _ => Redirect(routes.TimelineController.show).flashing("error" -> "指定されたユーザー名は存在しません")
          }
        }
        case _ => Future(Redirect(routes.LoginController.brandNew))
    }
  }

  // TODO テスト書く
  // TODO メソッド名変更
  import UserController._
  def brandNew = Action.async{ implicit rs =>
    Future(Ok(views.html.user.register(userForm)))
  }


  //TODO ユーザー名前のバリデーションを行う
  def create = AsyncStack{ implicit rs =>
    userForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.UserController.brandNew).flashing("error" -> "新規作成フォームに入力された値が正しくありません"))
      },
      form =>{
        val user = UserRow(0,form.username,form.email,form.password,false, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
        userService.add(user).flatMap {
          case Some(user) => gotoLoginSucceeded(user.username).map(_.flashing("success" -> "Tankkerへようこそ!"))
          case _ => Future(Redirect(routes.UserController.brandNew).flashing("error" -> "Emailもしくはユーザー名が既に存在しています"))
        }
      }
    )
  }

  def edit = AsyncStack { implicit rs =>
    Future{
      loggedIn match {
        case Some(user) => {
          val form = userEditForm.fill(UserEditForm(user.email, ""))
          Ok(views.html.user.edit(form))
        }
        // TODO エラーページ作って遷移させたい
        case _ => Redirect(routes.LoginController.brandNew)
      }
    }
  }

  def update = AsyncStack{ implicit rs =>
    userEditForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.UserController.edit).flashing("error" -> "ユーザー情報に誤った値が含まれています"))
      },
      form =>{
        loggedIn match{
          case Some(currentUuser) =>{
            val hasPasswordChanged: Boolean = !form.password.isEmpty && !(form.password == null)
            val user = UserRow(currentUuser.id,currentUuser.username,form.email, form.password ,false, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
            userService.change(user, hasPasswordChanged).map { _ =>
              Redirect(routes.TimelineController.show).flashing("success" -> "ユーザー情報を更新しました")
            }
          }
          case _ => Future(Redirect(routes.LoginController.brandNew))
        }
      }
    )
  }

  // TODO HTTPリクエストのメソッドをDeleteでRootを/userに変更
  def delete = AsyncStack { implicit rs =>
    loggedIn match{
      case Some(user) => userService.remove(user.id) map{
        // TODO 削除したユーザーの情報をFlashで表示
        // 削除する前に確認
        case Some(user) => Redirect(routes.LoginController.brandNew).flashing("success" -> "ログアウトしました")
        case None => Redirect(routes.LoginController.brandNew)
      }
    }
  }

  def search = AsyncStack{implicit rs =>
    searchForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.TimelineController.show))
      },

      form =>{
        loggedIn match{
          case Some(currentUser) => {
            userService.findByUsername(form.username) map {
              case Some(user) => Redirect(routes.UserController.show(form.username)).flashing("success" -> s"${form.username}のページに移動しました")
              case _ => Redirect(routes.TimelineController.show).flashing("error" -> "入力されたユーザー名は存在しません")
            }
          }
          case _ => Future(Redirect(routes.LoginController.brandNew))
        }
      }
    )
  }

  def henkas(username: String) = AsyncStack { implicit rs =>
    loggedIn match {
      case Some(currentUser) => {
        userService.fetchUserDataForHenkas(currentUser, username) map{
          case Some(carrier) => Ok(views.html.user.henkas(carrier))
          case _ => Redirect(routes.TimelineController.show).flashing("error" -> "指定されたユーザー名は存在しません")
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew))
    }
  }
}

object UserController {

  case class UserForm(id: Option[Long], username: String, email: String, password: String)

  val userForm = Form(
    // TODO フォームのバリデーションの追加
    mapping(
      "id" -> optional(longNumber),
      "username" -> nonEmptyText(1, 16),
      // TODO emailがrequiredになってない
      "email" -> email,
      "password" -> nonEmptyText(8, 16)
    )(UserForm.apply)(UserForm.unapply)
  )

  case class UserEditForm(email: String, password: String)


  val userEditForm = Form(
    // TODO Passwordのバリデーション,8文字以下が入ってします
    mapping(
      "email" -> email,
      "password" -> text(maxLength = 16)
    )(UserEditForm.apply)(UserEditForm.unapply) verifying(fields => fields.password.isEmpty || fields.password == null || fields.password.length > 7)
  )
  case class SearchForm(username: String)

  val searchForm = Form(
    mapping(
      "username" -> nonEmptyText(1,16)
    )(SearchForm.apply)(SearchForm.unapply)
  )
}

