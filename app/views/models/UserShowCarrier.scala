package views.models

import controllers.FirstPartController.{FirstPartForm, firstPartForm}
import models.Tables
import play.api.data.Form

import scala.concurrent.Future
/**
  * Created by shuhei.kitagawa on 2016/08/09.
  */
case class UserShowCarrier(currentUser: Tables.UserRow, requestedUser: Tables.UserRow, firstPartForm: Form[FirstPartForm], tankas: Seq[(Tables.FirstPartRow, Seq[Tables.LastPartRow])], isMyself: Boolean, isFollowing: Boolean)
