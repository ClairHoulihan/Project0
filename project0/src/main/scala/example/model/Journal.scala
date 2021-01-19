package example.model

import java.sql.ResultSet

case class Journal(journalId : Int, journal_name : String, pages : Int) {

}

object Journal {
  /**
    * Produces a Journal from a record in a ResultSet.  Note that this method does *not* call next()!
    *
    * @param rs
    * @return
    */
  def fromResultSet(rs : ResultSet) : Journal = {
    apply(
      rs.getInt("journal_id"),
      rs.getString("journal_name"),
      rs.getInt("pages")
    )
  }
}