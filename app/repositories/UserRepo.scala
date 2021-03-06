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
    User
      .filter(_.id === requestedUserId.toInt)
      .joinLeft(FirstPart)
      .on { case (firstUser, firstPart) => firstUser.id === firstPart.userId }
      .joinLeft(LastPart)
      .on { case ((firstUser, firstPart), lastPart) => firstPart.map(_.id === lastPart.firstPartId) }
      .joinLeft(User)
      .on { case (((firstUser, firstPart), lastPart), lastUser) => lastPart.map(_.userId === lastUser.id) }
      .map { case (((firstUser, firstPart), lastPart), lastUser) => ((firstPart, firstUser), lastPart, lastUser) }
      .sortBy{case ((firstPart, firstUser), lastPart, lastUser) => firstPart.map(_.createdAt).desc}
      .result.map(_.take(30))
  }

  def fetchHenkasForUserpage(requestedUserId: Long):DBIO[Seq[(Tables.UserRow, Tables.FirstPartRow, Tables.LastPartRow)]] = {
    LastPart
      .filter(_.userId === requestedUserId.toInt)
      .join(FirstPart)
      .on{case(lastPart,firstPart) => lastPart.firstPartId === firstPart.id}
      .join(User)
      .on{case((lastPart,firstPart), user) => firstPart.userId === user.id}
      .sortBy{case((lastPart,firstPart), user) => lastPart.createdAt.desc}
      .map{case((lastPart,firstPart), user) => (user, firstPart, lastPart)}
      .sortBy{case (user, firstPart, lastPart) => firstPart.createdAt.desc}
      .result.map(_.take(30))
  }

  def fetchUnfollowingUsers(userId: Long): DBIO[Seq[Tables.UserRow]] = {
    Following
      .filter(_.userId === userId.toInt)
      .joinRight(User)
      .on{case(following, user) => following.followingUserId === user.id}
      .filter{case(following, user) => following.isEmpty}
      .filterNot{case(following, user) => user.id === userId.toInt}
      .sortBy{case(following, user) => user.createdAt.desc}
      .map{case(following, user) => user}
      .result.map(_.take(5))
  }
}