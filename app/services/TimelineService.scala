package services

import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.Tables.{FirstPart, LastPart, User}
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

/**
  * Created by Shuhei on 2016/08/07.
  */
class TimelineService@Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val User = TableQuery[User]
  val FirstPart = TableQuery[FirstPart]
  val LastPart = TableQuery[LastPart]

  val findTankaQueryForTL = User
    .join(FirstPart).on(_.id === _.userId)
    .join(LastPart).on(_._2.id === _.firstPartId)
    .result

  val action = (for{
    tankaResultForTL <- findTankaQueryForTL
  }yield{
    tankaResultForTL.map{ row =>
      val (userTableRow,firstPartTableRow) = row._1
      val lastPartTableRow = row._2
    }
  }

    )

//  val findBooksQuery = libraries
//    .join(libraryToBooks).on(_.id === _.libraryId)
//    .join(books).on(_.id === _._2.bookId)
//    .result
//
//  val action = (for {
//    booksResult <- findBooksQuery
//  } yield {
//    booksResult.map { row =>
//      val (libraryTableRow, libraryToBooksTableRow) = row._1
//      val booksTableRow = row._2
//      // TODO: Access all data from the rows and construct desired DS
//    }
//  }



}
