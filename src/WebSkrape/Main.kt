package WebSkrape

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import tornadofx.View

class Main : Application() {

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("views/WebSkrape.MainWindow.fxml"))
        primaryStage.title = "Hello World"
        primaryStage.scene = Scene(root, 750.0, 500.0)
        primaryStage.show()

    }


    companion object {
        fun main(args: Array<String>) {
            Application.launch(*args)
        }
    }

}