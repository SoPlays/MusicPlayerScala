import Data.Album.{delete, getSongs}
import Data.DatabaseFunc.observableListToList
import Data._
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.transformation.FilteredList
import javafx.collections.{ListChangeListener, MapChangeListener, ObservableList}
import javafx.concurrent.Task
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.{ButtonData, setButtonData}
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.{AnchorPane, BorderPane, GridPane}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.scene.{Parent, Scene}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{DirectoryChooser, FileChooser, Modality, Stage}
import javafx.util.{Callback, Duration}

import java.io.{File, FileInputStream}
import java.util.concurrent.ExecutorService
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Random, Success, Try}

class Controller {
  var mediaPlayer: MediaPlayer = _
  //Panes
  @FXML private var BaseBorderPane: BorderPane = _
  @FXML private var centerGrid: GridPane = _
  @FXML private var bottomGrid: GridPane = _
  @FXML private var leftPane: AnchorPane = _
  //play
  @FXML private var playButtonBar: ButtonBar = _
  @FXML private var musicNameLabel: Label = _
  @FXML private var artistNameLabel: Label = _
  @FXML private var albumNameLabel: Label = _
  @FXML private var volumeLabel: Label = _
  @FXML private var volumeSlider: Slider = _
  @FXML private var balanceSlider: Slider = _
  @FXML private var durationSlider: Slider = _
  @FXML private var minDurationLabel: Label = _
  @FXML private var maxDurationLabel: Label = _
  @FXML private var rateLabel: Label = _
  @FXML private var nowPlaying: Label = _
  //button
  @FXML private var togglePlayPause: ToggleButton = _
  @FXML private var fastForwardButton: Button = _
  @FXML private var resetForwardButton: Button = _
  @FXML private var slowForwardButton: Button = _
  @FXML private var shuffleToggleButton: ToggleButton = _
  @FXML private var repeatButton: ToggleButton = _
  @FXML private var nextButton: Button = _
  @FXML private var beforeButton: Button = _
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
  @FXML private var listQueue: ListView[Song] = new ListView()
  @FXML private var listAlbums: ListView[Album] = new ListView()
  @FXML private var listArtists: ListView[Artist] = new ListView()
  @FXML private var listPlaylist: ListView[Playlist] = new ListView()
  @FXML private var listSongsAlbum: ListView[Song] = new ListView()
  @FXML private var listSongsPlaylist: ListView[Song] = new ListView()
  @FXML private var listSongsArtist: ListView[Song] = new ListView()
  @FXML private var listAlbumsArtist: ListView[Album] = new ListView()
  //Artist view
  @FXML private var addToPlaylistArtistButton: Button = _
  @FXML private var addToPlaylistArtistCombo: ComboBox[Playlist] = new ComboBox[Playlist]()
  @FXML private var ArtistShowAlbumOrSong: ComboBox[String] = _
  //Album view
  @FXML private var addToPlaylistAlbumButton: Button = _
  @FXML private var addToPlaylistAlbumCombo: ComboBox[Playlist] = new ComboBox[Playlist]()
  //Playlist view
  @FXML private var editPlaylistButton: Button = _
  @FXML private var createPlaylistButton: Button = _
  @FXML private var removePlaylistButton: Button = _
  @FXML private var remFromPlayButton: Button = _
  @FXML private var shufflePlaylistButton: Button = _
  @FXML private var image: ImageView = new ImageView()
  //var volume: Double = 100
  //Queue
  private var oldQueue: List[Song] = _
  private var oldPos: Int = _
  private var isShuffled: Boolean = false
  private var repeatState: String = "No Repeat"

  //"Repeat 1 , "Repeat All" , "No Repeat"

  //Graphic
  private var pauseGraphic: ImageView = new ImageView()
  private var playGraphic: ImageView = new ImageView()
  private var nextGraphic: ImageView = new ImageView()
  private var beforeGraphic: ImageView = new ImageView()
  private var fastforwardGraphic: ImageView = new ImageView()
  private var slowforwardGraphic: ImageView = new ImageView()
  private var repeatGraphic: ImageView = new ImageView()
  private var repeat1Graphic: ImageView = new ImageView()
  private var repeatInfGraphic: ImageView = new ImageView()
  private var shuffleGraphic: ImageView = new ImageView()

  def initialize(): Unit = {
    loadButtonImages()
    setInitialButtonIcons()

    DatabaseFunc.loadfiles()
    setLoadedListeners()
    listPlaylist.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    listQueue.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    listSongsAlbum.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    listSongsArtist.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)


    ArtistShowAlbumOrSong.getItems.addAll("Albums", "Songs")
    ArtistShowAlbumOrSong.getSelectionModel.select(0)
    listSongsArtist.setVisible(false)

    setButtonData(togglePlayPause,ButtonData.APPLY)
    setButtonData(beforeButton,ButtonData.BACK_PREVIOUS)
    setButtonData(nextButton,ButtonData.NEXT_FORWARD)
    setButtonData(fastForwardButton,ButtonData.RIGHT)
    setButtonData(slowForwardButton,ButtonData.LEFT)

    setCellFactories()
    /*
    listSongs.setItems(Song.loaded)
    listPlaylist.setItems(Playlist.loaded)
    listAlbums.setItems(Album.loaded)
    listArtists.setItems(Artist.loaded)
    */
    //updateQueue()
    updateListAlbums()
    updateListArtists()
    updateListPlaylists()
    //val firstSongPath:String=Song.loaded(0).filepath
    //mediaPlayer = new MediaPlayer(new Media(new File(firstSongPath).toURI.toString))
  }

  private def setCellFactories(): Unit = {

    def callbackPlaylist = new Callback[ListView[Playlist], ListCell[Playlist]]() {
      def call(p: ListView[Playlist]): ListCell[Playlist] = {
        val cell: ListCell[Playlist] = new ListCell[Playlist] {
          override def updateItem(t: Playlist, bln: Boolean): Unit = {
            super.updateItem(t, bln)
            if (bln || t == null) {
              setText("")
            } else {
              setText(t.name)
            }
          }
        }
        cell
      }
    }

    def callbackSong = new Callback[ListView[Song], ListCell[Song]]() {
      def call(p: ListView[Song]): ListCell[Song] = {
        val cell: ListCell[Song] = new ListCell[Song] {
          override def updateItem(t: Song, bln: Boolean): Unit = {
            super.updateItem(t, bln)
            if (bln || t == null) {
              setText("")
            } else {
              setText(t.name)
            }
          }
        }
        cell
      }
    }

    def callbackAlbum = new Callback[ListView[Album], ListCell[Album]]() {
      def call(p: ListView[Album]): ListCell[Album] = {
        val cell: ListCell[Album] = new ListCell[Album] {
          override def updateItem(t: Album, bln: Boolean): Unit = {
            super.updateItem(t, bln)
            if (bln || t == null) {
              setText("")
            } else {
              setText(t.name)
            }
          }
        }
        cell
      }
    }

    def callbackArtist = new Callback[ListView[Artist], ListCell[Artist]]() {
      def call(p: ListView[Artist]): ListCell[Artist] = {
        val cell: ListCell[Artist] = new ListCell[Artist] {
          override def updateItem(t: Artist, bln: Boolean): Unit = {
            super.updateItem(t, bln)
            if (bln || t == null) {
              setText("")
            } else {
              setText(t.name)
            }
          }
        }
        cell
      }
    }

    listPlaylist.setCellFactory(new Callback[ListView[Playlist], ListCell[Playlist]]() {
      def call(p: ListView[Playlist]): ListCell[Playlist] = {
        val cell: ListCell[Playlist] = new ListCell[Playlist] {
          override def updateItem(t: Playlist, bln: Boolean): Unit = {
            super.updateItem(t, bln)
            if (bln || t == null) {
              setText("")
            } else {
              val themeString = if (t.theme.isEmpty) {
                ""
              } else {
                "(" + t.theme + ")"
              }
              setText(t.name + themeString)
            }
          }
        }
        cell
      }
    })

    listQueue.setCellFactory(callbackSong)
    listArtists.setCellFactory(callbackArtist)
    listAlbums.setCellFactory(callbackAlbum)


    listSongsArtist.setCellFactory(callbackSong)
    listSongsAlbum.setCellFactory(callbackSong)
    listSongsPlaylist.setCellFactory(callbackSong)
    listAlbumsArtist.setCellFactory(callbackAlbum)

    addToPlaylistAlbumCombo.setCellFactory(callbackPlaylist)
    addToPlaylistAlbumCombo.setButtonCell(new ListCell[Playlist] {
      override def updateItem(t: Playlist, bln: Boolean): Unit = {
        super.updateItem(t, bln)
        if (bln || t == null) {
          setText("")
        } else {
          setText(t.name)
        }

      }
    })

    addToPlaylistArtistCombo.setCellFactory(callbackPlaylist)
    addToPlaylistArtistCombo.setButtonCell(new ListCell[Playlist] {
      override def updateItem(t: Playlist, bln: Boolean): Unit = {
        super.updateItem(t, bln)
        if (bln || t == null) {
          setText("")
        } else {
          setText(t.name)
        }

      }
    })
  }

  def setLoadedListeners(): Unit = {

    Artist.loaded.addListener(new ListChangeListener[Artist] {
      override def onChanged(change: ListChangeListener.Change[_ <: Artist]): Unit = {
        while (change.next()) {
          if (change.wasAdded() || change.wasRemoved()) {
            //println("Um novo artista foi adicionado")
            updateListArtists()

          }
        }
      }
    })

    Album.loaded.addListener(new ListChangeListener[Album] {
      override def onChanged(change: ListChangeListener.Change[_ <: Album]): Unit = {
        while (change.next()) {
          if (change.wasAdded() || change.wasRemoved()) {
            //println("Um novo album foi adicionado")
            updateListAlbums()
            updateSongListAlbums()
            updateAlbumListArtists()
          }
        }
      }
    })

    Playlist.loaded.addListener(new ListChangeListener[Playlist] {
      override def onChanged(change: ListChangeListener.Change[_ <: Playlist]): Unit = {
        while (change.next()) {
          if (change.wasAdded() || change.wasRemoved()) {
            //println("Uma nova Playlist foi adicionada")
            updateListPlaylists()
            updateSongListPlaylists()

          }
        }
      }
    })

    Song.loaded.addListener(new ListChangeListener[Song] {
      override def onChanged(change: ListChangeListener.Change[_ <: Song]): Unit = {
        while (change.next()) {
          if (change.wasAdded() || change.wasRemoved()) {
            updateSongListPlaylists()
            updateSongListAlbums()
            updateSongListArtists()
          }
        }
      }
    })

  }

  //UpdateLists
  /*
  def updateQueue(): Unit = {
    listQueue.getItems.clear()
    Song.loaded.forEach(x => listQueue.getItems.add(x))
    println("------------- número de items " + listQueue.getItems.size)
    listQueue.getItems.forEach(println)
  }
  */
  def updateListAlbums(): Unit = {
    listAlbums.getItems.clear()
    observableListToList(Album.loaded).map(x => listAlbums.getItems.add(x))
    //println("------------- número de items " + listAlbums.getItems.size)
    //listAlbums.getItems.forEach(println)
  }

  def updateSongListAlbums(): Unit = {
    if (listAlbums.getSelectionModel.getSelectedItems.isEmpty) {
      listAlbums.getSelectionModel.clearAndSelect(0)
    }
    val album: Album = listAlbums.getSelectionModel.getSelectedItem
    if (album != null){
      listSongsAlbum.getItems.clear()
      val songsAlbum: List[Song] = album.getSongs()
      songsAlbum.map(listSongsAlbum.getItems.add)
    }
  }

  def updateListArtists(): Unit = {
    listArtists.getItems.clear()
    observableListToList(Artist.loaded).map(x => listArtists.getItems.add(x))
    //println("------------- número de items " + listArtists.getItems.size)
    //listArtists.getItems.forEach(println)
  }

  def updateSongListArtists(): Unit = {
    if (listArtists.getSelectionModel.getSelectedItems.isEmpty) {
      listArtists.getSelectionModel.clearAndSelect(0)
    }
    val artist: Artist = listArtists.getSelectionModel.getSelectedItem
    if(artist != null){
      listSongsArtist.getItems.clear()
      artist.getSongs().map(listSongsArtist.getItems.add)
    }
  }

  def updateAlbumListArtists(): Unit = {
    if (listAlbumsArtist.getSelectionModel.getSelectedItems.isEmpty) {
      listAlbumsArtist.getSelectionModel.clearAndSelect(0)
    }
    val artists: List[Artist] = observableListToList(listArtists.getSelectionModel.getSelectedItems)
    if(artists.nonEmpty){
      listAlbumsArtist.getItems.clear()
      artists.map(x => x.getAlbums().map(listAlbumsArtist.getItems.add))
    }
  }

  def updateListPlaylists(): Unit = {
    listPlaylist.getItems.clear()
    observableListToList(Playlist.loaded).map(x => listPlaylist.getItems.add(x))

    addToPlaylistAlbumCombo.getItems.clear()
    addToPlaylistAlbumCombo.getItems.addAll(Playlist.loaded)

    addToPlaylistArtistCombo.getItems.clear()
    addToPlaylistArtistCombo.getItems.addAll(Playlist.loaded)

    //println("------------- número de items " + listPlaylist.getItems.size)
    //listPlaylist.getItems.forEach(println)
    //updateSongListPlaylists()

  }

  def updateSongListPlaylists(): Unit = {
    if (!listPlaylist.getItems.isEmpty) {
      if (!listPlaylist.getSelectionModel.getSelectedItems.isEmpty) {

      val play: Playlist = listPlaylist.getSelectionModel.getSelectedItem
      //listSongsPlaylist.getItems.clear()
      //val songsPlaylist:List[Song] = observableListToList(Song.loaded).filter(x=>play.songs.contains(x.id))
      //val songsPlaylist: List[Song] = play.getSongs()

      //songsPlaylist.map(listSongsPlaylist.getItems.add)
      val listSongs: List[Song] = observableListToList(Song.loaded)
      def aux(lst: List[Song], x: Int): List[Song] = {
        lst.appendedAll(listSongs.filter(y => y.id == x))
      }

      val songsPlaylist = play.songs.foldLeft(List[Song]())(aux)
      listSongsPlaylist.getItems.clear()
      songsPlaylist.map(listSongsPlaylist.getItems.add)

      }
    } else {
      listSongsPlaylist.getItems.clear()
    }
  }

  def loadButtonImages(): Unit = {
    def aux(img: ImageView, path: String): Unit = {
      //img.setImage(new Image(getClass.getResourceAsStream(path)))
      val stream = new FileInputStream(path)
      img.setImage(new Image(stream))
      img.setFitHeight(32)
      img.setPreserveRatio(true)
    }

    aux(pauseGraphic, "Images/pause.png")
    aux(playGraphic, "Images/play.png")
    aux(nextGraphic, "Images/next.png")
    aux(beforeGraphic, "Images/before.png")
    aux(fastforwardGraphic, "Images/fastforward.png")
    aux(slowforwardGraphic, "Images/slowforward.png")
    aux(repeatGraphic, "Images/repeat.png")
    aux(repeat1Graphic, "Images/repeat1.png")
    aux(repeatInfGraphic, "Images/repeatInf.png")
    aux(shuffleGraphic, "Images/shuffle.png")
  }

  def setInitialButtonIcons(): Unit = {
    togglePlayPause.setGraphic(playGraphic)
    nextButton.setGraphic(nextGraphic)
    beforeButton.setGraphic(beforeGraphic)
    fastForwardButton.setGraphic(fastforwardGraphic)
    //resetForwardButton.setGraphic()
    slowForwardButton.setGraphic(slowforwardGraphic)
    repeatButton.setGraphic(repeatGraphic)
    shuffleToggleButton.setGraphic(shuffleGraphic)
  }

  //Imports

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

        //println(info)
        val album = info.filter(x => x._1.equals("album"))
        val artist = info.filter(x => x._1.equals("artist"))

        val albumName: String = if (album.isEmpty) {
          "Unknown"
        } else {
          album.remove(0)._2.toString.trim
        }
        val artistNames: List[String] = if (artist.isEmpty) {
          List("Unknown")
        } else {
          artist.remove(0)._2.toString.split(",").toList
        }

        val songid: Int = DatabaseFunc.getlastidSongs(Song.loaded) + 1
        val songcheck:List[Song] = observableListToList(Song.loaded).filter(_.name.equals())

        val albumcheck: List[Album] = observableListToList(Album.loaded).filter(x => x.name.equals(albumName))

        val artistcheck: List[Artist] = observableListToList(Artist.loaded).filter(x => x.name.equals(artistNames.head.trim))

        val artistid: Int = {
          if (artistcheck.isEmpty) {
            val newid: Int = DatabaseFunc.getlastidArtists(Artist.loaded) + 1
            newid
          } else {
            artistcheck.head.id
          }
        }
        //album exists? if not creating it

        val albumid: Int = {
          if (albumcheck.isEmpty) {
            val newid: Int = DatabaseFunc.getlastidAlbums(Album.loaded) + 1
            Album.load(List(newid.toString, albumName, songid.toString, artistid.toString).mkString(";"))
            newid
          } else {
            albumcheck.head.addSong(songid)
            albumcheck.head.id
          }
        }

        //artist exists? if not creating it

        if (artistcheck.isEmpty) {
          Artist.loaded.add(Artist(List(artistid.toString, artistNames.head.trim, albumid.toString, songid.toString)))
        } else {
          val artist_temp = artistcheck.head.addSong(songid)
          artist_temp.addAlbum(albumid)

        }


        val nomeFeats = artistNames.tail
        val idFeats = nomeFeats.map(x => DatabaseFunc.GetIDArtistOrCreateFeats(x.trim, songid.toString))


        val trackNaux: ListBuffer[(String, AnyRef)] = info.filter(x => x._1.equals("track number"))
        val trackN = if (trackNaux.isEmpty) {
          0
        } else {
          trackNaux.head._2.toString.trim
        }

        val title: String = if (info.filter(x => x._1.equals("title")).isEmpty) {
          //print(selectedFile.getName.split(".mp3"))
          selectedFile.getName.split(".mp3").head.trim
        } else {
          info.filter(x => x._1.equals("title")).head._2.toString
        }

        val genre: String = if (info.filter(x => x._1.equals("genre")).isEmpty) {
          "Unknown"
        } else {
          info.filter(x => x._1.equals("genre")).head._2.toString
        }

        val song: Song = Song(List[String](
          songid.toString, //0
          title, //1
          selectedFile.getAbsolutePath, //2 filepath
          artistid.toString, //4 artist
          genre, //5 genre
          albumid.toString, //6 album
          idFeats.mkString(" "), //7 feats
          0.toString, //8
          trackN.toString, //9 TrackNumber resolver
        )
        )
        Song.loaded.add(song)
        metadataMediaPlayer.dispose()
      }
    }
    metadataMediaPlayer.setOnReady(runner)
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
    val task: Task[Unit] = new Task[Unit]() {
      override def call(): Unit = aux(selectedDirectory)
    }
    val th:Thread = new Thread(task)
    th.setDaemon(true)
    th.start()


  }

  def importMusic(): Unit = {
    val stage: Stage = chooseFileButton.getScene.getWindow.asInstanceOf[Stage]
    val fileChooser = new FileChooser
    fileChooser.getExtensionFilters.addAll(
      new FileChooser.ExtensionFilter("All Files", "*.*"),
      new FileChooser.ExtensionFilter("MP3", "*.mp3*"))

    val selectedFile: File = fileChooser.showOpenDialog(stage)
    if (selectedFile != null) {
      if (selectedFile.getName.endsWith(".mp3")) {
        uploadSong(selectedFile)
      }
    }

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

  //Media
  def mediaChange(filepath: String): Unit = {

    val media: Try[Media] = Try(new Media(new File(filepath).toURI.toString))
    media match {
      case Success(v) =>
        updateNowPlaying()
        if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
          //mediaPlayer has not been instanciated
          mediaPlayer = new MediaPlayer(v)
          mediaPlayer.setVolume(volumeSlider.getValue)
        } else {

          val rate: Double = mediaPlayer.getRate
          val cycleCount: Int = mediaPlayer.getCycleCount
          val vol: Double = mediaPlayer.getVolume
          val mute: Boolean = mediaPlayer.isMute
          mediaPlayer.dispose()
          mediaPlayer = new MediaPlayer(v)
          //setVolumeSlider()
          mediaPlayer.setRate(rate)
          mediaPlayer.setMute(mute)
          mediaPlayer.setVolume(vol)

          repeatState match {
            case "Repeat 1" => mediaPlayer.setCycleCount(2)
            case "Repeat All" => mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE)
            case "No Repeat" => mediaPlayer.setCycleCount(1)
          }

        }
        mediaPlayer.setBalance(balanceSlider.getValue)
        mediaPlayer.setAutoPlay(true)
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
        showWarning("Erro a criar media")
        throw e

    }

  }

  //MediaControl
  def updateNowPlaying(): Unit = {
    val song: Song = if (listQueue.getSelectionModel.getSelectedItems.isEmpty) {
      listQueue.getSelectionModel.clearAndSelect(0)
      listQueue.getSelectionModel.getSelectedItem
    } else {
      listQueue.getSelectionModel.getSelectedItem
    }

    val album: String = Album.loaded.filtered(x => x.id == song.album).get(0).name
    val artist: String = Artist.loaded.filtered(x => x.id == song.artist).get(0).name
    nowPlaying.setText(song.name)
    musicNameLabel.setText(song.name)
    artistNameLabel.setText(artist)
    albumNameLabel.setText(album)

  }

  def playpause(): Unit = {
    if (!listQueue.getItems.isEmpty) {
      if (mediaPlayer.isInstanceOf[MediaPlayer] && mediaPlayer.getStatus != MediaPlayer.Status.DISPOSED) {
        if (!mediaPlayer.getStatus.equals(MediaPlayer.Status.PLAYING)) {
          selectPlayButton()
          mediaPlayer.play()
        } else {
          deSelectPlayButton()
          mediaPlayer.pause()
        }
      } else {
        mediaChange(listQueue.getItems.get(0).filepath)
        listQueue.scrollTo(0)
        listQueue.getSelectionModel.select(0)
        selectPlayButton()
      }
    } else {
      showWarning("No Songs on added to queue")
      deSelectPlayButton()
      TabPane.getSelectionModel.clearAndSelect(TabPane.getTabs.indexOf(ArtistsTab))
    }

  }

  def before(): Unit = {
    val listview: ListView[Song] = listQueue
    if (showMediaNullDialogWarning() && !listQueue.getItems.isEmpty) {
      val song: Song = listview.getSelectionModel.getSelectedItems.get(0)
      if (mediaPlayer.getCurrentTime.toSeconds > 3) {
        mediaPlayer.seek(new Duration(0))
      } else {
        if (repeatButton.isSelected) {
          mediaPlayer.seek(new Duration(0))
        } else {
          val pos = listview.getItems.lastIndexOf(song) - 1
          if (pos < 0) {
            gotoSong(listview)(listview.getItems.size - 1)
          } else {
            gotoSong(listview)(pos)
          }
        }
      }
    }
  }

  def next(): Unit = {
    val listview: ListView[Song] = listQueue

    if (showMediaNullDialogWarning() && !listQueue.getItems.isEmpty) {
      val song: Song = listview.getSelectionModel.getSelectedItems.get(0)
      val cycles: Int = mediaPlayer.getCycleCount
      if (mediaPlayer.getCycleCount.equals(MediaPlayer.INDEFINITE)) {
        mediaPlayer.seek(new Duration(0))
      } else if (cycles > 1) {
        mediaPlayer.setCycleCount(cycles - 1)
        mediaPlayer.seek(new Duration(0))
      } else {
        val pos = listview.getItems.lastIndexOf(song) + 1
        if (pos > listview.getItems.size - 1) {
          gotoSong(listview)(0)
        } else {
          gotoSong(listview)(pos)
        }
      }
    }
  }
/*
  def random(): Unit = {
    if (showMediaNullDialogWarning()) {
      if (repeatButton.isSelected) {
        repeatreset
      }
    }
  }
  */
  def shuffleQueue(): Unit = {
    if (!listQueue.getItems.isEmpty) {
      if (!isShuffled) {
        shuffleToggleButton.setSelected(true)

        if (listQueue.getSelectionModel.getSelectedItem == null) {
          listQueue.getSelectionModel.clearAndSelect(0)
        }
        oldQueue = observableListToList(listQueue.getItems)
        oldPos = listQueue.getSelectionModel.getSelectedIndices.get(0)

        //newQueue.addAll(listQueue.getItems)
        //newQueue.forEach(x => print(x+"\n"))
        //removed selcted
        val filteredQueue = oldQueue.filter(x => x.id != listQueue.getSelectionModel.getSelectedItem.id)

        val shuffled: List[Song] = Random.shuffle(filteredQueue)
        val first: Song = listQueue.getSelectionModel.getSelectedItem

        listQueue.getItems.clear()
        listQueue.getItems.add(first)
        shuffled.map(listQueue.getItems.add)

        listQueue.getSelectionModel.clearAndSelect(0)
        //shuffled.foreach(x => listQueue.getItems.add(x))
        isShuffled = true
      } else {
        shuffleToggleButton.setSelected(false)
        listQueue.getItems.clear()
        //      listQueue.getItems.addAll(oldQueue)
        oldQueue.map(listQueue.getItems.add)
        listQueue.getSelectionModel.clearAndSelect(oldPos)
        oldQueue = oldQueue.filter(x => false)
        isShuffled = false
      }
    }
  }

  def repeatreset(): Unit ={
    repeatButton.setSelected(false)
    repeatButton.setGraphic(repeatGraphic)
    mediaPlayer.setCycleCount(1)
    repeatState = "No Repeat"
  }

  def repeat(): Unit = {
    val cycles: List[Int] = List(1, 2, MediaPlayer.INDEFINITE)
    if (showMediaNullDialogWarning()) {
      val currCycle: Int = mediaPlayer.getCycleCount
      val currCyleindex: Int = cycles.indexOf(currCycle)
      /*
      if (shuffleToggleButton.isSelected) {
        shuffleToggleButton.setSelected(false)
      }
      */
      currCyleindex match {
        case 0 =>
          //change from no repeat to repeat 1
          mediaPlayer.setCycleCount(2)
          repeatState="Repeat 1"

          repeatButton.setSelected(true)
          repeatButton.setGraphic(repeat1Graphic)
        case 1 =>
          //change from repeat 1 to repeat all
          mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE)

          repeatState="Repeat All"
          repeatButton.setSelected(true)
          repeatButton.setGraphic(repeatInfGraphic)
        case 2 =>
          //change from repeat all to no repeat
          mediaPlayer.setCycleCount(1)

          repeatState="No Repeat"
          repeatButton.setSelected(false)
          repeatButton.setGraphic(repeatGraphic)
      }
      //not working
      // mediaPlayer.setCycleCount(math.floorMod(cycles.size,cycles.indexOf(mediaPlayer.getCycleCount)+1))
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

  def muteVolume(): Unit = {
    if (mediaPlayer.isInstanceOf[MediaPlayer]) {
      if (mediaPlayer.isMute) {
        mediaPlayer.setMute(false)
        setVolumeSlider()
      } else {
        volumeLabel.setText("Mute")
        mediaPlayer.setMute(true)

      }
    }
  }

  def setVolumeSlider(): Unit = {
    val volume: Double = volumeSlider.getValue / 100
    setVolume(volume)
  }

  def setVolume(vol: Double): Unit = {
    if (mediaPlayer.isInstanceOf[MediaPlayer]) {
      mediaPlayer.setMute(false)
      mediaPlayer.setVolume(vol)

    }
    volumeLabel.setText("vol:" + volumeSlider.getValue.toInt.toString + "%")
  }

  def resetBalance(): Unit = {
    balanceSlider.adjustValue(0)
    setBalance()
  }

  def setBalance(): Unit = {
    if (mediaPlayer.isInstanceOf[MediaPlayer]) {
      mediaPlayer.setBalance(balanceSlider.getValue)
    }
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

  //PlayWithkeyboard
  def keyboardEvent(eventHandler: KeyEvent): Unit = {
    val key: KeyCode = eventHandler.getCode
    val keychar = eventHandler.getCharacter
    if (key.equals(KeyCode.SPACE) || key.equals(KeyCode.ENTER) || key.equals(KeyCode.P) || key.equals(KeyCode.PLAY) || key.equals(KeyCode.PAUSE)) {
      playpause()
    } else if (key.equals(KeyCode.TRACK_PREV)) {
      before()
    } else if (key.equals(KeyCode.TRACK_NEXT)) {
      next()
    } else if (key.equals(KeyCode.VOLUME_DOWN)) {
      setVolume(volumeSlider.getValue - 10)
    } else if (key.equals(KeyCode.VOLUME_UP)) {
      setVolume(volumeSlider.getValue + 10)
    }
    eventHandler.consume()

  }

  def selectFromQueue(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2) {
      val song: Song = listQueue.getSelectionModel.getSelectedItems.get(0)
      mediaChange(song.filepath)
      durationSlider.setValue(0)
      resetPlayButton()
      selectPlayButton()

    }

  }

  //Albums
  def AlbumListViewClick(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2 ) {
      addToQueue(listSongsAlbum.getItems)
      println(listSongsAlbum.getItems)
    }
    DisplayAlbums()
  }

  def SongAlbumListViewClick(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2 && !listSongsArtist.getSelectionModel.getSelectedItems.isEmpty) {
      addToQueue(listSongsAlbum.getSelectionModel.getSelectedItems)
    }
  }

  def ArtistListViewClick(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2) {
      val artist: Artist = listArtists.getSelectionModel.getSelectedItem
      //artist.getSongs()
      //val songs: List[Song] = observableListToList(Song.loaded).filter(x => x.artist == artist.id)
      addToQueue(artist.getSongs())
    }
    DisplaytArtist()
    chooseListArtist()
  }

  def DisplaytArtist(): Unit = {
    if (!listArtists.getItems.isEmpty) {
      val artist: Artist = if (listArtists.getSelectionModel.getSelectedItems.isEmpty) {
        listArtists.getSelectionModel.select(0)
        listArtists.getItems.get(0)
      } else {
        listArtists.getSelectionModel.getSelectedItems.get(0)
      }
      //val songsArtist: List[Song] = observableListToList(Song.loaded).filter(x => x.artist == artist.id || x.feats.contains(artist.id))
      //val albunsArtist: List[Album] = observableListToList(Album.loaded).filter(x => x.artist == artist.id)
      val songsArtist: List[Song] = observableListToList(Song.loaded).filter(x => x.artist == artist.id || x.feats.contains(artist.id))
      val albunsArtist: List[Album] = observableListToList(Album.loaded).filter(x => x.artist == artist.id)

      listSongsArtist.getItems.clear()
      songsArtist.map(listSongsArtist.getItems.add)

      listAlbumsArtist.getItems.clear()
      albunsArtist.map(listAlbumsArtist.getItems.add)
    }
  }

  // Artists
  def chooseListArtist(): Unit = {
    val listType: String = ArtistShowAlbumOrSong.getSelectionModel.getSelectedItem
    if (listType.equals("Albums")) {
      listAlbumsArtist.setVisible(true)
      listSongsArtist.setVisible(false)
      updateAlbumListArtists()

    } else if (listType.equals("Songs")) {
      listAlbumsArtist.setVisible(false)
      listSongsArtist.setVisible(true)
      updateSongListArtists()

    }
  }

  //Queue
  def addToQueue(lst: List[Song]): Unit = {
    if (lst.nonEmpty) {
      lst.map(listQueue.getItems.add)
    }
  }

  def addToQueue(lst: ObservableList[Song]): Unit = {
    addToQueue(observableListToList(lst))
  }

  def AlbumArtistListViewClick(mouseEvent: MouseEvent): Unit = {
    DisplayAlbumFromArtist(mouseEvent)
  }

  def DisplayAlbumFromArtist(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2) {
      val album: Album = listAlbumsArtist.getSelectionModel.getSelectedItem

      if(album != null){
        TabPane.getSelectionModel.clearAndSelect(TabPane.getTabs.indexOf(AlbumsTab))
        listAlbums.getSelectionModel.select(album)
        DisplayAlbums()
      }
    }
  }

  //Display Song from
  def DisplayAlbums(): Unit = {
    if (!listAlbums.getItems.isEmpty) {
      val album: Album = listAlbums.getSelectionModel.getSelectedItem
      if (album != null) {
        val songsAlbum: List[Song] = observableListToList(Song.loaded).filter(x => album.tracks.contains(x.id))
        listSongsAlbum.getItems.clear()
        songsAlbum.map(listSongsAlbum.getItems.add)
      }
    }
  }

  def SongArtistListViewClick(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2 && !listSongsArtist.getSelectionModel.getSelectedItems.isEmpty) {
        addToQueue(listSongsArtist.getSelectionModel.getSelectedItems)
    }
  }

  //Removes from BD
  def removeArtist(): Unit = {
    val artists :List[Artist]= observableListToList(listArtists.getSelectionModel.getSelectedItems)
    if(artists.nonEmpty){
      val queue:List[Song] = observableListToList(listQueue.getItems)
      val queueContains:List[Boolean]=artists.flatMap(x=> x.getSongs).map(queue.contains)
      val queuebool:Boolean=queueContains.foldRight(false)(_||_)

      if(queuebool){
        showWarning("Remove songs from the following Artists:\n"+artists.map(_.name).mkString(",")+"\nfrom queue before deleting" )
      }else{
        artists.map(x=> x.delete)
      }

    }

  }

  def removeAlbumFromArtist(): Unit = {
    val albums : List[Album] = observableListToList(listAlbumsArtist.getSelectionModel.getSelectedItems)
    if(albums.nonEmpty){
      val queue:List[Song] = observableListToList(listQueue.getItems)
      val queueContains:List[Boolean]=albums.flatMap(x=> x.getSongs).map(queue.contains)
      val queuebool:Boolean=queueContains.foldRight(false)(_||_)

      if(queuebool){
        showWarning("Remove songs from the following Albums:\n"+albums.map(_.name).mkString(",")+"\n from queue before deleting" )
      }else{
        albums.map(x=> x.delete)
      }
    }
  }

  def removeSongFromArtist(): Unit = {
    val songs = observableListToList(listSongsArtist.getSelectionModel.getSelectedItems)

    if(songs.nonEmpty){
      val queue:List[Song]=observableListToList(listQueue.getItems)
      val songsNo= songs.map(x=>if(queue.contains(x)) x)
      val songsNoString= songs.map(x=>if(queue.contains(x)) {x.name})
      songs.map(x => if(!queue.contains(x)){x.delete()})
      if(songsNo.nonEmpty){
        showWarning("Remove these songs from queue before deleting\n" + songsNoString)
      }

      updateSongListArtists()
    }
  }

  def removeAlbum(): Unit = {
      val albums: List[Album] = observableListToList(listAlbums.getSelectionModel.getSelectedItems)
    if(albums.nonEmpty){
      val queue:List[Song] = observableListToList(listQueue.getItems)
      val queueContains:List[Boolean]=albums.flatMap(x=> x.getSongs).map(queue.contains)
      val queuebool:Boolean=queueContains.foldRight(false)(_||_)

      if(queuebool){
        showWarning("Remove songs from the following Albums:\n"+albums.map(x=>x.name).mkString(",")+"\n from queue before deleting" )
      }else{
        albums.map(x=> x.delete)
      }

      updateSongListAlbums()
      updateAlbumListArtists()

    }else{
      showWarning("Select an album")
    }
  }

  def removeSongFromAlbum(): Unit = {
    val songs: List[Song] = observableListToList(listSongsAlbum.getSelectionModel.getSelectedItems)
    if(songs.nonEmpty){
      val queue:List[Song]=observableListToList(listQueue.getItems)

      val songsNo= songs.map(x=>if(queue.contains(x)) x)
      val songsNoString= songs.map(x=>if(queue.contains(x)) {x.name})
      if(songsNo.nonEmpty){
        showWarning("Remove these songs from queue before deleting\n" + songsNoString)
      }
      songs.map(x => if(!queue.contains(x)){x.delete()})

    }
  }

  //Playlists
  def createPlaylist(): Unit = {
    val loader: FXMLLoader = new FXMLLoader(getClass.getResource("CreatePlaylist.fxml"))
    val parent: Parent = loader.load().asInstanceOf[Parent]
    val stage: Stage = new Stage()
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.setTitle("CreatePlaylist")

    stage.setScene(new Scene(parent))
    stage.show()
  }

  def removePlaylist(): Unit = {
    val toRemove: List[Playlist] = observableListToList(listPlaylist.getSelectionModel.getSelectedItems)
    toRemove.map(x => Playlist.loaded.remove(x))

    updateSongListPlaylists()
  }

  def shufflePlaylist(): Unit = {
    var playlists: List[Playlist] = observableListToList(listPlaylist.getSelectionModel.getSelectedItems)
    Playlist.shuffle(playlists)
    listSongsPlaylist.getItems.clear()
    updateSongListPlaylists()
    updateListPlaylists()
  }

  def addToPlayFromAlbum(): Unit = {
    val lst: List[Song] = observableListToList[Song](listSongsAlbum.getSelectionModel.getSelectedItems)
    if (lst.nonEmpty) {

      val playlist: Playlist = addToPlaylistAlbumCombo.getSelectionModel.getSelectedItem

      if (playlist != null) {
        val songs: List[Int] = lst.map(x => x.id)
        playlist.addSong(songs)
        updateListPlaylists()
      } else {
        showWarning("Select a playlist")
      }
    } else {
      showWarning("Select some songs")
    }
  }

  //Warnings
  private def showWarning(content: String): Unit = {
    val alert = new Alert(AlertType.WARNING)
    alert.setTitle("Warning")
    alert.setHeaderText(content)

    alert.showAndWait()
  }

  private def showMediaNullDialogWarning(): Boolean = {
    val title: String = "You should select a Song to play first"
    if (!mediaPlayer.isInstanceOf[MediaPlayer]) {
      val alert = new Alert(AlertType.WARNING)
      alert.setTitle("No media selected")
      alert.setHeaderText(title)

      alert.showAndWait()
    }
    mediaPlayer.isInstanceOf[MediaPlayer]
  }

  def addToPlayFromArtist(): Unit = {
    val selectedMode: String = ArtistShowAlbumOrSong.getSelectionModel.getSelectedItem
    val lst: List[Song] = if (selectedMode.equals("Albums")) {

      val albums: List[Album] = observableListToList[Album](listAlbumsArtist.getSelectionModel.getSelectedItems)
      val tracks: List[Int] = albums.flatten(x => x.tracks)
      //println(tracks)
      //observableListToList(Song.loaded).filter(x => tracks.contains(x.id))
      albums.map(getSongs).flatten

    } else {
      observableListToList[Song](listSongsArtist.getSelectionModel.getSelectedItems)
    }
    if (lst.nonEmpty) {
      val playlist: Playlist = addToPlaylistArtistCombo.getSelectionModel.getSelectedItem

      if (playlist != null) {
        val songs: List[Int] = lst.map(x => x.id)
        playlist.addSong(songs)
        updateListPlaylists()
      } else {
        showWarning("Select a playlist")
      }
    } else {
      showWarning("Select some songs")
    }
  }

  def remFromPlay(): Unit = {
    val lst: List[Int] = observableListToList(listSongsPlaylist.getSelectionModel.getSelectedItems).map(x => x.id)
    val playlist: Playlist = listPlaylist.getSelectionModel.getSelectedItem
    playlist.removeSong(lst)
    updateSongListPlaylists()
  }

  def PlaylistListViewClick(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2) {
      val playlist: Playlist = listPlaylist.getSelectionModel.getSelectedItem
      if (playlist != null) {/*
        val loadedSongids: List[Int] = observableListToList(Song.loaded) map (x => x.id)
        val loadedSongs: List[Song] = observableListToList(Song.loaded)
        val songids: List[Int] = pl.songs.filter(x => loadedSongids.contains(x))
        val songs: List[Song] = loadedSongs.filter(x => songids.contains(x.id))

        val listSongs: List[Song] = observableListToList(Song.loaded)*/

        //songsPlaylist=playlist.songs.foreach(x => listSongs.filter(y => y.id == x))
        val listSongs: List[Song] = observableListToList(Song.loaded)

        def aux(lst: List[Song], x: Int): List[Song] = {
          lst.appendedAll(listSongs.filter(y => y.id == x))
        }

        val songsPlaylist = playlist.songs.foldLeft(List[Song]())(aux)
        listSongsPlaylist.getItems.clear()
        //songsPlaylist.map(listQueue.getItems.add)

        addToQueue(songsPlaylist)
        //addToQueue(songs)
      }
    }
    DisplayPlaylist()
  }

  def DisplayPlaylist(): Unit = {
    if (!listPlaylist.getItems.isEmpty) {
      val playlist: Playlist = listPlaylist.getSelectionModel.getSelectedItem
      if(playlist != null){
        //val songsPlaylist: List[Song] = observableListToList(Song.loaded).filter(x => songs.contains(x.id))
        val listSongs: List[Song] = observableListToList(Song.loaded)

        //songsPlaylist=playlist.songs.foreach(x => listSongs.filter(y => y.id == x))
        def aux(lst: List[Song], x: Int): List[Song] = {
          lst.appendedAll(listSongs.filter(y => y.id == x))
        }
        val songsPlaylist = playlist.songs.foldLeft(List[Song]())(aux)
        listSongsPlaylist.getItems.clear()
        songsPlaylist.map(listSongsPlaylist.getItems.add)
      }
    }
  }

  def PlaylistSongListViewClick(mouseEvent: MouseEvent): Unit = {
    if (mouseEvent.getClickCount == 2 && !listSongsPlaylist.getSelectionModel.getSelectedItems.isEmpty) {
      addToQueue(listSongsPlaylist.getSelectionModel.getSelectedItems)
    }
  }

  def remFromQueue(): Unit = {
    //song.name + "\nFrom " + album + " by " + artist
    val queueSelected: List[Song] = observableListToList(listQueue.getSelectionModel.getSelectedItems)
    if(queueSelected.nonEmpty){
      removeSongsQueue(queueSelected)
    }
  }

  def clearQueue(): Unit ={
    if(!listQueue.getItems.isEmpty){
      /*listQueue.getItems.clear()
      if(mediaPlayer.isInstanceOf[MediaPlayer]){
        mediaPlayer.dispose()
      }*/

      removeSongsQueue(observableListToList(listQueue.getItems))
    }
  }

  private def removeSongsQueue(song:List[Song]): Unit ={
    val currPlaying:List[Song] = observableListToList(Song.loaded).filter(x=>x.name == nowPlaying.getText)
    if(currPlaying.nonEmpty){
      song.filter(x => x != currPlaying(0)).map(x => listQueue.getItems.remove(x))
    }
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

  //Auxiliaries
  private def gotoSong(listView: ListView[Song])(pos: Int): Unit = {
    val newSong: Song = listView.getItems.get(pos)
    listView.getSelectionModel.clearAndSelect(pos)
    listView.scrollTo(pos)
    mediaChange(newSong.filepath)
    mediaPlayer.play()
  }

  private def msToMinSec(duration: Duration): (Int, Int, Int) = {
    val hours: Int = math.floor(duration.toHours).toInt
    val minutes: Int = math.floor(duration.toMinutes).toInt - (hours * 60)
    val sec: Int = math.floor(duration.toSeconds).toInt - (hours * 60 * 60) - (minutes * 60)

    (hours, minutes, sec)
  }

  private def resetPlayButton(): Unit = {
    deSelectPlayButton()
  }

  private def selectPlayButton(): Unit = {
    togglePlayPause.setSelected(true)
    togglePlayPause.setText("")
    togglePlayPause.setGraphic(pauseGraphic)
  }

  private def deSelectPlayButton(): Unit = {
    togglePlayPause.setSelected(false)
    togglePlayPause.setGraphic(playGraphic)
  }

  private def setSongImage(imageSong: Image): Unit = {
    image.setImage(imageSong)
    //println(image.getImage)
  }

  private def imageSong(media: Media): Unit = {
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
        if (imageSong.isEmpty) {
          val stream = new FileInputStream("Images/defaultAlbumCover.png")
          val imageSong = new Image(stream)
          //println(imageSong.getHeight)
          setSongImage(imageSong)
        } else setSongImage(imageSong.map(_._2).remove(0).asInstanceOf[Image])
      }
    }
    metadataMediaPlayer.setOnReady(runner)
  }



















}