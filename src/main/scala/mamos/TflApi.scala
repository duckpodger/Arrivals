package mamos
import java.time.Instant
import javax.inject.Inject

import TflApi._
import TflWebApi._
import play.api.libs.ws.WSClient
import spray.json._
import scala.concurrent.duration._
import InstantJson._

import scala.concurrent.{Await, ExecutionContext}

object TflApi {
  type StopPointId = String
  type LineId = String
}

object ExpectedArrivalProtocol extends DefaultJsonProtocol with CollectionFormats {
  implicit val arrivalFormat = jsonFormat4(ExpectedArrival)
  implicit val arrivalsFormat = listFormat[ExpectedArrival]
}

case class ExpectedArrival(naptanId:StopPointId, lineId:LineId, expectedArrival: Instant, destinationName: String) {
  override def toString: String = {
    s"The train to $destinationName will arrive at $expectedArrival"
  }
}

trait TflApi {
  def getArrivals(stopPoint: StopPointId, line: LineId): Seq[ExpectedArrival]
}

object TflWebApi {
  def tflArrivalsUrl(stopPoint: StopPointId, lineId:LineId): String =
    s"https://api.tfl.gov.uk/Line/$lineId/Arrivals/$stopPoint?direction=inbound"
}


import ExpectedArrivalProtocol._

class TflWebApi @Inject() (ws: WSClient, implicit val ec: ExecutionContext) extends TflApi {
  override def getArrivals(stopPoint: StopPointId, line: LineId): Seq[ExpectedArrival] = {
    val request = ws.url(tflArrivalsUrl(stopPoint,line))
      .addHttpHeaders("Accept" -> "application/json")
    val responseFuture = request.get().map {
      response =>
        response.body[String].toJson.convertTo[List[ExpectedArrival]]
    }
    Await.result(responseFuture, 0 nanos)
  }
}