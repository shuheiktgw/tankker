package controllers

import java.sql.Timestamp
import javax.inject.Inject

import controllers.FirstPartController.FirstPartForm
import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Tables.{FirstPartRow, LastPartRow, UserRow}
import models.Tables
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import repositories.{FirstPartRepo, LastPartRepo}
import services.{UserService, UserServiceLike}
import views.html.helper.form

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shuhei on 2016/08/07.
  */
class LastPartController @Inject()(val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo, val userService: UserService, val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with OptionalAuthElement with AuthConfigImpl {

  import controllers.LastPartController._

  def brandNew(firstPartId: Long) = AsyncStack { implicit rs =>
    loggedIn match {
      case Some(user) => {
        val firstPart: Future[Option[Tables.FirstPartRow]] = firstPartRepo.findByID(firstPartId)
        firstPart flatMap {
          case Some(firstPart) => {
            val firstPartOwner: Future[Option[Tables.UserRow]] = userService.findById(firstPart.userId)
            firstPartOwner.map{
              case Some(owner) => Ok(views.html.lastPart.brandNew(user, lastPartForm, firstPart, owner))
              case _ => Redirect(routes.TimelineController.show).flashing("error" -> "指定された短歌は存在しません")
            }
          }
          case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "指定された短歌は存在しません"))
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }

  // TODO FirstPartの方もhiddenじゃなくてcontrollerに直接値を渡すようにする
  def create(userId: Long, firstPartId: Long) = AsyncStack { implicit rs =>
    lastPartForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.LastPartController.brandNew(firstPartId)).flashing("error" -> "返歌が短すぎるか長すぎます"))
      },
      form => {
        loggedIn match {
          case Some(user) if user.id == userId => {
            if(form.lastPartContentSecond == "咳をしてもひとり"){
              Future(Redirect(routes.TinderController.authenticate))
            }else{
              val lastPart: LastPartRow = LastPartRow(0, userId.toInt, firstPartId.toInt, form.lastPartContentFirst, form.lastPartContentSecond, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
              lastPartRepo.reply(lastPart).map {
                lastPartOp => Redirect(routes.TimelineController.show).flashing("success" -> "返歌を投稿しました")
              }
            }
          }
          case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
        }
      }
    )
  }

  def edit(lastPartId: Long) = AsyncStack { implicit rs =>
    loggedIn match {
      case Some(user) => {
        lastPartRepo.findById(lastPartId) map {
          case Some(lastPart) if lastPart.userId == user.id => {
            val form = lastPartForm.fill(LastPartForm(lastPart.lastPartContentFirst, lastPart.lastPartContentSecond))
            Ok(views.html.lastPart.edit(lastPart, form))
          }
          case _ => Redirect(routes.TimelineController.show)
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }

  def update(userId: Long, firstPartId: Long, lastPartId: Long) = AsyncStack { implicit rs =>

    // TODO createと処理共通化したい
    lastPartForm.bindFromRequest.fold(
      error => {
        Future(Redirect(routes.LastPartController.edit(lastPartId)).flashing("error" -> "返歌が短すぎるか長すぎます"))
      },
      form => {
        loggedIn match {
          case Some(user) if user.id == userId => {
            val lastPart: LastPartRow = LastPartRow(lastPartId.toInt, userId.toInt, firstPartId.toInt, form.lastPartContentFirst, form.lastPartContentSecond, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
            lastPartRepo.change(lastPart).map {
              lastPartOp => Redirect(routes.TimelineController.show).flashing("success" -> "返歌を更新しました")
            }
          }
          case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
        }
      }
    )
  }

  def delete(id: Long) = AsyncStack { implicit rs =>
    loggedIn match{
      case Some(user) => {
        val fetchedLastPart: Future[Option[Tables.LastPartRow]] = lastPartRepo.findById(id)
        fetchedLastPart map{
          case f if f.get.userId == user.id => {
            lastPartRepo.remove(id)
            Redirect(routes.TimelineController.show).flashing("success" -> "返歌を削除しました")
          }
          case _ => Redirect(routes.TimelineController.show).flashing("error" -> "Goodbye bad boy...")
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }
}

object LastPartController {

  case class LastPartForm(lastPartContentFirst: String, lastPartContentSecond: String)

  val lastPartForm = Form(
    mapping(
      "lastPartContentFirst" -> nonEmptyText(3, 17),
      "lastPartContentSecond" -> nonEmptyText(3, 17)
    )(LastPartForm.apply)(LastPartForm.unapply)
  )

}
