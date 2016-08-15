package services

import com.google.inject.Inject
import models.Tables
import repositories.FollowingRepo

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/08.
  */
class FollowingService @Inject()(val followingRepo: FollowingRepo, val timelineService: TimelineService){

  def fetchFollowings(userId: Long): Future[Seq[(Tables.UserRow, (Int, Int, Int))]] = {
    followingRepo.fetchFollowings(userId).map{users =>
    users map{user =>
      (user, Await.result(timelineService.fetchProfileNumbers(user.id), Duration.Inf))
    }
  }
  }

  def fetchFollowers(userId: Long): Future[Seq[(Tables.UserRow, (Int, Int, Int))]] = {
    followingRepo.fetchFollowers(userId) map{users =>
      users map{user =>
        (user, Await.result(timelineService.fetchProfileNumbers(user.id), Duration.Inf))
      }
    }
  }

  def follow(userId: Long, followingUserId: Long): Future[Tables.FollowingRow] = {
    followingRepo.add(userId, followingUserId)
  }

  def unfollow(userId: Long, followingUserId: Long): Future[Option[Tables.FollowingRow]] = {
    followingRepo.remove(userId, followingUserId)
  }
}
