package example

import scala.util.control.Breaks._
import java.nio.file.{Paths, Files}
import java.nio.file.DirectoryStream
import scala.collection.JavaConverters._
import java.nio.file.StandardOpenOption
import java.nio.file.StandardCopyOption
import java.io.IOException

/** manageCSV
  * 
  * manageCSV will manage the csv file (JournalInfo.csv), creating, updating, reading, and deleting entries in
  * the csv file. A typical csv file entry will have:
  * 
  * journalName : String, dateOfJournalCreation : String, numberOfPages : Int, dateOfLatestPage : String
  * 
  * manageCSV is mostly unused now, it was for an earlier version of myJournal, the app now uses a database
  * running postgresql to store the names of Journals
  * 
  */
object manageCSV {

    val csvFile = "./JournalInfo.csv"

    /** createCSVEntry
      *
      * create an entry in the csv table for a journal.
      * 
      */
    def createCSVEntry(journalName : String, dateOfJournalCreation : String, numberOfPages : Int, dateOfLatestPage : String) : Unit = {

      var writeToCSVFile = s"${journalName},${dateOfJournalCreation},${numberOfPages},${dateOfLatestPage}"
      Files.write(Paths.get(csvFile), writeToCSVFile.getBytes(), StandardOpenOption.APPEND)

    }

    /** updateCSVEntry
      * 
      * update an existing entry in the csv table for a journal. In this case, we update the page number
      * and the date that the last page was added.
      *
      */
    def updateCSVEntry(journalName : String, numberOfPages : Int, dateOfLatestPage : String) : Unit = {

      var linesFromFile = Files.readAllLines(Paths.get(csvFile)).asScala.toList

      Files.write(Paths.get(csvFile), linesFromFile(0).getBytes())

      for(line <- linesFromFile) {
        var tokens = line.split(",")
        for(part <- tokens) {
          if( part == journalName) {
            var newEntry = s"${part},${tokens(1)},${numberOfPages},${dateOfLatestPage}"
            Files.write(Paths.get(csvFile), newEntry.getBytes())
          } else {
            Files.write(Paths.get(csvFile), line.getBytes())
            break()
          }
        }
      }

    }

    /** updateCSVEntry
      * 
      * update an existing entry in the csv table for a journal. In this case, we update the journal name.
      *
      */
    def updateCSVEntry(journalName : String, newJournalName : String) : Unit = {

      var linesFromFile = Files.readAllLines(Paths.get(csvFile)).asScala.toList

      Files.write(Paths.get(csvFile), linesFromFile(0).getBytes())

      for(line <- linesFromFile) {
        var tokens = line.split(",")
        for(part <- tokens) {
          if( part == journalName) {
            var newEntry = s"${newJournalName},${tokens(1)},${tokens(2)},${tokens(3)}"
            Files.write(Paths.get(csvFile), newEntry.getBytes())
          } else {
            Files.write(Paths.get(csvFile), line.getBytes())
            break()
          }
        }
      }

    }

    /** readCSVEntry
      *
      * read an existing entry in the csv table for a journal. This is the only method of manageCSV that is still
      * used in the new version of myJournal.
      * 
      */
    def readCSVEntries(journalName : String) : List[String] = {

      var linesFromFile = Files.readAllLines(Paths.get(csvFile))

      linesFromFile.remove(0)

      return linesFromFile.asScala.toList

    }

    /** deleteCSVEntry
      *
      * delete an entry in the csv table.
      * 
      * @params: journalName: Name of the Journal to be deleted.
      * 
      */
    def deleteCSVEntry(journalName : String) : Unit = {

      var linesFromFile = Files.readAllLines(Paths.get(csvFile))

      Files.write(Paths.get(csvFile), "Journal Name,Date Of Journal Creation,Number Of Pages,Date Of Latest Page".getBytes())

      for(line <- linesFromFile.asScala.toList) {
        var tokens = line.split(",")
        if(tokens(0) != journalName && tokens(0) != "Journal Name") {
          Files.write(Paths.get(csvFile), line.getBytes(), StandardOpenOption.APPEND)
        }
      }

    }

}