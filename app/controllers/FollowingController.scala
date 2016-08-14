package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Tables
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import repositories.{FirstPartRepo, UserRepoLike}
import services.FollowingService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/08.
  */
class FollowingController @Inject()(val followingService: FollowingService, firstPartRepo: FirstPartRepo,val userRepoLike: UserRepoLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with OptionalAuthElement with AuthConfigImpl{

  def followingIndex = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => {
        val followings: Future[Seq[Tables.UserRow]] = followingService.fetchFollowings(user.id)
        // TODO follwingとfollowerでテンプレートを共通化できないか検討する
        followings.map(followings => Ok(views.html.following.followingIndex(user.id, followings)))
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }

  def followerIndex = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => {
        val followers: Future[Seq[Tables.UserRow]] = followingService.fetchFollowers(user.id)
        followers map(followers => Ok(views.html.following.followerIndex(user.id, followers)))
      }
    }
  }

  def create(followingUserId: Long) = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => {
        val followingRow: Future[Tables.FollowingRow] = followingService.follow(user.id, followingUserId)
        followingRow.map(_ => Redirect(routes.TimelineController.show).flashing("success" -> "You've successfully followed new user"))
      }
      case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "Your request is not right."))
    }
  }

  def delete(followingUserId: Long) = AsyncStack{ implicit  rs =>
    loggedIn match{
      case Some(user) =>{
        followingService.unfollow(user.id, followingUserId).map(_ => Redirect(routes.FollowingController.followingIndex).flashing("success" -> "You've successfully unfollowed the user"))
      }
      case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "Your request is not right."))
    }
  }

  def block(userId: Long) = AsyncStack{ implicit  rs =>
    // TODO フラグ立てしてブロック機能を実装できるようにしたい
    loggedIn match{
      case Some(user) =>{
        followingService.unfollow(userId, user.id).map(_ => Redirect(routes.FollowingController.followerIndex).flashing("success" -> "You've successfully unfollowed the user"))
      }
      case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "Your request is not right."))
    }
  }


}
