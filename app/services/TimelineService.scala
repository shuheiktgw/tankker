package services

import com.google.inject.Inject
import models.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.Tables.{FirstPart, LastPart, User}
import repositories.{FirstPartRepo, FollowingRepo, LastPartRepo}
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shuhei on 2016/08/07.
  */
class TimelineService @Inject()(val followingRepo: FollowingRepo, val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo) {

  //TODO 動作確認したら消してOK 既にUserServiceに移動済み
  def fetchTankaForTL(userId: Long): Future[Seq[(Tables.FirstPartRow, Seq[Tables.LastPartRow])]] = {
    firstPartRepo.findByUserId(userId).map{ firstParts =>
      firstParts map{ firstPart =>
        (firstPart, Await.result(lastPartRepo.findByFirstPartId(firstPart.id), Duration.Inf))
      }
    }
  }

  // TODO ここは後で直す
  def fetchTankasForTimeline(userId: Long): Future[Seq[(Tables.FirstPartRow, Seq[Tables.LastPartRow])]] = {
    val userIds: Future[Seq[Int]] = followingRepo.fetchFollowingUserIds(userId)
    userIds flatMap {ids =>
      val userIdsForTl: Seq[Int] = userId.toInt +: ids
      firstPartRepo.findByUserIds(userIdsForTl).map { firstParts =>
        firstParts map { firstPart =>
          (firstPart, Await.result(lastPartRepo.findByFirstPartId(firstPart.id), Duration.Inf))
        }
      }
    }
  }
}
