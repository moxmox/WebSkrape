package WebSkrape

import WebSkrape.controllers.MainWindowController
import WebSkrape.controllers.Modes
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.stage.FileChooser
import tornadofx.*
import tornadofx.App
import tornadofx.View
import java.io.File

class MainTornado : App(MainWindow::class)

class MainWindow : View(){
    override val root: Pane by fxml("views/MainWindow.fxml")

    val controller: MainWindowController by inject()

    val singleSkrapeButton: Button by fxid()
    val singleUrlField: TextField by fxid()
    val uploadListButton: Button by fxid()
    val listLabel: Label by fxid()
    val listSkrapeButton: Button by fxid()
    val emailListView: ListView<String> by fxid()
    val phoneNumberListView: ListView<String> by fxid()

    var listPath = ""

    init {

        singleSkrapeButton.onAction = EventHandler{
            controller.skrape(singleUrlField.text, Modes.MODE_SINGLE){ emails, numbers ->
                emailListView.items.addAll(emails)
                phoneNumberListView.items.addAll(numbers)
            }
        }

        uploadListButton.onAction = EventHandler {
            chooseFile("Select url list", arrayOf(FileChooser.ExtensionFilter("Text Files", "*.txt")), FileChooserMode.Single)
                    .let {
                        listLabel.text = it[0].name
                        listPath = "${it[0].path}"
                    }
        }

        listSkrapeButton.onAction = EventHandler {
            /*controller.crawl{ emails, numbers ->
                println("crawl started...")
                emailListView.items.addAll(emails)
                phoneNumberListView.items.addAll(numbers)
            }*/

            //rxCrawl call will go here

            runAsync { File(listPath).readLines() }
                    .get().let {
                controller.rxCrawl(it){emails, numbers ->
                    emailListView.items.addAll(emails)
                    phoneNumberListView.items.addAll(numbers)
                }
            }

        }

        }

    }

