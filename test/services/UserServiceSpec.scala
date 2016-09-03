package services

import java.sql.Timestamp

import models.Tables
import org.specs2.mock._
import org.specs2.mutable._
import play.api.db.slick.DatabaseConfigProvider
import repositories.{FollowingRepo, TimelineRepo, UserRepo}
import models.Tables.UserRow

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Shuhei on 2016/09/03.
  */
class UserServiceSpec extends Specification with Mockito {

  val mockDbConf = mock[DatabaseConfigProvider]
  val mockUserRepo = mock[UserRepo]
  val mockTimelineRepo = mock[TimelineRepo]
  val mockFollowingRepo = mock[FollowingRepo]
  val dummyUser = UserRow(
    1,
    "test",
    "test@test.com",
    "password",
    false,
    new Timestamp(System.currentTimeMillis()),
    new Timestamp(System.currentTimeMillis())
  )


  "UserService#add" should {
    "return Future[None] if userExists returns true" in {
      //Arrange
      val mockUserService = new UserService(mockDbConf, mockUserRepo, mockTimelineRepo, mockFollowingRepo) {
        override def userExists(user: Tables.UserRow) = Future(true)
      }

      //Execute
      val actual = Await.result(mockUserService.add(any), Duration.Inf)

      //Evaluate
      actual mustEqual None
    }

    "return Future[Some[UserRow]] if userExists returns false" in {
      //Arrange
      val mockUserService = new UserService(mockDbConf, mockUserRepo, mockTimelineRepo, mockFollowingRepo) {
        override def userExists(user: Tables.UserRow) = Future(false)
        override def accessDbToInsert(user: Tables.UserRow) = Future(1)
      }

      //Execute
      val actual = Await.result(mockUserService.add(dummyUser), Duration.Inf)

      //Evaluate
      actual must haveClass[Some[UserRow]]
    }

    "return Future[Some[UserRow]] with a encrypted password if userExists returns false" in {
      //Arrange
      val mockUserService = new UserService(mockDbConf, mockUserRepo, mockTimelineRepo, mockFollowingRepo) {
        override def userExists(user: Tables.UserRow) = Future(false)
        override def accessDbToInsert(user: Tables.UserRow) = Future(1)
      }

      //Execute
      val actual = Await.result(mockUserService.add(dummyUser), Duration.Inf).get.password
      
      //Evaluate
      actual mustNotEqual dummyUser.password
    }
  }

}
