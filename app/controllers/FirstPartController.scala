package controllers

import java.sql.Timestamp
import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Tables.{FirstPartRow, UserRow}
import models.Tables
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import views.html.helper.form

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import controllers.LoginController.LoginForm
import repositories.FirstPartRepo
import services.UserServiceLike

/**
  * Created by shuhei.kitagawa on 2016/08/05.
  */
class FirstPartController @Inject()(val firstPartRepo: FirstPartRepo,val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with OptionalAuthElement with AuthConfigImpl{

  // TODO Auth周りのメソッド作ってすっきりさせたい

  import FirstPartController._
  def create = AsyncStack{ implicit rs =>

    firstPartForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.TimelineController.show).flashing("error" -> "短歌が短すぎるか長すぎます"))
      },
      form =>{
        loggedIn match {
          case Some(user) => if(user.id == form.userId){
            val firstPart: FirstPartRow = FirstPartRow(0, form.userId.toInt,form.firstPartContentFirst,form.firstPartContentSecond,form.firstPartContentThird, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
            firstPartRepo.add(firstPart).map{
              firstPartOp => Redirect(routes.TimelineController.show).flashing("success" -> "短歌を投稿しました")
            }
          }else{
            Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
          }
          case None => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
        }
      }
    )
  }

  def edit(id: Long) = AsyncStack{ implicit rs =>

    loggedIn match{
      case Some(user) => {
        firstPartRepo.findByID(id) map {
          case Some(firstPart) => {
            if (firstPart.userId != user.id) {
              Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy...")
            } else {
              val form = firstPartForm.fill(FirstPartForm(Some(firstPart.id),firstPart.userId,firstPart.firstPartContentFirst,firstPart.firstPartContentSecond,firstPart.firstPartContentThird))
              Ok(views.html.firstPart.edit(form))
            }
          }
          case _ => Redirect(routes.TimelineController.show)
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }

  def update = AsyncStack{ implicit rs =>

    // TODO createと処理共通化したい
    firstPartForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.TimelineController.show).flashing("error" -> "短歌が短すぎるか長すぎます"))
      },
      form =>{
        loggedIn match {
          case Some(user) => if(user.id == form.userId){
            val firstPart: FirstPartRow = FirstPartRow(form.id.get.toInt, form.userId.toInt,form.firstPartContentFirst,form.firstPartContentSecond,form.firstPartContentThird, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
            firstPartRepo.change(firstPart).map{
              firstPartOp => Redirect(routes.TimelineController.show).flashing("success" -> "短歌を更新しました")
            }
          }else{
            Future(Redirect(routes.TimelineController.show).flashing("error" -> "Goodbye bad boy..."))
          }
          case None => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
        }
      }
    )
  }

  def delete(id: Long) = AsyncStack { implicit rs =>
    loggedIn match{
      case Some(user) => {
        val fetchedFirstPart: Future[Option[Tables.FirstPartRow]] = firstPartRepo.findByID(id)
        fetchedFirstPart map{
          case f if f.get.userId == user.id => {
            firstPartRepo.remove(id)
            Redirect(routes.TimelineController.show).flashing("success" -> "短歌を削除しました")
          }
          case _ => Redirect(routes.TimelineController.show).flashing("error" -> "Goodbye bad boy...")
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }
}

object FirstPartController{
  case class FirstPartForm(id: Option[Long], userId: Long, firstPartContentFirst: String, firstPartContentSecond: String, firstPartContentThird: String)

  val firstPartForm = Form(
    // TODO フォームのバリデーションの追加
    mapping(
      "id" -> optional(longNumber),
      // TODO userIDがrequiredになってない
      "userId" -> longNumber,
      "firstPartContentFirst" -> nonEmptyText(2, 15),
      "firstPartContentSecond" -> nonEmptyText(3, 20),
      "firstPartContentThird" -> nonEmptyText(2, 15)
    )(FirstPartForm.apply)(FirstPartForm.unapply)
  )


}
