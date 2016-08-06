package controllers


import java.sql.Timestamp

import controllers.FirstPartController.firstPartForm
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.i18n.{I18nSupport, MessagesApi}
import models.{FirstPartRepo, Tables, UserRepo, UserRepoLike}
import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Tables.UserRow
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by shuhei.kitagawa on 2016/08/02.
  */
class UserController @Inject()(val userRepoLike: UserRepoLike, val usersRepo: UserRepo, val firstPartRepo: FirstPartRepo, val messagesApi: MessagesApi) extends Controller with I18nSupport with OptionalAuthElement with AuthConfigImpl{

  // TODO Admin権限に設定
  // TODO FLGがtrue のユーザーがログイン出来ないようにしなくてはいけない
  def index = Action.async{ implicit rs =>
    usersRepo.all().map{ users =>
      Ok(views.html.user.index(users))
    }
  }

  def show = AsyncStack { implicit rs =>
      loggedIn match {
        case Some(user) =>
          firstPartRepo.all(user.id).map{ firstPats =>
            Ok(views.html.user.show(user, firstPartForm, firstPats))
          }
        // TODO エラーページ作って遷移させたい
        case _ => Future(Redirect(routes.LoginController.brandNew))
    }
  }

  // TODO テスト書く
  // TODO メソッド名変更
  import UserController._
  def brandNew = Action.async{ implicit rs =>
    Future(Ok(views.html.user.brandNew(userForm)))
  }

  // TODO 新規作成ユーザーがすぐにログインできるようにしたい
  def create = AsyncStack{ implicit rs =>
    userForm.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.user.brandNew(userForm)))
      },
      form =>{
        val user = UserRow(0,form.username,form.email,form.password,false, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
        usersRepo.add(user).map { userOp =>
          Redirect(routes.UserController.show)
        }
      }
    )
  }

  // TODO ViewのほうがPOSTになってるので,Getに帰る
  def edit = AsyncStack { implicit rs =>
    Future{
      loggedIn match {
        case Some(user) => {
          val form = userForm.fill(UserForm(Some(user.id), user.username, user.email, ""))
          Ok(views.html.user.edit(form))
        }
        // TODO エラーページ作って遷移させたい
        case _ => Redirect(routes.LoginController.brandNew)
      }
    }
  }


  // TODO HTTPリクエストのメソッドをPatchでRootを/userに変更
  def update = AsyncStack{ implicit rs =>
    userForm.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.user.edit(userForm)))
      },
      // TODO LoggedIn でユーザー同じか確認
      form =>{
        val user = UserRow(form.id.get.toInt,form.username,form.email, form.password ,false, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
        // TODO usernameとemailが被ってた場合の処理を追加
        usersRepo.change(user).map { _ =>
          Redirect(routes.UserController.show)
        }
      }
    )
  }

  // TODO HTTPリクエストのメソッドをDeleteでRootを/userに変更
  def delete = AsyncStack { implicit rs =>
    loggedIn match{
      case Some(user) => usersRepo.remove(user.id) map{
        // TODO 削除したユーザーの情報をFlashで表示
        // 削除する前に確認
        case Some(user) => Redirect(routes.LoginController.brandNew)
        case None => Redirect(routes.LoginController.brandNew)
      }
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
}

