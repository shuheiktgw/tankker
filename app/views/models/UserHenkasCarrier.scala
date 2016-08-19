package views.models

/**
  * Created by shuhei.kitagawa on 2016/08/19.
  */
import controllers.FirstPartController.{FirstPartForm, firstPartForm}
import models.Tables
import play.api.data.Form

import scala.concurrent.Future
/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
case class UserHenkasCarrier(currentUser: Tables.UserRow, requestedUser: Tables.UserRow, firstPartForm: Form[FirstPartForm], userNumbers:(Int, Int, Int), henkas: Seq[(Tables.UserRow, Tables.FirstPartRow, Tables.LastPartRow)], isMyself: Boolean, isFollowing: Boolean, unfollowingUsers: Seq[Tables.UserRow])
