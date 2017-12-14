package mamos

import java.util.concurrent.Executors

object ArrivalsMain extends App {
  val arrivals = new Arrivals(
    "940GZZLUWLO","bakerloo",
    new ArrivalsBoard {
      override def send_following_arrivals(arrivals: Seq[String]): Unit = {}
      override def send_arrival(arrival: String): Unit = {}
    },
    Executors.newSingleThreadScheduledExecutor())
}
