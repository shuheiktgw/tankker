package services

import com.google.inject.Inject
import models.Tables
import repositories.FollowingRepo

import scala.concurrent.Future

/**
  * Created by shuhei.kitagawa on 2016/08/08.
  */
class FollowingService @Inject()(val followingRepo: FollowingRepo){

  def fetchFollowings(userId: Long): Future[Seq[Tables.UserRow]] = {
    followingRepo.fetchFollowings(userId)
  }

  def fetchFollowers(userId: Long): Future[Seq[Tables.UserRow]] = {
    followingRepo.fetchFollowers(userId)
  }

  def follow(userId: Long, followingUserId: Long): Future[Tables.FollowingRow] = {
    followingRepo.add(userId, followingUserId)
  }

  def unfollow(userId: Long, followingUserId: Long): Future[Option[Tables.FollowingRow]] = {
    followingRepo.remove(userId, followingUserId)
  }
}
