package mamos

import java.io.{FileOutputStream, PrintStream}
import java.time.{Clock, Instant}

import spray.json._
import InstantJson._

class ArrivalsRecordingProxy(board: ArrivalsBoard, clock: Clock, file: String) extends ArrivalsBoard {
  private val os = new PrintStream(new FileOutputStream(file))

  case class ArrivalEvent(eventTime: Instant, announcement: String)
  case class FollowingArrivalsEvent(eventTime: Instant, arrivals: Seq[String])

  object ArrivalsBoardProtocol extends DefaultJsonProtocol {
    implicit val arrivalEventFormat = jsonFormat2(ArrivalEvent)
    implicit val followingArrivalsEventFormat = jsonFormat2(FollowingArrivalsEvent)
  }
  import ArrivalsBoardProtocol._

  override def send_arrival(arrival: String): Unit = {
    val event = ArrivalEvent(Instant.now(clock), arrival)
    board.send_arrival(arrival)

    os.println(event.toJson)
  }

  override def send_following_arrivals(arrivals: Seq[String]): Unit = {
    val event = FollowingArrivalsEvent(Instant.now(clock), arrivals)
    board.send_following_arrivals(arrivals)

    os.println(event.toJson)
  }
}
