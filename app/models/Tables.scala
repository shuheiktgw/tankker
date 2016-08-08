package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = FirstPart.schema ++ LastPart.schema ++ User.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table FirstPart
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(INT)
   *  @param firstPartContentFirst Database column first_part_content_first SqlType(VARCHAR), Length(20,true)
   *  @param firstPartContentSecond Database column first_part_content_second SqlType(VARCHAR), Length(28,true)
   *  @param firstPartContentThird Database column first_part_content_third SqlType(VARCHAR), Length(28,true)
   *  @param createdAt Database column created_at SqlType(TIMESTAMP)
   *  @param updatedAt Database column updated_at SqlType(TIMESTAMP) */
  case class FirstPartRow(id: Int, userId: Int, firstPartContentFirst: String, firstPartContentSecond: String, firstPartContentThird: String, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching FirstPartRow objects using plain SQL queries */
  implicit def GetResultFirstPartRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[FirstPartRow] = GR{
    prs => import prs._
    FirstPartRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table FIRST_PART. Objects of this class serve as prototypes for rows in queries. */
  class FirstPart(_tableTag: Tag) extends Table[FirstPartRow](_tableTag, "FIRST_PART") {
    def * = (id, userId, firstPartContentFirst, firstPartContentSecond, firstPartContentThird, createdAt, updatedAt) <> (FirstPartRow.tupled, FirstPartRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userId), Rep.Some(firstPartContentFirst), Rep.Some(firstPartContentSecond), Rep.Some(firstPartContentThird), Rep.Some(createdAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> FirstPartRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column first_part_content_first SqlType(VARCHAR), Length(20,true) */
    val firstPartContentFirst: Rep[String] = column[String]("first_part_content_first", O.Length(20,varying=true))
    /** Database column first_part_content_second SqlType(VARCHAR), Length(28,true) */
    val firstPartContentSecond: Rep[String] = column[String]("first_part_content_second", O.Length(28,varying=true))
    /** Database column first_part_content_third SqlType(VARCHAR), Length(28,true) */
    val firstPartContentThird: Rep[String] = column[String]("first_part_content_third", O.Length(28,varying=true))
    /** Database column created_at SqlType(TIMESTAMP) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(TIMESTAMP) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Index over (userId) (database name user_id) */
    val index1 = index("user_id", userId)
  }
  /** Collection-like TableQuery object for table FirstPart */
  lazy val FirstPart = new TableQuery(tag => new FirstPart(tag))

  /** Entity class storing rows of table LastPart
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(INT)
   *  @param firstPartId Database column first_part_id SqlType(INT)
   *  @param lastPartContentFirst Database column last_part_content_first SqlType(VARCHAR), Length(28,true)
   *  @param lastPartContentSecond Database column last_part_content_second SqlType(VARCHAR), Length(28,true)
   *  @param createdAt Database column created_at SqlType(TIMESTAMP)
   *  @param updatedAt Database column updated_at SqlType(TIMESTAMP) */
  case class LastPartRow(id: Int, userId: Int, firstPartId: Int, lastPartContentFirst: String, lastPartContentSecond: String, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching LastPartRow objects using plain SQL queries */
  implicit def GetResultLastPartRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[LastPartRow] = GR{
    prs => import prs._
    LastPartRow.tupled((<<[Int], <<[Int], <<[Int], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table LAST_PART. Objects of this class serve as prototypes for rows in queries. */
  class LastPart(_tableTag: Tag) extends Table[LastPartRow](_tableTag, "LAST_PART") {
    def * = (id, userId, firstPartId, lastPartContentFirst, lastPartContentSecond, createdAt, updatedAt) <> (LastPartRow.tupled, LastPartRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userId), Rep.Some(firstPartId), Rep.Some(lastPartContentFirst), Rep.Some(lastPartContentSecond), Rep.Some(createdAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> LastPartRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column first_part_id SqlType(INT) */
    val firstPartId: Rep[Int] = column[Int]("first_part_id")
    /** Database column last_part_content_first SqlType(VARCHAR), Length(28,true) */
    val lastPartContentFirst: Rep[String] = column[String]("last_part_content_first", O.Length(28,varying=true))
    /** Database column last_part_content_second SqlType(VARCHAR), Length(28,true) */
    val lastPartContentSecond: Rep[String] = column[String]("last_part_content_second", O.Length(28,varying=true))
    /** Database column created_at SqlType(TIMESTAMP) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(TIMESTAMP) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Foreign key referencing FirstPart (database name last_part_ibfk_2) */
    lazy val firstPartFk = foreignKey("last_part_ibfk_2", firstPartId, FirstPart)(r => r.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name last_part_ibfk_1) */
    lazy val userFk = foreignKey("last_part_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table LastPart */
  lazy val LastPart = new TableQuery(tag => new LastPart(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param username Database column username SqlType(VARCHAR), Length(255,true)
   *  @param email Database column email SqlType(VARCHAR), Length(255,true)
   *  @param password Database column password SqlType(VARCHAR), Length(255,true)
   *  @param deleted Database column deleted SqlType(BIT), Default(false)
   *  @param createdAt Database column created_at SqlType(TIMESTAMP)
   *  @param updatedAt Database column updated_at SqlType(TIMESTAMP) */
  case class UserRow(id: Int, username: String, email: String, password: String, deleted: Boolean = false, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean], e3: GR[java.sql.Timestamp]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[Boolean], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table USER. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "USER") {
    def * = (id, username, email, password, deleted, createdAt, updatedAt) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(username), Rep.Some(email), Rep.Some(password), Rep.Some(deleted), Rep.Some(createdAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username SqlType(VARCHAR), Length(255,true) */
    val username: Rep[String] = column[String]("username", O.Length(255,varying=true))
    /** Database column email SqlType(VARCHAR), Length(255,true) */
    val email: Rep[String] = column[String]("email", O.Length(255,varying=true))
    /** Database column password SqlType(VARCHAR), Length(255,true) */
    val password: Rep[String] = column[String]("password", O.Length(255,varying=true))
    /** Database column deleted SqlType(BIT), Default(false) */
    val deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))
    /** Database column created_at SqlType(TIMESTAMP) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(TIMESTAMP) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Uniqueness Index over (email) (database name UNIQUE_EMAIL) */
    val index1 = index("UNIQUE_EMAIL", email, unique=true)
    /** Uniqueness Index over (username) (database name UNIQUE_USERNAME) */
    val index2 = index("UNIQUE_USERNAME", username, unique=true)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
}
