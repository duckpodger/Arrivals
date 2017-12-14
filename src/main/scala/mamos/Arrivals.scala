package mamos

import java.time.Clock
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import TflApi._

// Currently not doing a great job of timezones, need to come back to this
class Arrivals(stopPoint: StopPointId, line: LineId, arrivalsBoard: ArrivalsBoard, tfl: TflApi, scheduler: ScheduledExecutorService, clock: Clock, pollPeriod: Long = 1, pollUnits: TimeUnit = TimeUnit.MINUTES) {
  // Mutable state
  @volatile var nextArrival:Option[ExpectedArrival] = None

  // scheduler does our initial retrieval as well as the repeated one
  scheduler.scheduleAtFixedRate(
    new Runnable {
      override def run(): Unit = update()
    },
    0,pollPeriod, pollUnits
  )

  def update() = {
    // retrieve arrivals
    val initialArrivals = tfl.getArrivals(stopPoint,line).sortBy(_.expectedArrival)

    val newNextArrival = initialArrivals.headOption

    (newNextArrival, nextArrival) match {
      case (Some(a), Some(b)) if b == a => //do nothing, no change
      case (None, None) => //do nothing, no change
      case _ =>
        nextArrival = newNextArrival

        // announce new value, at the correct time
        nextArrival.foreach(a => {
          scheduler.schedule(
            new Runnable {
              override def run(): Unit = {
                // ignore if the next has changed, e.g. train was delayed
                // need to see when tfl remove the trains, would be annoying to miss the announcement
                // because api was updated too quickly
                if (nextArrival == a)
                  arrivalsBoard.send_arrival(a.toString)
              }
            },
            a.expectedArrival.toEpochMilli - clock.millis,
            TimeUnit.MILLISECONDS
          )
        })
    }

    // update the following arrivals board
    arrivalsBoard.send_following_arrivals(initialArrivals.map(_.toString))
  }

}
