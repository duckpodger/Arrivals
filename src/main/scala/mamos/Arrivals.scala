package mamos

import java.time.Clock
import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture, TimeUnit}

import TflApi._

// Currently not doing a great job of timezones, need to come back to this
class Arrivals(stopPoint: StopPointId, line: LineId, arrivalsBoard: ArrivalsBoard, tfl: TflApi, scheduler: ScheduledExecutorService, clock: Clock, pollPeriod: Long = 1, pollUnits: TimeUnit = TimeUnit.MINUTES) {
  // Mutable state
  @volatile var nextArrival:Option[ExpectedArrival] = None
  @volatile var pendingAnnouncement: Option[ScheduledFuture[_]] = None

  // scheduler does our initial retrieval as well as the repeated one
  scheduler.scheduleAtFixedRate(
    new Runnable {
      override def run(): Unit = update()
    },
    0,pollPeriod, pollUnits
  )

  private def scheduleAnnouncement(train: ExpectedArrival) = {
    val delay = train.expectedArrival.toEpochMilli - clock.millis
    scheduler.schedule(
      new Runnable {
        override def run(): Unit = {
            arrivalsBoard.send_arrival(s"The train that has now arrived at ${train.expectedArrival} is for ${train.destinationName}")
        }
      },
      delay,
      TimeUnit.MILLISECONDS
    )
  }

  def update() = {
    // retrieve arrivals & sort them
    val initialArrivals = tfl.getArrivals(stopPoint,line).sortBy(_.expectedArrival)

    val newNextArrival = initialArrivals.headOption

    (newNextArrival, nextArrival) match {
      // if the next train expected hasn't changed, but the time has, cancel and replace the scheduled announcement
      case (Some(newA), Some(oldA)) if newA.vehicleId == oldA.vehicleId =>
        if (newA.expectedArrival != oldA.expectedArrival) {
          pendingAnnouncement.foreach(_.cancel(false))
          pendingAnnouncement = Some(scheduleAnnouncement(newA))
        }
      // a new train, don't cancel the previous announcement to ensure that it is made
      case (Some(newA), _) =>
        pendingAnnouncement = Some(scheduleAnnouncement(newA))
      case _ =>
    }

    nextArrival = newNextArrival

    // update the following arrivals board
    arrivalsBoard.send_following_arrivals(initialArrivals.map(_.toString))
  }

}
