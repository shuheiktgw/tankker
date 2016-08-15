package services

import com.google.inject.Inject
import models.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.Tables.{FirstPart, LastPart, User}
import repositories.{FirstPartRepo, FollowingRepo, LastPartRepo, TimelineRepo}
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shuhei on 2016/08/07.
  */
class TimelineService @Inject()(val timelineRepo: TimelineRepo, followingRepo: FollowingRepo, val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo) {

  //TODO 動作確認したら消してOK 既にUserServiceに移動済み


  // TODO ここは後で直す
  def fetchTankasForTimeline(userId: Long): Future[Seq[(Tables.FirstPartRow, Seq[Tables.LastPartRow])]] = {
    val userIds: Future[Seq[Int]] = followingRepo.fetchFollowingUserIds(userId)
    userIds flatMap { ids =>
      val userIdsForTl: Seq[Int] = userId.toInt +: ids
      firstPartRepo.findByUserIds(userIdsForTl).map { firstParts =>
        firstParts map { firstPart =>
          (firstPart, Await.result(lastPartRepo.findByFirstPartId(firstPart.id), Duration.Inf))
        }
      }
    }
  }

  def fetchProfileNumbers(userId: Long): Future[(Int, Int, Int)] = {
    val futureNumbers: (Future[Int], Future[Int], Future[Int]) = timelineRepo.fetchProfileNumbers(userId)

    futureNumbers._1 flatMap { tweet =>
      futureNumbers._2 flatMap { following =>
        futureNumbers._3 map { followed =>
          (tweet, following, followed)
        }
      }
    }
  }
}
