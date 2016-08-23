package services

import com.google.inject.Inject
import models.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.Tables.{FirstPart, LastPart, User}
import repositories._
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.collection.immutable.ListMap
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shuhei on 2016/08/07.
  */
class TimelineService @Inject()(val dbConfigProvider: DatabaseConfigProvider, val timelineRepo: TimelineRepo, followingRepo: FollowingRepo, val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo, val userService: UserService) extends HasDatabaseConfigProvider[JdbcProfile] {

  def fetchTankasForTL(userId: Long): Future[Seq[((Option[Tables.FirstPartRow], Tables.UserRow), Seq[(Option[Tables.LastPartRow], Option[Tables.UserRow])])]] = {
    val followingsTankas = db.run(timelineRepo.fetchTweetForTimeline(userId)).map{ pairs =>
      val groupedPairs: Map[(Option[Tables.FirstPartRow], Tables.UserRow), Seq[((Option[Tables.FirstPartRow], Tables.UserRow), Option[Tables.LastPartRow], Option[Tables.UserRow])]] = pairs.groupBy(_._1)
      val groupedMaps: Map[(Option[Tables.FirstPartRow], Tables.UserRow), Seq[(Option[Tables.LastPartRow], Option[Tables.UserRow])]] = groupedPairs.mapValues{_.map{case((firstPart, firstUser), lastPart, lastUser) => (lastPart, lastUser)}}
      groupedMaps.toSeq
    }

    val myTankas = userService.fetchTankasForUserPage(userId)


    followingsTankas.flatMap { following =>
      myTankas.map{ mine =>
        (following ++ mine).sortWith{case (((firstPart1, firstUser1), lastParts1), ((firstPart2, firstUser2), lastParts2)) => {
          if (firstPart1.isDefined && firstPart2.isDefined) {
            firstPart1.get.createdAt.after(firstPart2.get.createdAt)
          } else {
            true
          }
        }}
      }
    }
  }

  def fetchProfileNumbers(userId: Long): Future[(Int, Int, Int)] = {
    val tweetsCount: Future[Int] = db.run(timelineRepo.countTweetNumbers(userId))
    val followingsCount: Future[Int] = db.run(timelineRepo.countFollowings(userId))
    val followersCount: Future[Int] = db.run(timelineRepo.countFollowers(userId))

    tweetsCount flatMap { tweet =>
      followingsCount flatMap { following =>
        followersCount map { followed =>
          (tweet, following, followed)
        }
      }
    }
  }
}
