package repositories

import com.google.inject.Inject
import models.Tables.{Following, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
class TimelineRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]{
  val Following = TableQuery[Following]
  val User = TableQuery[User]

}
