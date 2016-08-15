package services

import com.google.inject.Inject
import controllers.FirstPartController._
import controllers.routes
import models.Tables
import play.api.mvc._

import scala.concurrent.duration.Duration
import repositories.{FirstPartRepo, FollowingRepo, LastPartRepo, UserRepo}
import views.models.UserShowCarrier

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class UserService @Inject()(val userRepo: UserRepo, val timelineService: TimelineService, val followingRepo: FollowingRepo, val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo) {

  def fetchUserData(currentUser: Tables.UserRow, username: String): Future[Option[UserShowCarrier]] = {
    val futureRequestedUser: Future[Option[Tables.UserRow]] = userRepo.findByUsername(username)

    futureRequestedUser flatMap {
      case Some(requestedUser) => {
        val isMyself: Boolean = requestedUser.id == currentUser.id
        val isFollowing: Boolean = Await.result(followingRepo.findByUserIdAndFollowingUserId(currentUser.id, requestedUser.id), Duration.Inf).isDefined

        fetchTankasForUserPage(requestedUser.id) flatMap { tankas =>
          timelineService.fetchProfileNumbers(requestedUser.id) map{ numbers =>
            val carrier: UserShowCarrier = UserShowCarrier(currentUser, requestedUser, firstPartForm, numbers, tankas, isMyself, isFollowing)
            Some(carrier)
          }
        }
      }

      case _ => {
        Future(Option.empty[UserShowCarrier])
      }
    }
  }

  def fetchTankasForUserPage(userId: Long): Future[Seq[((String, Tables.FirstPartRow), Seq[(String, Tables.LastPartRow)])]] = {
    firstPartRepo.findByUserId(userId).map{ firstParts =>
      firstParts map{ firstPart =>
        ((Await.result(userRepo.findById(firstPart.userId), Duration.Inf).get.username, firstPart), Await.result(lastPartRepo.findByFirstPartId(firstPart.id), Duration.Inf).map(lastPartRow => (Await.result(userRepo.findById(lastPartRow.userId), Duration.Inf).get.username, lastPartRow)))
      }
    }
  }

}
