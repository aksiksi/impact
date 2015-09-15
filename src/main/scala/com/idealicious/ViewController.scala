package com.idealicious

import java.util.Comparator
import javafx.beans.property.{SimpleDoubleProperty, SimpleStringProperty}
import javafx.beans.value.ObservableValue
import javafx.collections.{ListChangeListener, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.control._
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.util.Callback

import scala.collection.JavaConverters._

class ViewController {
  @FXML
  private var pane: AnchorPane = _

  @FXML
  private var exportWarning: Label = _

  @FXML
  private var journalsList: TextArea = _

  @FXML
  var resultsTable: TableView[Journal] = _

  private val titleColumn = new TableColumn[Journal, String]("Journal")
  private val scoreColumn = new TableColumn[Journal, Number]("Impact Factor")
  private val abbrevColumn = new TableColumn[Journal, String]("Abbreviation")

  @FXML
  var retrieveBtn: Button = _

  @FXML
  private var exportBtn: Button = _

  @FXML
  private var currentProgress: ProgressBar = new ProgressBar()

  private var resultsList: ObservableList[Journal] = _

  private val resultsComparator = new Comparator[Journal] {
    override def compare(j1: Journal, j2: Journal) = {
      if (j1.score.get > j2.score.get) -1
      else if (j1.score.get < j2.score.get) 1
      else 0
    }
  }

  @FXML
  private def initialize() = {
    titleColumn.setCellValueFactory(new Callback[CellDataFeatures[Journal, String], ObservableValue[String]] {
      override def call(journal: CellDataFeatures[Journal, String]) = {
        new SimpleStringProperty(journal.getValue().title)
      }
    })

    scoreColumn.setCellValueFactory(new Callback[CellDataFeatures[Journal, Number], ObservableValue[Number]] {
      override def call(journal: CellDataFeatures[Journal, Number]) = {
        new SimpleDoubleProperty(journal.getValue().score.get)
      }
    })

    abbrevColumn.setCellValueFactory(new Callback[CellDataFeatures[Journal, String], ObservableValue[String]] {
      override def call(journal: CellDataFeatures[Journal, String]) = {
        new SimpleStringProperty(journal.getValue().abbrev)
      }
    })

    // Add columns to table
    resultsTable.getColumns().setAll(titleColumn, scoreColumn, abbrevColumn)

    // Setup ProgressBar manually and add to VBox
    currentProgress.setLayoutX(299)
    currentProgress.setLayoutY(469)
    currentProgress.setPrefWidth(189)
    currentProgress.setProgress(0)

    pane.getChildren.add(currentProgress)
  }

  @FXML
  private def retrieveResults(event: ActionEvent) = {
    val terms = journalsList.getText().split("\n").toSeq

    if (terms.filter(_.trim != "").length > 0) {
      // Return reference to partial ObservableList of results
      resultsList = new ImpactSearch(terms, 10).call()

      exportWarning.setText("")

      retrieveBtn.setText("Retrieving..")

      currentProgress.setProgress(0)

      // Whenever a value is added, update resultsTable
      resultsList.addListener(new ListChangeListener[Journal] {
        override def onChanged(c: ListChangeListener.Change[_ <: Journal]) = {
          while (c.next()) {
            if (c.wasAdded()) {
              // Reset button status once retrieval complete
              if (resultsList.size() == terms.length) retrieveBtn.setText("Retrieve!")

              // Update progress and re-sort list on add
              currentProgress.setProgress(resultsList.size().toDouble / terms.length)
              resultsList.sort(resultsComparator)

              // Update TableView
              resultsTable.setItems(resultsList)
            }
          }
        }
      })
    }
  }

  @FXML
  private def exportToCsv(event: ActionEvent) = {
    if (resultsList != null) {
      val results = resultsList.listIterator().asScala.toSeq
      val writer = new CSVWriter(results, Seq("Journal", "Score", "Abbreviation"))

      val chooser = new FileChooser
      chooser.setTitle("Export to CSV")
      chooser.setInitialFileName("export.csv")

      val primaryStage = resultsTable.getScene.getWindow
      val selectedFile = chooser.showSaveDialog(primaryStage)

      if (selectedFile != null) writer.write(selectedFile.getAbsolutePath)
    }

    else {
      exportWarning.setText("No rows to write!")
    }
  }
}
