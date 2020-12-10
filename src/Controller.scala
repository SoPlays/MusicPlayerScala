
import Data._
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.transformation.FilteredList
import javafx.collections.{ListChangeListener, MapChangeListener, ObservableList}
import javafx.event.EventHandler
import javafx.fxml.{FXML, FXMLLoader}
import javafx.geometry.Pos
import javafx.scene.{Parent, Scene}
import javafx.scene.control.{Alert, Button, ComboBox, Label, ListCell, ListView, MultipleSelectionModel, SelectionMode, Slider, Tab, TabPane, TextArea, TextField, ToggleButton}
import javafx.scene.layout.{AnchorPane, BorderPane, FlowPane, GridPane, StackPane}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.scene.{Parent, Scene}
import javafx.stage.{DirectoryChooser, FileChooser, Modality, Stage}
import javafx.util.{Callback, Duration}

import java.io.{File, FileInputStream, InputStream}
import java.util.Arrays.stream
import java.util.stream.StreamSupport.stream
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent

import scala.annotation.tailrec

class Controller {
  //Panes
  @FXML private var BaseBorderPane: BorderPane = _
  @FXML private var centerGrid: GridPane = _
  @FXML private var bottomGrid: GridPane = _
  @FXML private var volumeLabel: Label = _
  @FXML private var leftPane: AnchorPane = _

  //play
  @FXML private var musicNameLabel: Label = _
  @FXML private var togglePlayPause: ToggleButton = _
  @FXML private var volumeSlider: Slider = _
  @FXML private var balanceSlider: Slider = _
  @FXML private var durationSlider: Slider = _
  @FXML private var minDurationLabel: Label = _
  @FXML private var maxDurationLabel: Label = _
  @FXML private var randomButton: ToggleButton = _
  @FXML private var repeatButton: ToggleButton = _
  @FXML private var FastForwardButton: Button = _
  @FXML private var ResetForwardButton: Button = _
  @FXML private var SlowForwardButton: Button = _
  @FXML private var rateLabel: Label = _

  //Tabs
  @FXML private var PlayTab: Tab = _
  @FXML private var AlbumsTab: Tab = _
  @FXML private var ArtistsTab: Tab = _
  @FXML private var PlaylistsTab: Tab = _
  @FXML private var ImportTab: Tab = _
  @FXML private var TabPane: TabPane = _


  //import
  @FXML private var chooseFileButton: Button = _
  @FXML private var chooseDirectoryButton: Button = _

  //listviews
  @FXML private var listSongs: ListView[Song] = new ListView()
  @FXML private var listAlbums: ListView[Album] = new ListView()
  @FXML private var listArtists: ListView[Artist] = new ListView()
  @FXML private var listPlaylist: ListView[Playlist] = new ListView()
  @FXML private var listSongsAlbum: ListView[Song] = new ListView()
  @FXML private var listSongsPlaylist: ListView[Song] = new ListView()

  @FXML private var listSongsArtist: ListView[Song] = new ListView()
  @FXML private var listAlbumsArtist: ListView[Album] = new ListView()

  //Artist view
  @FXML private var addToPlaylistArtist: Button = _
  @FXML private var ArtistShowAlbumOrSong: ComboBox[String] = _

  //Album view
  @FXML private var addToPlaylistAlbum: Button = _

  //Playlist view
  @FXML private var editPlaylistButton: Button = _
  @FXML private var createPlaylistButton: Button = _
  @FXML private var removePlaylistButton: Button = _
  @FXML private var remFromPlayButton: Button = _

  @FXML private var image: ImageView = new ImageView()

  var mediaPlayer: MediaPlayer = _
  var volume: Double = 100

  def initialize(): Unit = {
    DatabaseFunc.loadfiles()
    setLoadedListeners()
    listPlaylist.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    listSongs.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    listSongsAlbum.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    listSongsArtist.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)

    ArtistShowAlbumOrSong.getItems.addAll("Albums","Songs")
    ArtistShowAlbumOrSong.getSelectionModel.select(0)
    listSongsArtist.setVisible(false)

    /*listAlbumsArtist.setOnMouseClicked(new EventHandler[MouseEvent]{

      override def handle(click: MouseEvent): Unit = {
        if(click.getClickCount == 2){
          val album:Album = listAlbumsArtist.getSelectionModel.getSelectedItem

          TabPane.getSelectionModel.clearSelection()
          TabPane.getSelectionModel.select(AlbumsTab)
        }
      }
    })*/

    setCellFactories()
    /*
    listSongs.setItems(Song.loaded)
    listPlaylist.setItems(Playlist.loaded)
    listAlbums.setItems(Album.loaded)
    listArtists.setItems(Artist.loaded)
    */
    updateListSongs()
    updateListAlbums()
    updateListArtists()
    updateListPlaylists()
    //val firstSongPath:String=Song.loaded(0).filepath
    //mediaPlayer = new MediaPlayer(new Media(new File(firstSongPath).toURI.toString))
  }
  private def setCellFactories(): Unit ={
    listSongs.setCellFactory(new Callback[ListView[Song], ListCell[Song]](){
      def call(p:ListView[Song] ):ListCell[Song] = {
        val cell: ListCell[Song] = new ListCell[Song] {
          override def updateItem(t: Song, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })
    listArtists.setCellFactory(new Callback[ListView[Artist], ListCell[Artist]](){
      def call(p:ListView[Artist] ):ListCell[Artist] = {
        val cell: ListCell[Artist] = new ListCell[Artist] {
          override def updateItem(t: Artist, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })
    listAlbums.setCellFactory(new Callback[ListView[Album], ListCell[Album]](){
      def call(p:ListView[Album] ):ListCell[Album] = {
        val cell: ListCell[Album] = new ListCell[Album] {
          override def updateItem(t: Album, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })

    listPlaylist.setCellFactory(new Callback[ListView[Playlist], ListCell[Playlist]](){
      def call(p:ListView[Playlist] ):ListCell[Playlist] = {
        val cell: ListCell[Playlist] = new ListCell[Playlist] {
          override def updateItem(t: Playlist, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              val themeString = if(t.theme.isEmpty){""}else{"("+t.theme+")"}
              setText(t.name+themeString)
            }
          }
        }
        cell
      }
    })
    listSongsArtist.setCellFactory(new Callback[ListView[Song], ListCell[Song]](){
      def call(p:ListView[Song] ):ListCell[Song] = {
        val cell: ListCell[Song] = new ListCell[Song] {
          override def updateItem(t: Song, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })
    listSongsAlbum.setCellFactory(new Callback[ListView[Song], ListCell[Song]](){
      def call(p:ListView[Song] ):ListCell[Song] = {
        val cell: ListCell[Song] = new ListCell[Song] {
          override def updateItem(t: Song, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })
    listSongsPlaylist.setCellFactory(new Callback[ListView[Song], ListCell[Song]](){
      def call(p:ListView[Song] ):ListCell[Song] = {
        val cell: ListCell[Song] = new ListCell[Song] {
          override def updateItem(t: Song, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })
    listAlbumsArtist.setCellFactory(new Callback[ListView[Album], ListCell[Album]](){
      def call(p:ListView[Album] ):ListCell[Album] = {
        val cell: ListCell[Album] = new ListCell[Album] {
          override def updateItem(t: Album, bln: Boolean): Unit={
            super.updateItem(t,bln)
            if (bln || t == null) {
              setText("")
            }else {
              setText(t.name)
            }
          }
        }
        cell
      }
    })

  }
  def fastForward(): Unit = {
    //mediaPlayer.setRate(mediaPlayer.getCurrentRate*2)

    if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
      showMediaNullDialogWarning()
    } else {
      val rate: Double = if (mediaPlayer.getRate > 1) {
        math.min(mediaPlayer.getRate + 1, 8)
      } else {
        math.min(mediaPlayer.getRate * 2, 8)
      }

      mediaPlayer.setRate(rate)
      rateLabel.setText("rate:" + rate + "x")

    }
  }
  def resetForward(): Unit = {
    //mediaPlayer.setRate(mediaPlayer.getCurrentRate*2)

    if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
      showMediaNullDialogWarning()
    } else {
      mediaPlayer.setRate(1)
      rateLabel.setText("rate:" + 1 + "x")
    }
  }
  def slowForward(): Unit = {
    //mediaPlayer.setRate(mediaPlayer.getCurrentRate*2)

    if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
      showMediaNullDialogWarning()
    } else {
      val rate: Double = if (mediaPlayer.getRate > 1) {
        mediaPlayer.getRate - 1
      } else {
        math.max(mediaPlayer.getRate / 2, 0.125)
      }

      mediaPlayer.setRate(rate)
      rateLabel.setText("rate:" + rate + "x")
    }
  }

  //Imports
  def importMusic(): Unit = {
    val stage: Stage = chooseFileButton.getScene.getWindow.asInstanceOf[Stage]
    val fileChooser = new FileChooser
    val selectedFile: File = fileChooser.showOpenDialog(stage)

    if (selectedFile.getName.endsWith(".mp3")) {
      uploadSong(selectedFile)
    }

  }
  def importFolder(): Unit = {

    def aux(file: File): Unit = {
      if (file.exists() && file.isDirectory) {
        file.listFiles().filter(_.getName.endsWith(".mp3")).foreach(aux)
        file.listFiles().filter(_.isDirectory).foreach(aux)
      } else if (file.exists() && file.isFile) {
        if (file.getName.endsWith(".mp3")) {
          uploadSong(file)
        }
      }
    }

    val stage: Stage = chooseFileButton.getScene.getWindow.asInstanceOf[Stage]
    val directoryChooser = new DirectoryChooser
    val selectedDirectory: File = directoryChooser.showDialog(stage)

    aux(selectedDirectory)

  }

  //Setters
  def setSeekSlider(): Unit = {
    //minDurationLabel.setText(math.round(seektime).toString)
    val time: (Int, Int, Int) = msToMinSec(mediaPlayer.getTotalDuration)
    val hours: String = if (time._1 != 0) {
      time._1.toString + ":"
    } else {
      ""
    }
    val sec: String = {
      if (time._3 - 10 < 0) {
        "0" + time._3.toString
      } else {
        time._3.toString
      }
    }
    val min: String = {
      if (time._1 != 0) {
        if (time._2 - 10 < 0) {
          "0" + time._2 + ":"
        } else {
          time._2 + ":"
        }
      } else {
        time._2 + ":"
      }
    }
    maxDurationLabel.setText(hours + min + sec)
    minDurationLabel.setText("-")
  }
  def currentTimeLabelSet(time: String): Unit = {
    minDurationLabel.setText(time)
  }
  def setDurationSlider(value: Double): Unit = {
    durationSlider.setValue(value)
  }

  //Listeners
  def setListeners(): Unit = {
    //progress Slider updater
    mediaPlayer.currentTimeProperty().addListener(new ChangeListener[Duration] {
      override def changed(observable: ObservableValue[_ <: Duration], oldValue: Duration, newValue: Duration): Unit = {
        val currtime: (Int, Int, Int) = msToMinSec(newValue)
        val hours: String = if (currtime._1 != 0) {
          currtime._1.toString + ":"
        } else {
          ""
        }
        val min: String = {
          if (hours.nonEmpty) {
            if (currtime._2 - 10 < 0) {
              "0" + currtime._2 + ":"
            } else {
              currtime._2 + ":"
            }
          } else {
            currtime._2 + ":"
          }
        }
        val currSec: String = {
          if (currtime._3 - 10 < 0) {
            "0" + currtime._3.toString
          }
          else {
            currtime._3.toString
          }
        }
        currentTimeLabelSet(hours + min + currSec)

        setDurationSlider((newValue.toSeconds * 100) / mediaPlayer.getTotalDuration.toSeconds)

      }
    })

    //onEndoFMedia
    mediaPlayer.setOnEndOfMedia(() => {
      next()
    })

  }
  def setLoadedListeners(): Unit = {
    Song.loaded.addListener(new ListChangeListener[Song] {
      override def onChanged(change: ListChangeListener.Change[_ <: Song]): Unit = {
        while (change.next()) {
          if (change.wasAdded()) {
            println("Uma nova música foi adicionada")
            updateListSongs()
          }
        }
      }
    })

    Artist.loaded.addListener(new ListChangeListener[Artist] {
      override def onChanged(change: ListChangeListener.Change[_ <: Artist]): Unit = {
        while (change.next()) {
          if (change.wasAdded()) {
            println("Um novo artista foi adicionado")
            updateListArtists()
          }
        }
      }
    })

    Album.loaded.addListener(new ListChangeListener[Album] {
      override def onChanged(change: ListChangeListener.Change[_ <: Album]): Unit = {
        while (change.next()) {
          if (change.wasAdded()) {
            println("Um novo album foi adicionado")
            updateListAlbums()
          }
        }
      }
    })

    Playlist.loaded.addListener(new ListChangeListener[Playlist] {
      override def onChanged(change: ListChangeListener.Change[_ <: Playlist]): Unit = {
        while (change.next()) {
          if (change.wasAdded()) {
            println("Uma nova Playlist foi adicionada")
            updateListPlaylists()
          }
        }
      }
    })

  }

  //Liseners End
  def mediaChange(filepath: String): Unit = {

    val media: Try[Media] = Try(new Media(new File(filepath).toURI.toString))
    media match {
      case Success(v) =>
        if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
          //mediaPlayer has not been instanciated
          mediaPlayer = new MediaPlayer(v)
          mediaPlayer.setVolume(volumeSlider.getValue)
          mediaPlayer.setBalance(balanceSlider.getValue)
        } else {
          val volume = mediaPlayer.getVolume
          val rate: Double = mediaPlayer.getRate
          mediaPlayer.dispose()
          mediaPlayer = new MediaPlayer(v)
          mediaPlayer.setVolume(volume)
          mediaPlayer.setRate(rate)

        }
        setListeners()
        //resetPlayButton()
        mediaPlayer.statusProperty().addListener(new ChangeListener[MediaPlayer.Status] {
          override def changed(observable: ObservableValue[_ <: MediaPlayer.Status], oldValue: MediaPlayer.Status, newValue: MediaPlayer.Status): Unit = {
            if (newValue.equals(MediaPlayer.Status.READY)) {
              setSeekSlider()


            }
          }
        })
        imageSong(v)
      case Failure(e) =>
        print("Erro a criar media")
        throw e

    }

  }
  private def uploadSong(selectedFile: File): Unit = {
    val media = new Media(selectedFile.toURI.toString)
    val metadataMediaPlayer = new MediaPlayer(media)
    val info: ListBuffer[(String, AnyRef)] = ListBuffer[(String, AnyRef)]()

    media.getMetadata.addListener(new MapChangeListener[String, AnyRef] {
      override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: AnyRef]): Unit = {
        if (change.wasAdded()) {
          info.addOne((change.getKey, change.getValueAdded))
        }
      }
    })

    val runner = new Runnable {
      override def run(): Unit = {

        println(info)
        val album: String = info.filter(x => x._1.equals("album")).head._2.toString.trim
        val artist: String = info.filter(x => x._1.equals("artist")).map(_._2).remove(0).toString.split(",").head.trim
        val songid = DatabaseFunc.getlastidSongs(Song.loaded) + 1

        val albumcheck = Album.loaded.filtered(x => x.name.equals(album))

        val artistcheck = Artist.loaded.filtered(x => x.name.equals(artist))

        println()
        println()
        println(album + "   " + albumcheck)
        println(artist + "   " + artistcheck)
        println()
        println()

        val artistid: Int = {
          if (artistcheck.isEmpty) {
            val newid: Int = DatabaseFunc.getlastidArtists(Artist.loaded) + 1
            newid
          } else {
            artistcheck.get(0).id
          }
        }
        //album exists? if not creating it

        val albumid: Int = {
          if (albumcheck.isEmpty) {
            val newid: Int = DatabaseFunc.getlastidAlbums(Album.loaded) + 1
            Album.loaded.add(Album(List(newid.toString, album, songid.toString, artistid.toString)))
            newid
          } else {
            val album_temp = albumcheck.get(0).addSong(songid)
            Album.loaded.remove(albumcheck.get(0))
            Album.loaded.add(album_temp)
            albumcheck.get(0).id
          }
        }

        //artist exists? if not creating it

        if (artistcheck.isEmpty) {
          Artist.loaded.add(Artist(List(artistid.toString, artist, albumid.toString, songid.toString)))
        } else {
          val artist_temp = artistcheck.get(0).addSong(songid)
          Artist.loaded.remove(artistcheck.get(0))
          Artist.loaded.add(artist_temp.addAlbum(albumid))
        }


        val nomeFeats = info.filter(x => x._1.equals("artist")).map(_._2).remove(0).toString.split(", ").tail.toList

        val idFeats = nomeFeats.map(x => DatabaseFunc.GetIDArtistOrCreateFeats(x.trim, songid.toString))

        val trackNaux: ListBuffer[(String, AnyRef)] = info.filter(x => x._1.equals("track number"))
        val trackN = if (trackNaux.isEmpty) {
          0
        } else {
          trackNaux.head._2.toString.trim
        }

        val song: Song = Song(List[String](
          songid.toString, //0
          info.filter(x => x._1.equals("title")).head._2.toString.trim, //1
          selectedFile.getAbsolutePath, //2 filepath
          artistid.toString, //4 artist
          info.filter(x => x._1.equals("genre")).head._2.toString, //5 genre
          albumid.toString, //6 album
          idFeats.mkString(" "), //7 feats
          0.toString, //8
          trackN.toString, //9 TrackNumber resolver
        )
        )
        Song.loaded.add(song)

      }
    }
    metadataMediaPlayer.setOnReady(runner)

  }

  //UpdateLists
  def updateListSongs(): Unit = {
    listSongs.getItems.clear()
    Song.loaded.forEach(x => listSongs.getItems.add(x))
    println("------------- número de items " + listSongs.getItems.size)
    listSongs.getItems.forEach(println)
  }
  def updateListAlbums(): Unit = {
    listAlbums.getItems.clear()
    Album.loaded.forEach(x => listAlbums.getItems.add(x))
    println("------------- número de items " + listAlbums.getItems.size)
    listAlbums.getItems.forEach(println)
  }
  def updateListArtists(): Unit = {
    listArtists.getItems.clear()
    Artist.loaded.forEach(x => listArtists.getItems.add(x))
    println("------------- número de items " + listArtists.getItems.size)
    listArtists.getItems.forEach(println)
  }
  def updateListPlaylists(): Unit = {
    listPlaylist.getItems.clear()
    Playlist.loaded.forEach(x => listPlaylist.getItems.add(x))
    println("------------- número de items " + listPlaylist.getItems.size)
    listPlaylist.getItems.forEach(println)
  }

  //MediaControl
  def playpause(): Unit = {
    if (showMediaNullDialogWarning()) {
      if (!mediaPlayer.getStatus.equals(MediaPlayer.Status.PLAYING)) {
        togglePlayPause.setSelected(true)
        togglePlayPause.setText("Pause")
        mediaPlayer.play()
      } else {
        togglePlayPause.setSelected(false)
        togglePlayPause.setText("Play")
        mediaPlayer.pause()
      }
    }else{
      resetPlayButton()
    }

  }
  def before(): Unit = {
    val listview: ListView[Song] = listSongs
    if (showMediaNullDialogWarning()) {
      val song: Song = listview.getSelectionModel.getSelectedItems.get(0)
      if (mediaPlayer.getCurrentTime.toSeconds > 3) {
        mediaPlayer.seek(new Duration(0))
      } else {
        if (repeatButton.isSelected) {
          mediaPlayer.seek(new Duration(0))
        }
        else if (randomButton.isSelected) {
          val r = scala.util.Random
          val pos = r.nextInt(listview.getItems.size())
          gotoSong(listview, pos)
        } else {
          val pos = listview.getItems.lastIndexOf(song) - 1
          if (pos < 0) {
            gotoSong(listview, listview.getItems.size - 1)
          } else {
            gotoSong(listview, pos)
          }
        }
      }
    }
  }
  def next(): Unit = {
    val listview: ListView[Song] = listSongs

    if (showMediaNullDialogWarning()) {
      val song: Song = listview.getSelectionModel.getSelectedItems.get(0)
      if (repeatButton.isSelected) {
        mediaPlayer.seek(new Duration(0))
      }
      else if (randomButton.isSelected) {
        val r = scala.util.Random
        val pos = r.nextInt(listSongs.getItems.size())
        gotoSong(listview, pos)
      } else {
        val pos = listview.getItems.lastIndexOf(song) + 1
        if (pos > listview.getItems.size - 1) {
          gotoSong(listview, 0)
        } else {
          gotoSong(listview, pos)
        }
      }
    }
  }
  def random(): Unit = {
    if (showMediaNullDialogWarning()) {
      if (repeatButton.isSelected) {
        repeatButton.setSelected(false)
        mediaPlayer.setCycleCount(1)
      }
    }
  }
  def repeat(): Unit = {
    if (showMediaNullDialogWarning()) {
      if (randomButton.isSelected) {
        randomButton.setSelected(false)
      }
      if (repeatButton.isSelected) {
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE)
      } else {
        mediaPlayer.setCycleCount(1)
      }
    }
  }
  def dragDuration(): Unit = {
    if (showMediaNullDialogWarning()) {
      this.synchronized {
        val seektime: Double = (durationSlider.getValue * mediaPlayer.getTotalDuration.toMillis) / 100
        mediaPlayer.seek(new Duration(seektime))
      }
    }
  }
  def setVolume(): Unit = {
    if(!mediaPlayer.isInstanceOf[MediaPlayer]){
      volumeLabel.setText("vol:" + volumeSlider.getValue.toInt.toString + "%")
    }else {
      volumeLabel.setText("vol:" + volumeSlider.getValue.toInt.toString + "%")
      mediaPlayer.setVolume(volumeSlider.getValue / 100)
    }
  }
  def muteVolume(): Unit = {
    if(volumeSlider.getValue==0){volumeSlider.adjustValue(volume); setVolume()}
    else {volume=volumeSlider.getValue;volumeSlider.adjustValue(0); setVolume()}
  }
  def setBalance(): Unit = {
    if(mediaPlayer.isInstanceOf[MediaPlayer]){
      mediaPlayer.setBalance(balanceSlider.getValue)
    }
  }
  def resetBalance(): Unit = {
    balanceSlider.adjustValue(0)
    setBalance()
  }

  def selectFromListSongs(): Unit = {
    val song: Song = listSongs.getSelectionModel.getSelectedItems.get(0)
    mediaChange(song.filepath)
    musicNameLabel.setText(song.name)

  }
  //Display Song from
  def selectFromListAlbums(): Unit = {
    val album: Album = listAlbums.getSelectionModel.getSelectedItems.get(0)
    val songsAlbum:FilteredList[Song] = Song.loaded.filtered(x=> x.album == album.id)
    listSongsAlbum.getItems.clear()
    listSongsAlbum.getItems.addAll(songsAlbum)

  }
  def selectFromListArtist(): Unit = {
    val artist: Artist = if(listArtists.getSelectionModel.getSelectedItems.isEmpty){
      listArtists.getSelectionModel.select(0)
      listArtists.getItems.get(0)
    }else{
      listArtists.getSelectionModel.getSelectedItems.get(0)
    }
    val songsArtist: FilteredList[Song] = Song.loaded.filtered(x => x.artist == artist.id || x.feats.contains(artist.id))
    val albunsArtist:FilteredList[Album] = Album.loaded.filtered(x => x.artist == artist.id )

    listSongsArtist.getItems.clear()
    listSongsArtist.getItems.addAll(songsArtist)

    listAlbumsArtist.getItems.clear()
    listAlbumsArtist.getItems.addAll(albunsArtist)
  }
  def selectFromListPlaylist(): Unit = {
    val playlist: Playlist = listPlaylist.getSelectionModel.getSelectedItems.get(0)
    val songs:List[Int] = playlist.songs
    val songsPlaylist:FilteredList[Song] = Song.loaded.filtered(x=> songs.contains(x.id))
    listSongsPlaylist.getItems.clear()
    listSongsPlaylist.getItems.addAll(songsPlaylist)

  }

  //Artists
  def chooseListArtist(): Unit ={
    val listType:String = ArtistShowAlbumOrSong.getSelectionModel.getSelectedItem
    if(listType.equals("Albums")){
      listAlbumsArtist.setVisible(true)
      listSongsArtist.setVisible(false)
    }else if (listType.equals("Songs")){
      listAlbumsArtist.setVisible(false)
      listSongsArtist.setVisible(true )
    }
  }

  //Playlists
  def createPlaylist(): Unit = {
    val loader:FXMLLoader=new FXMLLoader(getClass.getResource("CreatePlaylist.fxml"))
    val parent:Parent = loader.load().asInstanceOf[Parent]
    val stage:Stage = new Stage()
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.setTitle("CreatePlaylist")

    stage.setScene(new Scene(parent))
    stage.show()
  }
  def removePlaylist(): Unit ={
    val toRemove :ObservableList[Playlist]= listPlaylist.getSelectionModel.getSelectedItems
    toRemove.forEach(x=> {
      Playlist.loaded.remove(x)
    })
    updateListPlaylists()
  }
  def addToPlayFromAlbum(): Unit = {
    val lst:List[Song] =ObservableListToList[Song](listSongsAlbum.getSelectionModel.getSelectedItems ,List[Song](), 0)

    val playlist:Playlist = listPlaylist.getSelectionModel.getSelectedItem
    val songs:List[Int]  = lst.map(x=>x.id)
    playlist.addSong(songs)
  }
  def remFromPlay(): Unit ={
    val lst:List[Int] =  ObservableListToList(listSongsPlaylist.getSelectionModel.getSelectedItems,List(),0).map(x=>x.id)
    val playlist:Playlist = listPlaylist.getSelectionModel.getSelectedItem
    playlist.removeSong(lst)
  }

  //Auxiliaries
  private def gotoSong(listView: ListView[Song], pos: Int): Unit = {
    val newSong: Song = listView.getItems.get(pos)
    listView.getSelectionModel.clearAndSelect(pos)
    mediaChange(newSong.filepath)
    musicNameLabel.setText(newSong.name)
    mediaPlayer.play()
  }
  private def msToMinSec(duration: Duration): (Int, Int, Int) = {
    val hours: Int = math.floor(duration.toHours).toInt
    val minutes: Int = math.floor(duration.toMinutes).toInt - (hours * 60)
    val sec: Int = math.floor(duration.toSeconds).toInt - (hours * 60 * 60) - (minutes * 60)

    (hours, minutes, sec)
  }
  private def resetPlayButton(): Unit = {
    togglePlayPause.setSelected(false)
    togglePlayPause.setText("Play")
  }

  private def showMediaNullDialogWarning(): Boolean = {
    val title: String = "You should select a Song to play first"
    if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
      val alert = new Alert(AlertType.WARNING)
      alert.setTitle("Warning Dialog")
      alert.setHeaderText(title)

      alert.showAndWait()
    }
    mediaPlayer.isInstanceOf[MediaPlayer]
  }
  @tailrec
  private def ObservableListToList[A](oblst:ObservableList[A],list:List[A], index:Int): List[A] ={
    if(oblst.size() == index){
      list
    } else{
      val obj:A=oblst.get(index)
      ObservableListToList(oblst, list:::List(obj), index+1)
    }
  }


  private def setImage(imageSong: Image): Unit ={
    image.setImage(imageSong)
    println(image.getImage)
  }
  private def imageSong(media: Media): Unit ={
    val metadataMediaPlayer = new MediaPlayer(media)
    val info: ListBuffer[(String, AnyRef)] = ListBuffer[(String, AnyRef)]()
    media.getMetadata.addListener(new MapChangeListener[String, AnyRef] {
      override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: AnyRef]): Unit = {
        if (change.wasAdded()) {
          info.addOne((change.getKey, change.getValueAdded))
        }
      }
    })
    val runner = new Runnable {
      override def run(): Unit = {
        val imageSong = info.filter(x => x._1.equals("image"))
        if(imageSong.isEmpty){
          val stream = new FileInputStream("Images/default.png")
          val imageSong = new Image(stream)
          println(imageSong.getHeight)
          setImage(imageSong)
        }else setImage(imageSong.map(_._2).remove(0).asInstanceOf[Image])
      }
    }
    metadataMediaPlayer.setOnReady(runner)
  }
}
