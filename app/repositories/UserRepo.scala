package repositories

import com.github.t3hnar.bcrypt._
import com.google.inject.{ImplementedBy, Inject}
import controllers.LoginController.LoginForm
import models.Tables
import models.Tables.{User, UserRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by shuhei.kitagawa on 2016/08/02.
  */

class UserRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] with UserRepoLike{

  val User = TableQuery[User]

  def all(): Future[Seq[Tables.UserRow]] = {
    db.run(User.filter(_.deleted === false).result)
  }

  def get(id: Long): Future[Option[Tables.UserRow]] = {
    db.run(User.filter(t => t.id === id.toInt).result.headOption)
  }

  def add(user: Tables.UserRow): Future[Option[Tables.UserRow]] = {
    //TODO ここのif文ダサいから後でリファクタリング
    if(userExists(user)){
      Future(Option.empty[Tables.UserRow])
    }else{
      // done viewに暗号化したパスワードを出ないように設定
      val encryptedUser: UserRow = user.copy(password = user.password.bcrypt)
      db.run(User += encryptedUser).map(_ => Option(user))
    }
  }

  def change(user: Tables.UserRow): Future[Option[Tables.UserRow]] = {
    if(userExists(user)){
      Future(Option.empty[Tables.UserRow])
    }else{
      val encryptedUser: UserRow = user.copy(password = user.password.bcrypt)
      db.run(User.filter(t => t.id === user.id.bind).update(encryptedUser)).map{ _ =>
        //TODO ここOptionで囲う意味あるか検討する
        Option(encryptedUser)
      }
    }
  }

  def remove(id: Long): Future[Option[Tables.UserRow]] = {
      get(id).map{
        case Some(user) => {
          val deletedUser: Tables.UserRow = user.copy(deleted = true)
          Await.result(change(deletedUser), Duration.Inf)
        }
        case _ => Option.empty[Tables.UserRow]
      }
  }


//  def authenticate(username: String, password: String): Option[Tables.UserRow] = {
//    val result: Future[Option[Tables.UserRow]] = findByUsername(username).map{
//      case Some(user) => if(password.isBcrypted(user.password)) Some(user) else Option.empty[Tables.UserRow]
//      case None => Option.empty[Tables.UserRow]
//    }
//
//    // TODO Awaitしない処理ないか確認
//    Await.result(result, Duration.Inf)
//  }

  def authenticate(form: LoginForm): Future[Option[Tables.UserRow]] = {
    findByUsername(form.username).map{
      case Some(user) => if(form.password.isBcrypted(user.password)) Some(user) else Option.empty[Tables.UserRow]
      case None => Option.empty[Tables.UserRow]
    }
  }

  def findByEmail(email: String): Future[Option[Tables.UserRow]] ={
    db.run(User.filter(t => t.email === email && t.deleted === false).result.headOption)
  }

  def findByUsername(username: String): Future[Option[Tables.UserRow]] ={
    db.run(User.filter(t => t.username === username && t.deleted === false).result.headOption)
  }

  def findById(id: Long): Future[Option[Tables.UserRow]] ={
    db.run(User.filter(t => t.id === id.toInt && t.deleted === false).result.headOption)
  }

  def userExists(user: Tables.UserRow): Boolean ={
    // TODO ここも本当は非同期のまま処理したいけど一旦保留
    val futureResult: Future[Option[Tables.UserRow]] = db.run(User.filter(row => ((row.email === user.email) || (row.username === user.username)) && !(row.id === user.id)).result.headOption)
    val result = Await.result(futureResult, Duration.Inf)
    result.isDefined
  }
}

@ImplementedBy(classOf[UserRepo])
trait UserRepoLike{
  def findByUsername(username: String): Future[Option[Tables.UserRow]]
  def authenticate(form: LoginForm): Future[Option[Tables.UserRow]]
}