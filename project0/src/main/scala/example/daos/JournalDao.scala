package example.daos

import example.model.Journal
import example.util.ConnectionUtil
import scala.util.Using
import scala.collection.mutable.ArrayBuffer
import java.rmi.UnexpectedException

/** A Journal Data Access Object.  JournalDao has CRUD methods for Journals
  *
  * It allows us to keep all of our database access logic in this file,
  * while still allowing the rest of our application to use Journals
  * retrieved from the database.
  */
object JournalDao {

  /** Retrieves all Journals from the journal table in the db
    *
    * @return
    */
  def getAll(): Seq[Journal] = {
    val conn = ConnectionUtil.getConnection();
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("SELECT * FROM journal;"))
      stmt.execute()
      val rs = use(stmt.getResultSet())
      // lets use an ArrayBuffer, we're adding one element at a time
      val allJournals: ArrayBuffer[Journal] = ArrayBuffer()
      while (rs.next()) {
        allJournals.addOne(Journal.fromResultSet(rs))
      }
      allJournals.toList
    }.get
    // the .get retrieves the value from inside the Try[Seq[Journal]] returned by Using.Manager { ...
    // it may be better to not call .get and instead return the Try[Seq[Journal]]
    // that would let the calling method unpack the Try and take action in case of failure
  }

  def get(journalName: String): Seq[Journal] = {
    val conn = ConnectionUtil.getConnection()
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE journal_name = ?"))
      stmt.setString(1, journalName)
      stmt.execute()
      val rs = use(stmt.getResultSet())
      val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
      while (rs.next()) {
        journalsWithName.addOne(Journal.fromResultSet(rs))
      }
      journalsWithName.toList
    }.get
  }

  def searchByDate(dateSearch: String): Seq[Journal] = {
    val conn = ConnectionUtil.getConnection()
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE date_of_creation = ?"))
      stmt.setString(1, dateSearch)
      stmt.execute()
      val rs = use(stmt.getResultSet())
      val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
      while (rs.next()) {
        journalsWithName.addOne(Journal.fromResultSet(rs))
      }
      journalsWithName.toList
    }.get
  }

  def searchByPage(condition: String): Seq[Journal] = {
    val conn = ConnectionUtil.getConnection()
    var tokens = condition.split(" ")
    if(tokens(0) == ">") {
      Using.Manager { use =>
        val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE pages > ?"))
        stmt.setInt(1, tokens(1).toInt)
        stmt.execute()
        val rs = use(stmt.getResultSet())
        val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
        while (rs.next()) {
          journalsWithName.addOne(Journal.fromResultSet(rs))
        }
        journalsWithName.toList
      }.get
    } else if(tokens(0) == "<") {
      Using.Manager { use =>
        val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE pages < ?"))
        stmt.setInt(1, tokens(1).toInt)
        stmt.execute()
        val rs = use(stmt.getResultSet())
        val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
        while (rs.next()) {
          journalsWithName.addOne(Journal.fromResultSet(rs))
        }
        journalsWithName.toList
      }.get
    } else if(tokens(0) == "=") {
      Using.Manager { use =>
        val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE pages = ?"))
        stmt.setInt(1, tokens(1).toInt)
        stmt.execute()
        val rs = use(stmt.getResultSet())
        val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
        while (rs.next()) {
          journalsWithName.addOne(Journal.fromResultSet(rs))
        }
        journalsWithName.toList
      }.get
    } else if(tokens(0) == ">=") {
      Using.Manager { use =>
        val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE pages >= ?"))
        stmt.setInt(1, tokens(1).toInt)
        stmt.execute()
        val rs = use(stmt.getResultSet())
        val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
        while (rs.next()) {
          journalsWithName.addOne(Journal.fromResultSet(rs))
        }
        journalsWithName.toList
      }.get
    } else if(tokens(0) == "<=") {
      Using.Manager { use =>
        val stmt = use(conn.prepareStatement("SELECT * FROM journal WHERE pages < ?"))
        stmt.setInt(1, tokens(1).toInt)
        stmt.execute()
        val rs = use(stmt.getResultSet())
        val journalsWithName : ArrayBuffer[Journal] = ArrayBuffer()
        while (rs.next()) {
          journalsWithName.addOne(Journal.fromResultSet(rs))
        }
        journalsWithName.toList
      }.get
    } else {
      throw new UnexpectedException("Unexpected Input.")
    }
      
  }

  def saveNew(journal : Journal) : Boolean = {
    val conn = ConnectionUtil.getConnection()
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("INSERT INTO journal VALUES (DEFAULT, ?, ?, ?);"))
      stmt.setString(1, journal.journal_name)
      stmt.setInt(2, journal.pages)
      stmt.setString(3, journal.date_of_creation)
      stmt.execute()
      //check if rows were updated, return true is yes, false if no
      stmt.getUpdateCount() > 0
    }.getOrElse(false)
    // also returns false if a failure occurred
  }

  def deleteThis(journal_name : String) : Boolean = {
    val conn = ConnectionUtil.getConnection()
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("DELETE FROM journal WHERE journal_name = ?;"))
      stmt.setString(1, journal_name)
      stmt.execute()
      //check if rows were updated, return true is yes, false if no
      stmt.getUpdateCount() > 0
    }.getOrElse(false)
    // also returns false if a failure occurred
  }

  def updateName(oldName : String, newName : String) : Boolean = {
      val conn = ConnectionUtil.getConnection()
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("UPDATE journal SET journal_name = ? WHERE journal_name = ?;"))
      stmt.setString(1, newName)
      stmt.setString(2, oldName)
      stmt.execute()
      //check if rows were updated, return true is yes, false if no
      stmt.getUpdateCount() > 0
    }.getOrElse(false)
    // also returns false if a failure occurred
  }

  def updatePageCount(journalName : String, pages : Int) : Boolean = {
      val conn = ConnectionUtil.getConnection()
    Using.Manager { use =>
      val stmt = use(conn.prepareStatement("UPDATE journal SET pages = ? WHERE journal_name = ?;"))
      stmt.setInt(1, pages)
      stmt.setString(2, journalName)
      stmt.execute()
      //check if rows were updated, return true is yes, false if no
      stmt.getUpdateCount() > 0
    }.getOrElse(false)
    // also returns false if a failure occurred
  }

}