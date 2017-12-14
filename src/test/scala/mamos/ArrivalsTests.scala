package mamos

import java.time.Clock
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import mamos.TflApi.{LineId, StopPointId}
import org.jmock.lib.concurrent.DeterministicScheduler
import org.scalatest.mockito.MockitoSugar
import org.junit.Test

class ArrivalsTests extends MockitoSugar {
  val scheduler = new DeterministicScheduler

  //todo get a clock linked to the scheduler
  val clock: Clock = ???
  val arrivalsBoard = mock[ArrivalsBoard]
  val tflApi = mock[TflApi]

  val arrivals = new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)

  @Test def testInitialValues() = {}
  @Test def testEmptyResult() = {}
  @Test def testUpdatesToEmptyResult() = {}
  @Test def testUpdatesNextArrivalChanges() = {}
  @Test def testAnnouncements() = {}
}