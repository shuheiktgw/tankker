package services

import com.google.inject.{ImplementedBy, Inject}
import controllers.FirstPartController._
import controllers.routes
import models.Tables
import models.Tables.{LastPartRow, UserRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import com.github.t3hnar.bcrypt._
import controllers.LoginController.LoginForm

import scala.concurrent.duration.Duration
import repositories.{FirstPartRepo, FollowingRepo, LastPartRepo, UserRepo, TimelineRepo}
import slick.driver.JdbcProfile
import views.models.UserShowCarrier

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class UserService @Inject()(val dbConfigProvider: DatabaseConfigProvider, val userRepo: UserRepo, val timelineRepo: TimelineRepo, val followingRepo: FollowingRepo) extends HasDatabaseConfigProvider[JdbcProfile] with UserServiceLike {

  def getAll: Future[Seq[Tables.UserRow]] = {
    db.run(userRepo.all())
  }

  def findById(id: Long): Future[Option[Tables.UserRow]] = {
    db.run(userRepo.findById(id))
  }

  def findByEmail(email: String): Future[Option[Tables.UserRow]] = {
    db.run(userRepo.findByEmail(email))
  }

  def findByUsername(username: String): Future[Option[Tables.UserRow]] = {
    db.run(userRepo.findByUsername(username))
  }

  def add(user: Tables.UserRow): Future[Option[Tables.UserRow]] = {
    userExists(user) flatMap {
      case true => Future(Option.empty[Tables.UserRow])
      case _ => {
        val encryptedUser: UserRow = user.copy(password = user.password.bcrypt)
        db.run(userRepo.add(encryptedUser)).map(_ => Option(user))
      }
    }
  }

  def userExists(user: Tables.UserRow): Future[Boolean] = {
    val futureResult: Future[Option[Tables.UserRow]] = db.run(userRepo.userExists(user))
    futureResult.map(_.isDefined)
  }

  def change(user: Tables.UserRow, hasPasswordChanged: Boolean = false): Future[Option[Tables.UserRow]] = {
    userExists(user) flatMap {
      case true => Future(Option.empty[Tables.UserRow])
      case _ => {
        if(hasPasswordChanged) {
          val encryptedUser: UserRow = user.copy(password = user.password.bcrypt)
          db.run(userRepo.change(encryptedUser)).map(_ => Option(encryptedUser))
        }else{
          findById(user.id).flatMap{ fetchedUser =>
            val encryptedUser: UserRow = user.copy(password = fetchedUser.get.password)
            db.run(userRepo.change(encryptedUser)).map(_ => Option(encryptedUser))
          }
        }
      }
    }
  }

  def remove(id: Long): Future[Option[Tables.UserRow]] = {
    findById(id).flatMap {
      case Some(user) => {
        val deletedUser: Tables.UserRow = user.copy(deleted = true)
        change(deletedUser)
      }
      case _ => Future(Option.empty[Tables.UserRow])
    }
  }

  def authenticate(form: LoginForm): Future[Option[Tables.UserRow]] = {
    findByUsername(form.username).map {
      case Some(user) => if (form.password.isBcrypted(user.password)) Some(user) else Option.empty[Tables.UserRow]
      case None => Option.empty[Tables.UserRow]
    }
  }

  def fetchTankasForUserPage(requestedUserId: Long): Future[Seq[((Option[Tables.FirstPartRow], Tables.UserRow), Seq[(Option[Tables.LastPartRow], Option[Tables.UserRow])])]] = {
    db.run(userRepo.fetchTankasForUserpage(requestedUserId)).map{ pairs =>
      val groupedPairs: Map[(Option[Tables.FirstPartRow], Tables.UserRow), Seq[((Option[Tables.FirstPartRow], Tables.UserRow), Option[Tables.LastPartRow], Option[Tables.UserRow])]] = pairs.groupBy(_._1)
      val groupedMaps: Map[(Option[Tables.FirstPartRow], Tables.UserRow), Seq[(Option[Tables.LastPartRow], Option[Tables.UserRow])]] = groupedPairs.mapValues{_.map{case((firstPart, firstUser), lastPart, lastUser) => (lastPart, lastUser)}}
      groupedMaps.toSeq.sortWith((a, b) => {
        if (a._1._1.isDefined && b._1._1.isDefined) {
          a._1._1.get.createdAt.after(b._1._1.get.createdAt)
        } else {
          false
        }
      })
    }
  }

  def fetchUserData(currentUser: Tables.UserRow, username: String): Future[Option[UserShowCarrier]] = {
    val futureRequestedUser: Future[Option[Tables.UserRow]] = findByUsername(username)

    futureRequestedUser flatMap {
      case Some(requestedUser) => {
        val isMyself: Boolean = requestedUser.id == currentUser.id
        val futureIsFollowing: Future[Boolean] = db.run(followingRepo.findByUserIdAndFollowingUserIdForUserService(currentUser.id, requestedUser.id)).map(_.isDefined)

        val tweetsCount: Future[Int] = db.run(timelineRepo.countTweetNumbers(requestedUser.id))
        val followingsCount: Future[Int] = db.run(timelineRepo.countFollowings(requestedUser.id))
        val followersCount: Future[Int] = db.run(timelineRepo.countFollowers(requestedUser.id))

        val profileNumbers: Future[(Int, Int, Int)] = tweetsCount.flatMap(t => followingsCount.flatMap(fi => followersCount.map(fw => (t, fi, fw))))

        fetchTankasForUserPage(requestedUser.id) flatMap { tankas =>
          profileNumbers flatMap { numbers =>
            futureIsFollowing map { isFollowing =>
              val carrier: UserShowCarrier = UserShowCarrier(currentUser, requestedUser, firstPartForm, numbers, tankas, isMyself, isFollowing)
              Some(carrier)
            }
          }
        }
      }

      case _ => {
        Future(Option.empty[UserShowCarrier])
      }
    }
  }
}

@ImplementedBy(classOf[UserService])
trait UserServiceLike {
  def findByUsername(username: String): Future[Option[Tables.UserRow]]
  def authenticate(form: LoginForm): Future[Option[Tables.UserRow]]
}
