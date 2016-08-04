package models

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import java.sql.Timestamp

import com.google.inject.{ImplementedBy, Inject}
import slick.profile.SqlProfile.ColumnOption.SqlType
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{Await, Future}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import models.Tables.{Users, UsersRow}
import com.github.t3hnar.bcrypt._

/**
  * Created by shuhei.kitagawa on 2016/08/02.
  */

class UsersRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] with UsersRepoLike{

  val users = TableQuery[Users]

  def all(): Future[Seq[Tables.UsersRow]] = {
    db.run(users.filter(_.deleted === false).result)
  }

  def get(id: Long): Future[Option[Tables.UsersRow]] = {
    db.run(users.filter(t => t.id === id.toInt).result.headOption)
  }

  def add(user: Tables.UsersRow): Future[Option[Tables.UsersRow]] = {
    //TODO ここのif文ダサいから後でリファクタリング
    if(userExists(user)){
      Future(Option.empty[Tables.UsersRow])
    }else{
      // TODO viewに暗号化したパスワードを出ないように設定
      val encryptedUser: UsersRow = user.copy(password = user.password.bcrypt)
      db.run(users += encryptedUser).map(_ => Option(user))
    }
  }

//    userExists(user).map {
//      case true => Option.empty[User]
//        // TODO ここエラーが出る可能性が高いので修正
//        // TODO Recoverでエラー処理する
//      case _ => db.run(users += user).flatMap((_: Int) => Option(user))
//    }


  def change(user: Tables.UsersRow): Future[Option[Tables.UsersRow]] = {
    if(userExists(user)){
      Future(Option.empty[Tables.UsersRow])
    }else{
      val encryptedUser: UsersRow = user.copy(password = user.password.bcrypt)
      db.run(users.filter(t => t.id === user.id.bind).update(encryptedUser)).map{ _ =>
        //TODO ここOptionで囲う意味あるか検討する
        Option(encryptedUser)
      }
    }
  }

  def remove(id: Long): Future[Option[Tables.UsersRow]] = {
      get(id).map{
        case Some(user) => {
          val deletedUser: Tables.UsersRow = user.copy(deleted = true)
          Await.result(change(deletedUser), Duration.Inf)
        }
        case _ => Option.empty[Tables.UsersRow]
      }
  }

  // done addのBycriptも忘れずに
  def authenticate(username: String, password: String): Option[Tables.UsersRow] = {
    val result: Future[Option[Tables.UsersRow]] = findByUsername(username).map{
      case Some(user) => if(password.isBcrypted(user.password)) Some(user) else Option.empty[Tables.UsersRow]
      case None => Option.empty[Tables.UsersRow]
    }

    // TODO Awaitしない処理ないか確認
    Await.result(result, Duration.Inf)
  }

  def findByEmail(email: String): Future[Option[Tables.UsersRow]] ={
    db.run(users.filter(t => t.email === email && t.deleted === false).result.headOption)
  }

  def findByUsername(username: String): Future[Option[Tables.UsersRow]] ={
    db.run(users.filter(t => t.username === username && t.deleted === false).result.headOption)
  }

  def findById(id: Long): Future[Option[Tables.UsersRow]] ={
    db.run(users.filter(t => t.id === id.toInt && t.deleted === false).result.headOption)
  }

  def userExists(user: Tables.UsersRow): Boolean ={
    // TODO ここも本当は非同期のまま処理したいけど一旦保留
    val futureResult: Future[Option[Tables.UsersRow]] = db.run(users.filter(row => ((row.email === user.email) || (row.username === user.username)) && !(row.id === user.id)).result.headOption)
    val result = Await.result(futureResult, Duration.Inf)
    result.isDefined
  }
}

@ImplementedBy(classOf[UsersRepo])
trait UsersRepoLike{
  def findByUsername(username: String): Future[Option[Tables.UsersRow]]
  def authenticate(username: String, password: String): Option[Tables.UsersRow]
}