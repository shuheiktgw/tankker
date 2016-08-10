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
class UserService @Inject()(val userRepo: UserRepo, val followingRepo: FollowingRepo, val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo) {

  def showUserData(currentUser: Tables.UserRow, username: String): Future[Option[UserShowCarrier]] = {
    val futureRequestedUser: Future[Option[Tables.UserRow]] = userRepo.findByUsername(username)

    futureRequestedUser flatMap {
      case Some(requestedUser) => {
        val isMyself: Boolean = requestedUser.id == currentUser.id
        val isFollowing: Boolean = Await.result(followingRepo.findByUserIdAndFollowingUserId(currentUser.id, requestedUser.id), Duration.Inf).isDefined

        fetchTankasForUserPage(requestedUser.id) map { tankas =>
          val carrier: UserShowCarrier = UserShowCarrier(currentUser, requestedUser, firstPartForm, tankas, isMyself, isFollowing)
          Some(carrier)
        }
      }

      case _ => {
        Future(Option.empty[UserShowCarrier])
      }
    }
  }

  def fetchTankasForUserPage(userId: Long): Future[Seq[(Tables.FirstPartRow, Seq[Tables.LastPartRow])]] = {
    firstPartRepo.findByUserId(userId).map{ firstParts =>
      firstParts map{ firstPart =>
        (firstPart, Await.result(lastPartRepo.findByFirstPartId(firstPart.id), Duration.Inf))
      }
    }
  }

}
