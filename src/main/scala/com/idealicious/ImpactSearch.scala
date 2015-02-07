package com.idealicious

import java.util.concurrent._

import javafx.application.Platform
import javafx.collections.{ObservableList, FXCollections}
import javafx.concurrent.Task

import scala.collection.JavaConverters._

import org.jsoup._

class ImpactSearch(val terms: Seq[String], val numThreads: Int, controller: ViewController) extends Task[ObservableList[Journal]] {
  // Auxiliary constructor for one term
  def this(term: String, numThreads: Int, controller: ViewController) =
    this(Seq(term), numThreads, controller)

  private val BASE_URL = "http://www.impact-factor.org/journal/"

  // Thread pool of daemon threads. Once they are done, they die.
  // http://stackoverflow.com/questions/13883293/turning-an-executorservice-to-daemon-in-java
  private val pool = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
    override def newThread(r: Runnable) = {
      val t = new Thread(r)
      t.setDaemon(true)
      t
    }
  })

  private val partialResults = FXCollections.observableArrayList[Journal]()

  override def call() = {
    fetch()

    // Return partial results as ObservableList to UI thread
    partialResults
  }

  // Fetch results for each term using a thread pool
  private def fetch() = terms.map((term) => pool.execute(fetchOne(term)))

  // Fetch a single result using a Runnable
  private def fetchOne(term: String) = {
    new Runnable() {
      override def run() = {
        // Construct valid URL
        val url = BASE_URL + term.toLowerCase.replaceAll(" & ", " ").replaceAll(" ", "-") + ".html"

        try {
          // Grab page
          val page = Jsoup.connect(url).get

          // Get table data; skip header row
          val cols = page.select("table.table tr td").asScala.tail.map((col) => col.text)

          // Get the score for the latest year
          val score = cols(2).toDouble

          // Additional data
          val abbrev = page.select("div.title p").first.text
          val issn = page.select("div.col-sm-12 p").first.text

          // Wrap in runLater, as in docs; schedules task on UI thread
          Platform.runLater(new Runnable {
            override def run() = partialResults.add(Journal(term, abbrev, issn, Some(score)))
          })
        }

        // Return a default case if page not found
        catch { case e: org.jsoup.HttpStatusException =>
          // Return a sane default in case of invalid journal
          Platform.runLater(new Runnable {
            override def run(): Unit = partialResults.add(Journal(term, "Invalid", "Invalid", Some(-1)))
          })
        }
      }
    }
  }
}
