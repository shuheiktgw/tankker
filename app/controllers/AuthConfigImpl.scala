package controllers


import jp.t2v.lab.play2.auth.AuthConfig
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}
import models.{Tables, UserRepoLike}
//import jp.t2v.lab.play2.auth.sample.{Role, Account}
//import jp.t2v.lab.play2.auth.sample.Role._
import scala.reflect.{ClassTag, classTag}

/**
  * Created by shuhei.kitagawa on 2016/08/03.
  */
trait AuthConfigImpl extends AuthConfig{

  val userRepoLike: UserRepoLike

  // TODO Admin機能の実装
  type Authority = None.type

  /**
    * ユーザを識別するIDの型です。String や Int や Long などが使われるでしょう。
    */
  type Id = String

  /**
    * あなたのアプリケーションで認証するユーザを表す型です。
    * User型やAccount型など、アプリケーションに応じて設定してください。
    */
  type User = Tables.UserRow

  /**
    * 認可(権限チェック)を行う際に、アクション毎に設定するオブジェクトの型です。
    * このサンプルでは例として以下のような trait を使用しています。
    *
    * sealed trait Role
    * case object Administrator extends Role
    * case object NormalUser extends Role
    */

  /**
    * CacheからユーザIDを取り出すための ClassTag です。
    * 基本的にはこの例と同じ記述をして下さい。
    */
  val idTag: ClassTag[Id] = classTag[Id]

  /**
    * セッションタイムアウトの時間(秒)です。
    */
  val sessionTimeoutInSeconds: Int = 3600

  /**
    * ユーザIDからUserブジェクトを取得するアルゴリズムを指定します。
    * 任意の処理を記述してください。
    */
  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = userRepoLike.findByUsername(id)


  /**
    * ログインが成功した際に遷移する先を指定します。
    */
  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) =
    Future.successful(Redirect(routes.UserController.show))

  /**
    * ログアウトが成功した際に遷移する先を指定します。
    */
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) =
    Future.successful(Redirect(routes.LoginController.brandNew))

  /**
    * 認証が失敗した場合に遷移する先を指定します。
    */
  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) =
    Future.successful(Redirect(routes.LoginController.brandNew))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext) = {
    Future.successful(Redirect(routes.LoginController.brandNew))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    // TODO check role
//    (user.roleId, authority) match {
//      case _ => true
//    }
    true
  }


  /**
    * (Optional)
    * SessionID Tokenの保存場所の設定です。
    * デフォルトでは Cookie を使用します。
    */
//  override lazy val tokenAccessor = new CookieTokenAccessor(
//    /*
//     * cookie の secureオプションを使うかどうかの設定です。
//     * デフォルトでは利便性のために false になっていますが、
//     * 実際のアプリケーションでは true にすることを強く推奨します。
//     */
//    cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
//    cookieMaxAge       = Some(sessionTimeoutInSeconds)
//  )

}
