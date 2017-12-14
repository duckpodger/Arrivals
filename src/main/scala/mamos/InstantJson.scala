package mamos

import java.time.Instant
import spray.json._

object InstantJson {
  implicit object InstantJsonFormat extends RootJsonFormat[Instant] {
    def write(c: Instant) =
      JsString(c.toString)

    def read(value: JsValue) = value match {
      case JsString(s) => Instant.parse(s)
      case _ => deserializationError("Color expected")
    }
  }
}
