package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.Controller
import services.{UserService, UserServiceLike}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by shuhei.kitagawa on 2016/08/20.
  */
class TinderController @Inject()(val userService: UserService, val userServiceLike: UserServiceLike, val ws: WSClient, val messagesApi: MessagesApi) extends Controller with I18nSupport with LoginLogout with OptionalAuthElement with AuthConfigImpl{

  import controllers.TinderController._

  def authenticate = AsyncStack{ implicit rs =>
    loggedIn match{
      case Some(user) => Future(Ok(views.html.tinder.authentication(tinderAuthForm)))
      case None => Future(Redirect(routes.LoginController.brandNew))
    }
  }

  def swipe = AsyncStack{ implicit rs =>
    tinderAuthForm.bindFromRequest.fold(
      error => Future(Redirect(routes.TinderController.authenticate).flashing("error" -> "入力された値の形式が間違っています")),

      form =>{
        sendRequest(form.facebookId, form.authToken).map{ response =>
          (response.json \ "result").asOpt[Int]  match {
            case Some(number) => Redirect(routes.TimelineController.show).flashing("success" -> s"${number.toString}人をスワイプしました")
            case None => Redirect(routes.TinderController.authenticate).flashing("error" -> "Authenticationに失敗しました. ID, Tokenが正しいか確認してください")
          }
        }
      }
    )
  }


  private val URL = "https://qi85ppbkj2.execute-api.ap-northeast-1.amazonaws.com/beta/autoswipe"

  private def sendRequest(fbid: String, fbtoken: String):Future[WSResponse] = {

    val authInfo: JsObject = Json.obj(
      "fbid" -> fbid,
      "fbtoken" -> fbtoken
    )

    ws.url(URL).withHeaders(
      "x-api-key" -> "WBiq7TBqR185ueFSHjdzJ4u6pTy3Zjrk1K6CjV96",
      "Content-type" -> "application/json"
    ).post(authInfo)
  }
}

object TinderController{
  case class TinderAuthForm(facebookId: String, authToken: String)

  val tinderAuthForm = Form(
    mapping(
      "facebookId" -> nonEmptyText(),
      "authToken" -> nonEmptyText()
    )(TinderAuthForm.apply)(TinderAuthForm.unapply)
  )
}