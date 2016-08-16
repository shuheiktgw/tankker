package repositories

import com.google.inject.Inject
import models.Tables.{FirstPart, Following, LastPart, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class TimelineRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]{
  val Following = TableQuery[Following]
  val User = TableQuery[User]
  val FirstPart = TableQuery[FirstPart]
  val LastPart = TableQuery[LastPart]

  def countTweetNumbers(userId: Long): DBIO[Int] = {
    (FirstPart.filter(_.userId === userId.toInt).length + LastPart.filter(_.userId === userId.toInt).length).result
  }

  def countFollowings(userId: Long): DBIO[Int] = {
    Following.filter(_.userId === userId.toInt).length.result
  }

  def countFollowers(userId: Long): DBIO[Int] = {
    Following.filter(_.followingUserId === userId.toInt).length.result
  }

  def fetchTweetForTimeline(userId: Long) = {
    Following
      .filter(following => following.userId === userId.toInt)
      .join(User)
      .on{case (following, firstUser) => following.followingUserId === firstUser.id}
      .joinLeft(FirstPart)
      .on{case((following,firstUser),firstPart) => firstUser.id === firstPart.userId}
      .joinLeft(LastPart)
      .on{case(((following,firstUser), firstPart), lastPart) => firstPart.map(_.id === lastPart.firstPartId)}
      .join(User)
      .on{case((((following,firstUser), firstPart), lastPart), lastUser) => lastPart.map(_.userId === lastUser.id)}
      .map{case((((following,firstUser), firstPart), lastPart), lastUser) => ((firstPart, firstUser), lastPart, lastUser)}
      .result
  }



  def fetchProfileNumbers(userId: Long): (Future[Int], Future[Int], Future[Int]) ={
    val firstPartCount: Future[Int] = db.run(FirstPart.filter(_.userId === userId.toInt).length.result)
    val lastPartCount: Future[Int] = db.run(LastPart.filter(_.userId === userId.toInt).length.result)
    val tweetCount: Future[Int] = firstPartCount flatMap{ first =>
      lastPartCount map{second =>
        first + second
      }
    }


    val followingCount: Future[Int] = db.run(Following.filter(_.userId === userId.toInt).length.result)
    val followersCount: Future[Int] = db.run(Following.filter(_.followingUserId === userId.toInt).length.result)
    (tweetCount, followingCount,followersCount)
  }
}

