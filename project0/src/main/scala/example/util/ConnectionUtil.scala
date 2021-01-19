package example.util

import java.sql.DriverManager
import java.sql.Connection

object ConnectionUtil {

    /**
      * utility for retrieving connection, with hardcoded credentials
      * 
      * @return Connection
      */
    def getConnection() : Connection = {
        
        classOf[org.postgresql.Driver].newInstance() // manually load the Driver
        
        // missing a bit of documentation from the java code:
        // getConnection uses a URL, username and password
        // hardcoding these at the moment, but this is bad practice
        DriverManager.getConnection("jdbc:postgresql://localhost:5026/postgres", "postgres", "wasspord")

    }

}