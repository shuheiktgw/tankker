package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import models.Tables
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import repositories.UserRepo
import services.{TimelineService, UserServiceLike}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class TimelineController @Inject()(val timelineService: TimelineService, val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with OptionalAuthElement with AuthConfigImpl{

  import controllers.FirstPartController.firstPartForm
  import controllers.UserController.searchForm

  def show = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) =>{
        val futureTankas: Future[Map[(Option[Tables.FirstPartRow], Tables.UserRow), Seq[(Option[Tables.LastPartRow], Tables.UserRow)]]] = timelineService.fetchTankasForTL(user.id)
        futureTankas.flatMap{ tankas =>
          timelineService.fetchProfileNumbers(user.id) map{ profileNumbers =>
            Ok(views.html.timeline.show(user, firstPartForm, tankas, searchForm, profileNumbers))
          }
        }
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Please login!"))
    }
  }
}
