package repositories

import java.sql.Timestamp

import com.google.inject.Inject
import models.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import models.Tables.{Following, FollowingRow, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by shuhei.kitagawa on 2016/08/08.
  */
class FollowingRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val Following = TableQuery[Following]
  val User = TableQuery[User]

  def fetchFollowings(userId: Long): Future[Seq[Tables.UserRow]] = {
    val followingRows: Future[Seq[Tables.FollowingRow]] = db.run(Following.filter(_.userId === userId.toInt).sortBy(_.createdAt.desc).result)
    val followingUserIds: Future[Seq[Int]] = followingRows.map(_.map(_.followingUserId))
    followingUserIds flatMap (ids => db.run(User.filter(_.id inSetBind (ids)).result))
  }

  def fetchFollowingUserIds(userId: Long): Future[Seq[Int]] = {
    fetchFollowings(userId).map(_.map(_.id))
  }

  def fetchFollowers(userId: Long): Future[Seq[Tables.UserRow]] = {
    val followingRows: Future[Seq[Tables.FollowingRow]] = db.run(Following.filter(_.followingUserId === userId.toInt).sortBy(_.createdAt.desc).result)
    val followersUserIds: Future[Seq[Int]] = followingRows.map(_.map(_.userId))
    followersUserIds flatMap (ids => db.run(User.filter(_.id inSetBind (ids)).result))
  }

  def add(userId: Long, followingUserId: Long): Future[FollowingRow] = {
    val followingRow: FollowingRow = FollowingRow(0, userId.toInt, followingUserId.toInt, new Timestamp(System.currentTimeMillis()))
    db.run(Following += followingRow).map(_ => followingRow)
  }

  def remove(userId: Long, followingUserId: Long): Future[Option[Tables.FollowingRow]] = {
    findByUserIdAndFollowingUserId(userId, followingUserId) flatMap {
      case Some(followingRow) => db.run(Following.filter(_.id === followingRow.id).delete).map(_ => Some(followingRow))
      case _ => Future(Option.empty[Tables.FollowingRow])
    }
  }

  def findByUserIdAndFollowingUserId(userId: Long, followingUserId: Long) : Future[Option[FollowingRow]] = {
    db.run(Following.filter(row => row.userId === userId.toInt && row.followingUserId === followingUserId.toInt).result.headOption)
  }

  def findById(followingId: Long): Future[Option[FollowingRow]] = {
    db.run(Following.filter(_.id === followingId.toInt).result.headOption)
  }
}
