package mamos

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import TflApi._

class Arrivals(stopPoint: StopPointId, line: LineId, arrivalsBoard: ArrivalsBoard, tfl: TflApi, scheduler: ScheduledExecutorService, pollPeriod: Long = 1, pollUnits: TimeUnit = TimeUnit.MINUTES) {
  // Mutable state
  var nextArrival:Option[ExpectedArrival] = None

  update()
  scheduler.scheduleAtFixedRate(
    new Runnable {
    override def run(): Unit = update()
  },0,1, TimeUnit.MINUTES)

  def update() = {
    // retrieve arrivals
    val initialArrivals = tfl.getArrivals(stopPoint,line)

    val newNextArrival = initialArrivals.headOption

    (newNextArrival, nextArrival) match {
      case (Some(a), Some(b)) if b == a => //do nothing, no change
      case (None, None) => //do nothing, no change
      case _ =>
        nextArrival = newNextArrival

        // announce new value
        nextArrival.foreach(a => arrivalsBoard.send_arrival(a.toString))
    }

    //update the following arrivals board
    arrivalsBoard.send_following_arrivals(initialArrivals.tail.map(_.toString))
  }


}
