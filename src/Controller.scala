
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.MapChangeListener
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, Slider, ToggleButton}
import javafx.scene.layout.{AnchorPane, FlowPane, GridPane}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.stage.{FileChooser, Stage}
import javafx.util.Duration

import java.io.File
import scala.collection.mutable.ListBuffer



class Controller {

  var selectedFile: File = _
  var pick: Media = _
  var player: MediaPlayer = _

  @FXML private var centerGrid: GridPane = _
  @FXML private var bottomGrid: GridPane = _
  @FXML private var togglePlayPause: ToggleButton = _
  @FXML private var chooseFileButton: Button = _
  @FXML private var musicNameLabel: Label = _
  @FXML private var seekButton: Button = _
  @FXML private var volumeSlider: Slider = _
  @FXML private var durationSlider: Slider = _
  @FXML private var minDurationLabel: Label = _
  @FXML private var maxDurationLabel: Label = _
  @FXML private var volumeLabel: Label = _
  @FXML private var musicListFlowPane: FlowPane = _


  def chooseFile(): Unit = {
    val stage: Stage = (chooseFileButton.getScene.getWindow).asInstanceOf[Stage]
    val fileChooser = new FileChooser
    selectedFile = fileChooser.showOpenDialog(stage)
    pick = new Media(selectedFile.toURI.toString)
    //val mc=MediaController(selectedFile.getAbsolutePath)
    player = new MediaPlayer(pick)

    val info:ListBuffer[(String,AnyRef)]=ListBuffer[(String,AnyRef)]()
    musicNameLabel.setText(selectedFile.getName)

    pick.getMetadata().addListener(new MapChangeListener[String , AnyRef]{
      override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: AnyRef]): Unit ={
        if(change.wasAdded()){
          info.addOne((change.getKey() , change.getValueAdded))
        }
       }
    })

    player.currentTimeProperty().addListener(new ChangeListener[Duration] {
      override def changed(observable: ObservableValue[_ <: Duration], oldValue: Duration, newValue: Duration): Unit = {
        val time:(Int,Int,Int)= msToMinSec(newValue)
        currentTimeLabelSet(time._2+":"+time._3)
        setDurationSlider((newValue.toSeconds * 100) / pick.getDuration.toSeconds)
      }
    })

    player.setOnReady(new Runnable {
      override def run(): Unit = {
        setSeekSlider()
      }
    })
  }

  def setSeekSlider(): Unit = {
    //minDurationLabel.setText(math.round(seektime).toString)
    val time:(Int,Int,Int)= msToMinSec(pick.getDuration)
    maxDurationLabel.setText(time._2+":"+time._3)
  }

  def currentTimeLabelSet(time: String): Unit = {
    minDurationLabel.setText(time)
  }

  def setDurationSlider(value: Double): Unit = {
    durationSlider.setValue(value)
  }

  def seek(): Unit = {
    player.seek(player.getCurrentTime.add(new Duration(5000)))
  }

  def playpause(): Unit = {

    if (!player.getStatus.equals(MediaPlayer.Status.PLAYING)) {
      togglePlayPause.setText("Pause")
      player.play()
    } else {
      togglePlayPause.setText("Play")
      player.pause()
    }

  }

  def dragDuration(): Unit = {
    this.synchronized{
      val seektime: Double = ((durationSlider.getValue * pick.getDuration.toMillis) / 100)
      player.seek(new Duration(seektime))
    }
  }

  def setVolume(): Unit = {
    val volume: Double = volumeSlider.getValue
    player.setVolume(volume / 100)
    volumeLabel.setText(volume.toInt.toString + "%")

  }

  private def msToMinSec(duration: Duration):(Int,Int,Int)={
    val hours:Int   = math.floor(duration.toHours).toInt
    val minutes:Int = math.floor(duration.toMinutes).toInt-(hours*60)
    val sec:Int     = math.floor(duration.toSeconds).toInt-(hours*60*60)-(minutes*60)

    (hours,minutes,sec)
  }

}
