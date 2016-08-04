package models

object SlickCodeGen extends App {
  val slickDriver = "slick.driver.MySQLDriver"
  val jdbcDriver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://localhost/playdb"
  val outputDir = "app"
  val pkg = "models"
  val user = "root"
  val password = ""
  slick.codegen.SourceCodeGenerator.main(
    Array(slickDriver, jdbcDriver, url, outputDir, pkg, user, password))
}