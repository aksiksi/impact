package com.idealicious

import java.io.{FileWriter, IOException}

// http://examples.javacodegeeks.com/core-java/writeread-csv-files-in-java-example/
class CSVWriter(val data: Seq[Journal], val header: Seq[String], val delimiter: String = ",", val newLine: String = "\n") {
  def write(path: String) = {
    var fw: FileWriter = null

    try {
      fw = new FileWriter(path)

      // Write header
      fw.append(header.mkString(delimiter) + newLine)

      // Write each row
      data.foreach((e) => fw.append(e.title + delimiter + e.score.get + delimiter + e.abbrev + newLine))
    } catch {
      case e: IOException => {
        println("Error writing to file.")
        e.printStackTrace()
      }
    }

    finally {
      try {
        fw.flush()
        fw.close()
      } catch {
        case e: IOException => {
          println("Error while flushing (LOL).")
          e.printStackTrace()
        }
      }
    }
  }
}
