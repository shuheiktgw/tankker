package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import models.Tables
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import repositories.{UserRepo, UserRepoLike}
import services.TimelineService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class TimelineController @Inject()(val timelineService: TimelineService, val userRepoLike: UserRepoLike, val usersRepo: UserRepo, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with OptionalAuthElement with AuthConfigImpl{

  import controllers.FirstPartController.firstPartForm
  import controllers.UserController.searchForm

  def show = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) =>{
        val futureTankas: Future[Seq[((String, Tables.FirstPartRow), Seq[(String, Tables.LastPartRow)])]] = timelineService.fetchTankasForTimeline(user.id)
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
