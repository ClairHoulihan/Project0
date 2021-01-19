package example

import example.model.Journal
import example.manageCSV
import example.util.ConnectionUtil
import example.daos.JournalDao
import java.sql.DriverManager
import java.sql.ResultSet
import java.nio.file.{Paths, Files}
import java.nio.file.DirectoryStream
import scala.collection.JavaConverters._
import java.nio.file.StandardOpenOption
import java.nio.file.StandardCopyOption
import java.util.List
import java.io.IOException
import java.util.Calendar
import scala.collection.mutable.ArrayBuffer
import java.text.SimpleDateFormat
import scala.util.control.Breaks._
import java.util.Date

/** MyJournal
  * 
  * MyJournal is the main application for the program. It runs a command line interface with a given list of 
  * options which go to their respective methods. 
  * 
  */
object MyJournal extends App {

    val commands = "C - Create a new Journal, D - Delete a Journal, U - Update a Journal, " +
      "F - Add journals from csv file, A - Add a page to a Journal, E - Delete a page from a Journal, " +
      "P - Update a page from a Journal, R - Read from a page in a journal, " +
      "N - Return a listing for all journals that currently exist, O - For debugging only, " +
      "S - Search by date, Z - Exit the program"

    println(commands)

        var userInput = scala.io.StdIn.readLine()

        while(userInput.toUpperCase != "Z") {

            if(userInput.toUpperCase == "C") {
              create()
            } else if(userInput.toUpperCase == "D") {
              delete()
            } else if(userInput.toUpperCase == "U") {
              update()
            } else if(userInput.toUpperCase == "F") {
              csvRead()
            } else if(userInput.toUpperCase == "A") {
              addPage()
            } else if(userInput.toUpperCase == "E") {
              deletePage()
            } else if(userInput.toUpperCase == "P") {
              updatePage()
            } else if(userInput.toUpperCase == "R") {
              readPage()
            } else if(userInput.toUpperCase == "N") {
              listing()
            } else if(userInput.toUpperCase == "O") {
              debug()
            } else if(userInput.toUpperCase == "S") {
              search()
            } else {
              println("Unknown command, please try again.")
            }

            println(commands)            
            userInput = scala.io.StdIn.readLine()

        }


    /** create
      * 
      * create a directory to contain the pages of the journal.
      * 
      */
    def create() : Unit = {
      println("Name of the Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} exists already.")
        return
      }

      try {
        Files.createDirectory(Paths.get("./" + nameOfJournal))
      } catch {
        case e: IOException => {
          println("Unexpected IOException, returning to the main menu.")
          return
        }
      }

      var date = new Date();
      var formatter = new SimpleDateFormat("MM/dd/yyyy")
      var strDate = formatter.format(date)
      
      try {
      if (JournalDao.saveNew(Journal(0, nameOfJournal, 0, strDate))) {
        println("Added a new journal.")
      } else {
        println("Could not add journal.")
      }
    } catch {
      case e : Exception => {
        println("failed to add journal.")
      }
    }

  }

    /** delete
      * 
      * delete a journal (directory) and all of the pages contained within it.
      * 
      */
    def delete() : Unit = {
      println("Name of the Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} does not exist.")
        return
      }

      var stream = Files.newDirectoryStream(Paths.get("./" + nameOfJournal))
      var listStream = stream.iterator().asScala.toList
      var numOfPages = listStream.length

      for(pages <- listStream) {
        Files.delete(pages)
      }

      Files.delete(Paths.get("./" + nameOfJournal))
      
      try {
        if (JournalDao.deleteThis(nameOfJournal)) {
        println(s"deleted ${nameOfJournal}.")
       } else {
        println("Could not delete journal.")
        }
    } catch {
      case e : Exception => {
        println("failed to delete journal.")
      }
    }

      
    }

    /** update
      * 
      * update a journal by changing its name.
      * 
      */
    def update() : Unit = {

      println("Name of Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} does not exist.")
        return
      }

      println("New name for the Journal: ")
      var newName = scala.io.StdIn.readLine()

      Files.move(Paths.get("./" + nameOfJournal), Paths.get("./" + newName))

      JournalDao.updateName(nameOfJournal, newName)
      
    }



    /** search
      * 
      * search through fields chosen by the user to search for a journal (or a journal and page number).
      * 
      */
    def search() : Unit =  {

      println("Date to search for (format = MM/dd/yyyy): ")
      var dateSearch = scala.io.StdIn.readLine()

      var journals = JournalDao.searchByDate(dateSearch)

      journals.foreach( (journ) => { println(s"${journ.journal_name} ${journ.date_of_creation}") } )

    }



    /** addPage
      * 
      * add a page to a journal and give the option of either typing in text, or using an existing text document.
      * 
      */
     def addPage() : Unit = {

      println("Name of Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} does not exist.")
        return
      }

      var stream = Files.newDirectoryStream(Paths.get("./" + nameOfJournal))
      var listStream = stream.iterator().asScala.toList

      var numOfPages = listStream.length

      Files.createFile(Paths.get("./" + nameOfJournal + "/" + numOfPages.toString()))

      println("Would you like to write to the file [Y/n]?")
      var yesOrNo = scala.io.StdIn.readLine()

      if(yesOrNo.toUpperCase == "Y") {
        println("Would you like to import from another file [Y/n]?")
        yesOrNo = scala.io.StdIn.readLine()

        if(yesOrNo.toUpperCase() == "Y") {
          println("Enter the exact path of the file you want to copy from.")
          var filePath = scala.io.StdIn.readLine()

          Files.copy(Paths.get(filePath), Paths.get("./" + nameOfJournal + "/" + numOfPages.toString()), StandardCopyOption.REPLACE_EXISTING)

        } else if(yesOrNo.toUpperCase() == "N") {
          println("Write lines to the file, when finished, type: EOF")
          var writeToFile = scala.io.StdIn.readLine()
          while(writeToFile != "EOF") {
            Files.write(Paths.get("./" + nameOfJournal + "/" + numOfPages.toString()), writeToFile.getBytes, StandardOpenOption.APPEND)
            Files.write(Paths.get("./" + nameOfJournal + "/" + numOfPages.toString()), "\n".getBytes(), StandardOpenOption.APPEND)
            writeToFile = scala.io.StdIn.readLine()
          }
        } else {
          println("Unexpected input, returning to the main menu.")
        }

      } else if(yesOrNo.toUpperCase() == "N") {
        println("Returning to the main menu.")
      } else {
        println("Unexpected input, returning to the main menu.")
      }

      stream = Files.newDirectoryStream(Paths.get("./" + nameOfJournal))
      listStream = stream.iterator().asScala.toList

      numOfPages = listStream.length

      JournalDao.updatePageCount(nameOfJournal, numOfPages)

    }

    /** deletePage
      * 
      * delete a page from a journal and update the names of the other pages.
      * 
      */
    def deletePage() : Unit = {

      println("Name of Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} does not exist.")
        return
      }

      println("Page to be removed: ")
      var removePage = scala.io.StdIn.readLine()
      
      if(!Files.exists(Paths.get("./" + nameOfJournal + "/" + removePage))) {
        println(s"The page number ${removePage} in ${nameOfJournal} does not exist.")
        return
      }

      var stream = Files.newDirectoryStream(Paths.get("./" + nameOfJournal))
      var listStream = stream.iterator().asScala.toList
      var numOfPages = listStream.length
      Files.delete(Paths.get("./" + nameOfJournal + "/" + removePage))
      
      for (pages <- listStream) {

        val regexPat = ("""(.*)(\d)""".r)
        var intPages : String = ""
        pages.toString() match {
          case regexPat(notMatching, matching) => intPages = matching
        }
        if(intPages.toInt > removePage.toInt) {
          var newName = (intPages.toInt - 1)
          Files.move(Paths.get("./" + nameOfJournal + "/" + intPages), 
          Paths.get("./" + nameOfJournal + "/" + newName.toString()))
        }
            
      }

      stream = Files.newDirectoryStream(Paths.get("./" + nameOfJournal))
      listStream = stream.iterator().asScala.toList

      numOfPages = listStream.length

      JournalDao.updatePageCount(nameOfJournal, numOfPages)

    }

    /** updatePage
      * 
      * update a page with new text by either overwriting what is already written or appending to the text.
      * You can also copy a file from another location, while choosing between overwritting the existing text
      * or appending the other file to the page.
      * 
      */
    def updatePage() : Unit = {

      println("Name of Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} does not exist.")
        return
      }

      println("Page to update: ")
      var pageToUpdate = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal + "/" + pageToUpdate))) {
        println(s"The page number ${pageToUpdate} in ${nameOfJournal} does not exist.")
        return
      }

      var stream = Files.newDirectoryStream(Paths.get("./" + nameOfJournal))
      var listStream = stream.iterator().asScala.toList

      var numOfPages = listStream.length

      println("Append or Overwrite? (A - for append, O - for overwrite)")
      var option = scala.io.StdIn.readLine().toUpperCase()

      println("Write from another file [Y/n]?")
      var yesOrNo = scala.io.StdIn.readLine().toUpperCase()

      if(yesOrNo == "Y") {
        println("Enter the exact path of the file you want to copy from.")
          var filePath = scala.io.StdIn.readLine()

          if (option == "O") {
            Files.copy(Paths.get(filePath), Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), StandardCopyOption.REPLACE_EXISTING)

          } else if (option == "A") {
            var allLines = Files.readAllLines(Paths.get(filePath))
            
            Files.write(Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), allLines, StandardOpenOption.APPEND)
          } else {
            println("Unexpected input, returning to main menu")
          }
      } else if (yesOrNo == "N") {
        if (option == "O") {
          println("Write lines to the file, when finished, type: EOF")
          var writeToFile = scala.io.StdIn.readLine()
          Files.write(Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), "".getBytes)
          while(writeToFile != "EOF") {
            Files.write(Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), writeToFile.getBytes, StandardOpenOption.APPEND)
            Files.write(Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), "\n".getBytes(), StandardOpenOption.APPEND)
            writeToFile = scala.io.StdIn.readLine()
          }
        } else if (option == "A") {
          println("Write lines to the file, when finished, type: EOF")
          var writeToFile = scala.io.StdIn.readLine()
          while(writeToFile != "EOF") {
            Files.write(Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), writeToFile.getBytes, StandardOpenOption.APPEND)
            Files.write(Paths.get("./" + nameOfJournal + "/" + pageToUpdate.toString()), "\n".getBytes(), StandardOpenOption.APPEND)
            writeToFile = scala.io.StdIn.readLine()
          }
        } else {
          println("Unexpected input, returning to main menu")
        }

      } else {
        println("Unexpected input, returning to main menu")
      }

      return

    }

    /** readPage
      * 
      * print out the contents of a page to the console.
      * 
      */
    def readPage() : Unit = {
      println("Name of the Journal: ")
      var nameOfJournal = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal))) {
        println(s"The journal ${nameOfJournal} does not exist.")
        return
      }

      println("Page to read: ")
      var pageNum = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get("./" + nameOfJournal + "/" + pageNum))) {
        println(s"The page number ${pageNum} in ${nameOfJournal} does not exist.")
        return
      }

      try {
      var linesToRead = Files.readAllLines(Paths.get("./" + nameOfJournal + "/" + pageNum))

      linesToRead.forEach(println)
      } catch {
        case e : IOException => {
          println("Unexpected IO Error, unable to read from file")
          return
        }

      }

    }

    /** listing
      *
      * list all of the journals that currently exist (read from the sql server)
      *  
      */
    def listing() : Unit = {

      println("Here are the journals that currently exist (in the sql table):");
      JournalDao.getAll().foreach( (journ) => { println(s"${journ.journal_name} ${journ.date_of_creation}") } )

    }

    /** debug
      * 
      * this function only exists to test new functions in other classes and objects
      * 
      */
    def debug() : Unit = {

      println("Finished debugging.")

    }

    def csvRead() : Unit = {

      println("CSV file to read from (Enter the whole path): ")
      var csvFile = scala.io.StdIn.readLine()

      if(!Files.exists(Paths.get(csvFile))) {
        println(s"${csvFile} does not exist.")
        return
      }

      try {
        var linesToRead = Files.readAllLines(Paths.get(csvFile)).asScala.toList
        
        for(line <- linesToRead) {
          var tokens = line.split(",")
          var journalExists = JournalDao.get(tokens(1))
          breakable {
            if(tokens(1) == "Name of Journal" || !(journalExists.isEmpty)) {
             break 
            }

            var newJournal = new Journal(tokens(0).toInt, tokens(1), tokens(2).toInt, tokens(3))

            JournalDao.saveNew(newJournal)

          }

        }

      } catch {
        case e : IOException => {
          println("Unexpected IO Error, unable to read from file")
          return
        } case e : NumberFormatException => {
          println("Conversion Error, a value in the excel file was a string when it should have been an integer")
        
        } case e : Exception => {
          println("Unable to add a journal to the database")
          return
        }

      }

    }

}