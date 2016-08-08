package models

import com.google.inject.Inject
import models.Tables.FirstPart
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/04.
  */
class FirstPartRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val FirstPart = TableQuery[FirstPart]

  def findByUserId(userId: Long): Future[Seq[Tables.FirstPartRow]] ={
    db.run(FirstPart.filter(_.userId === userId.toInt).sortBy(_.createdAt.desc).result)
  }

  def findByID(id: Long): Future[Option[Tables.FirstPartRow]] = {
    db.run(FirstPart.filter(t => t.id === id.toInt).result.headOption)
  }

  def add(firstPart: Tables.FirstPartRow): Future[Option[Tables.FirstPartRow]] = {
    db.run(FirstPart += firstPart).map(_ => Option(firstPart))
  }

  def change(firstPart: Tables.FirstPartRow): Future[Option[Tables.FirstPartRow]] = {
    db.run(FirstPart.filter(t => t.id === firstPart.id.bind).update(firstPart)).map(_ => Option(firstPart))
  }

  def remove(id: Long): Future[Option[Tables.FirstPartRow]] = {
    findByID(id).flatMap{
      case Some(firstPart) => db.run(FirstPart.filter(t => t.id === firstPart.id.bind).delete).map(_ => Some(firstPart))
      case _ => Future(Option.empty[Tables.FirstPartRow])
    }
  }
}
