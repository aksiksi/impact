package com.idealicious

import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.application.{Platform, Application}
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.{WindowEvent, Stage}

object App {
  def main(args: Array[String]) = {
    Application.launch(classOf[App], args: _*)
  }
}

class App extends Application {
  var layout: VBox = _
  var primaryStage: Stage = _

  def start(primaryStage: Stage) = {
    this.primaryStage = primaryStage

    this.primaryStage.setTitle("Impact")
    this.primaryStage.setResizable(false)

    setupRootLayout()

    primaryStage.show()
  }

  def setupRootLayout() = {
    val loader = new FXMLLoader

    // URL root is the resources folder
    loader.setLocation(getClass.getResource("/design.fxml"))
    loader.setController(new ViewController)

    layout = loader.load()

    val scene = new Scene(layout)

    primaryStage.setScene(scene)
  }
}