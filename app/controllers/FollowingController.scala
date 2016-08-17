package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Tables
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import repositories.FirstPartRepo
import services.{FollowingService, UserServiceLike}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/08.
  */
class FollowingController @Inject()(val followingService: FollowingService, firstPartRepo: FirstPartRepo, val userServiceLike: UserServiceLike, val messagesApi: MessagesApi) extends Controller with I18nSupport with OptionalAuthElement with AuthConfigImpl{

  // TODO これ他のユーザーのフォロー状況見れるように,username引数に取ろう
  def followingIndex(userId: Long) = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => {
        val followings: Future[Seq[(Tables.UserRow, (Int, Int, Int))]] = followingService.fetchFollowings(userId)
        // TODO follwingとfollowerでテンプレートを共通化できないか検討する
        followings.map(followings => Ok(views.html.following.followingIndex(user.id, userId, followings)))
      }
      case _ => Future(Redirect(routes.LoginController.brandNew).flashing("error" -> "Goodbye bad boy..."))
    }
  }

  // TODO これ他のユーザーのフォロー状況見れるように,username引数に取ろう
  def followerIndex(userId: Long) = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => {
        val followers: Future[Seq[(Tables.UserRow, (Int, Int, Int))]] = followingService.fetchFollowers(userId)
        followers map(followers => Ok(views.html.following.followerIndex(user.id, userId, followers)))
      }
    }
  }

  def create(followingUserId: Long) = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => {
        val followingRow: Future[Tables.FollowingRow] = followingService.follow(user.id, followingUserId)
        followingRow.map(_ => Redirect(routes.TimelineController.show).flashing("success" -> "フォローしました"))
      }
      case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "Your request is not right."))
    }
  }

  def delete(followingUserId: Long) = AsyncStack{ implicit  rs =>
    loggedIn match{
      case Some(user) =>{
        followingService.unfollow(user.id, followingUserId).map(_ => Redirect(routes.TimelineController.show).flashing("success" -> "フォローを解除しました"))
      }
      case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "Your request is not right."))
    }
  }

  def block(userId: Long) = AsyncStack{ implicit  rs =>
    // TODO フラグ立てしてブロック機能を実装できるようにしたい
    loggedIn match{
      case Some(user) =>{
        followingService.unfollow(userId, user.id).map(_ => Redirect(routes.TimelineController.show).flashing("success" -> "ユーザーのフォローをブロックしました"))
      }
      case _ => Future(Redirect(routes.TimelineController.show).flashing("error" -> "Your request is not right."))
    }
  }


}
