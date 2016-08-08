package services

import com.google.inject.Inject
import models.{FirstPartRepo, LastPartRepo, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.Tables.{FirstPart, LastPart, User}
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shuhei on 2016/08/07.
  */
class TimelineService @Inject()(val firstPartRepo: FirstPartRepo, val lastPartRepo: LastPartRepo) {

  def fetchTankaForTL(userId: Long): Future[Seq[(Tables.FirstPartRow, Seq[Tables.LastPartRow])]] = {
    firstPartRepo.findByUserId(userId).map{ firstParts =>
      firstParts map{ firstPart =>
        (firstPart, Await.result(lastPartRepo.findByFirstPartId(firstPart.id), Duration.Inf))
      }
    }
  }
}
