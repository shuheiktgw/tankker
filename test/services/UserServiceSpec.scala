package services

import models.Tables
import org.specs2.mock._
import org.specs2.mutable._
import play.api.db.slick.DatabaseConfigProvider
import repositories.{FollowingRepo, TimelineRepo, UserRepo}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Shuhei on 2016/09/03.
  */
class UserServiceSpec extends Specification with Mockito {

  "UserService" should{
    val mockDbConf = mock[DatabaseConfigProvider]
    val mockUserRepo = mock[UserRepo]
    val mockTimelineRepo = mock[TimelineRepo]
    val mockFollowingRepo = mock[FollowingRepo]

    "UserServiceSpec#add"in{
      "return Future[Option[Tables.UserRow]] if userExists returns true" in{
        //Arrange
        val mockUserService = new UserService(mockDbConf, mockUserRepo, mockTimelineRepo, mockFollowingRepo){
          override def userExists(user: Tables.UserRow) = Future(true)
        }

        //Execute
        val actual =  Await.result(mockUserService.add(any),Duration.Inf)

        //Evaluate
        actual mustEqual None
      }

      "return Future[Option[Tables.UserRow]] if userExists returns true" in{

      }
    }
  }

}
