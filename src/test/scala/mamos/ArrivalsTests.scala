package mamos

import java.time.{Clock, Instant, ZoneId}
import java.util.concurrent.TimeUnit

import mamos.TflApi.{LineId, StopPointId}
import org.scalatest.mockito.MockitoSugar
import org.mockito.BDDMockito._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.junit.Test

class ArrivalsTests extends MockitoSugar {
  // start our test in the 70s why not
  var currentTime = Instant.ofEpochMilli(0)

  val scheduler = new DeterministicScheduler()

  //Clock based on our time
  val clock: Clock = new Clock {
    override def withZone(zone: ZoneId): Clock = this

    override def getZone: ZoneId = ??? // won't get called

    override def instant(): Instant = Instant.ofEpochMilli(scheduler.getElapsed)
  }
  val arrivalsBoard = mock[ArrivalsBoard]
  val tflApi = mock[TflApi]

  @Test def testInitialValues() = {
    val arrivals = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(10),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(110),"Wigan","Thunderbird2")
    )
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(arrivals)

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)
    scheduler.runUntilIdle

    verify(arrivalsBoard, never).send_arrival(any[String])
    verify(arrivalsBoard).send_following_arrivals(arrivals.map(_.toString))
  }

  @Test def testOrdersTrains() = {
    val arrivals = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(110),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(10),"Wigan","Thunderbird2")
    )
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(arrivals)

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)
    scheduler.runUntilIdle

    verify(arrivalsBoard, never).send_arrival(any[String])
    val expectedArrivalsReported = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(10),"Wigan","Thunderbird2"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(110),"Barnsley","Thunderbird1")
    )
    verify(arrivalsBoard).send_following_arrivals(expectedArrivalsReported.map(_.toString))
  }

  @Test def testEmptyResult() = {
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(Seq())

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)
    scheduler.runUntilIdle

    verify(arrivalsBoard, never).send_arrival(any[String])
    verify(arrivalsBoard).send_following_arrivals(Seq())
  }


  @Test def testPolls() = {
    val initialArrivals = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(130000),"Wigan","Thunderbird2")
    )
    val firstPollArrivals = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(125000),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(140000),"Wigan","Thunderbird2"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(130000),"Burton","Thunderbird3")
    )
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(initialArrivals, firstPollArrivals)

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)
    //advance one minute and we should poll
    scheduler.tick(1, TimeUnit.MINUTES)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(2)).send_following_arrivals(any[Seq[String]])
    val initialReportExpected = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(130000),"Wigan","Thunderbird2")
    )
    verify(arrivalsBoard).send_following_arrivals(initialReportExpected.map(_.toString))
    val expectedReportAfterPoll = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(125000),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(130000),"Burton","Thunderbird3"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(140000),"Wigan","Thunderbird2")
    )
    verify(arrivalsBoard).send_following_arrivals(expectedReportAfterPoll.map(_.toString))
  }

  @Test def testAnnouncesTrain() = {
    val arrivals = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(10),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(110),"Wigan","Thunderbird2")
    )
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(arrivals)

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)
    scheduler.tick(10, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard).send_arrival("The train that has now arrived at 1970-01-01T00:00:00.010Z is for Barnsley")
  }

  @Test def testUpdatesToEmptyResult() = {
    val initialArrivals = Seq(
      ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird1"),
      ExpectedArrival("a","b",Instant.ofEpochMilli(130000),"Wigan","Thunderbird2")
    )
    val firstPollArrivals = Seq()
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(initialArrivals, firstPollArrivals)

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)
    //advance one minute and we should poll
    scheduler.tick(1, TimeUnit.MINUTES)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(2)).send_following_arrivals(any[Seq[String]])
    verify(arrivalsBoard).send_following_arrivals(initialArrivals.map(_.toString))
    verify(arrivalsBoard).send_following_arrivals(Seq())
  }

  @Test def testTrainGetsEarlier() = {
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird1")),
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(110000),"Barnsley","Thunderbird1"))
    )

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)

    // advance to earliest time, announcement made
    scheduler.tick(110000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(1)).send_arrival(any[String])

    //advance to the second time, and ensure that no more invocations had been made
    scheduler.tick(120000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(1)).send_arrival(any[String])
  }

  @Test def testTrainGetsLater() = {
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(110000),"Barnsley","Thunderbird1")),
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird1"))
    )

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)

    // advance to earliest time, announcement not made
    scheduler.tick(110000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(0)).send_arrival(any[String])

    //advance to the second time, and ensure that announcement is now made
    scheduler.tick(120000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(1)).send_arrival(any[String])
  }

  @Test def testTrainRemovedBeforeAnnouncement() = {
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(110000),"Barnsley","Thunderbird1")),
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird2"))
    )

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)

    // advance to earliest time, announcement is made, because the time for that train wasn't adjusted
    scheduler.tick(110000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(1)).send_arrival(any[String])

    //advance to the second time, and ensure that the announcement for the second train is made
    scheduler.tick(120000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(2)).send_arrival(any[String])

  }

  @Test def testTrainRemovedAfterAnnouncement() = {
    given(tflApi.getArrivals(any[StopPointId],any[LineId])).willReturn(
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(4000),"Barnsley","Thunderbird1")),
      Seq(ExpectedArrival("a","b",Instant.ofEpochMilli(120000),"Barnsley","Thunderbird2"))
    )

    new Arrivals("940GZZLUWLO", "bakerloo", arrivalsBoard, tflApi, scheduler, clock)

    // advance to the poll, the announcement should have been made
    scheduler.tick(1, TimeUnit.MINUTES)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(1)).send_arrival(any[String])

    //advance to the second time, and ensure that the announcement for the second train is made
    scheduler.tick(120000, TimeUnit.MILLISECONDS)
    scheduler.runUntilIdle

    verify(arrivalsBoard, times(2)).send_arrival(any[String])

  }
}