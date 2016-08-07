package models

import com.google.inject.Inject
import models.Tables.LastPart
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future

/**
  * Created by Shuhei on 2016/08/07.
  */
class LastPartRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]{

  val LastPart = TableQuery[LastPart]

  def findByUserId(userId: Long): Future[Seq[Tables.LastPartRow]] = {
    db.run(LastPart.filter(_.userId === userId.toInt).sortBy(_.createdAt.desc).result)
  }

  def findByFirstPartId(firstPartId: Long): Future[Seq[Tables.LastPartRow]] = {
    db.run(LastPart.filter(_.firstPartId === firstPartId.toInt).sortBy(_.createdAt.desc).result)
  }

  def findById(id: Long): Future[Option[Tables.LastPartRow]] = {
    db.run(LastPart.filter(_.id === id.toInt).result.headOption)
  }

  def reply(lastPart: Tables.LastPartRow): Future[Option[Tables.LastPartRow]] = {
    db.run(LastPart += lastPart).map(_ => Option(lastPart))
  }

  def change(lastPart: Tables.LastPartRow): Future[Option[Tables.LastPartRow]] = {
    db.run(LastPart.filter(t => t.id === lastPart.id.bind).update(lastPart)).map(_ => Option(lastPart))
  }

  def remove(id: Long): Future[Option[Tables.LastPartRow]] = {
    findById(id).flatMap{
      case Some(lastPart) => db.run(LastPart.filter(_.id === lastPart.id.bind).delete).map(_ => Some(lastPart))
      case None => Future(Option.empty[Tables.LastPartRow])
    }
  }

}
