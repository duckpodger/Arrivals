package mamos
import java.time.Instant

import TflApi._
import TflWebApi._
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import spray.json._

import InstantJson._
import org.apache.http.util.EntityUtils

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

class TflWebApi() extends TflApi {
  private val httpClient = HttpClientBuilder.create().build()

  override def getArrivals(stopPoint: StopPointId, line: LineId): Seq[ExpectedArrival] = {
    val req = new HttpGet(tflArrivalsUrl(stopPoint,line))
    req.setHeader("Accept", "application/json")

    //Tooo: sort out error handling
    val resp = httpClient.execute(req)

    EntityUtils.toString(resp.getEntity).parseJson.convertTo[List[ExpectedArrival]]
  }
}