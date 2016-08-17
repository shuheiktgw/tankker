package repositories

import com.github.t3hnar.bcrypt._
import com.google.inject.{ImplementedBy, Inject}
import controllers.LoginController.LoginForm
import models.Tables
import models.Tables.{User, Following, FirstPart, LastPart}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.{JdbcProfile, MySQLDriver}
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by shuhei.kitagawa on 2016/08/02.
  */

class UserRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val User = TableQuery[User]
  val FirstPart = TableQuery[FirstPart]
  val LastPart = TableQuery[LastPart]

  def all() = {
    User.filter(_.deleted === false).result
  }

  def findById(id: Long) = {
    User.filter(t => t.id === id.toInt && t.deleted === false).result.headOption
  }

  def findByEmail(email: String) = {
    User.filter(t => t.email === email && t.deleted === false).result.headOption
  }

  def findByUsername(username: String):DBIO[Option[Tables.UserRow]] = {
    User.filter(t => t.username === username && t.deleted === false).result.headOption
  }

  def add(user: Tables.UserRow): DBIO[Int] = {
    User += user
  }

  def change(user: Tables.UserRow) = {
    User.filter(t => t.id === user.id.bind).update(user)
  }

  def userExists(user: Tables.UserRow) = {
    User.filter(row => ((row.email === user.email) || (row.username === user.username)) && !(row.id === user.id)).result.headOption
  }

  def fetchTankasForUserpage(requestedUserId: Long) = {
    FirstPart
      .filter(_.userId === requestedUserId.toInt)
      .joinLeft(LastPart)
        .on { case (f,l) => f.id === l.firstPartId }
      .join(User)
        .on { case ((f, l), u) => l.map(_.userId === u.id ) }
      .map{ case ((f, l), u) => (f, l, u) }
      .result
  }
}