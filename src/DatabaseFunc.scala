import java.io.{BufferedWriter, File, FileWriter}

import Data.{Album, Artist, MusicObject, Playlist, Song}
import com.sun.prism.PixelFormat.{BYTE_ALPHA, DataType}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.io.Source

object DatabaseFunc {

  def loadfiles(): Unit={

    readFile(Song.load,Song.db)
    readFile(Album.load,Album.db)
    readFile(Artist.load,Artist.db)
    readFile(Playlist.load,Playlist.db)
  }

  def unload[A](a:A):Unit= a match{
    case a: Data.MusicObject[A] => {
      println("Unloaded" + a.toString)
      a.loaded -= a.asInstanceOf[A]
    }
  }

  def update [A](a:MusicObject[A], field:Int, newv:String):Unit = {
    if(field ==0){
      println("UpdateError:Cant change id")
    }else{
        val objectold:MusicObject[A] = a.asInstanceOf[MusicObject[A]]
        val loadedObject:A =objectold.loaded.filter(_.asInstanceOf[MusicObject[A]].id == objectold.id)(0)
        val info: List[String] = loadedObject.toString().split(";").toList.updated(field,newv)
        //val objectNew:A = a.getClass.getConstructor(classOf[MusicObject[A]]).newInstance(info).asInstanceOf[A]
        val objectNew:A = a.apply(info)

        //deletefromDB(loadedObject, a.db)
        //addtoDB(objectNew, a.db)
        a.load(info.mkString(";"))
    }
  }

  def readFile(load: String=>Any, filename: String): Unit={
    val bufferedFile = Source.fromFile(filename)
    val lines = bufferedFile.getLines.toList
    readline(load,lines)
    bufferedFile.close()
  }

  @tailrec
  def readline(load: String=>Any, lines: List[String]): Unit= lines match{
    case a::Nil => load(a)
    case a::t => load(a);readline(load,t)
  }

  def getlastid[A](loaded: List[MusicObject[A]]): Int = {
    def aux(max: Int, lst: List[MusicObject[A]]): Int = lst match {
      case h :: t => aux(scala.math.max(h.id, max), t)
      case Nil => max
    }
    aux(0, loaded)
  }

  def GetIDArtistOrCreate(artist: String): String={
    val artistcheck:ListBuffer[Artist]=Artist.loaded.filter(x=>x.name.equals(artist))

    val artistid:Int={
      if (artistcheck.isEmpty) {
        val newid:Int=DatabaseFunc.getlastid(Artist.loaded.toList)+1
        Artist.loaded+=Artist( List(newid.toString,artist,"","")  )
        newid
      } else {
        artistcheck(0).id
      }
    }
    artistid.toString
  }

  def writeDB[A](loaded: ListBuffer[A],dbPath: String): Unit={
    val file=new File(dbPath)
    val bw = new BufferedWriter(new FileWriter(file))
    loaded.map(x=>{
      bw.write(x.toString+"\n")}
    )
    bw.close()
  }
  def printLoaded(): Unit={
    println("Songs:")
    Song.loaded.sortWith((x1,x2)=>x1.id<x2.id)      .map(x => println("    " + x))
    println("Artists:")
    Artist.loaded.sortWith((x1,x2)=>x1.id<x2.id)    .map(x => println("    " + x))
    println("Albums:")
    Album.loaded.sortWith((x1,x2)=>x1.id<x2.id)     .map(x => println("    " + x))
    println("Playlists:")
    Playlist.loaded.sortWith((x1,x2)=>x1.id<x2.id)  .map(x => println("    " + x))
  }
}
