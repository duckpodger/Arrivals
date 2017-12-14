package mamos

import java.time.Clock
import java.util.concurrent.{Executors, TimeUnit}

object ArrivalsMain extends App {
  val clock = Clock.systemUTC()
  val arrivals = new Arrivals(
    "940GZZLUWLO","bakerloo",
    new ArrivalsRecordingProxy(
      new ArrivalsBoard {
        override def send_following_arrivals(arrivals: Seq[String]): Unit = {}
        override def send_arrival(arrival: String): Unit = {}
      },
      clock,
      "Arrivals.log"
    ),
    new TflWebApi,
    Executors.newSingleThreadScheduledExecutor(),
    clock,
    pollPeriod = 30,
    pollUnits = TimeUnit.SECONDS)
}
