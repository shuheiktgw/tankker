package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import models.Tables
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import repositories.UserRepo
import services.{TimelineService, UserService, UserServiceLike}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import controllers.FirstPartController.firstPartForm
import controllers.UserController.searchForm

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class TimelineController @Inject()(val timelineService: TimelineService,val userService: UserService, val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with OptionalAuthElement with AuthConfigImpl{

  def show = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) =>{
        for{
          tankas <- timelineService.fetchTankasForTL(user.id)
          numbers <- timelineService.fetchProfileNumbers(user.id)
          notFollowingUsers <- userService.fetchNotfollowingUsers(user.id)
        } yield {
          Ok(views.html.timeline.show(user, firstPartForm, tankas, searchForm, numbers, notFollowingUsers))
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "セッションがタイムアウトしました"))
    }
  }
}
