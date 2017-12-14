package mamos

import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{Clock, Instant, ZoneId}
import java.util.concurrent.TimeUnit

import org.jmock.lib.concurrent.DeterministicScheduler
import org.scalatest.mockito.MockitoSugar
import org.junit.Test

class ArrivalsTests extends MockitoSugar {
  // start our test in the 70s why not
  var currentTime = Instant.ofEpochMilli(0)

  val scheduler = new DeterministicScheduler {
    override def tick(duration: Long, timeUnit: TimeUnit): Unit = {
      currentTime = currentTime.plus(duration, ChronoUnit.MINUTES)
      super.tick(duration, timeUnit)
    }
  }

  //Clock based on our time
  val clock: Clock = new Clock {
    override def withZone(zone: ZoneId): Clock = this

    override def getZone: ZoneId = ??? // won't get called

    override def instant(): Instant = currentTime
  }
  val arrivalsBoard = mock[ArrivalsBoard]
  val tflApi = mock[TflApi]

  val arrivals = new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)

  @Test def testInitialValues() = {}
  @Test def testEmptyResult() = {}
  @Test def testUpdatesToEmptyResult() = {}
  @Test def testUpdatesNextArrivalChanges() = {}
  @Test def testAnnouncements() = {}
}